package com.xrayvpn.domain.model

/**
 * Result of a server connection test
 */
data class TestResult(
    val serverId: String,
    val success: Boolean,
    val delayMs: Long? = null,
    val errorMessage: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * State for server testing operations
 */
data class TestingState(
    val isTesting: Boolean = false,
    val totalServers: Int = 0,
    val testedCount: Int = 0,
    val successfulCount: Int = 0,
    val failedCount: Int = 0,
    val currentTestingServerId: String? = null,
    val results: Map<String, TestResult> = emptyMap()
)
