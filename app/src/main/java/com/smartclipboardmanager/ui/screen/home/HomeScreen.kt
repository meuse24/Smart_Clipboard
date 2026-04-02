package com.smartclipboardmanager.ui.screen.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartclipboardmanager.R
import com.smartclipboardmanager.domain.model.ClipContentType
import com.smartclipboardmanager.ui.components.MediaCardPreview
import com.smartclipboardmanager.ui.theme.AppSpacing
import com.smartclipboardmanager.ui.viewmodel.HomeEntryUiModel
import com.smartclipboardmanager.ui.viewmodel.HomeUiState
import com.smartclipboardmanager.ui.viewmodel.HomeViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HomeRoute(
    onOpenDetail: (Long) -> Unit,
    onOpenInfo: () -> Unit,
    onImportClipboardRequest: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        uiState = uiState,
        onOpenDetail = onOpenDetail,
        onOpenInfo = onOpenInfo,
        onImportClipboardRequest = onImportClipboardRequest
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onOpenDetail: (Long) -> Unit,
    onOpenInfo: () -> Unit,
    onImportClipboardRequest: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { AppBarTitle() },
                actions = {
                    IconButton(onClick = onOpenInfo) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = stringResource(R.string.info_title)
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.l),
            contentPadding = PaddingValues(horizontal = AppSpacing.l, vertical = AppSpacing.l)
        ) {
            item {
                HeroCard(onImportClipboardRequest = onImportClipboardRequest)
            }

            item {
                Text(
                    text = stringResource(R.string.recent_entries_title),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (uiState.recentEntries.isEmpty()) {
                item { EmptyState() }
            } else {
                items(uiState.recentEntries, key = { it.id }) { item ->
                    HomeEntryCard(item = item, onClick = { onOpenDetail(item.id) })
                }
            }
        }
    }
}

@Composable
private fun AppBarTitle() {
    val primaryColor = MaterialTheme.colorScheme.primary
    val title = buildAnnotatedString {
        withStyle(SpanStyle(color = primaryColor, fontWeight = FontWeight.ExtraBold)) {
            append("M24")
        }
        append(" Smart Clipboard")
    }
    Text(text = title)
}

@Composable
private fun HeroCard(onImportClipboardRequest: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.l),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.s)
        ) {
            Icon(
                imageVector = Icons.Filled.ContentPaste,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = stringResource(R.string.hero_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = stringResource(R.string.import_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Button(
                onClick = onImportClipboardRequest,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = AppSpacing.xs)
            ) {
                Text(stringResource(R.string.import_clipboard_now))
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.s)
    ) {
        Icon(
            imageVector = Icons.Filled.ContentPaste,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = stringResource(R.string.empty_history),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HomeEntryCard(item: HomeEntryUiModel, onClick: () -> Unit) {
    val context = LocalContext.current
    val isMediaType = item.contentType == ClipContentType.IMAGE ||
        item.contentType == ClipContentType.FILE ||
        item.contentType == ClipContentType.COLOR ||
        item.contentType == ClipContentType.GEO_LOCATION

    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(AppSpacing.m),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TypeChip(label = item.typeLabel)
                Text(
                    text = "${item.sourceApp ?: stringResource(R.string.unknown_source)} • ${formatTimestamp(item.createdAtMillis)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isMediaType) {
                MediaCardPreview(
                    contentType = item.contentType,
                    content = item.previewContent,
                    mediaUri = item.mediaUri,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = item.previewContent,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                IconButton(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("clipboard", item.previewContent))
                    Toast.makeText(context, context.getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = stringResource(R.string.action_copy))
                }
                IconButton(onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, item.previewContent)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, null))
                }) {
                    Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.action_share))
                }
            }
        }
    }
}

@Composable
private fun TypeChip(label: String) {
    SuggestionChip(
        onClick = {},
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    )
}

private fun formatTimestamp(epochMillis: Long): String =
    DateTimeFormatter.ofPattern("dd.MM. HH:mm")
        .withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochMilli(epochMillis))
