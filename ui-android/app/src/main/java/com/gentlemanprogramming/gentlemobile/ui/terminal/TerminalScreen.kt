package com.gentlemanprogramming.gentlemobile.ui.terminal

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.gentlemanprogramming.gentlemobile.R
import com.gentlemanprogramming.gentlemobile.core.AgentDto
import com.gentlemanprogramming.gentlemobile.core.TerminalSession
import com.gentlemanprogramming.gentlemobile.core.WorkspaceRowDto
import com.gentlemanprogramming.gentlemobile.dictation.DictationEngineChoice
import com.gentlemanprogramming.gentlemobile.dictation.LocalDictationState
import com.gentlemanprogramming.gentlemobile.dictation.LocalWhisperDictation
import com.gentlemanprogramming.gentlemobile.dictation.WordReplacement
import com.gentlemanprogramming.gentlemobile.executor
import com.gentlemanprogramming.gentlemobile.mainHandler
import com.gentlemanprogramming.gentlemobile.ui.session.AndroidDictationEngine
import com.gentlemanprogramming.gentlemobile.ui.session.Attachment
import com.gentlemanprogramming.gentlemobile.ui.session.AttachmentState
import com.gentlemanprogramming.gentlemobile.ui.session.DictationEngine
import com.gentlemanprogramming.gentlemobile.ui.session.PaneSwitcherModel
import com.gentlemanprogramming.gentlemobile.ui.session.ShortcutAction
import com.gentlemanprogramming.gentlemobile.ui.session.ShortcutKey
import com.gentlemanprogramming.gentlemobile.ui.session.ShortcutPad
import com.gentlemanprogramming.gentlemobile.ui.session.TerminalKeyBar
import com.gentlemanprogramming.gentlemobile.ui.session.TerminalSidebar
import com.gentlemanprogramming.gentlemobile.ui.session.bytes
import com.gentlemanprogramming.gentlemobile.ui.session.formatBytes
import com.gentlemanprogramming.gentlemobile.ui.theme.Air
import com.gentlemanprogramming.gentlemobile.ui.theme.AppTheme
import com.gentlemanprogramming.gentlemobile.ui.theme.LocalTerminalFace
import com.gentlemanprogramming.gentlemobile.ui.theme.LocalPalette
import com.gentlemanprogramming.gentlemobile.ui.workspaces.WorkspacesModel
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

/**
 * The only session view (T48/T61): the PTY's byte stream rendered by the
 * vendored xterm.js engine in a WebView, full bleed. The top bar
 * (back · panes · refresh, 44 dp) stays tappable while the keyboard is up;
 * the key row and the composer sit at the bottom above the IME. The panes
 * button opens the workspace's pane/tab list with a `+` to create a tab;
 * picking one focuses it on the host.
 *
 * The composer is a normal field: the phone keyboard's own voice-to-text
 * types into it. When the terminal runs an agent (the app launched one), the
 * composer also offers camera and clip — an image uploads over SFTP and its
 * `[attached image: <path>]` line is typed into the PTY on send. A bare
 * shell offers no attachments: nothing would receive them.
 */
@Composable
fun TerminalScreen(
    title: String,
    theme: AppTheme,
    session: TerminalSession?,
    starting: Boolean,
    error: String?,
    attachments: List<Attachment>,
    thumbnails: Map<Long, ImageBitmap>,
    paneRows: List<PaneSwitcherModel.Row>,
    tabRows: List<PaneSwitcherModel.TabRow>,
    tabError: String?,
    workspaces: List<WorkspaceRowDto>,
    agents: List<AgentDto>,
    currentWorkspaceId: String,
    currentPaneId: String,
    onStart: (cols: Int, rows: Int) -> Unit,
    onReconnect: (cols: Int, rows: Int) -> Unit,
    onBack: () -> Unit,
    onCreateTab: () -> Unit,
    onDeleteTab: (PaneSwitcherModel.TabRow) -> Unit,
    onPaneSelected: (PaneSwitcherModel.Row) -> Unit,
    onCamera: () -> Unit,
    onPickImage: () -> Unit,
    onPickFile: () -> Unit,
    onRetryUpload: (Long) -> Unit,
    onRemoveAttachment: (Long) -> Unit,
    onAttachmentsConsumed: () -> Unit,
    cursorMode: CursorMode,
    cursorBlink: Boolean,
    useComposer: Boolean,
    onUseComposerChange: (Boolean) -> Unit,
    sendOnEnter: Boolean,
    shortcutPad: ShortcutPad,
    dictationEngine: DictationEngineChoice,
    autoSend: Boolean,
    activeModelPath: String?,
    replacements: List<WordReplacement>,
) {
    val palette = LocalPalette.current
    val terminalColors = remember(theme) { TerminalPalette.forTheme(theme) }
    val xtermTheme = remember(theme) { XtermTheme.from(terminalColors) }
    val terminalFace = LocalTerminalFace.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val fontCss = remember(terminalFace) { terminalFontCss(context, terminalFace) }
    val controller = remember { TerminalWebController() }
    var rendererReady by remember { mutableStateOf(false) }
    var closed by remember { mutableStateOf(false) }
    var lastCols by remember { mutableStateOf(0) }
    var lastRows by remember { mutableStateOf(0) }
    var input by remember { mutableStateOf(TextFieldValue("")) }
    val composer = remember { TerminalComposer() }
    var paneSheet by remember { mutableStateOf(false) }
    var padOpen by remember { mutableStateOf(false) }
    var sidebarOpen by remember { mutableStateOf(false) }
    var padLocked by remember { mutableStateOf(false) }
    var clipboardNotice by remember { mutableStateOf<String?>(null) }
    val clipboard = LocalClipboardManager.current
    val clipboardEmptyMessage = stringResource(R.string.cd_clipboard_empty)

    // T54/T60 (S8): dictation — record with the microphone, transcribe on
    // Stop, and land the text in the field (auto-sent only when the setting
    // says so). The composer's mic uses the resolved engine: local Whisper
    // when Voz is set to it, otherwise the phone's own recognizer — the same
    // two paths the Voz screen's test row drives.
    val localEngineActive = dictationEngine == DictationEngineChoice.LOCAL_WHISPER
    var dictationState by remember { mutableStateOf<LocalDictationState>(LocalDictationState.Idle) }
    val whisperDictation = remember(activeModelPath) {
        LocalWhisperDictation(
            modelPathProvider = { activeModelPath },
            replacementsProvider = { replacements },
            executor = executor,
            mainHandler = mainHandler,
        )
    }
    DisposableEffect(whisperDictation) {
        onDispose { whisperDictation.dispose() }
    }
    val phoneEngine = remember { AndroidDictationEngine(context) }
    DisposableEffect(phoneEngine) {
        onDispose { phoneEngine.dispose() }
    }
    var phoneListening by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var pendingDictation by remember { mutableStateOf<DictationPath?>(null) }
    fun submitNow() {
        val suffixes = attachments.mapNotNull {
            (it.state as? AttachmentState.Uploaded)?.promptSuffix
        }
        val bytes = composer.submit() + TerminalAttachments.suffixBytes(suffixes)
        if (bytes.isNotEmpty()) session?.write(bytes)
        input = TextFieldValue("")
        if (suffixes.isNotEmpty()) onAttachmentsConsumed()
    }
    fun dictationListener() = object : LocalWhisperDictation.Listener {
        override fun onState(state: LocalDictationState) {
            dictationState = state
        }

        override fun onFinal(text: String, elapsedMillis: Long) {
            dictationState = LocalDictationState.Idle
            if (text.isNotBlank()) {
                input = TextFieldValue(mergeDictated(input.text, text))
                if (autoSend) submitNow()
            }
        }
    }
    val phoneListener = remember(phoneEngine) {
        object : DictationEngine.Listener {
            override fun onPartial(text: String) {}

            override fun onFinal(text: String) {
                phoneListening = false
                phoneError = null
                if (text.isNotBlank()) {
                    input = TextFieldValue(mergeDictated(input.text, text))
                }
            }

            override fun onError(message: String) {
                phoneListening = false
                phoneError = message
            }
        }
    }
    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        when (pendingDictation) {
            DictationPath.LOCAL -> {
                if (granted) {
                    whisperDictation.start(dictationListener())
                } else {
                    dictationState = LocalDictationState.Error(
                        context.getString(R.string.dict_permission_refused),
                    )
                }
            }
            DictationPath.PHONE -> {
                if (granted) {
                    phoneListening = true
                    phoneError = null
                    phoneEngine.start(phoneListener)
                } else {
                    phoneError = context.getString(R.string.dict_permission_refused)
                }
            }
            null -> {}
        }
        pendingDictation = null
    }
    fun startDictation() {
        if (localEngineActive) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                whisperDictation.start(dictationListener())
            } else {
                pendingDictation = DictationPath.LOCAL
                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
            return
        }
        phoneError = null
        val reason = phoneEngine.availabilityReason()
        if (reason != null) {
            phoneError = reason
            return
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            pendingDictation = DictationPath.PHONE
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        phoneListening = true
        phoneEngine.start(phoneListener)
    }
    fun stopDictation() {
        if (localEngineActive) {
            whisperDictation.stop(dictationListener())
        } else {
            phoneEngine.stop()
            phoneListening = false
        }
    }
    fun cancelDictation() {
        if (localEngineActive) {
            whisperDictation.cancel(dictationListener())
        } else {
            phoneEngine.cancel()
            phoneListening = false
        }
    }

    fun pressKey(key: ShortcutKey) {
        val bytes = key.bytes(composer.ctrlSticky, composer.altSticky)
        if (bytes.isNotEmpty()) {
            session?.write(bytes)
            composer.disarm()
        }
    }

    fun pressChip(key: ShortcutKey) {
        when ((key.action as? ShortcutAction.Key)?.name) {
            "ctrl" -> composer.toggleCtrl()
            "alt" -> composer.toggleAlt()
            else -> pressKey(key)
        }
    }

    fun pasteClipboard() {
        val text = clipboard.getText()?.text.orEmpty()
        if (text.isEmpty()) {
            clipboardNotice = clipboardEmptyMessage
        } else {
            session?.write(text.toByteArray(Charsets.UTF_8))
        }
    }

    LaunchedEffect(clipboardNotice) {
        if (clipboardNotice != null) {
            delay(4000)
            clipboardNotice = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.bg),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
        ) {
            TerminalTopBar(title = title, onBack = onBack, onOpenPanes = { paneSheet = true })
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            TerminalWebView(
                theme = xtermTheme,
                fontCss = fontCss,
                cursorMode = cursorMode,
                cursorBlink = cursorBlink,
                controller = controller,
                onData = { bytes -> session?.write(bytes) },
                onResize = { cols, rows ->
                    lastCols = cols
                    lastRows = rows
                    val s = session
                    if (s == null) {
                        onStart(cols, rows)
                    } else {
                        val grid = XtermResize.decide(s.cols().toInt(), s.rows().toInt(), cols, rows)
                        if (grid != null) s.resize(grid.cols.toUInt(), grid.rows.toUInt())
                    }
                },
                onReady = { rendererReady = true },
                modifier = Modifier.fillMaxSize(),
            )
            // Drain the PTY as bytes arrive; nothing is batched.
            LaunchedEffect(session) {
                val s = session ?: return@LaunchedEffect
                closed = false
                while (true) {
                    val bytes = s.read(4096u)
                    if (bytes.isNotEmpty()) controller.write(bytes)
                    if (s.isClosed()) {
                        closed = true
                        break
                    }
                    delay(20)
                }
            }
            // Direct mode types into xterm's own textarea: focus it once the
            // renderer is up and whenever the mode flips back to direct.
            LaunchedEffect(useComposer, rendererReady) {
                if (!useComposer && rendererReady) controller.focus()
            }
            val overlay = when {
                error != null -> error
                starting -> "Starting the shell…"
                closed -> "The shell has ended."
                else -> null
            }
            if (overlay != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = overlay,
                            color = palette.muted,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                        )
                        if ((closed || error != null) && !starting && lastCols > 0 && lastRows > 0) {
                            TextButton(onClick = { onReconnect(lastCols, lastRows) }) {
                                Text(
                                    text = stringResource(R.string.terminal_reconnect),
                                    color = palette.accent,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Monospace,
                                )
                            }
                        }
                    }
                }
            }
            if (clipboardNotice != null) {
                Text(
                    text = clipboardNotice.orEmpty(),
                    color = palette.muted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(palette.surfaceRaised.copy(alpha = 0.92f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
            if (padOpen) {
                FloatingPad(
                    locked = padLocked,
                    onToggleLock = { padLocked = !padLocked },
                    onDismiss = { padOpen = false },
                    onKey = ::pressChip,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
        TerminalKeyRow(
            composer = composer,
            useComposer = useComposer,
            padOpen = padOpen,
            sidebarOpen = sidebarOpen,
            onUseComposerChange = onUseComposerChange,
            onTogglePad = { padOpen = !padOpen },
            onToggleSidebar = { sidebarOpen = !sidebarOpen },
            onPasteClipboard = ::pasteClipboard,
            onKey = { bytes -> session?.write(bytes) },
        )
        if (useComposer) {
            TerminalComposerRow(
                enabled = session != null && !closed,
                attachments = attachments,
                thumbnails = thumbnails,
                value = input,
                sendOnEnter = sendOnEnter,
                dictationState = dictationState,
                phoneListening = phoneListening,
                phoneError = phoneError,
                onValueChange = { new ->
                    val delta = composer.commit(new.text)
                    if (delta.isNotEmpty()) session?.write(delta)
                    input = if (composer.fieldText == new.text) new else TextFieldValue(composer.fieldText)
                },
                onSubmit = { submitNow() },
                onStartDictation = { startDictation() },
                onStopDictation = { stopDictation() },
                onCancelDictation = { cancelDictation() },
                onCamera = onCamera,
                onPickImage = onPickImage,
                onPickFile = onPickFile,
                onRetryUpload = onRetryUpload,
                onRemoveAttachment = onRemoveAttachment,
            )
        }
        }
        if (sidebarOpen) {
            TerminalWorkspacesSidebar(
                workspaces = workspaces,
                agents = agents,
                currentWorkspaceId = currentWorkspaceId,
                currentPaneId = currentPaneId,
                onDismiss = { sidebarOpen = false },
            )
        }
    }

    if (paneSheet) {
        TerminalPaneSheet(
            rows = paneRows,
            tabRows = tabRows,
            tabError = tabError,
            onDismiss = { paneSheet = false },
            onPick = { row ->
                paneSheet = false
                onPaneSelected(row)
            },
            onCreateTab = {
                paneSheet = false
                onCreateTab()
            },
            onDeleteTab = onDeleteTab,
        )
    }
}

@Composable
private fun TerminalTopBar(title: String, onBack: () -> Unit, onOpenPanes: () -> Unit) {
    val palette = LocalPalette.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .padding(horizontal = 4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back_chevron),
                contentDescription = stringResource(R.string.inbox_back),
                tint = palette.accent,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
        Box(
            modifier = Modifier
                .size(44.dp)
                .clickable(onClick = onOpenPanes),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_pane),
                contentDescription = stringResource(R.string.session_panes),
                tint = palette.accent,
                modifier = Modifier.size(18.dp),
            )
        }
        Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(R.drawable.ic_refresh),
                contentDescription = stringResource(R.string.session_refresh),
                tint = palette.accent,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun TerminalKeyRow(
    composer: TerminalComposer,
    useComposer: Boolean,
    padOpen: Boolean,
    sidebarOpen: Boolean,
    onUseComposerChange: (Boolean) -> Unit,
    onTogglePad: () -> Unit,
    onToggleSidebar: () -> Unit,
    onPasteClipboard: () -> Unit,
    onKey: (ByteArray) -> Unit,
) {
    fun press(key: ShortcutKey) {
        val bytes = key.bytes(composer.ctrlSticky, composer.altSticky)
        if (bytes.isNotEmpty()) {
            onKey(bytes)
            composer.disarm()
        }
    }

    fun pressChip(key: ShortcutKey) {
        when ((key.action as? ShortcutAction.Key)?.name) {
            "ctrl" -> composer.toggleCtrl()
            "alt" -> composer.toggleAlt()
            else -> press(key)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TerminalKeyBar.barChips().forEach { chip ->
            val name = (chip.action as? ShortcutAction.Key)?.name
            val pressed = name == "ctrl" && composer.ctrlSticky
            TerminalKeyChip(
                label = chip.label,
                pressed = pressed,
                modifier = Modifier.weight(1f),
            ) { pressChip(chip) }
        }
        BarIcon(
            drawable = R.drawable.ic_arrows,
            description = stringResource(R.string.cd_pad),
            selected = padOpen,
            onClick = onTogglePad,
        )
        BarIcon(
            drawable = R.drawable.ic_branches,
            description = stringResource(R.string.cd_branches),
            selected = sidebarOpen,
            onClick = onToggleSidebar,
        )
        BarIcon(
            drawable = R.drawable.ic_clipboard,
            description = stringResource(R.string.cd_clipboard),
            selected = false,
            onClick = onPasteClipboard,
        )
        BarIcon(
            drawable = R.drawable.ic_chat,
            description = stringResource(R.string.cd_input_composer),
            selected = useComposer,
            onClick = { onUseComposerChange(!useComposer) },
        )
        BarIcon(
            drawable = R.drawable.ic_keyboard,
            description = stringResource(R.string.cd_input_direct),
            selected = !useComposer,
            onClick = { onUseComposerChange(false) },
        )
    }
}

/** One 44 dp icon of the bar; the selected surface is marked in the accent. */
@Composable
private fun BarIcon(
    drawable: Int,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val palette = LocalPalette.current
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) palette.selection else Color.Transparent)
            .clickable(onClick = onClick)
            .semantics { contentDescription = description },
    ) {
        Icon(
            painter = painterResource(drawable),
            contentDescription = null,
            tint = if (selected) palette.accent else palette.muted,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun TerminalKeyChip(
    label: String,
    pressed: Boolean,
    modifier: Modifier = Modifier,
    onPress: () -> Unit,
) {
    val palette = LocalPalette.current
    Box(
        modifier = modifier
            .height(44.dp)
            .padding(horizontal = 3.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .border(
                    1.dp,
                    if (pressed) palette.accentEdge else palette.line,
                    RoundedCornerShape(8.dp),
                )
                .background(
                    if (pressed) palette.selection else palette.surfaceRaised,
                    RoundedCornerShape(8.dp),
                )
                .clickable(onClick = onPress),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                color = if (pressed) palette.accentEdge else MaterialTheme.colorScheme.onBackground,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
            )
        }
    }
}



/**
 * The terminal composer (T49): the app's rounded field with its side
 * margins and, beside it, the mic and the send action. The mic dictates
 * into the field with the resolved Voz engine (local Whisper or the phone's
 * recognizer); the send action sends. When the terminal runs an agent, the
 * field carries camera and clip inside; a bare shell keeps a plain field.
 *
 * T60 (S8): the mic is its own 44 dp target next to send, never a swap —
 * the person speaks, corrects and sends.
 */
@Composable
private fun TerminalComposerRow(
    enabled: Boolean,
    attachments: List<Attachment>,
    thumbnails: Map<Long, ImageBitmap>,
    value: TextFieldValue,
    sendOnEnter: Boolean,
    dictationState: LocalDictationState,
    phoneListening: Boolean,
    phoneError: String?,
    onValueChange: (TextFieldValue) -> Unit,
    onSubmit: () -> Unit,
    onStartDictation: () -> Unit,
    onStopDictation: () -> Unit,
    onCancelDictation: () -> Unit,
    onCamera: () -> Unit,
    onPickImage: () -> Unit,
    onPickFile: () -> Unit,
    onRetryUpload: (Long) -> Unit,
    onRemoveAttachment: (Long) -> Unit,
) {
    val palette = LocalPalette.current
    var attachmentsSheet by remember { mutableStateOf(false) }
    val listening = dictationState is LocalDictationState.Listening || phoneListening
    val transcribing = dictationState is LocalDictationState.Transcribing
    val dictationError = (dictationState as? LocalDictationState.Error)?.message ?: phoneError
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
    ) {
        when {
            listening -> DictationStatusLine(
                text = stringResource(R.string.comp_listening),
                onStop = onStopDictation,
                onCancel = onCancelDictation,
            )
            transcribing -> DictationStatusLine(
                text = stringResource(R.string.voz_test_transcribing),
            )
            dictationError != null -> DictationStatusLine(
                text = dictationError,
                error = true,
            )
        }
        attachments.forEach { attachment ->
            TerminalAttachmentRow(
                attachment = attachment,
                thumbnail = thumbnails[attachment.id],
                onRetry = { onRetryUpload(attachment.id) },
                onRemove = { onRemoveAttachment(attachment.id) },
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 44.dp, max = 96.dp)
                    .clip(RoundedCornerShape(Air.RADIUS_TERMINAL_DP.dp))
                    .background(palette.surface, RoundedCornerShape(Air.RADIUS_TERMINAL_DP.dp))
                    .border(1.dp, palette.line, RoundedCornerShape(Air.RADIUS_TERMINAL_DP.dp))
                    .padding(horizontal = 2.dp),
            ) {
                ComposerIcon(
                    drawable = R.drawable.ic_attach,
                    description = stringResource(R.string.cd_attach),
                    enabled = enabled,
                    onClick = { attachmentsSheet = true },
                )
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    enabled = enabled,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = if (enabled) MaterialTheme.colorScheme.onBackground else palette.textDisabled,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                    ),
                    cursorBrush = SolidColor(palette.accent),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        imeAction = if (sendOnEnter) ImeAction.Send else ImeAction.None,
                    ),
                    keyboardActions = if (sendOnEnter) {
                        KeyboardActions(onSend = { onSubmit() })
                    } else {
                        KeyboardActions()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 12.dp),
                )
            }
            Spacer(Modifier.width(8.dp))
            ComposerMicButton(
                enabled = enabled,
                listening = listening,
                transcribing = transcribing,
                onStart = onStartDictation,
                onStop = onStopDictation,
            )
            Spacer(Modifier.width(4.dp))
            ComposerSendCircle(
                enabled = enabled && value.text.isNotBlank(),
                onSubmit = onSubmit,
            )
        }
    }
    if (attachmentsSheet) {
        TerminalAttachmentsSheet(
            onDismiss = { attachmentsSheet = false },
            onCamera = {
                attachmentsSheet = false
                onCamera()
            },
            onPickImage = {
                attachmentsSheet = false
                onPickImage()
            },
            onPickFile = {
                attachmentsSheet = false
                onPickFile()
            },
        )
    }
}

/** The mic: its own 44 dp target next to send (T60). Idle it starts the
 *  dictation; listening it stops; transcribing it is quiet. */
@Composable
private fun ComposerMicButton(
    enabled: Boolean,
    listening: Boolean,
    transcribing: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
) {
    val palette = LocalPalette.current
    val description = if (listening) stringResource(R.string.cd_mic_stop) else stringResource(R.string.cd_mic)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(if (listening) palette.accent else Color.Transparent)
            .clickable(enabled = enabled && !transcribing, onClick = { if (listening) onStop() else onStart() })
            .semantics { contentDescription = description },
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_mic),
            contentDescription = null,
            tint = if (listening) palette.bg else palette.text,
            modifier = Modifier.size(20.dp),
        )
    }
}

/** The send action: the accent circle with the paper-plane, enabled once the
 *  field has text. */
@Composable
private fun ComposerSendCircle(
    enabled: Boolean,
    onSubmit: () -> Unit,
) {
    val palette = LocalPalette.current
    val description = stringResource(R.string.cd_send)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(if (enabled) palette.accent else palette.accentPressed)
            .clickable(enabled = enabled, onClick = onSubmit)
            .semantics { contentDescription = description },
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_send),
            contentDescription = null,
            tint = palette.bg.copy(alpha = if (enabled) 1f else 0.6f),
            modifier = Modifier.size(20.dp),
        )
    }
}

/** The one quiet line above the composer: listening/transcribing/error. */
@Composable
private fun DictationStatusLine(
    text: String,
    error: Boolean = false,
    onStop: (() -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
) {
    val palette = LocalPalette.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
    ) {
        Text(
            text = text,
            color = if (error) palette.error else palette.muted,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (onStop != null) {
            Text(
                text = stringResource(R.string.comp_stop),
                color = palette.accentEdge,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clickable(onClick = onStop)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
        if (onCancel != null) {
            Text(
                text = stringResource(R.string.comp_cancel),
                color = palette.muted,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clickable(onClick = onCancel)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun ComposerIcon(drawable: Int, description: String, enabled: Boolean, onClick: () -> Unit) {
    val palette = LocalPalette.current
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(44.dp)
            .clickable(enabled = enabled, onClick = onClick),
    ) {
        Icon(
            painter = painterResource(drawable),
            contentDescription = description,
            tint = if (enabled) palette.text else palette.muted,
            modifier = Modifier.size(20.dp),
        )
    }
}

/** The floating key pad (T59/T60): a cross of big keys anchored just above
 *  the bar with a transparent surround, a drag handle and a lock toggle.
 *  Locked it stays pinned and ignores the drag; unlocked it can be dragged
 *  down and dismissed past the threshold. */
@Composable
private fun FloatingPad(
    locked: Boolean,
    onToggleLock: () -> Unit,
    onDismiss: () -> Unit,
    onKey: (ShortcutKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalPalette.current
    var offsetY by remember { mutableStateOf(0f) }
    val dismissThresholdPx = with(LocalDensity.current) { 120.dp.toPx() }
    val lockDescription = stringResource(if (locked) R.string.cd_unlock_pad else R.string.cd_lock_pad)
    // Locking pins the pad back to its anchor: any drag offset is dropped.
    LaunchedEffect(locked) {
        if (locked) offsetY = 0f
    }
    Box(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .offset { IntOffset(0, offsetY.roundToInt()) }
                .padding(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clickable(onClick = onToggleLock)
                        .semantics { contentDescription = lockDescription },
                ) {
                    Icon(
                        painter = painterResource(if (locked) R.drawable.ic_lock else R.drawable.ic_lock_open),
                        contentDescription = null,
                        tint = palette.text,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .width(120.dp)
                        .height(32.dp)
                        .pointerInput(locked) {
                            if (!locked) {
                                detectVerticalDragGestures(
                                    onDragEnd = {
                                        if (offsetY > dismissThresholdPx) onDismiss() else offsetY = 0f
                                    },
                                    onVerticalDrag = { change, dragAmount ->
                                        change.consume()
                                        offsetY = (offsetY + dragAmount).coerceAtLeast(0f)
                                    },
                                )
                            }
                        },
                ) {
                    Box(
                        Modifier
                            .width(36.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(if (locked) palette.muted else palette.text),
                    )
                }
                Spacer(Modifier.width(44.dp))
            }
            PadCross(onKey = onKey)
        }
    }
}

/** The pad's fixed cross: backspace, up, clear on top; left, Enter, right in
 *  the middle; down at the bottom — the seven keys in row-major order. */
@Composable
private fun PadCross(onKey: (ShortcutKey) -> Unit) {
    val keys = TerminalKeyBar.padKeys()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PadKey(keys[0], onKey)
            PadKey(keys[1], onKey)
            PadKey(keys[2], onKey)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            PadKey(keys[3], onKey)
            PadKey(keys[4], onKey)
            PadKey(keys[5], onKey)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.size(56.dp))
            PadKey(keys[6], onKey)
            Spacer(Modifier.size(56.dp))
        }
    }
}

/** One big key of the floating pad: a filled plate, never a bare glyph on
 *  the transparent surround. */
@Composable
private fun PadKey(key: ShortcutKey, onKey: (ShortcutKey) -> Unit) {
    val palette = LocalPalette.current
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(3.dp)
            .size(50.dp)
            .border(1.dp, palette.line, RoundedCornerShape(10.dp))
            .background(palette.surfaceRaised, RoundedCornerShape(10.dp))
            .clickable { onKey(key) },
    ) {
        when (key.id) {
            "pad_backspace" -> Icon(
                painter = painterResource(R.drawable.ic_backspace),
                contentDescription = key.label,
                tint = palette.text,
                modifier = Modifier.size(22.dp),
            )
            "pad_clear" -> Icon(
                painter = painterResource(R.drawable.ic_brush),
                contentDescription = key.label,
                tint = palette.text,
                modifier = Modifier.size(22.dp),
            )
            else -> Text(
                text = key.label,
                color = palette.text,
                fontSize = 16.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
            )
        }
    }
}

/** The attachment sheet (T59): Cámara, Foto o video, Archivo — the three
 *  upload sources behind the composer's `+`. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TerminalAttachmentsSheet(
    onDismiss: () -> Unit,
    onCamera: () -> Unit,
    onPickImage: () -> Unit,
    onPickFile: () -> Unit,
) {
    val p = LocalPalette.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = p.surface,
    ) {
        Column(Modifier.padding(horizontal = 16.dp)) {
            TerminalKeyBar.attachmentRows().forEach { kind ->
                val drawable = when (kind) {
                    TerminalKeyBar.AttachmentKind.CAMERA -> R.drawable.ic_camera
                    TerminalKeyBar.AttachmentKind.GALLERY -> R.drawable.ic_clip
                    TerminalKeyBar.AttachmentKind.FILE -> R.drawable.ic_file
                }
                val label = when (kind) {
                    TerminalKeyBar.AttachmentKind.CAMERA -> R.string.att_camera
                    TerminalKeyBar.AttachmentKind.GALLERY -> R.string.att_gallery
                    TerminalKeyBar.AttachmentKind.FILE -> R.string.att_file
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 44.dp)
                        .clickable {
                            when (kind) {
                                TerminalKeyBar.AttachmentKind.CAMERA -> onCamera()
                                TerminalKeyBar.AttachmentKind.GALLERY -> onPickImage()
                                TerminalKeyBar.AttachmentKind.FILE -> onPickFile()
                            }
                        },
                ) {
                    Icon(
                        painter = painterResource(drawable),
                        contentDescription = null,
                        tint = p.text,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = stringResource(label),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 15.sp,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun TerminalAttachmentRow(
    attachment: Attachment,
    thumbnail: ImageBitmap?,
    onRetry: () -> Unit,
    onRemove: () -> Unit,
) {
    val palette = LocalPalette.current
    val removeCd = stringResource(R.string.att_remove_cd, attachment.displayName)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = attachment.displayName,
                color = palette.text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            when (val state = attachment.state) {
                is AttachmentState.Uploading -> Text(
                    text = stringResource(
                        R.string.att_uploading,
                        if (attachment.sizeBytes > 0) ((state.sentBytes * 100) / attachment.sizeBytes).toInt().coerceIn(0, 100) else 0,
                        formatBytes(state.sentBytes),
                        formatBytes(attachment.sizeBytes),
                    ),
                    color = palette.muted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                )
                is AttachmentState.Uploaded -> Text(
                    text = stringResource(R.string.att_uploaded, formatBytes(attachment.sizeBytes), state.remotePath),
                    color = palette.success,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                is AttachmentState.Failed -> Text(
                    text = stringResource(R.string.att_failed, state.message),
                    color = palette.error,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                AttachmentState.Ready -> Text(
                    text = stringResource(R.string.att_ready, formatBytes(attachment.sizeBytes)),
                    color = palette.muted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }
        if (attachment.state is AttachmentState.Failed) {
            Text(
                text = stringResource(R.string.att_retry),
                color = palette.accentEdge,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clickable(onClick = onRetry)
                    .padding(horizontal = 8.dp, vertical = 12.dp),
            )
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clickable(onClick = onRemove),
        ) {
            Text(text = "✕", color = palette.muted, fontSize = 15.sp)
        }
    }
}

/** The workspace's pane/tab list (T47/T51): tabs, each with a delete
 *  action, the panes under each, and a `+` that creates a tab. Picking a
 *  pane focuses it on the host; deleting a tab kills its processes, so it
 *  asks first — and the workspace's last tab is refused, never deleted. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TerminalPaneSheet(
    rows: List<PaneSwitcherModel.Row>,
    tabRows: List<PaneSwitcherModel.TabRow>,
    tabError: String?,
    onDismiss: () -> Unit,
    onPick: (PaneSwitcherModel.Row) -> Unit,
    onCreateTab: () -> Unit,
    onDeleteTab: (PaneSwitcherModel.TabRow) -> Unit,
) {
    val p = LocalPalette.current
    var pendingDelete by remember { mutableStateOf<PaneSwitcherModel.TabRow?>(null) }
    var refusedLast by remember { mutableStateOf(false) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = p.surface,
    ) {
        Column(Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = stringResource(R.string.session_panes),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            tabRows.forEach { tabRow ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 44.dp),
                ) {
                    Text(
                        text = tabRow.label,
                        color = p.muted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.weight(1f))
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .clickable {
                                if (tabRow.deletable) pendingDelete = tabRow else refusedLast = true
                            },
                    ) {
                        Text(
                            text = "✕",
                            color = if (tabRow.deletable) p.error else p.textDisabled,
                            fontSize = 15.sp,
                        )
                    }
                }
                rows.filter { it.tabId == tabRow.tabId }.forEach { row ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 44.dp)
                            .padding(start = 16.dp)
                            .clickable { onPick(row) },
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = row.title,
                                color = if (row.agentPane) p.accent else MaterialTheme.colorScheme.onBackground,
                                fontSize = 15.sp,
                                fontWeight = if (row.current) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = paneStateWord(row),
                                color = p.muted,
                                fontSize = 11.sp,
                            )
                        }
                        if (row.current) {
                            Text(text = "‹", color = p.accent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 44.dp)
                    .clickable(onClick = onCreateTab),
            ) {
                Text(text = "+", color = p.accent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.terminal_new_tab),
                    color = p.accent,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (tabError != null) {
                Text(
                    text = tabError,
                    color = p.error,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                )
            }
            Spacer(Modifier.height(12.dp))
        }
    }
    pendingDelete?.let { tab ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(R.string.set_tab_delete_title), fontWeight = FontWeight.SemiBold) },
            text = { Text(stringResource(R.string.set_tab_delete_body, tab.label), fontSize = 13.sp, lineHeight = 18.sp) },
            confirmButton = {
                TextButton(onClick = {
                    pendingDelete = null
                    onDeleteTab(tab)
                }) {
                    Text(stringResource(R.string.hosts_delete), color = p.error, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(stringResource(R.string.set_cancel), color = p.muted)
                }
            },
            containerColor = p.surfaceRaised,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            textContentColor = p.muted,
        )
    }
    if (refusedLast) {
        AlertDialog(
            onDismissRequest = { refusedLast = false },
            title = { Text(stringResource(R.string.set_tab_delete_title), fontWeight = FontWeight.SemiBold) },
            text = { Text(stringResource(R.string.set_tab_delete_last), fontSize = 13.sp, lineHeight = 18.sp) },
            confirmButton = {
                TextButton(onClick = { refusedLast = false }) {
                    Text(stringResource(R.string.set_cancel), color = p.accentEdge)
                }
            },
            containerColor = p.surfaceRaised,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            textContentColor = p.muted,
        )
    }
}

@Composable
private fun paneStateWord(row: PaneSwitcherModel.Row): String = when (val state = row.state) {
    is PaneSwitcherModel.State.Agent ->
        WorkspacesModel.statusWordRes(state.status)?.let { stringResource(it) }
            ?: stringResource(R.string.status_unknown)
    PaneSwitcherModel.State.Shell -> stringResource(R.string.session_pane_shell)
}

/**
 * The workspaces sidebar (T60): a slide-in panel from the left listing the
 * host's workspaces and their agents, the current one marked. Read-only —
 * entering another workspace focuses it in the shared daemon and would
 * disturb the desktop session (L22–L25), so the note says exactly that.
 */
@Composable
private fun TerminalWorkspacesSidebar(
    workspaces: List<WorkspaceRowDto>,
    agents: List<AgentDto>,
    currentWorkspaceId: String,
    currentPaneId: String,
    onDismiss: () -> Unit,
) {
    val palette = LocalPalette.current
    val rows = TerminalSidebar.rows(workspaces, agents, currentWorkspaceId, currentPaneId)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable(onClick = onDismiss),
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 300.dp)
                .fillMaxWidth(0.78f)
                .background(palette.surface)
                .clickable(enabled = false) {}
                .verticalScroll(androidx.compose.foundation.rememberScrollState())
                .padding(vertical = 12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Air.SIDE_DP.dp),
            ) {
                Text(
                    text = stringResource(R.string.cd_sidebar_title),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clickable(onClick = onDismiss),
                ) {
                    Text("✕", color = palette.muted, fontSize = 15.sp)
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.cd_sidebar_readonly),
                color = palette.muted,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                modifier = Modifier.padding(horizontal = Air.SIDE_DP.dp, vertical = 4.dp),
            )
            Spacer(Modifier.height(8.dp))
            rows.forEach { row ->
                when (row) {
                    is TerminalSidebar.Row.Workspace -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = Air.TOUCH_DP.dp)
                                .background(if (row.current) palette.selection else Color.Transparent)
                                .padding(horizontal = Air.SIDE_DP.dp),
                        ) {
                            Text(
                                text = row.label,
                                color = if (row.current) palette.accent else MaterialTheme.colorScheme.onBackground,
                                fontSize = Air.ROW_NAME_SP.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                            if (row.branch != null) {
                                Text(
                                    text = row.branch,
                                    color = palette.muted,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                    is TerminalSidebar.Row.Agent -> {
                        val statusWord = WorkspacesModel.statusWordRes(row.agent.status)
                            ?.let { stringResource(it) }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = Air.TOUCH_DP.dp)
                                .padding(start = Air.SIDE_DP.dp * 2, end = Air.SIDE_DP.dp),
                        ) {
                            Text(
                                text = row.title,
                                color = if (row.current) palette.accent else palette.text,
                                fontSize = Air.ROW_NAME_SP.sp,
                                fontWeight = if (row.current) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                            if (statusWord != null) {
                                Text(
                                    text = statusWord,
                                    color = palette.muted,
                                    fontSize = 11.sp,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** A canned PTY stream for the offline fixture: a prompt, coloured `ls`,
 *  wide glyphs, 256-colour and truecolour — no SSH, no host. */
fun fixtureTerminalBytes(): ByteArray = buildString {
    append("\u001B[1;32muser@host\u001B[0m:\u001B[1;34m~/gentle-mobile\u001B[0m$ ")
    append("\u001B[1;31mls --color=auto\u001B[0m\r\n")
    append("\u001B[01;34msrc\u001B[0m  \u001B[01;34mui-android\u001B[0m  \u001B[01;32mrun.sh\u001B[0m  ")
    append("\u001B[0;36mlink\u001B[0m  \u001B[0;31mbroken\u001B[0m\r\n")
    append("$ echo 界 💥\r\n")
    append("界 💥\r\n")
    append("$ \u001B[38;5;196mred256\u001B[0m \u001B[38;2;120;200;255mtruecolour\u001B[0m\r\n")
    append("red256 truecolour\r\n")
    append("$ ")
}.toByteArray(Charsets.UTF_8)

/** The offline terminal proof (T61): the xterm.js WebView over a canned PTY
 *  stream, so colours, wide characters and the four palettes can be seen
 *  without sshd. Launch with `--ez terminal_fixture true`. */
@Composable
fun TerminalFixtureScreen(theme: AppTheme, onBack: () -> Unit) {
    val palette = LocalPalette.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val terminalColors = remember(theme) { TerminalPalette.forTheme(theme) }
    val xtermTheme = remember(theme) { XtermTheme.from(terminalColors) }
    val terminalFace = LocalTerminalFace.current
    val fontCss = remember(terminalFace) { terminalFontCss(context, terminalFace) }
    val controller = remember { TerminalWebController() }
    var ready by remember { mutableStateOf(false) }
    LaunchedEffect(ready) {
        if (ready) controller.write(fixtureTerminalBytes())
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.bg)
            .safeDrawingPadding(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .padding(horizontal = 4.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_back_chevron),
                    contentDescription = stringResource(R.string.inbox_back),
                    tint = palette.accent,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Terminal · fixture (${theme.label})",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        }
        TerminalWebView(
            theme = xtermTheme,
            fontCss = fontCss,
            cursorMode = CursorMode.BLOCK,
            cursorBlink = false,
            controller = controller,
            onData = {},
            onResize = { _, _ -> },
            onReady = { ready = true },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
    }
}

/**
 * The transcript merge rule (T54, S8): the dictated text is appended to the
 * typed text with exactly one separating space; a blank field takes the
 * transcript as-is. Typed text is never dropped or duplicated.
 */
private fun mergeDictated(base: String, spoken: String): String {
    val spokenTrimmed = spoken.trim()
    if (spokenTrimmed.isEmpty()) return base
    val baseTrimmed = base.trimEnd()
    if (baseTrimmed.isEmpty()) return spokenTrimmed
    return "$baseTrimmed $spokenTrimmed"
}

/** Which dictation engine the composer's mic drives (T60). */
private enum class DictationPath { LOCAL, PHONE }
