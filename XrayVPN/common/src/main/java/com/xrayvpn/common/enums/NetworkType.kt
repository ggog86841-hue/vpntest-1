package com.xrayvpn.common.enums

/**
 * Network transport types
 */
enum class NetworkType(val type: String) {
    TCP("tcp"),
    KCP("kcp"),
    WS("ws"),
    HTTP_UPGRADE("httpupgrade"),
    XHTTP("xhttp"),
    HTTP("http"),
    H2("h2"),
    GRPC("grpc"),
    QUIC("quic"),
    HYSTERIA("hysteria");

    companion object {
        fun fromString(value: String?): NetworkType = 
            entries.firstOrNull { it.type == value } ?: TCP
    }
}
