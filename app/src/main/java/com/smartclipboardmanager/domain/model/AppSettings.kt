package com.smartclipboardmanager.domain.model

data class AppSettings(
    val isDarkTheme: Boolean = false,
    val historyLimit: Int = 100,
    val persistOtpEntries: Boolean = false,
    val hideSensitivePreview: Boolean = true,
    val retentionDays: Int = 30
)
