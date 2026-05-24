package com.xrayvpn.data.parser

import android.net.Uri
import com.xrayvpn.common.constants.AppConfig
import com.xrayvpn.common.enums.EConfigType
import com.xrayvpn.common.utils.decodeBase64
import com.xrayvpn.domain.model.ServerConfig
import kotlinx.serialization.json.Json
import java.net.URLDecoder
import java.util.UUID

/**
 * Parser for various proxy configuration URLs
 * Supports VLESS, Trojan, Shadowsocks, Hysteria2, and more
 */
object ConfigUrlParser {
    private val json = Json { ignoreUnknownKeys = true }
    
    /**
     * Parse a configuration URL into a ServerConfig object
     */
    fun parse(url: String, sourceUrl: String? = null): ServerConfig? {
        return try {
            val trimmedUrl = url.trim()
            when {
                trimmedUrl.startsWith("vless://") -> parseVless(trimmedUrl, sourceUrl)
                trimmedUrl.startsWith("trojan://") -> parseTrojan(trimmedUrl, sourceUrl)
                trimmedUrl.startsWith("ss://") -> parseShadowsocks(trimmedUrl, sourceUrl)
                trimmedUrl.startsWith("hysteria2://") || trimmedUrl.startsWith("hy2://") -> 
                    parseHysteria2(trimmedUrl, sourceUrl)
                trimmedUrl.startsWith("vmess://") -> parseVmess(trimmedUrl, sourceUrl)
                trimmedUrl.startsWith("tuic://") -> parseTuic(trimmedUrl, sourceUrl)
                trimmedUrl.startsWith("hysteria://") -> parseHysteria(trimmedUrl, sourceUrl)
                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Parse VLESS URL format:
     * vless://uuid@host:port?encryption=none&security=tls|reality&sni=xxx&pbk=xxx&sid=xxx&type=grpc&serviceName=xxx#remark
     */
    private fun parseVless(url: String, sourceUrl: String?): ServerConfig? {
        val uri = Uri.parse(url)
        val uuid = uri.userInfo ?: return null
        val host = uri.host ?: return null
        val port = uri.port.takeIf { it != -1 } ?: return null
        
        val queryParams = uri.queryParameterNames.associateWith { name ->
            uri.getQueryParameter(name) ?: ""
        }
        
        val remark = uri.fragment?.let { URLDecoder.decode(it, "UTF-8") }
        val security = queryParams["security"] ?: "none"
        val tls = if (security == "none") AppConfig.NONE else security
        val reality = security == AppConfig.REALITY
        
        return ServerConfig(
            id = generateId(),
            name = remark ?: "VLESS-$host-$port",
            protocol = EConfigType.VLESS.protocolScheme,
            address = host,
            port = port,
            uuid = uuid,
            security = security,
            encryption = queryParams["encryption"] ?: "none",
            flow = queryParams["flow"],
            network = queryParams["type"] ?: AppConfig.TCP,
            host = queryParams["host"],
            path = queryParams["path"],
            sni = queryParams["sni"],
            alpn = queryParams["alpn"]?.split(","),
            tls = tls,
            reality = reality,
            publicKey = queryParams["pbk"] ?: queryParams["publickey"],
            shortId = queryParams["sid"] ?: queryParams["shortid"],
            spiderX = queryParams["spx"],
            headerType = queryParams["headerType"],
            grpcServiceName = queryParams["serviceName"],
            grpcMode = queryParams["mode"],
            allowInsecure = queryParams["allowInsecure"]?.toBooleanOrNull() ?: false,
            fingerprint = queryParams["fp"],
            remark = remark,
            sourceUrl = sourceUrl
        )
    }
    
    /**
     * Parse Trojan URL format:
     * trojan://password@host:port?sni=xxx&type=ws&host=xxx&path=xxx#remark
     */
    private fun parseTrojan(url: String, sourceUrl: String?): ServerConfig? {
        val uri = Uri.parse(url)
        val password = uri.userInfo ?: return null
        val host = uri.host ?: return null
        val port = uri.port.takeIf { it != -1 } ?: return null
        
        val queryParams = uri.queryParameterNames.associateWith { name ->
            uri.getQueryParameter(name) ?: ""
        }
        
        val remark = uri.fragment?.let { URLDecoder.decode(it, "UTF-8") }
        val security = queryParams["security"] ?: AppConfig.TLS
        
        return ServerConfig(
            id = generateId(),
            name = remark ?: "Trojan-$host-$port",
            protocol = EConfigType.TROJAN.protocolScheme,
            address = host,
            port = port,
            password = password,
            security = security,
            network = queryParams["type"] ?: AppConfig.TCP,
            host = queryParams["host"] ?: queryParams["peer"],
            path = queryParams["path"],
            sni = queryParams["sni"] ?: queryParams["peer"],
            alpn = queryParams["alpn"]?.split(","),
            tls = security,
            allowInsecure = queryParams["allowInsecure"]?.toBooleanOrNull() 
                ?: queryParams["skip-cert-verify"]?.toBooleanOrNull() ?: false,
            fingerprint = queryParams["fp"],
            remark = remark,
            sourceUrl = sourceUrl
        )
    }
    
    /**
     * Parse Shadowsocks URL format:
     * ss://base64(method:password)@host:port#remark
     * or ss://base64(method:password@host:port#remark)
     */
    private fun parseShadowsocks(url: String, sourceUrl: String?): ServerConfig? {
        val uri = Uri.parse(url)
        var userInfo = uri.userInfo ?: return null
        val host = uri.host ?: return null
        val port = uri.port.takeIf { it != -1 } ?: return null
        val remark = uri.fragment?.let { URLDecoder.decode(it, "UTF-8") }
        
        // Try to decode base64 userInfo
        var method: String? = null
        var password: String? = null
        
        if (userInfo.contains(":")) {
            // Format: method:password
            val parts = userInfo.split(":", limit = 2)
            method = parts.firstOrNull()
            password = parts.lastOrNull()
            
            // Check if method is base64 encoded
            if (method != null && !isValidCipher(method)) {
                try {
                    val decoded = decodeBase64(userInfo)
                    val decodedParts = decoded.split(":", limit = 2)
                    method = decodedParts.firstOrNull()
                    password = decodedParts.lastOrNull()
                } catch (e: Exception) {
                    // Keep original values
                }
            }
        } else {
            // Try full base64 decode
            try {
                val decoded = decodeBase64(userInfo)
                val decodedParts = decoded.split("@", limit = 2)
                if (decodedParts.size == 2) {
                    val cipherPass = decodedParts.first().split(":", limit = 2)
                    method = cipherPass.firstOrNull()
                    password = cipherPass.lastOrNull()
                    // Override host and port from the decoded part
                    val hostPort = decodedParts.last().split(":")
                    if (hostPort.size >= 2) {
                        // Already set from URI
                    }
                }
            } catch (e: Exception) {
                // Try alternative parsing
            }
        }
        
        val queryParams = uri.queryParameterNames.associateWith { name ->
            uri.getQueryParameter(name) ?: ""
        }
        
        return ServerConfig(
            id = generateId(),
            name = remark ?: "Shadowsocks-$host-$port",
            protocol = EConfigType.SHADOWSOCKS.protocolScheme,
            address = host,
            port = port,
            password = password,
            encryption = method,
            security = queryParams["encryption"],
            network = queryParams["type"] ?: queryParams["network"],
            host = queryParams["host"],
            path = queryParams["path"],
            sni = queryParams["sni"],
            remark = remark,
            sourceUrl = sourceUrl
        )
    }
    
    /**
     * Parse Hysteria2 URL format:
     * hysteria2://password@host:port?sni=xxx&insecure=1&obfs=salamander&obfs-password=xxx#remark
     */
    private fun parseHysteria2(url: String, sourceUrl: String?): ServerConfig? {
        val uri = Uri.parse(url)
        val password = uri.userInfo ?: return null
        val host = uri.host ?: return null
        val port = uri.port.takeIf { it != -1 } ?: return null
        
        val queryParams = uri.queryParameterNames.associateWith { name ->
            uri.getQueryParameter(name) ?: ""
        }
        
        val remark = uri.fragment?.let { URLDecoder.decode(it, "UTF-8") }
        
        return ServerConfig(
            id = generateId(),
            name = remark ?: "Hysteria2-$host-$port",
            protocol = EConfigType.HYSTERIA2.protocolScheme,
            address = host,
            port = port,
            password = password,
            sni = queryParams["sni"],
            alpn = queryParams["alpn"]?.split(","),
            tls = AppConfig.TLS,
            allowInsecure = queryParams["insecure"]?.toIntOrNull() == 1 
                || queryParams["allowInsecure"]?.toBooleanOrNull() == true,
            fingerprint = queryParams["fp"],
            network = queryParams["obfs"]?.let { "hysteria2-${it}" },
            remark = remark,
            sourceUrl = sourceUrl
        )
    }
    
    /**
     * Parse Hysteria (v1) URL format
     */
    private fun parseHysteria(url: String, sourceUrl: String?): ServerConfig? {
        val uri = Uri.parse(url)
        val host = uri.host ?: return null
        val port = uri.port.takeIf { it != -1 } ?: return null
        
        val queryParams = uri.queryParameterNames.associateWith { name ->
            uri.getQueryParameter(name) ?: ""
        }
        
        val remark = uri.fragment?.let { URLDecoder.decode(it, "UTF-8") }
        val protocol = queryParams["protocol"] ?: "udp"
        
        return ServerConfig(
            id = generateId(),
            name = remark ?: "Hysteria-$host-$port",
            protocol = EConfigType.HYSTERIA.protocolScheme,
            address = host,
            port = port,
            password = queryParams["auth"] ?: queryParams["password"],
            sni = queryParams["sni"],
            alpn = queryParams["alpn"]?.split(","),
            tls = AppConfig.TLS,
            allowInsecure = queryParams["insecure"]?.toBooleanOrNull() == true,
            fingerprint = queryParams["fp"],
            network = protocol,
            remark = remark,
            sourceUrl = sourceUrl
        )
    }
    
    /**
     * Parse VMess URL format (base64 encoded JSON)
     */
    private fun parseVmess(url: String, sourceUrl: String?): ServerConfig? {
        val base64Part = url.removePrefix("vmess://")
        val jsonStr = try {
            decodeBase64(base64Part)
        } catch (e: Exception) {
            return null
        }
        
        return try {
            val jsonElement = json.parseToJsonElement(jsonStr)
            val jsonObj = jsonElement.asJsonObject
            
            val addrs = jsonObj["add"]?.asString ?: return null
            val port = jsonObj["port"]?.asString?.toIntOrNull() ?: return null
            val id = jsonObj["id"]?.asString ?: return null
            val remark = jsonObj["ps"]?.asString
            
            ServerConfig(
                id = generateId(),
                name = remark ?: "VMess-$addrs-$port",
                protocol = EConfigType.VMESS.protocolScheme,
                address = addrs,
                port = port,
                uuid = id,
                security = jsonObj["scy"]?.asString ?: "auto",
                network = jsonObj["net"]?.asString ?: AppConfig.TCP,
                host = jsonObj["host"]?.asString,
                path = jsonObj["path"]?.asString,
                sni = jsonObj["sni"]?.asString,
                tls = jsonObj["tls"]?.asString ?: AppConfig.NONE,
                allowInsecure = jsonObj["verify_cert"]?.toString()?.toBooleanOrNull() 
                    ?: jsonObj["allowInsecure"]?.toString()?.toBooleanOrNull() ?: false,
                fingerprint = jsonObj["fp"]?.asString,
                remark = remark,
                sourceUrl = sourceUrl,
                configJson = jsonStr
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Parse TUIC URL format
     */
    private fun parseTuic(url: String, sourceUrl: String?): ServerConfig? {
        val uri = Uri.parse(url)
        val userInfo = uri.userInfo ?: return null
        val host = uri.host ?: return null
        val port = uri.port.takeIf { it != -1 } ?: return null
        
        val queryParams = uri.queryParameterNames.associateWith { name ->
            uri.getQueryParameter(name) ?: ""
        }
        
        val remark = uri.fragment?.let { URLDecoder.decode(it, "UTF-8") }
        val parts = userInfo.split(":", limit = 2)
        val uuid = parts.firstOrNull()
        val password = parts.lastOrNull()
        
        return ServerConfig(
            id = generateId(),
            name = remark ?: "TUIC-$host-$port",
            protocol = EConfigType.TUIC.protocolScheme,
            address = host,
            port = port,
            uuid = uuid,
            password = password,
            sni = queryParams["sni"],
            alpn = queryParams["alpn"]?.split(","),
            tls = AppConfig.TLS,
            allowInsecure = queryParams["allow_insecure"]?.toBooleanOrNull() == true,
            fingerprint = queryParams["fp"],
            remark = remark,
            sourceUrl = sourceUrl
        )
    }
    
    private fun generateId(): String {
        return UUID.randomUUID().toString()
    }
    
    private fun String.toBooleanOrNull(): Boolean? {
        return when (this.lowercase()) {
            "true", "1", "yes" -> true
            "false", "0", "no" -> false
            else -> null
        }
    }
    
    private fun isValidCipher(cipher: String): Boolean {
        val validCiphers = listOf(
            "aes-256-gcm", "aes-128-gcm", "chacha20-poly1305", 
            "chacha20-ietf-poly1305", "xchacha20-poly1305",
            "aes-256-cfb", "aes-128-cfb", "des-cfb", "rc4-md5",
            "camellia-128-cfb", "camellia-192-cfb", "camellia-256-cfb",
            "bf-cfb", "cast5-cfb", "rc4-md5-6", "salsa20", "chacha20"
        )
        return cipher.lowercase() in validCiphers
    }
}
