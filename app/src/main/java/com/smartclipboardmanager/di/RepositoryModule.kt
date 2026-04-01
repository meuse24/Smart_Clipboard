package com.smartclipboardmanager.di

import com.smartclipboardmanager.data.media.MediaStoreHelper
import com.smartclipboardmanager.data.repository.ClipboardRepositoryImpl
import com.smartclipboardmanager.data.repository.SettingsRepositoryImpl
import com.smartclipboardmanager.domain.media.MediaDeleter
import com.smartclipboardmanager.domain.repository.ClipboardRepository
import com.smartclipboardmanager.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindClipboardRepository(impl: ClipboardRepositoryImpl): ClipboardRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindMediaDeleter(impl: MediaStoreHelper): MediaDeleter
}
