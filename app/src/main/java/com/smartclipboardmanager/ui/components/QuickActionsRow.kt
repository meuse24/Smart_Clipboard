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

@Composable
fun QuickActionsRow(
    content: String,
    contentType: ClipContentType
) {
    val context = LocalContext.current
    val trimmed = content.trim()
    val digitsOnly = trimmed.filter { it.isDigit() }

    // Fallback detection keeps actions available even if stored type is temporarily stale.
    val isUrl = contentType == ClipContentType.URL ||
        (trimmed.isNotEmpty() && URL_REGEX.matches(trimmed))
    val isEmail = contentType == ClipContentType.EMAIL || EMAIL_REGEX.matches(trimmed)
    val isPhone = contentType == ClipContentType.PHONE_NUMBER ||
        (PHONE_ALLOWED_CHARS.matches(trimmed) && digitsOnly.length in 7..15)

    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.s)) {
        if (isUrl) {
            Button(onClick = {
                val normalized = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                    trimmed
                } else {
                    "https://$trimmed"
                }
                launchIntent(
                    context = context,
                    intent = Intent(Intent.ACTION_VIEW, Uri.parse(normalized)),
                    errorMessage = context.getString(R.string.quick_action_error_url)
                )
            }) {
                Text(stringResource(R.string.quick_action_open))
            }
        }

        if (isPhone) {
            Button(onClick = {
                val normalized = trimmed.filter { it.isDigit() || it == '+' }
                launchIntent(
                    context = context,
                    intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$normalized")),
                    errorMessage = context.getString(R.string.quick_action_error_dialer)
                )
            }) {
                Text(stringResource(R.string.quick_action_call))
            }
        }

        if (isEmail) {
            Button(onClick = {
                launchIntent(
                    context = context,
                    intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$trimmed")),
                    errorMessage = context.getString(R.string.quick_action_error_email)
                )
            }) {
                Text(stringResource(R.string.quick_action_email))
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
