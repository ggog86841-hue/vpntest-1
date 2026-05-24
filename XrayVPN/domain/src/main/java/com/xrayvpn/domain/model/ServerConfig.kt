package com.xrayvpn.domain.model

/**
 * Represents a VPN server configuration
 */
data class ServerConfig(
    val id: String,
    val name: String,
    val protocol: String,
    val address: String,
    val port: Int,
    val uuid: String? = null,
    val password: String? = null,
    val security: String? = null,
    val encryption: String? = null,
    val flow: String? = null,
    val network: String? = null,
    val host: String? = null,
    val path: String? = null,
    val sni: String? = null,
    val alpn: List<String>? = null,
    val tls: String? = null,
    val reality: Boolean = false,
    val publicKey: String? = null,
    val shortId: String? = null,
    val spiderX: String? = null,
    val headerType: String? = null,
    val quicSecurity: String? = null,
    val quicKey: String? = null,
    val grpcServiceName: String? = null,
    val grpcMode: String? = null,
    val allowInsecure: Boolean = false,
    val fingerprint: String? = null,
    val remark: String? = null,
    val sourceUrl: String? = null,
    val addedTime: Long = System.currentTimeMillis(),
    val lastTestTime: Long? = null,
    val delay: Long? = null,
    val isActive: Boolean = false,
    val configJson: String? = null
)
