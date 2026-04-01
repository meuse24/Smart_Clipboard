package com.smartclipboardmanager.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartclipboardmanager.R
import com.smartclipboardmanager.ui.theme.AppSpacing
import com.smartclipboardmanager.ui.viewmodel.SettingsUiState
import com.smartclipboardmanager.ui.viewmodel.SettingsViewModel
import kotlin.math.roundToInt

@Composable
fun SettingsRoute(
    onNavigateHelp: () -> Unit,
    onNavigateInfo: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsScreen(
        uiState = uiState,
        onNavigateHelp = onNavigateHelp,
        onNavigateInfo = onNavigateInfo,
        onDarkThemeChange = viewModel::setDarkTheme,
        onPersistOtpChange = viewModel::setPersistOtpEntries,
        onHideSensitivePreviewChange = viewModel::setHideSensitivePreview,
        onHistoryLimitChange = viewModel::updateHistoryLimit,
        onRetentionDaysChange = viewModel::updateRetentionDays
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onNavigateHelp: () -> Unit,
    onNavigateInfo: () -> Unit,
    onDarkThemeChange: (Boolean) -> Unit,
    onPersistOtpChange: (Boolean) -> Unit,
    onHideSensitivePreviewChange: (Boolean) -> Unit,
    onHistoryLimitChange: (Int) -> Unit,
    onRetentionDaysChange: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.settings_title)) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(AppSpacing.l),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.m)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(AppSpacing.m),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.m)
                ) {
                    Text(stringResource(R.string.section_appearance), style = MaterialTheme.typography.titleMedium)
                    ToggleRow(
                        label = stringResource(R.string.dark_theme),
                        checked = uiState.isDarkTheme,
                        onCheckedChange = onDarkThemeChange
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(AppSpacing.m),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.m)
                ) {
                    Text(stringResource(R.string.section_privacy), style = MaterialTheme.typography.titleMedium)
                    ToggleRow(
                        label = stringResource(R.string.persist_otp_entries),
                        checked = uiState.persistOtpEntries,
                        onCheckedChange = onPersistOtpChange
                    )
                    ToggleRow(
                        label = stringResource(R.string.hide_sensitive_preview),
                        checked = uiState.hideSensitivePreview,
                        onCheckedChange = onHideSensitivePreviewChange
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(AppSpacing.m),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.m)
                ) {
                    Text(stringResource(R.string.section_storage), style = MaterialTheme.typography.titleMedium)

                    Text(
                        stringResource(R.string.history_limit, uiState.historyLimit),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = uiState.historyLimit.toFloat(),
                        valueRange = 25f..1000f,
                        onValueChange = { onHistoryLimitChange(it.roundToInt()) }
                    )

                    Text(
                        stringResource(R.string.retention_days, uiState.retentionDays),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = uiState.retentionDays.toFloat(),
                        valueRange = 1f..365f,
                        onValueChange = { onRetentionDaysChange(it.roundToInt()) }
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    Text(
                        stringResource(R.string.section_about),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = AppSpacing.m, vertical = AppSpacing.m)
                    )
                    NavRow(
                        label = stringResource(R.string.help_title),
                        icon = { Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null) },
                        onClick = onNavigateHelp
                    )
                    NavRow(
                        label = stringResource(R.string.info_title),
                        icon = { Icon(Icons.Filled.Info, contentDescription = null) },
                        onClick = onNavigateInfo
                    )
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.m),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Box(modifier = Modifier.widthIn(min = 52.dp), contentAlignment = Alignment.CenterEnd) {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun NavRow(
    label: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = AppSpacing.m, vertical = AppSpacing.m),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.m),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
