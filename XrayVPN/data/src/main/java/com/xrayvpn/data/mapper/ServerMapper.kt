package com.xrayvpn.data.mapper

import com.xrayvpn.data.model.ServerEntity
import com.xrayvpn.domain.model.ServerConfig
import kotlinx.serialization.json.Json

object ServerMapper {
    private val json = Json { ignoreUnknownKeys = true }
    
    fun toDomain(entity: ServerEntity): ServerConfig {
        return ServerConfig(
            id = entity.id,
            name = entity.name,
            protocol = entity.protocol,
            address = entity.address,
            port = entity.port,
            uuid = entity.uuid,
            password = entity.password,
            security = entity.security,
            encryption = entity.encryption,
            flow = entity.flow,
            network = entity.network,
            host = entity.host,
            path = entity.path,
            sni = entity.sni,
            alpn = entity.alpn?.let { 
                try {
                    json.decodeFromString<List<String>>(it)
                } catch (e: Exception) {
                    null
                }
            },
            tls = entity.tls,
            reality = entity.reality,
            publicKey = entity.publicKey,
            shortId = entity.shortId,
            spiderX = entity.spiderX,
            headerType = entity.headerType,
            quicSecurity = entity.quicSecurity,
            quicKey = entity.quicKey,
            grpcServiceName = entity.grpcServiceName,
            grpcMode = entity.grpcMode,
            allowInsecure = entity.allowInsecure,
            fingerprint = entity.fingerprint,
            remark = entity.remark,
            sourceUrl = entity.sourceUrl,
            addedTime = entity.addedTime,
            lastTestTime = entity.lastTestTime,
            delay = entity.delay,
            isActive = entity.isActive,
            configJson = entity.configJson
        )
    }
    
    fun toEntity(domain: ServerConfig): ServerEntity {
        return ServerEntity(
            id = domain.id,
            name = domain.name,
            protocol = domain.protocol,
            address = domain.address,
            port = domain.port,
            uuid = domain.uuid,
            password = domain.password,
            security = domain.security,
            encryption = domain.encryption,
            flow = domain.flow,
            network = domain.network,
            host = domain.host,
            path = domain.path,
            sni = domain.sni,
            alpn = domain.alpn?.let { json.encodeToString(it) },
            tls = domain.tls,
            reality = domain.reality,
            publicKey = domain.publicKey,
            shortId = domain.shortId,
            spiderX = domain.spiderX,
            headerType = domain.headerType,
            quicSecurity = domain.quicSecurity,
            quicKey = domain.quicKey,
            grpcServiceName = domain.grpcServiceName,
            grpcMode = domain.grpcMode,
            allowInsecure = domain.allowInsecure,
            fingerprint = domain.fingerprint,
            remark = domain.remark,
            sourceUrl = domain.sourceUrl,
            addedTime = domain.addedTime,
            lastTestTime = domain.lastTestTime,
            delay = domain.delay,
            isActive = domain.isActive,
            configJson = domain.configJson
        )
    }
}
