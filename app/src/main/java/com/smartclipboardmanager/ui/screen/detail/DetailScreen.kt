package com.smartclipboardmanager.ui.screen.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartclipboardmanager.R
import com.smartclipboardmanager.domain.model.ClipboardEntry
import com.smartclipboardmanager.ui.components.QuickActionsRow
import com.smartclipboardmanager.ui.theme.AppSpacing
import com.smartclipboardmanager.ui.viewmodel.DetailViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun DetailRoute(
    onBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    if (uiState.deleted) {
        LaunchedEffect(Unit) {
            onBack()
        }
    }
    DetailScreen(
        entry = uiState.entry,
        onBack = onBack,
        onTogglePinned = viewModel::togglePinned,
        onDelete = viewModel::deleteEntry
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    entry: ClipboardEntry?,
    onBack: () -> Unit,
    onTogglePinned: () -> Unit,
    onDelete: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_action)
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (entry == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(AppSpacing.l)
            ) {
                Text(stringResource(R.string.entry_not_found))
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(AppSpacing.l),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.m)
        ) {
            Text(stringResource(R.string.detail_section_content), style = MaterialTheme.typography.titleMedium)
            Text(entry.content, style = MaterialTheme.typography.bodyLarge)

            Text(stringResource(R.string.detail_section_source), style = MaterialTheme.typography.titleMedium)
            Text(entry.sourceApp ?: stringResource(R.string.unknown_source), style = MaterialTheme.typography.bodyMedium)

            Text(stringResource(R.string.detail_section_captured_at), style = MaterialTheme.typography.titleMedium)
            Text(formatTimestamp(entry.createdAtMillis), style = MaterialTheme.typography.bodyMedium)

            Text(stringResource(R.string.detail_section_type), style = MaterialTheme.typography.titleMedium)
            Text(entry.contentType.name, style = MaterialTheme.typography.bodyMedium)

            if (entry.isSensitive) {
                Text(stringResource(R.string.detail_sensitive_marker), style = MaterialTheme.typography.bodyMedium)
            }

            QuickActionsRow(
                content = entry.content,
                contentType = entry.contentType
            )

            Button(onClick = onTogglePinned, modifier = Modifier.fillMaxWidth()) {
                Text(
                    if (entry.isPinned) stringResource(R.string.unpin_entry)
                    else stringResource(R.string.pin_entry)
                )
            }

            Button(onClick = onDelete, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.delete_entry))
            }
        }
    }
}

private fun formatTimestamp(epochMillis: Long): String {
    return DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        .withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochMilli(epochMillis))
}
