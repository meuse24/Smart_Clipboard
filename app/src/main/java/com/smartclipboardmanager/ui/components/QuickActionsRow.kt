package com.smartclipboardmanager.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.smartclipboardmanager.R
import com.smartclipboardmanager.domain.model.ClipContentType
import com.smartclipboardmanager.ui.theme.AppSpacing
import java.io.File

@Composable
fun QuickActionsRow(
    content: String,
    contentType: ClipContentType,
    mediaUri: String? = null
) {
    val context = LocalContext.current
    val trimmed = content.trim()
    val digitsOnly = trimmed.filter { it.isDigit() }

    val isUrl = contentType == ClipContentType.URL ||
        (trimmed.isNotEmpty() && URL_REGEX.matches(trimmed))
    val isEmail = contentType == ClipContentType.EMAIL || EMAIL_REGEX.matches(trimmed)
    val isPhone = contentType == ClipContentType.PHONE_NUMBER ||
        (PHONE_ALLOWED_CHARS.matches(trimmed) && digitsOnly.length in 7..15)
    val isGeo = contentType == ClipContentType.GEO_LOCATION
    val isFile = contentType == ClipContentType.FILE

    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.s)) {
        if (isUrl) {
            Button(onClick = {
                if (trimmed.isBlank()) {
                    Toast.makeText(context, context.getString(R.string.quick_action_error_url), Toast.LENGTH_SHORT).show()
                    return@Button
                }
                val normalized = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                    trimmed
                } else {
                    "https://$trimmed"
                }
                val parsed = Uri.parse(normalized)
                val isSupportedScheme = parsed.scheme.equals("http", ignoreCase = true) ||
                    parsed.scheme.equals("https", ignoreCase = true)
                if (!isSupportedScheme || parsed.host.isNullOrBlank()) {
                    Toast.makeText(context, context.getString(R.string.quick_action_error_url), Toast.LENGTH_SHORT).show()
                    return@Button
                }
                launchIntent(context, Intent(Intent.ACTION_VIEW, parsed), context.getString(R.string.quick_action_error_url))
            }) {
                Text(stringResource(R.string.quick_action_open))
            }
        }

        if (isPhone) {
            Button(onClick = {
                val normalized = trimmed.filter { it.isDigit() || it == '+' }
                launchIntent(
                    context,
                    Intent(Intent.ACTION_DIAL, Uri.parse("tel:$normalized")),
                    context.getString(R.string.quick_action_error_dialer)
                )
            }) {
                Text(stringResource(R.string.quick_action_call))
            }
        }

        if (isEmail) {
            Button(onClick = {
                launchIntent(
                    context,
                    Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$trimmed")),
                    context.getString(R.string.quick_action_error_email)
                )
            }) {
                Text(stringResource(R.string.quick_action_email))
            }
        }

        if (isGeo) {
            Button(onClick = {
                val geoUri = Uri.parse("geo:0,0?q=${Uri.encode(trimmed)}")
                launchIntent(
                    context,
                    Intent(Intent.ACTION_VIEW, geoUri),
                    context.getString(R.string.quick_action_error_maps)
                )
            }) {
                Text(stringResource(R.string.quick_action_open_maps))
            }
        }

        if (isFile && mediaUri != null) {
            Button(onClick = {
                val fileUri = runCatching {
                    androidx.core.content.FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        File(mediaUri)
                    )
                }.getOrNull()
                if (fileUri == null) {
                    Toast.makeText(context, context.getString(R.string.quick_action_error_file), Toast.LENGTH_SHORT).show()
                    return@Button
                }
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(fileUri, "*/*")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                launchIntent(context, intent, context.getString(R.string.quick_action_error_file))
            }) {
                Text(stringResource(R.string.quick_action_open_file))
            }
        }
    }
}

private fun launchIntent(
    context: android.content.Context,
    intent: Intent,
    errorMessage: String
) {
    runCatching {
        context.startActivity(intent)
    }.onFailure {
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
    }
}

private val URL_REGEX = Regex(
    pattern = "^(https?://|ftp://|www\\.)[^\\s/$.?#].[^\\s]*$",
    option = RegexOption.IGNORE_CASE
)

private val EMAIL_REGEX = Regex(
    pattern = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
    option = RegexOption.IGNORE_CASE
)

private val PHONE_ALLOWED_CHARS = Regex("^[+()\\-./\\s\\d]{7,25}$")
