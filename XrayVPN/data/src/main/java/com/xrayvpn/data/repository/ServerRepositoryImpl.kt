package com.xrayvpn.data.repository

import com.xrayvpn.common.constants.AppConfig
import com.xrayvpn.data.database.AppDatabase
import com.xrayvpn.data.mapper.ServerMapper
import com.xrayvpn.data.parser.ConfigUrlParser
import com.xrayvpn.data.remote.ConfigApiServiceImpl
import com.xrayvpn.domain.model.ServerConfig
import com.xrayvpn.domain.model.TestResult
import com.xrayvpn.domain.repository.ServerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.Socket
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * Implementation of ServerRepository
 */
class ServerRepositoryImpl(
    private val database: AppDatabase,
    private val apiService: ConfigApiServiceImpl
) : ServerRepository {
    
    private val serverDao = database.serverDao()
    
    override fun getAllServers(): Flow<List<ServerConfig>> {
        return serverDao.getAllServers()
            .map { entities -> entities.map { ServerMapper.toDomain(it) } }
            .flowOn(Dispatchers.IO)
    }
    
    override fun getServersBySource(sourceUrl: String): Flow<List<ServerConfig>> {
        return serverDao.getServersBySource(sourceUrl)
            .map { entities -> entities.map { ServerMapper.toDomain(it) } }
            .flowOn(Dispatchers.IO)
    }
    
    override suspend fun getServersGroupedBySource(): Map<String, List<ServerConfig>> {
        return withContext(Dispatchers.IO) {
            serverDao.getAllServers().firstOrNull()?.let { entities ->
                entities.groupBy { it.sourceUrl ?: "local" }
                    .mapValues { entry -> entry.value.map { ServerMapper.toDomain(it) } }
            } ?: emptyMap()
        }
    }
    
    override suspend fun getServerById(id: String): ServerConfig? {
        return withContext(Dispatchers.IO) {
            serverDao.getServerById(id)?.let { ServerMapper.toDomain(it) }
        }
    }
    
    override suspend fun addServer(server: ServerConfig) {
        withContext(Dispatchers.IO) {
            serverDao.insertServer(ServerMapper.toEntity(server))
        }
    }
    
    override suspend fun updateServer(server: ServerConfig) {
        withContext(Dispatchers.IO) {
            serverDao.updateServer(ServerMapper.toEntity(server))
        }
    }
    
    override suspend fun deleteServer(id: String) {
        withContext(Dispatchers.IO) {
            serverDao.deleteServerById(id)
        }
    }
    
    override suspend fun deleteServersBySource(sourceUrl: String) {
        withContext(Dispatchers.IO) {
            serverDao.deleteServersBySource(sourceUrl)
        }
    }
    
    override suspend fun setActiveServer(id: String?) {
        withContext(Dispatchers.IO) {
            serverDao.deactivateAllServers()
            id?.let { serverDao.activateServer(it) }
        }
    }
    
    override suspend fun getActiveServer(): ServerConfig? {
        return withContext(Dispatchers.IO) {
            serverDao.getActiveServer()?.let { ServerMapper.toDomain(it) }
        }
    }
    
    override suspend fun testServer(serverId: String): TestResult {
        return withContext(Dispatchers.IO) {
            val server = serverDao.getServerById(serverId)?.let { ServerMapper.toDomain(it) }
            
            if (server == null) {
                return@withContext TestResult(
                    serverId = serverId,
                    success = false,
                    errorMessage = "Server not found"
                )
            }
            
            try {
                val startTime = System.currentTimeMillis()
                val success = testConnection(server.address, server.port, server.tls)
                val delay = System.currentTimeMillis() - startTime
                
                // Update test result in database
                serverDao.updateServerTestResult(
                    serverId, 
                    if (success) delay else null,
                    System.currentTimeMillis()
                )
                
                TestResult(
                    serverId = serverId,
                    success = success,
                    delayMs = if (success) delay else null,
                    errorMessage = if (!success) "Connection failed" else null
                )
            } catch (e: Exception) {
                TestResult(
                    serverId = serverId,
                    success = false,
                    errorMessage = e.message ?: "Unknown error"
                )
            }
        }
    }
    
    override suspend fun testServers(serverIds: List<String>): Flow<Map<String, TestResult>> = flow {
        val results = mutableMapOf<String, TestResult>()
        
        // Test servers in parallel using coroutines
        serverIds.chunked(10).forEach { batch ->
            val batchResults = batch.map { serverId ->
                kotlinx.coroutines.async {
                    serverId to testServer(serverId)
                }
            }.awaitAll().toMap()
            
            results.putAll(batchResults)
            emit(results.toMap())
        }
        
        emit(results.toMap())
    }.flowOn(Dispatchers.IO)
    
    override suspend fun importFromUrl(url: String): List<ServerConfig> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.fetchConfigList(url)
                val configs = parseConfigContent(response, url)
                
                // Delete old configs from this source and insert new ones
                serverDao.deleteServersBySource(url)
                if (configs.isNotEmpty()) {
                    serverDao.insertServers(configs.map { ServerMapper.toEntity(it) })
                }
                
                configs
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }
    
    override suspend fun parseConfigUrl(url: String): ServerConfig? {
        return withContext(Dispatchers.IO) {
            ConfigUrlParser.parse(url)
        }
    }
    
    /**
     * Parse configuration content (supports both plain text and base64 encoded)
     */
    private fun parseConfigContent(content: String, sourceUrl: String): List<ServerConfig> {
        val configs = mutableListOf<ServerConfig>()
        
        // Try to decode as base64 first (subscription format)
        val decodedContent = try {
            android.util.Base64.decode(content.trim(), android.util.Base64.DEFAULT)
                .toString(Charsets.UTF_8)
        } catch (e: Exception) {
            content // Not base64, use as is
        }
        
        decodedContent.split("\n").forEach { line ->
            val trimmedLine = line.trim()
            if (trimmedLine.isNotEmpty() && !trimmedLine.startsWith("#")) {
                ConfigUrlParser.parse(trimmedLine, sourceUrl)?.let { config ->
                    configs.add(config)
                }
            }
        }
        
        return configs
    }
    
    /**
     * Test TCP/TLS connection to server
     */
    private fun testConnection(host: String, port: Int, tls: String?): Boolean {
        return try {
            val socket = if (tls != AppConfig.NONE && tls != null) {
                // Create SSL socket for TLS connections
                val sslContext = createTrustAllSslContext()
                val factory = sslContext.socketFactory
                factory.createSocket(host, port).apply {
                    soTimeout = 5000
                    connectTimeout = 5000
                }
            } else {
                // Regular TCP socket
                Socket(Proxy.Type.DIRECT).apply {
                    soTimeout = 5000
                    connect(InetSocketAddress(host, port), 5000)
                }
            }
            
            socket.isConnected && !socket.isClosed
        } catch (e: IOException) {
            false
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Create SSL context that trusts all certificates (for testing purposes)
     */
    private fun createTrustAllSslContext(): SSLContext {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = emptyArray()
        })
        
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())
        return sslContext
    }
}
