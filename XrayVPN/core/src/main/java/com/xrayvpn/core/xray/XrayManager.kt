package com.xrayvpn.core.xray

import android.content.Context
import com.xrayvpn.common.constants.AppConfig
import com.xrayvpn.domain.model.ServerConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.*
import libv2ray.Libv2ray
import java.io.File

/**
 * Xray core manager for handling VPN connections
 */
class XrayManager(private val context: Context) {
    
    private val json = Json { 
        ignoreUnknownKeys = true 
        encodeDefaults = true
    }
    
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState
    
    private var coreInstance: Libv2ray? = null
    private var currentConfigPath: String? = null
    
    /**
     * Generate Xray configuration from ServerConfig
     */
    fun generateConfig(server: ServerConfig): String {
        val config = when (server.protocol) {
            AppConfig.VLESS -> buildVlessConfig(server)
            AppConfig.TROJAN -> buildTrojanConfig(server)
            AppConfig.SHADOWSOCKS -> buildShadowsocksConfig(server)
            AppConfig.HYSTERIA2, AppConfig.HYS -> buildHysteria2Config(server)
            AppConfig.VMESS -> buildVmessConfig(server)
            AppConfig.TUIC -> buildTuicConfig(server)
            else -> buildCustomConfig(server)
        }
        
        return json.encodeToString(config)
    }
    
    private fun buildVlessConfig(server: ServerConfig): XrayConfig {
        val outbound = Outbound(
            protocol = "vless",
            settings = VlessSettings(
                vnext = listOf(
                    Vnext(
                        address = server.address,
                        port = server.port,
                        users = listOf(
                            VlessUser(
                                id = server.uuid ?: "",
                                encryption = server.encryption ?: "none",
                                flow = server.flow
                            )
                        )
                    )
                )
            ),
            streamSettings = buildStreamSettings(server)
        )
        
        return XrayConfig(
            log = LogConfig(loglevel = "warning"),
            inbounds = buildInbounds(),
            outbounds = listOf(outbound, buildDirectOutbound(), buildBlockOutbound()),
            routing = buildRouting()
        )
    }
    
    private fun buildTrojanConfig(server: ServerConfig): XrayConfig {
        val outbound = Outbound(
            protocol = "trojan",
            settings = TrojanSettings(
                servers = listOf(
                    TrojanServer(
                        address = server.address,
                        port = server.port,
                        password = server.password ?: ""
                    )
                )
            ),
            streamSettings = buildStreamSettings(server)
        )
        
        return XrayConfig(
            log = LogConfig(loglevel = "warning"),
            inbounds = buildInbounds(),
            outbounds = listOf(outbound, buildDirectOutbound(), buildBlockOutbound()),
            routing = buildRouting()
        )
    }
    
    private fun buildShadowsocksConfig(server: ServerConfig): XrayConfig {
        val outbound = Outbound(
            protocol = "shadowsocks",
            settings = ShadowsocksSettings(
                servers = listOf(
                    ShadowsocksServer(
                        address = server.address,
                        port = server.port,
                        method = server.encryption ?: "aes-256-gcm",
                        password = server.password ?: ""
                    )
                )
            ),
            streamSettings = server.network?.let { buildStreamSettings(server) }
        )
        
        return XrayConfig(
            log = LogConfig(loglevel = "warning"),
            inbounds = buildInbounds(),
            outbounds = listOf(outbound, buildDirectOutbound(), buildBlockOutbound()),
            routing = buildRouting()
        )
    }
    
    private fun buildHysteria2Config(server: ServerConfig): XrayConfig {
        // Hysteria2 requires custom configuration
        val hy2Config = buildJsonObject {
            put("log", buildJsonObject { put("loglevel", "warning") })
            put("inbounds", buildInboundsJson())
            put("outbounds", buildJsonArray {
                add(buildJsonObject {
                    put("protocol", "hysteria2")
                    put("settings", buildJsonObject {
                        put("servers", buildJsonArray {
                            add(buildJsonObject {
                                put("address", server.address)
                                put("port", server.port)
                                put("password", server.password ?: "")
                            })
                        })
                    })
                    put("streamSettings", buildJsonObject {
                        put("network", "tcp")
                        put("security", "tls")
                        put("tlsSettings", buildJsonObject {
                            put("serverName", server.sni ?: server.address)
                            put("allowInsecure", server.allowInsecure)
                        }.also { tlsObj ->
                            server.alpn?.let { alpn ->
                                tlsObj.put("alpn", buildJsonArray { alpn.forEach { add(it) } })
                            }
                        })
                    })
                })
                add(buildDirectOutboundJson())
                add(buildBlockOutboundJson())
            })
            put("routing", buildRoutingJson())
        }
        
        return XrayConfig(
            rawConfig = hy2Config.toString()
        )
    }
    
    private fun buildVmessConfig(server: ServerConfig): XrayConfig {
        val outbound = Outbound(
            protocol = "vmess",
            settings = VmessSettings(
                vnext = listOf(
                    VmessNext(
                        address = server.address,
                        port = server.port,
                        users = listOf(
                            VmessUser(
                                id = server.uuid ?: "",
                                security = server.security ?: "auto"
                            )
                        )
                    )
                )
            ),
            streamSettings = buildStreamSettings(server)
        )
        
        return XrayConfig(
            log = LogConfig(loglevel = "warning"),
            inbounds = buildInbounds(),
            outbounds = listOf(outbound, buildDirectOutbound(), buildBlockOutbound()),
            routing = buildRouting()
        )
    }
    
    private fun buildTuicConfig(server: ServerConfig): XrayConfig {
        val tuicConfig = buildJsonObject {
            put("log", buildJsonObject { put("loglevel", "warning") })
            put("inbounds", buildInboundsJson())
            put("outbounds", buildJsonArray {
                add(buildJsonObject {
                    put("protocol", "tuic")
                    put("settings", buildJsonObject {
                        put("servers", buildJsonArray {
                            add(buildJsonObject {
                                put("address", server.address)
                                put("port", server.port)
                                put("uuid", server.uuid ?: "")
                                put("password", server.password ?: "")
                            })
                        })
                    })
                    put("streamSettings", buildJsonObject {
                        put("network", "udp")
                        put("security", "tls")
                        put("tlsSettings", buildJsonObject {
                            put("serverName", server.sni ?: server.address)
                            put("allowInsecure", server.allowInsecure)
                        })
                    })
                })
                add(buildDirectOutboundJson())
                add(buildBlockOutboundJson())
            })
            put("routing", buildRoutingJson())
        }
        
        return XrayConfig(rawConfig = tuicConfig.toString())
    }
    
    private fun buildCustomConfig(server: ServerConfig): XrayConfig {
        // Use provided config JSON or generate minimal config
        return server.configJson?.let { jsonStr ->
            try {
                XrayConfig(rawConfig = jsonStr)
            } catch (e: Exception) {
                buildMinimalConfig(server)
            }
        } ?: buildMinimalConfig(server)
    }
    
    private fun buildMinimalConfig(server: ServerConfig): XrayConfig {
        return XrayConfig(
            log = LogConfig(loglevel = "warning"),
            inbounds = buildInbounds(),
            outbounds = listOf(buildDirectOutbound(), buildBlockOutbound()),
            routing = buildRouting()
        )
    }
    
    private fun buildInbounds(): List<Inbound> {
        return listOf(
            Inbound(
                tag = AppConfig.TAG_PROXY,
                port = AppConfig.PORT_SOCKS,
                listen = AppConfig.LOOPBACK,
                protocol = "socks",
                settings = SocksSettings(
                    auth = "noauth",
                    udp = true
                )
            ),
            Inbound(
                tag = "http",
                port = AppConfig.PORT_HTTP,
                listen = AppConfig.LOOPBACK,
                protocol = "http",
                settings = null
            )
        )
    }
    
    private fun buildInboundsJson(): JsonElement {
        return buildJsonArray {
            add(buildJsonObject {
                put("tag", AppConfig.TAG_PROXY)
                put("port", AppConfig.PORT_SOCKS)
                put("listen", AppConfig.LOOPBACK)
                put("protocol", "socks")
                put("settings", buildJsonObject {
                    put("auth", "noauth")
                    put("udp", true)
                })
            })
            add(buildJsonObject {
                put("tag", "http")
                put("port", AppConfig.PORT_HTTP)
                put("listen", AppConfig.LOOPBACK)
                put("protocol", "http")
            })
        }
    }
    
    private fun buildStreamSettings(server: ServerConfig): StreamSettings? {
        val network = server.network ?: AppConfig.TCP
        
        val tlsSettings = when (server.tls) {
            AppConfig.TLS -> buildTlsSettings(server)
            AppConfig.REALITY -> buildRealitySettings(server)
            else -> null
        }
        
        val transportSettings = when (network) {
            AppConfig.WS -> buildWsTransport(server)
            AppConfig.GRPC -> buildGrpcTransport(server)
            AppConfig.HTTP_UPGRADE -> buildHttpUpgradeTransport(server)
            AppConfig.TCP -> if (server.headerType == "http") buildHttpHeaders(server) else null
            else -> null
        }
        
        return StreamSettings(
            network = network,
            security = server.tls?.takeIf { it != AppConfig.NONE },
            tlsSettings = tlsSettings,
            realitySettings = if (server.reality) tlsSettings as? RealitySettings else null,
            wsSettings = transportSettings as? WsSettings,
            grpcSettings = transportSettings as? GrpcSettings,
            httpupgradeSettings = transportSettings as? HttpUpgradeSettings,
            tcpSettings = transportSettings as? TcpSettings
        )
    }
    
    private fun buildTlsSettings(server: ServerConfig): TlsSettings {
        return TlsSettings(
            serverName = server.sni ?: server.address,
            allowInsecure = server.allowInsecure,
            alpn = server.alpn,
            fingerprint = server.fingerprint ?: "chrome"
        )
    }
    
    private fun buildRealitySettings(server: ServerConfig): RealitySettings {
        return RealitySettings(
            serverName = server.sni ?: server.address,
            publicKey = server.publicKey,
            shortId = server.shortId,
            spiderX = server.spiderX,
            fingerprint = server.fingerprint ?: "chrome"
        )
    }
    
    private fun buildWsTransport(server: ServerConfig): WsSettings {
        return WsSettings(
            path = server.path ?: "/",
            headers = server.host?.let { mapOf("Host" to it) }
        )
    }
    
    private fun buildGrpcTransport(server: ServerConfig): GrpcSettings {
        return GrpcSettings(
            serviceName = server.grpcServiceName ?: "",
            multiMode = server.grpcMode == "multi"
        )
    }
    
    private fun buildHttpUpgradeTransport(server: ServerConfig): HttpUpgradeSettings {
        return HttpUpgradeSettings(
            path = server.path ?: "/",
            host = server.host ?: server.address
        )
    }
    
    private fun buildHttpHeaders(server: ServerConfig): TcpSettings {
        return TcpSettings(
            header = TcpHeader(
                type = server.headerType ?: "http",
                request = TcpRequest(
                    path = listOf(server.path ?: "/"),
                    headers = mapOf("Host" to (server.host ?: server.address))
                )
            )
        )
    }
    
    private fun buildDirectOutbound(): Outbound {
        return Outbound(
            protocol = "freedom",
            tag = AppConfig.TAG_DIRECT,
            settings = FreedomSettings()
        )
    }
    
    private fun buildDirectOutboundJson(): JsonElement {
        return buildJsonObject {
            put("protocol", "freedom")
            put("tag", AppConfig.TAG_DIRECT)
        }
    }
    
    private fun buildBlockOutbound(): Outbound {
        return Outbound(
            protocol = "blackhole",
            tag = AppConfig.TAG_BLOCK,
            settings = BlackholeSettings()
        )
    }
    
    private fun buildBlockOutboundJson(): JsonElement {
        return buildJsonObject {
            put("protocol", "blackhole")
            put("tag", AppConfig.TAG_BLOCK)
        }
    }
    
    private fun buildRouting(): RoutingConfig {
        return RoutingConfig(
            domainStrategy = "IPIfNonMatch",
            rules = listOf(
                RoutingRule(
                    type = "field",
                    ip = listOf(AppConfig.GEOIP_PRIVATE),
                    outboundTag = AppConfig.TAG_DIRECT
                ),
                RoutingRule(
                    type = "field",
                    domain = listOf(AppConfig.GEOSITE_PRIVATE),
                    outboundTag = AppConfig.TAG_DIRECT
                )
            )
        )
    }
    
    private fun buildRoutingJson(): JsonElement {
        return buildJsonObject {
            put("domainStrategy", "IPIfNonMatch")
            put("rules", buildJsonArray {
                add(buildJsonObject {
                    put("type", "field")
                    put("ip", buildJsonArray { add(AppConfig.GEOIP_PRIVATE) })
                    put("outboundTag", AppConfig.TAG_DIRECT)
                })
                add(buildJsonObject {
                    put("type", "field")
                    put("domain", buildJsonArray { add(AppConfig.GEOSITE_PRIVATE) })
                    put("outboundTag", AppConfig.TAG_DIRECT)
                })
            })
        }
    }
    
    /**
     * Start VPN connection with the given server configuration
     */
    suspend fun startConnection(server: ServerConfig) {
        try {
            _connectionState.value = ConnectionState.Connecting
            
            val configJson = generateConfig(server)
            
            // Write config to file
            val configFile = File(context.filesDir, "config.json")
            configFile.writeText(configJson)
            currentConfigPath = configFile.absolutePath
            
            // Initialize and start core
            coreInstance = Libv2ray().apply {
                initCore(configFile.absolutePath)
                start()
            }
            
            _connectionState.value = ConnectionState.Connected(server)
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.Error(e.message ?: "Unknown error")
        }
    }
    
    /**
     * Stop VPN connection
     */
    suspend fun stopConnection() {
        try {
            coreInstance?.stop()
            coreInstance = null
            currentConfigPath = null
            _connectionState.value = ConnectionState.Disconnected
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.Error(e.message ?: "Error stopping connection")
        }
    }
    
    /**
     * Get current connection state
     */
    fun getConnectionState(): ConnectionState {
        return _connectionState.value
    }
}

/**
 * Connection state sealed class
 */
sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    data class Connected(val server: ServerConfig) : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

// Xray configuration data classes
@kotlinx.serialization.Serializable
data class XrayConfig(
    val log: LogConfig? = null,
    val inbounds: List<Inbound>? = null,
    val outbounds: List<Outbound>? = null,
    val routing: RoutingConfig? = null,
    val dns: DnsConfig? = null,
    val rawConfig: String? = null
)

@kotlinx.serialization.Serializable
data class LogConfig(val loglevel: String = "warning")

@kotlinx.serialization.Serializable
data class Inbound(
    val tag: String,
    val port: Int,
    val listen: String,
    val protocol: String,
    val settings: Any? = null
)

@kotlinx.serialization.Serializable
data class SocksSettings(val auth: String, val udp: Boolean)

@kotlinx.serialization.Serializable
data class Outbound(
    val protocol: String,
    val tag: String? = null,
    val settings: Any? = null,
    val streamSettings: StreamSettings? = null
)

@kotlinx.serialization.Serializable
data class VlessSettings(val vnext: List<Vnext>)

@kotlinx.serialization.Serializable
data class Vnext(
    val address: String,
    val port: Int,
    val users: List<VlessUser>
)

@kotlinx.serialization.Serializable
data class VlessUser(
    val id: String,
    val encryption: String,
    val flow: String? = null
)

@kotlinx.serialization.Serializable
data class TrojanSettings(val servers: List<TrojanServer>)

@kotlinx.serialization.Serializable
data class TrojanServer(
    val address: String,
    val port: Int,
    val password: String
)

@kotlinx.serialization.Serializable
data class ShadowsocksSettings(val servers: List<ShadowsocksServer>)

@kotlinx.serialization.Serializable
data class ShadowsocksServer(
    val address: String,
    val port: Int,
    val method: String,
    val password: String
)

@kotlinx.serialization.Serializable
data class VmessSettings(val vnext: List<VmessNext>)

@kotlinx.serialization.Serializable
data class VmessNext(
    val address: String,
    val port: Int,
    val users: List<VmessUser>
)

@kotlinx.serialization.Serializable
data class VmessUser(
    val id: String,
    val security: String
)

@kotlinx.serialization.Serializable
data class StreamSettings(
    val network: String,
    val security: String? = null,
    val tlsSettings: TlsSettings? = null,
    val realitySettings: RealitySettings? = null,
    val wsSettings: WsSettings? = null,
    val grpcSettings: GrpcSettings? = null,
    val httpupgradeSettings: HttpUpgradeSettings? = null,
    val tcpSettings: TcpSettings? = null
)

@kotlinx.serialization.Serializable
data class TlsSettings(
    val serverName: String,
    val allowInsecure: Boolean,
    val alpn: List<String>? = null,
    val fingerprint: String? = null
)

@kotlinx.serialization.Serializable
data class RealitySettings(
    val serverName: String,
    val publicKey: String?,
    val shortId: String?,
    val spiderX: String?,
    val fingerprint: String?
)

@kotlinx.serialization.Serializable
data class WsSettings(
    val path: String,
    val headers: Map<String, String>? = null
)

@kotlinx.serialization.Serializable
data class GrpcSettings(
    val serviceName: String,
    val multiMode: Boolean = false
)

@kotlinx.serialization.Serializable
data class HttpUpgradeSettings(
    val path: String,
    val host: String
)

@kotlinx.serialization.Serializable
data class TcpSettings(
    val header: TcpHeader
)

@kotlinx.serialization.Serializable
data class TcpHeader(
    val type: String,
    val request: TcpRequest? = null
)

@kotlinx.serialization.Serializable
data class TcpRequest(
    val path: List<String>,
    val headers: Map<String, String>
)

@kotlinx.serialization.Serializable
data class FreedomSettings(val domainStrategy: String? = null)

@kotlinx.serialization.Serializable
data class BlackholeSettings()

@kotlinx.serialization.Serializable
data class RoutingConfig(
    val domainStrategy: String,
    val rules: List<RoutingRule>
)

@kotlinx.serialization.Serializable
data class RoutingRule(
    val type: String,
    val ip: List<String>? = null,
    val domain: List<String>? = null,
    val outboundTag: String
)

@kotlinx.serialization.Serializable
data class DnsConfig(
    val hosts: Map<String, String>? = null,
    val servers: List<String>
)
