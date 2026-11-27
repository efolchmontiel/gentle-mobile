package com.gentlemanprogramming.gentlemobile.ui.session

import android.speech.SpeechRecognizer
import com.gentlemanprogramming.gentlemobile.R
import org.junit.Assert.assertEquals
import org.junit.Test

class DictationErrorMessageTest {
    @Test
    fun language_errors_map_to_a_clear_message() {
        assertEquals(
            R.string.dict_language_unsupported,
            dictationErrorMessageId(SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED),
        )
        assertEquals(
            R.string.dict_language_unsupported,
            dictationErrorMessageId(SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE),
        )
    }

    @Test
    fun audio_and_server_errors_are_mapped() {
        assertEquals(R.string.dict_audio_failed, dictationErrorMessageId(SpeechRecognizer.ERROR_AUDIO))
        assertEquals(R.string.dict_network_failed, dictationErrorMessageId(SpeechRecognizer.ERROR_SERVER))
        assertEquals(R.string.dict_busy, dictationErrorMessageId(SpeechRecognizer.ERROR_TOO_MANY_REQUESTS))
    }

    @Test
    fun already_mapped_codes_keep_their_messages() {
        assertEquals(R.string.dict_no_speech, dictationErrorMessageId(SpeechRecognizer.ERROR_NO_MATCH))
        assertEquals(R.string.dict_silent, dictationErrorMessageId(SpeechRecognizer.ERROR_SPEECH_TIMEOUT))
        assertEquals(R.string.dict_busy, dictationErrorMessageId(SpeechRecognizer.ERROR_RECOGNIZER_BUSY))
    }

    @Test
    fun an_unknown_code_falls_back_to_the_generic_message() {
        assertEquals(R.string.dict_failed, dictationErrorMessageId(9999))
    }
}
