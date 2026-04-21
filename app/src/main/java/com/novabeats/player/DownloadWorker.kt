package com.novabeats.player

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.novabeats.data.local.dao.DownloadDao
import com.novabeats.data.local.dao.SongDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

@HiltWorker
class DownloadWorker @AssistedInject constructor(
    @Assisted ctx: Context,
    @Assisted params: WorkerParameters,
    private val downloadDao: DownloadDao,
    private val songDao: SongDao,
    private val okHttpClient: OkHttpClient
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val songId   = inputData.getString(KEY_SONG_ID)   ?: return Result.failure()
        val url      = inputData.getString(KEY_STREAM_URL) ?: return Result.failure()
        val fileName = inputData.getString(KEY_FILE_NAME)  ?: return Result.failure()

        return try {
            downloadDao.updateProgress(songId, "downloading", 0)

            val dir = File(applicationContext.getExternalFilesDir(null), "NovaBeats")
            dir.mkdirs()
            val file = File(dir, fileName)

            val request  = Request.Builder().url(url).build()
            val response = okHttpClient.newCall(request).execute()
            val body     = response.body ?: return Result.failure()

            val total   = body.contentLength()
            var written = 0L

            FileOutputStream(file).use { out ->
                body.byteStream().use { input ->
                    val buf = ByteArray(8192)
                    var read: Int
                    while (input.read(buf).also { read = it } != -1) {
                        out.write(buf, 0, read)
                        written += read
                        if (total > 0) {
                            val pct = ((written * 100) / total).toInt()
                            downloadDao.updateProgress(songId, "downloading", pct)
                        }
                    }
                }
            }

            downloadDao.updateProgress(songId, "done", 100)
            songDao.markDownloaded(songId, file.absolutePath)

            Result.success()
        } catch (e: Exception) {
            downloadDao.updateProgress(songId, "failed", 0)
            Result.failure()
        }
    }

    companion object {
        const val KEY_SONG_ID    = "song_id"
        const val KEY_STREAM_URL = "stream_url"
        const val KEY_FILE_NAME  = "file_name"

        fun buildRequest(songId: String, streamUrl: String, fileName: String): OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<DownloadWorker>()
                .setInputData(
                    workDataOf(
                        KEY_SONG_ID    to songId,
                        KEY_STREAM_URL to streamUrl,
                        KEY_FILE_NAME  to fileName
                    )
                )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
    }
}
