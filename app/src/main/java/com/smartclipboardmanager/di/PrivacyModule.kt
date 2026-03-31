package com.smartclipboardmanager.di

import com.smartclipboardmanager.domain.privacy.SensitiveContentPolicy
import com.smartclipboardmanager.domain.privacy.SensitiveContentRedactor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PrivacyModule {

    @Provides
    @Singleton
    fun provideSensitiveContentPolicy(): SensitiveContentPolicy = SensitiveContentPolicy()

    @Provides
    @Singleton
    fun provideSensitiveContentRedactor(): SensitiveContentRedactor = SensitiveContentRedactor()
}
