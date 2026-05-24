package com.xrayvpn.common.enums

/**
 * Configuration types supported by the application
 */
enum class EConfigType(val value: Int, val protocolScheme: String) {
    VMESS(1, "vmess://"),
    CUSTOM(2, ""),
    SHADOWSOCKS(3, "ss://"),
    SOCKS(4, "socks://"),
    VLESS(5, "vless://"),
    TROJAN(6, "trojan://"),
    WIREGUARD(7, "wg://"),
    HYSTERIA2(9, "hysteria2://"),
    HYSTERIA(900, "hysteria://"),
    HTTP(10, "http://"),
    TUIC(11, "tuic://"),
    POLICYGROUP(101, ""),
    PROXYCHAIN(102, "");

    companion object {
        fun fromInt(value: Int): EConfigType? = entries.firstOrNull { it.value == value }
        
        fun fromScheme(scheme: String): EConfigType? = entries.firstOrNull { 
            scheme.startsWith(it.protocolScheme) && it.protocolScheme.isNotEmpty()
        }
    }
}
