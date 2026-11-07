package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.SpeedTestResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SpeedTestResultDao {
    @Query("SELECT * FROM speed_test_results ORDER BY timestamp DESC")
    fun getAllResults(): Flow<List<SpeedTestResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(entity: SpeedTestResultEntity): Long

    @Query("DELETE FROM speed_test_results WHERE id = :id")
    suspend fun deleteResult(id: Long)

    @Query("DELETE FROM speed_test_results")
    suspend fun clearAll()
}
