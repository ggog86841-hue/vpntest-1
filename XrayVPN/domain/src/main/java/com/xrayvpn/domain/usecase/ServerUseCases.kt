package com.xrayvpn.domain.usecase

import com.xrayvpn.domain.model.ServerConfig
import com.xrayvpn.domain.model.TestResult
import com.xrayvpn.domain.repository.ServerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Use case to get all servers
 */
class GetAllServersUseCase(
    private val repository: ServerRepository
) {
    operator fun invoke(): Flow<List<ServerConfig>> = repository.getAllServers()
}

/**
 * Use case to get servers grouped by source
 */
class GetServersGroupedBySourceUseCase(
    private val repository: ServerRepository
) {
    suspend operator fun invoke(): Map<String, List<ServerConfig>> = 
        repository.getServersGroupedBySource()
}

/**
 * Use case to add a server
 */
class AddServerUseCase(
    private val repository: ServerRepository
) {
    suspend operator fun invoke(server: ServerConfig) {
        repository.addServer(server)
    }
}

/**
 * Use case to update a server
 */
class UpdateServerUseCase(
    private val repository: ServerRepository
) {
    suspend operator fun invoke(server: ServerConfig) {
        repository.updateServer(server)
    }
}

/**
 * Use case to delete a server
 */
class DeleteServerUseCase(
    private val repository: ServerRepository
) {
    suspend operator fun invoke(id: String) {
        repository.deleteServer(id)
    }
}

/**
 * Use case to set active server
 */
class SetActiveServerUseCase(
    private val repository: ServerRepository
) {
    suspend operator fun invoke(id: String?) {
        repository.setActiveServer(id)
    }
}

/**
 * Use case to get active server
 */
class GetActiveServerUseCase(
    private val repository: ServerRepository
) {
    suspend operator fun invoke(): ServerConfig? = repository.getActiveServer()
}

/**
 * Use case to test a single server
 */
class TestServerUseCase(
    private val repository: ServerRepository
) {
    suspend operator fun invoke(serverId: String): TestResult {
        return repository.testServer(serverId)
    }
}

/**
 * Use case to test multiple servers in parallel
 */
class TestServersUseCase(
    private val repository: ServerRepository
) {
    operator fun invoke(serverIds: List<String>): Flow<Map<String, TestResult>> = 
        repository.testServers(serverIds)
}

/**
 * Use case to import servers from URL
 */
class ImportFromUrlUseCase(
    private val repository: ServerRepository
) {
    suspend operator fun invoke(url: String): List<ServerConfig> = 
        repository.importFromUrl(url)
}

/**
 * Use case to parse a single config URL
 */
class ParseConfigUrlUseCase(
    private val repository: ServerRepository
) {
    suspend operator fun invoke(url: String): ServerConfig? = 
        repository.parseConfigUrl(url)
}
