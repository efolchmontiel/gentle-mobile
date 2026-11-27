package com.gentlemanprogramming.gentlemobile.dictation

import android.os.Handler
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService

/**
 * T54 (S8): the model manager — the one place that downloads, cancels and
 * deletes models, and the one place that knows each model's current
 * [ModelStatus]. Every transition goes through [ModelDownloadStateMachine],
 * so the row state on the Voz screen is exactly the tested machine.
 *
 * Downloads run on the caller's [ExecutorService]; every state change is
 * posted to the main [Handler] via [onState]. Cancellation is a flag the
 * download loop checks between reads, so a running download stops promptly
 * and leaves no partial file behind (the `.part` is removed on cancel and
 * on failure).
 */
class ModelManager(
    private val repository: ModelRepository,
    private val executor: ExecutorService,
    private val mainHandler: Handler,
    private val onState: (String, ModelStatus) -> Unit,
) {
    private val machines = ConcurrentHashMap<String, ModelDownloadStateMachine>()
    private val cancelFlags = ConcurrentHashMap.newKeySet<String>()

    /** Deliver a state change on the main thread (the class's contract), so
     *  the Compose snapshot state behind [onState] is only touched there even
     *  when the download loop runs on the executor. */
    private fun emit(id: String, status: ModelStatus) {
        mainHandler.post { onState(id, status) }
    }

    /** Re-read disk and report the current state of every model. */
    fun refresh() {
        repository.ensureDir()
        for (model in WhisperCatalogue.models) {
            val machine = machines.computeIfAbsent(model.id) { ModelDownloadStateMachine() }
            if (machine.isDownloading()) continue
            val size = repository.onDiskSize(model)
            if (size != null) {
                machine.hydrateInstalled(size)
            }
            emit(model.id, machine.state)
        }
    }

    /** The current state of one model, from the machine if live, else disk. */
    fun statusOf(model: WhisperModel): ModelStatus =
        machines[model.id]?.state
            ?: repository.onDiskSize(model)?.let { ModelStatus.Installed(it) }
            ?: ModelStatus.NotInstalled

    /** Begin downloading [model]; the row shows progress from real bytes. */
    fun download(model: WhisperModel) {
        repository.ensureDir()
        val machine = machines.computeIfAbsent(model.id) { ModelDownloadStateMachine() }
        machine.start(totalBytes = null)
        emit(model.id, machine.state)
        executor.execute { runDownload(model, machine) }
    }

    /** The user cancelled; the row returns to "Download". */
    fun cancel(model: WhisperModel) {
        cancelFlags.add(model.id)
        machines[model.id]?.let { machine ->
            machine.cancel()
            emit(model.id, machine.state)
        }
    }

    /** Delete the installed file; the row returns to "Download". */
    fun delete(model: WhisperModel) {
        cancel(model)
        repository.delete(model)
        machines.remove(model.id)
        emit(model.id, ModelStatus.NotInstalled)
    }

    private fun runDownload(model: WhisperModel, machine: ModelDownloadStateMachine) {
        val tmp = repository.tempFileFor(model)
        var connection: HttpURLConnection? = null
        try {
            val url = URL(model.url)
            connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 20_000
                readTimeout = 20_000
                instanceFollowRedirects = true
                requestMethod = "GET"
            }
            connection.connect()
            val code = connection.responseCode
            if (code != 200) {
                fail(model, machine, "HTTP $code")
                return
            }
            val total = connection.contentLengthLong.takeIf { it > 0 }
            FileOutputStream(tmp).use { out ->
                val buffer = ByteArray(64 * 1024)
                var received = 0L
                val input = connection.inputStream
                while (true) {
                    if (cancelFlags.contains(model.id)) {
                        machine.cancel()
                        emit(model.id, machine.state)
                        tmp.delete()
                        return
                    }
                    val read = input.read(buffer)
                    if (read < 0) break
                    out.write(buffer, 0, read)
                    received += read
                    machine.progress(received, total)
                    emit(model.id, machine.state)
                }
            }
            if (!tmp.renameTo(repository.fileFor(model))) {
                fail(model, machine, "download could not be finalised")
                return
            }
            machine.complete(repository.onDiskSize(model) ?: tmp.length())
            emit(model.id, machine.state)
        } catch (error: Exception) {
            if (cancelFlags.contains(model.id)) {
                machine.cancel()
                emit(model.id, machine.state)
                tmp.delete()
            } else {
                fail(model, machine, error.message ?: "download failed")
            }
        } finally {
            cancelFlags.remove(model.id)
            connection?.disconnect()
        }
    }

    private fun fail(model: WhisperModel, machine: ModelDownloadStateMachine, message: String) {
        machine.fail(message)
        emit(model.id, machine.state)
        repository.tempFileFor(model).delete()
    }
}
