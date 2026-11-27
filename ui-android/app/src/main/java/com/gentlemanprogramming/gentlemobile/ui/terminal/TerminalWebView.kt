package com.gentlemanprogramming.gentlemobile.ui.terminal

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.gentlemanprogramming.gentlemobile.mainHandler
import com.gentlemanprogramming.gentlemobile.ui.theme.TerminalFace
import java.util.Base64

/**
 * The Android glue of the xterm.js bridge (T61): a WebView rendering the
 * vendored xterm.js dist, fed by our PTY. The pure half (theme JSON, byte
 * encoding, message framing, resize decision) lives in [XtermBridge] and is
 * unit-tested; this class only owns the WebView instance and the two
 * directions of the message bridge.
 *
 * Direction one — Kotlin → JS: `evaluateJavascript` of the exact messages
 * [XtermMessages] builds (`write` carries the PTY's bytes as base64).
 * Direction two — JS → Kotlin: the [TerminalJsBridge] `@JavascriptInterface`
 * receives `onData` (keystrokes, base64), `onResize` (cols/rows from the fit
 * addon) and `onReady`, each marshalled to the main thread.
 */
class TerminalWebController {
    private var webView: WebView? = null
    private var initialized = false
    private val pending = ArrayDeque<String>()

    private var themeJson: String? = null
    private var fontCss: String? = null
    private var cursorMode: String? = null
    private var cursorBlink: Boolean? = null

    var onData: ((ByteArray) -> Unit)? = null
    var onResize: ((Int, Int) -> Unit)? = null
    var onReady: (() -> Unit)? = null

    /** Store the current surface and, once the page is up, push the live
     *  changes: theme and cursor travel as updates, a font change rebuilds
     *  the terminal (the font is part of its construction). */
    fun configure(theme: XtermTheme, fontCss: String, cursorMode: String, cursorBlink: Boolean) {
        val themeJson = theme.toJson()
        val fontChanged = fontCss != this.fontCss
        val themeChanged = themeJson != this.themeJson
        val cursorChanged = cursorMode != this.cursorMode || cursorBlink != this.cursorBlink
        this.themeJson = themeJson
        this.fontCss = fontCss
        this.cursorMode = cursorMode
        this.cursorBlink = cursorBlink
        if (!initialized) return
        if (fontChanged) {
            reinit()
            return
        }
        if (themeChanged) post(XtermMessages.theme(themeJson))
        if (cursorChanged) post(XtermMessages.cursor(cursorMode, cursorBlink))
    }

    /** Feed host bytes into the terminal, encoded as base64. */
    fun write(bytes: ByteArray) = post(XtermMessages.write(XtermCodec.encode(bytes)))

    /** Focus xterm's own textarea so the IME opens in direct mode. */
    fun focus() = post(XtermMessages.focus())

    /** Tear down the WebView when the terminal leaves the screen: drop the JS
     *  bridge and destroy the view so its JS context and page timers do not
     *  leak, and reset so a later re-entry builds a fresh one. */
    fun dispose() {
        val wv = webView ?: return
        wv.removeJavascriptInterface("AndroidBridge")
        wv.destroy()
        webView = null
        initialized = false
        synchronized(pending) { pending.clear() }
    }

    private fun reinit() {
        val t = themeJson ?: return
        val f = fontCss ?: return
        val m = cursorMode ?: return
        val b = cursorBlink ?: return
        post(XtermMessages.init(t, f, m, b))
    }

    private fun post(command: String) {
        val wv = webView
        if (wv == null || !initialized) {
            synchronized(pending) { pending.addLast(command) }
            return
        }
        wv.post { runCatching { evaluate(wv, command) } }
    }

    private fun evaluate(wv: WebView, command: String) {
        wv.evaluateJavascript("window.gm.receive($command)", null)
    }

    internal fun attach(wv: WebView) {
        webView = wv
    }

    /** The page is loaded: send `init` first, then anything that was queued
     *  while the WebView was still loading. */
    internal fun onPageLoaded() {
        if (initialized) return
        val t = themeJson ?: return
        val f = fontCss ?: return
        val m = cursorMode ?: return
        val b = cursorBlink ?: return
        initialized = true
        val queued: List<String>
        synchronized(pending) {
            pending.addFirst(XtermMessages.init(t, f, m, b))
            queued = pending.toList()
            pending.clear()
        }
        val wv = webView ?: return
        wv.post { queued.forEach { runCatching { evaluate(wv, it) } } }
    }

    @SuppressLint("SetJavaScriptEnabled")
    internal fun newWebView(context: Context): WebView {
        // Debug builds only: expose the WebView to adb/Chrome DevTools so the
        // terminal's own state (buffer, grid, theme) can be inspected.
        if (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        return WebView(context).apply {
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        isFocusable = true
        isFocusableInTouchMode = true
        setBackgroundColor(Color.BLACK)
        addJavascriptInterface(TerminalJsBridge(this@TerminalWebController), "AndroidBridge")
        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String?) {
                onPageLoaded()
            }
        }
        attach(this)
        loadUrl(INDEX_URL)
        }
    }
}

private const val INDEX_URL = "file:///android_asset/terminal/index.html"

/** The JS → Kotlin back-channel. */
private class TerminalJsBridge(private val controller: TerminalWebController) {
    @JavascriptInterface
    fun onData(data: String) {
        val bytes = runCatching { XtermCodec.decode(data) }.getOrNull() ?: return
        mainHandler.post { controller.onData?.invoke(bytes) }
    }

    @JavascriptInterface
    fun onResize(cols: Int, rows: Int) {
        mainHandler.post { controller.onResize?.invoke(cols, rows) }
    }

    @JavascriptInterface
    fun onReady() {
        mainHandler.post { controller.onReady?.invoke() }
    }
}

/** The chosen mono face as a base64 data-URI @font-face pair, read from the
 *  bundled `res/font` so the APK ships the face once (no second copy in
 *  assets). A failure to read the font yields an empty rule and xterm falls
 *  back to its monospace. */
fun terminalFontCss(context: Context, face: TerminalFace): String = runCatching {
    fun read(id: Int): String =
        Base64.getEncoder().encodeToString(context.resources.openRawResource(id).readBytes())
    XtermFont.faceCss(read(face.regular), read(face.bold))
}.getOrDefault("")

/** The terminal surface: the WebView that owns xterm.js. */
@Composable
fun TerminalWebView(
    theme: XtermTheme,
    fontCss: String,
    cursorMode: CursorMode,
    cursorBlink: Boolean,
    controller: TerminalWebController,
    onData: (ByteArray) -> Unit,
    onResize: (Int, Int) -> Unit,
    onReady: () -> Unit,
    modifier: Modifier = Modifier,
) {
    controller.onData = onData
    controller.onResize = onResize
    controller.onReady = onReady
    controller.configure(theme, fontCss, cursorMode.xtermStyle(), cursorBlink)

    AndroidView(
        factory = { context -> controller.newWebView(context) },
        onRelease = { controller.dispose() },
        modifier = modifier,
    )
}
