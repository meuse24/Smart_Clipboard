package com.smartclipboardmanager.ui.screen.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartclipboardmanager.R
import com.smartclipboardmanager.domain.model.ClipContentType
import com.smartclipboardmanager.ui.components.MediaCardPreview
import com.smartclipboardmanager.ui.components.QuickActionsRow
import com.smartclipboardmanager.ui.theme.AppSpacing
import com.smartclipboardmanager.ui.viewmodel.HistoryItemUiModel
import com.smartclipboardmanager.ui.viewmodel.HistoryUiState
import com.smartclipboardmanager.ui.viewmodel.HistoryViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HistoryRoute(
    onOpenDetail: (Long) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HistoryScreen(
        uiState = uiState,
        onOpenDetail = onOpenDetail,
        onQueryChange = viewModel::updateQuery
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    onOpenDetail: (Long) -> Unit,
    onQueryChange: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.history_title)) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(AppSpacing.l),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.s)
        ) {
            OutlinedTextField(
                value = uiState.query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(stringResource(R.string.search_history_label)) }
            )

            if (uiState.entries.isEmpty()) {
                Text(stringResource(R.string.empty_history))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.s)
                ) {
                    items(uiState.entries, key = { it.id }) { entry ->
                        HistoryRow(entry = entry, onClick = { onOpenDetail(entry.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(entry: HistoryItemUiModel, onClick: () -> Unit) {
    val isMediaType = entry.type == ClipContentType.IMAGE ||
        entry.type == ClipContentType.FILE ||
        entry.type == ClipContentType.COLOR ||
        entry.type == ClipContentType.GEO_LOCATION

    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(AppSpacing.m),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.s)
        ) {
            val pinPrefix = if (entry.isPinned) stringResource(R.string.badge_pinned) else ""
            val sensitivityPrefix = if (entry.isSensitive) stringResource(R.string.badge_sensitive) else ""

            if (isMediaType) {
                if (pinPrefix.isNotEmpty() || sensitivityPrefix.isNotEmpty()) {
                    Text(
                        text = pinPrefix + sensitivityPrefix,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                MediaCardPreview(
                    contentType = entry.type,
                    content = entry.displayContent,
                    mediaUri = entry.mediaUri,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = pinPrefix + sensitivityPrefix + entry.displayContent,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Text(
                text = "${entry.typeLabel} • ${entry.sourceApp ?: stringResource(R.string.unknown_source)} • ${formatTimestamp(entry.createdAtMillis)}",
                style = MaterialTheme.typography.bodySmall
            )

            QuickActionsRow(
                content = entry.rawContent,
                contentType = entry.type,
                mediaUri = entry.mediaUri
            )
        }
    }
}

private fun formatTimestamp(epochMillis: Long): String {
    return DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        .withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochMilli(epochMillis))
}
