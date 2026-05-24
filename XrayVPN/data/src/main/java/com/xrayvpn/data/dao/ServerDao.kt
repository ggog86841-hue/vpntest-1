package com.xrayvpn.data.dao

import androidx.room.*
import com.xrayvpn.data.model.ServerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerDao {
    @Query("SELECT * FROM servers ORDER BY addedTime DESC")
    fun getAllServers(): Flow<List<ServerEntity>>
    
    @Query("SELECT * FROM servers WHERE sourceUrl = :sourceUrl ORDER BY addedTime DESC")
    fun getServersBySource(sourceUrl: String): Flow<List<ServerEntity>>
    
    @Query("SELECT * FROM servers WHERE id = :id")
    suspend fun getServerById(id: String): ServerEntity?
    
    @Query("SELECT * FROM servers WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveServer(): ServerEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServer(server: ServerEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServers(servers: List<ServerEntity>)
    
    @Update
    suspend fun updateServer(server: ServerEntity)
    
    @Delete
    suspend fun deleteServer(server: ServerEntity)
    
    @Query("DELETE FROM servers WHERE id = :id")
    suspend fun deleteServerById(id: String)
    
    @Query("DELETE FROM servers WHERE sourceUrl = :sourceUrl")
    suspend fun deleteServersBySource(sourceUrl: String)
    
    @Query("UPDATE servers SET isActive = 0")
    suspend fun deactivateAllServers()
    
    @Query("UPDATE servers SET isActive = 1 WHERE id = :id")
    suspend fun activateServer(id: String)
    
    @Query("UPDATE servers SET delay = :delay, lastTestTime = :testTime WHERE id = :id")
    suspend fun updateServerTestResult(id: String, delay: Long?, testTime: Long)
}
