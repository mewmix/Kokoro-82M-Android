package com.example.kokoro82m.data

import android.content.Context
import com.example.kokoro82m.R
import com.example.kokoro82m.utils.SettingsManager
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
            val modelDir = java.io.File(context.filesDir, "models")
            val modelFile = java.io.File(modelDir, "${model.id}.task")
            val partialFile = java.io.File(modelDir, "${model.id}.task.part")
            model.isDownloaded = modelFile.exists()
            model.hasPartial = !model.isDownloaded && partialFile.exists()
            model.localPath = if (model.isDownloaded) modelFile.absolutePath else null
            modelList.add(model)
        }

        SettingsManager.getModelDir(context)?.let { path ->
            val dir = java.io.File(path)
            if (dir.exists()) {
                dir.listFiles { file -> file.extension == "task" || file.extension == "onnx" }?.forEach { file ->
                    modelList.add(
                        Model(
                            id = file.nameWithoutExtension,
                            name = file.nameWithoutExtension,
                            description = "External model",
                            repo = "",
                            downloadUrl = "",
                            gated = false,
                            isDownloaded = true,
                            hasPartial = false,
                            localPath = file.absolutePath
                        )
                    )
                }
            }
        }
        return modelList
    }

    fun getModel(id: String): Model? {
        return models.find { it.id == id }
    }

    fun deleteModel(model: Model) {
        val internalDir = java.io.File(context.filesDir, "models")
        if (model.localPath != null && !model.localPath!!.startsWith(internalDir.absolutePath)) {
            return
        }
        java.io.File(internalDir, "${model.id}.task").delete()
        java.io.File(internalDir, "${model.id}.task.part").delete()
        model.isDownloaded = false
        model.hasPartial = false
        model.localPath = null
    }
}
