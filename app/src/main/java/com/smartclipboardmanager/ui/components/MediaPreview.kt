package com.smartclipboardmanager.ui.components

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import kotlin.math.roundToInt
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.smartclipboardmanager.domain.model.ClipContentType
import com.smartclipboardmanager.ui.theme.AppSpacing
import java.io.File

/**
 * Renders a type-specific preview widget for cards in Home and History.
 * For text-based types (URL, OTP, …) no preview is shown (caller renders text itself).
 */
@Composable
fun MediaCardPreview(
    contentType: ClipContentType,
    content: String,
    mediaUri: String?,
    modifier: Modifier = Modifier
) {
    when (contentType) {
        ClipContentType.IMAGE -> ImageThumbnail(mediaUri = mediaUri, modifier = modifier)
        ClipContentType.FILE  -> FileThumbnail(fileName = content, modifier = modifier)
        ClipContentType.COLOR -> ColorThumbnail(colorString = content, modifier = modifier)
        ClipContentType.GEO_LOCATION -> GeoThumbnail(coords = content, modifier = modifier)
        else -> Unit
    }
}

/**
 * Full-size preview for the Detail screen.
 */
@Composable
fun MediaDetailPreview(
    contentType: ClipContentType,
    content: String,
    mediaUri: String?,
    modifier: Modifier = Modifier
) {
    when (contentType) {
        ClipContentType.IMAGE -> ImageFull(mediaUri = mediaUri, modifier = modifier)
        ClipContentType.COLOR -> ColorSwatch(colorString = content, modifier = modifier)
        else -> Unit
    }
}

// ─── Image ───────────────────────────────────────────────────────────────────

@Composable
private fun ImageThumbnail(mediaUri: String?, modifier: Modifier = Modifier) {
    val model = mediaUri?.let { File(it) }
    AsyncImage(
        model = model,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(8.dp))
    )
}

@Composable
fun ImageFull(mediaUri: String?, modifier: Modifier = Modifier) {
    val model = mediaUri?.let { File(it) }
    AsyncImage(
        model = model,
        contentDescription = null,
        contentScale = ContentScale.FillWidth,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
    )
}

// ─── File ────────────────────────────────────────────────────────────────────

@Composable
private fun FileThumbnail(fileName: String, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Filled.AttachFile,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(AppSpacing.s))
        Text(
            text = fileName,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ─── Color ───────────────────────────────────────────────────────────────────

@Composable
private fun ColorThumbnail(colorString: String, modifier: Modifier = Modifier) {
    val color = parseColor(colorString)
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
        )
        Spacer(modifier = Modifier.width(AppSpacing.s))
        Text(
            text = colorString,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ColorSwatch(colorString: String, modifier: Modifier = Modifier) {
    val color = parseColor(colorString)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
    )
}

private fun parseColor(colorString: String): Color {
    val s = colorString.trim()
    // Hex: #RGB, #RRGGBB, #AARRGGBB — handled by AndroidColor.parseColor
    if (s.startsWith('#')) {
        return runCatching { Color(AndroidColor.parseColor(s)) }.getOrDefault(Color.Gray)
    }
    // rgb(r, g, b) / rgba(r, g, b, a)
    val rgbMatch = Regex(
        "^rgba?\\(\\s*(\\d{1,3})\\s*,\\s*(\\d{1,3})\\s*,\\s*(\\d{1,3})(\\s*,\\s*([\\d.]+))?\\s*\\)$",
        RegexOption.IGNORE_CASE
    ).find(s)
    if (rgbMatch != null) {
        val r = rgbMatch.groupValues[1].toIntOrNull()?.coerceIn(0, 255) ?: return Color.Gray
        val g = rgbMatch.groupValues[2].toIntOrNull()?.coerceIn(0, 255) ?: return Color.Gray
        val b = rgbMatch.groupValues[3].toIntOrNull()?.coerceIn(0, 255) ?: return Color.Gray
        val a = rgbMatch.groupValues[5].toFloatOrNull()?.coerceIn(0f, 1f) ?: 1f
        return Color(r / 255f, g / 255f, b / 255f, a)
    }
    // hsl(h, s%, l%) / hsla(h, s%, l%, a)
    val hslMatch = Regex(
        "^hsla?\\(\\s*(\\d{1,3})\\s*,\\s*(\\d{1,3})%\\s*,\\s*(\\d{1,3})%(\\s*,\\s*([\\d.]+))?\\s*\\)$",
        RegexOption.IGNORE_CASE
    ).find(s)
    if (hslMatch != null) {
        val h = hslMatch.groupValues[1].toFloatOrNull() ?: return Color.Gray
        val sat = (hslMatch.groupValues[2].toFloatOrNull() ?: return Color.Gray) / 100f
        val lig = (hslMatch.groupValues[3].toFloatOrNull() ?: return Color.Gray) / 100f
        val a = hslMatch.groupValues[5].toFloatOrNull()?.coerceIn(0f, 1f) ?: 1f
        return Color(AndroidColor.HSVToColor(floatArrayOf(h % 360f, sat, lig)).let { argb ->
            // HSL → HSV conversion: V = L + S*min(L,1-L), S_hsv = 2*(1-L/V)
            val v = lig + sat * minOf(lig, 1f - lig)
            val sHsv = if (v == 0f) 0f else 2f * (1f - lig / v)
            val packed = AndroidColor.HSVToColor(
                (a * 255).roundToInt(),
                floatArrayOf(h % 360f, sHsv, v)
            )
            packed
        })
    }
    return Color.Gray
}

// ─── Geo ─────────────────────────────────────────────────────────────────────

@Composable
private fun GeoThumbnail(coords: String, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(AppSpacing.s))
        Text(
            text = coords,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
