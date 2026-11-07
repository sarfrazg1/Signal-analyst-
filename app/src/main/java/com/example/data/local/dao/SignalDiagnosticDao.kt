package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.SignalDiagnosticEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SignalDiagnosticDao {
    @Query("SELECT * FROM signal_diagnostics ORDER BY timestamp DESC")
    fun getAllDiagnostics(): Flow<List<SignalDiagnosticEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiagnostic(entity: SignalDiagnosticEntity): Long

    @Query("DELETE FROM signal_diagnostics WHERE id = :id")
    suspend fun deleteDiagnostic(id: Long)

    @Query("DELETE FROM signal_diagnostics")
    suspend fun clearAll()
}
