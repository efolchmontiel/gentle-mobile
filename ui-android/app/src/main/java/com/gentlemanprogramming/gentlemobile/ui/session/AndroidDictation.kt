package com.gentlemanprogramming.gentlemobile.ui.session

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.content.ContextCompat
import com.gentlemanprogramming.gentlemobile.R

/**
 * T14 (S8): the real recognizer behind the dictation state machine.
 *
 * Engine choice, in order:
 * 1. the on-device recognizer (`createOnDeviceSpeechRecognizer`, API 31+,
 *    when the device has an on-device model) — audio never leaves the phone
 *    and no network service can fail mid-dictation;
 * 2. the standard recognizer (`createSpeechRecognizer`) as an explicit
 *    fallback — same recording, same transcript, network-based.
 *
 * Both recognizers record the microphone, so both are gated by
 * RECORD_AUDIO ([needsPermission]); the on-device engine changes where the
 * audio is processed, not whether it is recorded. The permission is
 * requested at the moment of use — never ahead of the first tap.
 *
 * When neither exists there is no fake: [availabilityReason] carries the
 * exact reason and the machine renders the microphone disabled with it.
 *
 * Audio is never recorded or kept beyond the recognition request: no file
 * is written, no clip is stored — the only output is the transcript.
 *
 * The callbacks arrive on the main thread; [DictationSession] hands them
 * to the [DictationController], which owns every state decision.
 */
class AndroidDictationEngine(private val context: Context) : DictationEngine {

    /** The exact refusal line, in the stored locale (S24-9): plain words,
     *  never the request explanation reused as a refusal. */
    override val refusalMessage: String
        get() = context.getString(R.string.dict_permission_refused)

    /** The exact line when the system will not show the dialog again (S24-9). */
    override val permanentlyRefusedMessage: String
        get() = context.getString(R.string.dict_permission_permanent)

    override val needsPermission: Boolean
        get() = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO,
        ) != PackageManager.PERMISSION_GRANTED

    /**
     * `null` when a recognizer can start; otherwise the exact reason the
     * composer must show under the disabled microphone.
     */
    override fun availabilityReason(): String? = when {
        onDeviceAvailable() -> null
        SpeechRecognizer.isRecognitionAvailable(context) -> null
        else -> context.getString(R.string.dict_no_recognizer)
    }

    private fun onDeviceAvailable(): Boolean =
        android.os.Build.VERSION.SDK_INT >= 31 && SpeechRecognizer.isOnDeviceRecognitionAvailable(context)

    /**
     * Start listening. The caller has already routed the permission gate;
     * partials and finals go to [listener] as they arrive.
     */
    override fun start(listener: DictationEngine.Listener) {
        val recognizer = createRecognizer() ?: run {
            listener.onError(context.getString(R.string.dict_no_recognizer))
            return
        }
        recognizer.setRecognitionListener(Bridge(listener))
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
            )
            // Partials are the point of dictation: the field fills as the
            // user speaks, then the final result replaces them.
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        try {
            recognizer.startListening(intent)
        } catch (error: Exception) {
            destroy()
            // S24-9: the recognizer's raw error never reaches the screen —
            // the user sees the plain sentence, the detail stays in the log.
            Log.w(TAG, "dictation start failed", error)
            listener.onError(context.getString(R.string.dict_start_failed))
        }
    }

    private companion object {
        private const val TAG = "AndroidDictation"
    }

    /**
     * The user pressed Stop: let the recognizer deliver its final result —
     * recognizers flush it right after stopListening. The recognizer stays
     * alive until that final (or an error) arrives, or until cancel/dispose.
     */
    override fun stop() {
        try {
            recognizer?.stopListening()
        } catch (_: Exception) {
        }
    }

    /** The user pressed Cancel: discard the request, nothing is kept. */
    override fun cancel() {
        try {
            recognizer?.cancel()
        } catch (_: Exception) {
        }
        destroy()
    }

    /** Release the recognizer; called when the screen goes away. */
    override fun dispose() = destroy()

    private var recognizer: SpeechRecognizer? = null

    private fun createRecognizer(): SpeechRecognizer? {
        destroy()
        recognizer = if (onDeviceAvailable()) {
            SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        } else if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else {
            null
        }
        return recognizer
    }

    private fun destroy() {
        recognizer?.destroy()
        recognizer = null
    }

    /** Maps the framework callbacks onto the listener's three honest events. */
    private inner class Bridge(private val listener: DictationEngine.Listener) : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) = Unit
        override fun onBeginningOfSpeech() = Unit
        override fun onRmsChanged(rmsdB: Float) = Unit
        override fun onBufferReceived(buffer: ByteArray?) = Unit
        override fun onEndOfSpeech() = Unit
        override fun onPartialResults(partialResults: Bundle?) {
            firstLine(partialResults)?.let { listener.onPartial(it) }
        }

        override fun onResults(results: Bundle?) {
            val line = firstLine(results)
            destroy()
            line?.let { listener.onFinal(it) }
        }

        override fun onError(error: Int) {
            val message = describe(error)
            destroy()
            listener.onError(message)
        }

        override fun onEvent(eventType: Int, params: Bundle?) = Unit

        private fun firstLine(bundle: Bundle?): String? =
            bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()

        /** The recognizer's error codes, in words the composer can show. */
        private fun describe(error: Int): String {
            val message = dictationErrorMessageId(error)
            return if (message == R.string.dict_failed) {
                context.getString(message, error)
            } else {
                context.getString(message)
            }
        }
    }
}

/** The string resource for a [SpeechRecognizer] error code. [R.string.dict_failed]
 *  is the generic fallback and carries the raw code as its format argument;
 *  every other id is a complete message. */
internal fun dictationErrorMessageId(error: Int): Int = when (error) {
    SpeechRecognizer.ERROR_NO_MATCH -> R.string.dict_no_speech
    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> R.string.dict_silent
    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> R.string.dict_permission_refused
    SpeechRecognizer.ERROR_NETWORK,
    SpeechRecognizer.ERROR_NETWORK_TIMEOUT,
    SpeechRecognizer.ERROR_SERVER,
    SpeechRecognizer.ERROR_SERVER_DISCONNECTED -> R.string.dict_network_failed
    SpeechRecognizer.ERROR_RECOGNIZER_BUSY,
    SpeechRecognizer.ERROR_TOO_MANY_REQUESTS -> R.string.dict_busy
    SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED,
    SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE -> R.string.dict_language_unsupported
    SpeechRecognizer.ERROR_AUDIO -> R.string.dict_audio_failed
    else -> R.string.dict_failed
}
