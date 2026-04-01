package com.smartclipboardmanager.data.media

import android.content.Context
import android.net.Uri
import com.smartclipboardmanager.domain.media.MediaDeleter
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaStoreHelper @Inject constructor(
    @ApplicationContext private val context: Context
) : MediaDeleter {
    private val mediaDir: File
        get() = File(context.filesDir, "clipboard_media").also { it.mkdirs() }

    /**
     * Copies the content behind [uriString] into app-private storage so it
     * remains accessible after the clipboard is cleared.
     * Returns the absolute path of the saved file, or null on failure.
     */
    fun copyToLocalStorage(uriString: String, mimeType: String): String? {
        val uri = runCatching { Uri.parse(uriString) }.getOrNull() ?: return null
        val ext = extensionForMimeType(mimeType, uriString)
        val destFile = File(mediaDir, "${System.currentTimeMillis()}.$ext")
        return runCatching {
            val copied = context.contentResolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output -> input.copyTo(output) }
                true
            } ?: false
            if (copied) destFile.absolutePath else null
        }.getOrNull()
    }

    override fun deleteFile(path: String) {
        runCatching { File(path).delete() }
    }

    fun displayNameForUri(uriString: String): String {
        val uri = runCatching { Uri.parse(uriString) }.getOrNull() ?: return "file"
        return uri.lastPathSegment?.substringAfterLast('/')?.substringAfterLast(':') ?: "file"
    }

    private fun extensionForMimeType(mimeType: String, uriString: String): String = when {
        mimeType.contains("png", ignoreCase = true) -> "png"
        mimeType.contains("gif", ignoreCase = true) -> "gif"
        mimeType.contains("webp", ignoreCase = true) -> "webp"
        mimeType.startsWith("image/", ignoreCase = true) -> "jpg"
        else -> {
            val name = runCatching { Uri.parse(uriString).lastPathSegment } .getOrNull() ?: "bin"
            name.substringAfterLast('.', "bin")
        }
    }
}
