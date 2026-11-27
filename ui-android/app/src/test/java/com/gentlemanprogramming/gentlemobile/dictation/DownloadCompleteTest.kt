package com.gentlemanprogramming.gentlemobile.dictation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadCompleteTest {
    @Test
    fun a_fully_received_download_is_complete() {
        assertTrue(downloadIsComplete(received = 1_000L, contentLength = 1_000L))
    }

    @Test
    fun a_truncated_download_is_incomplete() {
        assertFalse(downloadIsComplete(received = 600L, contentLength = 1_000L))
    }

    @Test
    fun an_unknown_content_length_cannot_be_verified_so_it_is_accepted() {
        assertTrue(downloadIsComplete(received = 600L, contentLength = null))
    }
}
