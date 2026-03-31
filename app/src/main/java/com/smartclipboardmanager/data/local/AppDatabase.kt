package com.smartclipboardmanager.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.smartclipboardmanager.data.local.dao.ClipboardDao
import com.smartclipboardmanager.data.local.entity.ClipboardEntryEntity

@Database(
    entities = [ClipboardEntryEntity::class],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clipboardDao(): ClipboardDao
}
