package com.smartclipboardmanager.di

import android.content.Context
import androidx.room.Room
import com.smartclipboardmanager.data.local.AppDatabase
import com.smartclipboardmanager.data.local.dao.ClipboardDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "smart_clipboard.db"
        ).fallbackToDestructiveMigration(dropAllTables = true).build()
    }

    @Provides
    fun provideClipboardDao(database: AppDatabase): ClipboardDao = database.clipboardDao()
}
