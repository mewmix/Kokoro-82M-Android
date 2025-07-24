package com.example.kokoro82m.data

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class DownloadWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val modelId = inputData.getString("model_id")
        val downloadUrl = inputData.getString("download_url")
        val hfToken = inputData.getString("hf_token")

        if (modelId.isNullOrEmpty() || downloadUrl.isNullOrEmpty()) {
            return Result.failure()
        }

        return try {
            val modelDir = File(applicationContext.filesDir, "models")
            if (!modelDir.exists()) {
                modelDir.mkdirs()
            }
            val destinationFile = File(modelDir, "$modelId.task")

            Log.d("DownloadWorker", "Starting download for model '$modelId' from: $downloadUrl")

            val url = URL(downloadUrl)
            val connection = url.openConnection() as HttpsURLConnection
            hfToken?.let {
                connection.setRequestProperty("Authorization", "Bearer $it")
                Log.d("DownloadWorker", "Authorization header set for Hugging Face.")
            }
            connection.connect()

            val responseCode = connection.responseCode
            Log.d("DownloadWorker", "HTTP Response Code: $responseCode")

            if (responseCode != HttpsURLConnection.HTTP_OK) {
                Log.e("DownloadWorker", "HTTP error $responseCode for URL: $downloadUrl")
                return Result.failure()
            }

            val inputStream = connection.getInputStream()
            val outputStream = FileOutputStream(destinationFile)

            val buffer = ByteArray(1024)
            var len: Int
            while (inputStream.read(buffer).also { len = it } != -1) {
                outputStream.write(buffer, 0, len)
            }

            outputStream.close()
            inputStream.close()

            Log.d("DownloadWorker", "Model '$modelId' downloaded successfully to ${destinationFile.absolutePath}")
            Result.success()
        } catch (e: Exception) {
            Log.e("DownloadWorker", "Error downloading model '$modelId' from $downloadUrl", e)
            Result.failure()
        }
    }
}
