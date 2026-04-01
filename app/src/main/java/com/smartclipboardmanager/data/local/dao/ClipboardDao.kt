package com.smartclipboardmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.smartclipboardmanager.data.local.entity.ClipboardEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClipboardDao {

    @Query("SELECT * FROM clipboard_entries ORDER BY isPinned DESC, createdAtMillis DESC")
    fun observeAll(): Flow<List<ClipboardEntryEntity>>

    @Query("SELECT * FROM clipboard_entries ORDER BY isPinned DESC, createdAtMillis DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<ClipboardEntryEntity>>

    @Query("SELECT * FROM clipboard_entries WHERE id = :id")
    fun observeById(id: Long): Flow<ClipboardEntryEntity?>

    @Query("UPDATE clipboard_entries SET isPinned = :pinned WHERE id = :id")
    suspend fun setPinned(id: Long, pinned: Boolean)

    @Query("DELETE FROM clipboard_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM clipboard_entries")
    suspend fun countAll(): Int

    @Query("SELECT mediaUri FROM clipboard_entries WHERE createdAtMillis < :thresholdMillis AND mediaUri IS NOT NULL")
    suspend fun getMediaUrisOlderThan(thresholdMillis: Long): List<String>

    @Query("DELETE FROM clipboard_entries WHERE createdAtMillis < :thresholdMillis")
    suspend fun deleteOlderThan(thresholdMillis: Long)

    @Transaction
    suspend fun getMediaUrisAndDeleteOlderThan(thresholdMillis: Long): List<String> {
        val uris = getMediaUrisOlderThan(thresholdMillis)
        deleteOlderThan(thresholdMillis)
        return uris
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ClipboardEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ClipboardEntryEntity>)
}
