package com.smartclipboardmanager.ui.screen.help

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.smartclipboardmanager.R
import com.smartclipboardmanager.ui.theme.AppSpacing

@Composable
fun HelpRoute(onBack: () -> Unit) {
    HelpScreen(onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.help_title)) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(AppSpacing.l),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.m)
        ) {
            HelpSection(
                title = stringResource(R.string.help_section_getting_started_title),
                body = stringResource(R.string.help_section_getting_started_body)
            )
            HelpSection(
                title = stringResource(R.string.help_section_import_title),
                body = stringResource(R.string.help_section_import_body)
            )
            HelpSection(
                title = stringResource(R.string.help_section_history_title),
                body = stringResource(R.string.help_section_history_body)
            )
            HelpSection(
                title = stringResource(R.string.help_section_quick_actions_title),
                body = stringResource(R.string.help_section_quick_actions_body)
            )
            HelpSection(
                title = stringResource(R.string.help_section_privacy_title),
                body = stringResource(R.string.help_section_privacy_body)
            )
            HelpSection(
                title = stringResource(R.string.help_section_settings_title),
                body = stringResource(R.string.help_section_settings_body)
            )
            HelpSection(
                title = stringResource(R.string.help_section_tile_title),
                body = stringResource(R.string.help_section_tile_body)
            )
            HelpSection(
                title = stringResource(R.string.help_section_troubleshooting_title),
                body = stringResource(R.string.help_section_troubleshooting_body)
            )
        }
    }
}

@Composable
private fun HelpSection(title: String, body: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(AppSpacing.m),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.s)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
