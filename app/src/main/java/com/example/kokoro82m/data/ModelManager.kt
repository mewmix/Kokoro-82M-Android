package com.example.kokoro82m.data

import android.content.Context
import com.example.kokoro82m.R
import org.json.JSONObject

class ModelManager(private val context: Context) {

    val models: List<Model> by lazy {
        loadModelsFromAllowlist()
    }

    private fun loadModelsFromAllowlist(): List<Model> {
        val modelList = mutableListOf<Model>()
        val jsonString = context.resources.openRawResource(R.raw.model_allowlist).bufferedReader().use { it.readText() }
        val json = JSONObject(jsonString)
        val keys = json.keys()
        val externalDirPath = System.getenv("KOKORO_MODEL_DIR")
        while (keys.hasNext()) {
            val key = keys.next()
            val modelJson = json.getJSONObject(key)
            val model = Model(
                id = key,
                name = modelJson.getString("name"),
                description = modelJson.getString("description"),
                repo = modelJson.getString("repo"),
                downloadUrl = modelJson.getString("downloadUrl"),
                gated = modelJson.optBoolean("gated", false)
            )

            val internalDir = java.io.File(context.filesDir, "models")
            val internalFile = java.io.File(internalDir, "${model.id}.task")
            val externalFile = externalDirPath?.let { java.io.File(it, "${model.id}.task") }
            val partialFile = java.io.File(internalDir, "${model.id}.task.part")

            model.localPath = when {
                internalFile.exists() -> internalFile.absolutePath
                externalFile?.exists() == true -> externalFile.absolutePath
                else -> null
            }

            model.isDownloaded = model.localPath != null
            model.hasPartial = model.localPath == null && partialFile.exists()
            modelList.add(model)
        }
        return modelList
    }

    fun getModel(id: String): Model? {
        return models.find { it.id == id }
    }

    fun deleteModel(model: Model) {
        val modelDir = java.io.File(context.filesDir, "models")
        java.io.File(modelDir, "${model.id}.task").delete()
        java.io.File(modelDir, "${model.id}.task.part").delete()
        model.isDownloaded = false
        model.hasPartial = false
    }
}
