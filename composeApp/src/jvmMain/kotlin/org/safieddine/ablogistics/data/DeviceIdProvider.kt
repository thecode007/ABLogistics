package org.safieddine.ablogistics.data

import java.io.File
import java.nio.charset.StandardCharsets
import java.util.UUID

object DeviceIdProvider {
    private fun storageFile(): File {
        val appData = System.getenv("APPDATA")
        val baseDir = if (!appData.isNullOrBlank()) File(appData) else File(System.getProperty("user.home"))
        val dir = File(baseDir, "WarehouseHub")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "device-id")
    }

    fun getOrCreate(): String {
        val file = storageFile()
        return try {
            if (file.exists()) {
                file.readText(StandardCharsets.UTF_8).trim().ifBlank { newAndPersist(file) }
            } else {
                newAndPersist(file)
            }
        } catch (_: Exception) {
            UUID.randomUUID().toString()
        }
    }

    private fun newAndPersist(file: File): String {
        val id = UUID.randomUUID().toString()
        try { file.writeText(id, StandardCharsets.UTF_8) } catch (_: Exception) {}
        return id
    }
}

