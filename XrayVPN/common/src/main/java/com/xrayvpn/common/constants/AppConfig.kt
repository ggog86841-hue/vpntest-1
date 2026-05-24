package com.xrayvpn.common.constants

/**
 * Application-wide constants
 */
object AppConfig {
    const val TAG = "XrayVPN"
    
    // Protocol schemes
    const val VMESS = "vmess://"
    const val VLESS = "vless://"
    const val TROJAN = "trojan://"
    const val SHADOWSOCKS = "ss://"
    const val SOCKS = "socks://"
    const val SOCKS4 = "socks4://"
    const val SOCKS5 = "socks5://"
    const val WIREGUARD = "wg://"
    const val HYSTERIA2 = "hysteria2://"
    const val HY2 = "hy2://"
    const val TUIC = "tuic://"
    const val HYS = "hys://"
    const val CUSTOM = ""
    
    // Security types
    const val TLS = "tls"
    const val REALITY = "reality"
    const val NONE = "none"
    
    // Network types
    const val TCP = "tcp"
    const val KCP = "kcp"
    const val WS = "ws"
    const val HTTP_UPGRADE = "httpupgrade"
    const val XHTTP = "xhttp"
    const val HTTP = "http"
    const val H2 = "h2"
    const val GRPC = "grpc"
    const val QUIC = "quic"
    
    // Tags for routing
    const val TAG_PROXY = "proxy"
    const val TAG_DIRECT = "direct"
    const val TAG_BLOCK = "block"
    const val TAG_BALANCER = "balancer"
    const val TAG_BALANCER_PRE = "balancer-"
    const val TAG_FRAGMENT = "fragment"
    
    // Ports
    const val PORT_SOCKS = 10808
    const val PORT_HTTP = 10809
    const val PORT_LOCAL_DNS = 10853
    const val PORT_REMOTE_DNS = 10854
    
    // Loopback
    const val LOOPBACK = "127.0.0.1"
    
    // MMKV IDs
    const val MMKV_ID_MAIN = "MAIN"
    const val MMKV_ID_SERVER_CONFIG = "SERVER_CONFIG"
    const val MMKV_ID_SERVER_RAW = "SERVER_RAW"
    const val MMKV_ID_SUBSCRIPTION = "SUBSCRIPTION"
    const val MMKV_ID_SETTINGS = "SETTINGS"
    
    // Preferences
    const val PREF_MODE = "pref_mode"
    const val PREF_VPN = "pref_vpn"
    const val PREF_PROXY_ONLY = "pref_proxy_only"
    const val PREF_PER_APP_PROXY = "pref_per_app_proxy"
    const val PREF_PER_APP_PROXY_UPDATE = "pref_per_app_proxy_update"
    const val PREF_BYPASS_APPS = "pref_bypass_apps"
    const val PREF_LOCAL_DNS_PORT = "pref_local_dns_port"
    const val PREF_VPN_DNS = "pref_vpn_dns"
    const val PREF_REMOTE_DNS = "pref_remote_dns"
    const val PREF_DOMESTIC_DNS = "pref_domestic_dns"
    const val PREF_MITM_DNS = "pref_mitm_dns"
    const val PREF_SPEED_TEST_ENABLED = "pref_speed_test_enabled"
    const val PREF_SPEED_TEST_INTERVAL = "pref_speed_test_interval"
    const val PREF_SPEED_TEST_TIMEOUT = "pref_speed_test_timeout"
    const val PREF_SPEED_TEST_URL = "pref_speed_test_url"
    const val PREF_ALLOW_INSECURE = "pref_allow_insecure"
    const val PREF_PREFER_IPV6 = "pref_prefer_ipv6"
    const val PREF_ROUTING_DOMAIN_STRATEGY = "pref_routing_domain_strategy"
    const val PREF_ROUTING_RULESET = "pref_routing_ruleset"
    const val PREF_MUX_ENABLED = "pref_mux_enabled"
    const val PREF_MUX_CONCURRENCY = "pref_mux_concurrency"
    const val PREF_MUX_XUDP_ENABLED = "pref_mux_xudp_enabled"
    const val PREF_MUX_XUDP_QUIC = "pref_mux_xudp_quic"
    const val PREF_FRAGMENT_ENABLED = "pref_fragment_enabled"
    const val PREF_FRAGMENT_PACKETS = "pref_fragment_packets"
    const val PREF_FRAGMENT_LENGTH = "pref_fragment_length"
    const val PREF_FRAGMENT_INTERVAL = "pref_fragment_interval"
    const val SUBSCRIPTION_MIN_INTERVAL_MINUTES = 15L
    const val SUBSCRIPTION_UPDATE_TASK_NAME = "subscription_update_task"
    
    // Routing domains
    const val GEOIP_PRIVATE = "geoip:private"
    const val GEOSITE_PRIVATE = "geosite:private"
    const val GEOSITE_CN = "geosite:cn"
    const val GEOIP_CN = "geoip:cn"
    
    // UI
    const val DARK_MODE_SETTING_SYSTEM = "system"
    const val DARK_MODE_SETTING_LIGHT = "light"
    const val DARK_MODE_SETTING_DARK = "dark"
}
