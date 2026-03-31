package com.smartclipboardmanager

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartclipboardmanager.ui.navigation.AppNavHost
import com.smartclipboardmanager.ui.theme.SmartClipboardTheme
import com.smartclipboardmanager.ui.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

            SmartClipboardTheme(darkTheme = uiState.isDarkTheme) {
                AppNavHost(
                    homeViewModel = homeViewModel,
                    onImportClipboardRequest = ::importFromClipboardInForeground
                )
            }
        }

        handleIncomingIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return

        var handled = false
        when (intent.action) {
            ClipboardEntryPoints.ACTION_QUICK_IMPORT -> {
                importFromClipboardInForeground()
                handled = true
            }
            Intent.ACTION_SEND -> {
                val text = intent.getStringExtra(Intent.EXTRA_TEXT)
                if (!text.isNullOrBlank()) {
                    homeViewModel.importClipboardContent(text, sourceApp = "Shared Text")
                    toast(getString(R.string.imported_shared_text))
                    handled = true
                }
            }
            Intent.ACTION_PROCESS_TEXT -> {
                val text = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()
                if (!text.isNullOrBlank()) {
                    homeViewModel.importClipboardContent(text, sourceApp = "Process Text")
                    toast(getString(R.string.imported_processed_text))
                    handled = true
                }
            }
        }

        if (handled) {
            intent.action = Intent.ACTION_MAIN
            intent.removeExtra(Intent.EXTRA_TEXT)
            intent.removeExtra(Intent.EXTRA_PROCESS_TEXT)
        }
    }

    private fun importFromClipboardInForeground() {
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val text = clipboardManager.primaryClip
            ?.takeIf { it.itemCount > 0 }
            ?.getItemAt(0)
            ?.coerceToText(this)
            ?.toString()
            ?.trim()

        if (text.isNullOrBlank()) {
            toast(getString(R.string.clipboard_empty))
            return
        }

        homeViewModel.importClipboardContent(text, sourceApp = "Clipboard")
        toast(getString(R.string.imported_clipboard))
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
