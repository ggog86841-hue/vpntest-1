package com.xrayvpn.domain.repository

import com.xrayvpn.domain.model.ServerConfig
import com.xrayvpn.domain.model.TestResult
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for server configuration operations
 */
interface ServerRepository {
    /**
     * Get all server configurations
     */
    fun getAllServers(): Flow<List<ServerConfig>>
    
    /**
     * Get servers by source URL
     */
    fun getServersBySource(sourceUrl: String): Flow<List<ServerConfig>>
    
    /**
     * Get servers grouped by source
     */
    suspend fun getServersGroupedBySource(): Map<String, List<ServerConfig>>
    
    /**
     * Get a single server by ID
     */
    suspend fun getServerById(id: String): ServerConfig?
    
    /**
     * Add a new server configuration
     */
    suspend fun addServer(server: ServerConfig)
    
    /**
     * Update an existing server configuration
     */
    suspend fun updateServer(server: ServerConfig)
    
    /**
     * Delete a server configuration
     */
    suspend fun deleteServer(id: String)
    
    /**
     * Delete all servers from a specific source
     */
    suspend fun deleteServersBySource(sourceUrl: String)
    
    /**
     * Set active server
     */
    suspend fun setActiveServer(id: String?)
    
    /**
     * Get the currently active server
     */
    suspend fun getActiveServer(): ServerConfig?
    
    /**
     * Test server connection and return delay
     */
    suspend fun testServer(serverId: String): TestResult
    
    /**
     * Test multiple servers in parallel
     */
    suspend fun testServers(serverIds: List<String>): Flow<Map<String, TestResult>>
    
    /**
     * Import servers from a subscription URL
     */
    suspend fun importFromUrl(url: String): List<ServerConfig>
    
    /**
     * Parse a single configuration URL
     */
    suspend fun parseConfigUrl(url: String): ServerConfig?
}
