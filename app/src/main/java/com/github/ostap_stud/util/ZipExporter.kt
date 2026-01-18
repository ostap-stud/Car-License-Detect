package com.github.ostap_stud.util

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.coroutines.CoroutineContext

object ZipExporter {

    var coroutineContext: CoroutineContext = Dispatchers.IO

    suspend fun export(
        context: Context,
        exportFileUri: Uri,
        dataPaths: Set<String>?,
        data: Map<String, ByteArray>? = null
    ): Boolean = withContext(coroutineContext) {
        return@withContext try {
            context.contentResolver.openOutputStream(exportFileUri).use {
                ZipOutputStream(it).use { zipOutputStream ->
                    dataPaths?.forEach { filePath ->
                        val file = File(filePath)
                        val zipEntry = ZipEntry("/data/${file.name}")
                        zipOutputStream.putNextEntry(zipEntry)
                        zipOutputStream.write(file.readBytes())
                        zipOutputStream.closeEntry()
                    }
                    data?.forEach { entry ->
                        val zipEntry = ZipEntry(entry.key)
                        zipOutputStream.putNextEntry(zipEntry)
                        zipOutputStream.write(entry.value)
                        zipOutputStream.closeEntry()
                    }
                }
            }
            true
        } catch (ex: Exception) {
            Log.e(this@ZipExporter::class.simpleName, ex.message.toString())
            false
        }
    }

}