package com.smartclipboardmanager.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartclipboardmanager.R
import com.smartclipboardmanager.ui.theme.AppSpacing
import com.smartclipboardmanager.ui.viewmodel.HomeEntryUiModel
import com.smartclipboardmanager.ui.viewmodel.HomeUiState
import com.smartclipboardmanager.ui.viewmodel.HomeViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

@Composable
fun HomeRoute(
    onNavigateHistory: () -> Unit,
    onNavigateSettings: () -> Unit,
    onNavigateHelp: () -> Unit,
    onNavigateInfo: () -> Unit,
    onOpenDetail: (Long) -> Unit,
    onImportClipboardRequest: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        uiState = uiState,
        onNavigateHistory = onNavigateHistory,
        onNavigateSettings = onNavigateSettings,
        onNavigateHelp = onNavigateHelp,
        onNavigateInfo = onNavigateInfo,
        onOpenDetail = onOpenDetail,
        onImportClipboardRequest = onImportClipboardRequest
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onNavigateHistory: () -> Unit,
    onNavigateSettings: () -> Unit,
    onNavigateHelp: () -> Unit,
    onNavigateInfo: () -> Unit,
    onOpenDetail: (Long) -> Unit,
    onImportClipboardRequest: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = androidx.compose.material3.DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = stringResource(R.string.menu_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(AppSpacing.l)
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.settings_title)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateSettings()
                    }
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.help_title)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateHelp()
                    }
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.info_title)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateInfo()
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.home_title)) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = stringResource(R.string.menu_title)
                            )
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(AppSpacing.l),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.l)
            ) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(AppSpacing.m),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.s)
                    ) {
                        Text(
                            text = stringResource(R.string.import_description),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(
                            onClick = onImportClipboardRequest,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.import_clipboard_now))
                        }
                        Button(
                            onClick = onNavigateHistory,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.open_full_history))
                        }
                    }
                }

                Text(
                    text = stringResource(R.string.recent_entries_title),
                    style = MaterialTheme.typography.titleMedium
                )

                if (uiState.recentEntries.isEmpty()) {
                    Text(stringResource(R.string.empty_history))
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(AppSpacing.s)) {
                        items(uiState.recentEntries, key = { it.id }) { item ->
                            HomeEntryCard(item = item, onClick = { onOpenDetail(item.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeEntryCard(
    item: HomeEntryUiModel,
    onClick: () -> Unit
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(AppSpacing.m),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            Text(
                text = item.previewContent,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${item.typeLabel} • ${item.sourceApp ?: stringResource(R.string.unknown_source)} • ${formatTimestamp(item.createdAtMillis)}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun formatTimestamp(epochMillis: Long): String {
    return DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        .withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochMilli(epochMillis))
}
