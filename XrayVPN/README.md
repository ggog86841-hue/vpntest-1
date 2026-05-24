# XrayVPN - Android VPN Client with Xray Core

Modern Android VPN application built with Kotlin, Jetpack Compose, and Clean MVVM Architecture.
Supports multiple proxy protocols via Xray core: VLESS, Trojan, Shadowsocks, Hysteria 2, VMess, WireGuard, etc.

## Features

- **Multiple Protocol Support**: VLESS (with Reality), Trojan, Shadowsocks, Hysteria 2, VMess, WireGuard, SOCKS
- **Subscription Management**: Import configs from multiple URLs with automatic updates
- **Parallel Speed Testing**: Test multiple servers simultaneously for optimal performance
- **Clean MVVM Architecture**: Modular, scalable, and maintainable codebase
- **Jetpack Compose UI**: Modern, responsive user interface
- **Multi-module Project**: Separated concerns for better organization

## Project Structure

```
XrayVPN/
├── build.gradle.kts                 # Root build configuration
├── settings.gradle.kts              # Project settings and module includes
├── gradle.properties                # Gradle properties
├── app/                             # Main application module
│   ├── build.gradle.kts
│   └── src/main/java/com/xrayvpn/app/
├── core/                            # Core functionality module
│   ├── build.gradle.kts
│   └── src/main/java/com/xrayvpn/core/
├── data/                            # Data layer module
│   ├── build.gradle.kts
│   └── src/main/java/com/xrayvpn/data/
├── domain/                          # Domain layer module
│   ├── build.gradle.kts
│   └── src/main/java/com/xrayvpn/domain/
├── ui/                              # UI layer module
│   ├── build.gradle.kts
│   └── src/main/java/com/xrayvpn/ui/
└── common/                          # Common utilities module
    ├── build.gradle.kts
    └── src/main/java/com/xrayvpn/common/
```

## Architecture

The project follows Clean Architecture principles with MVVM pattern:

### Layers

1. **Presentation Layer (ui module)**
   - Jetpack Compose UI components
   - ViewModels for state management
   - Navigation and screens

2. **Domain Layer (domain module)**
   - Use cases / Interactors
   - Business logic
   - Repository interfaces
   - Domain models

3. **Data Layer (data module)**
   - Repository implementations
   - Data sources (local and remote)
   - DTOs and mappers

4. **Core Layer (core module)**
   - Xray core integration
   - Service management
   - Configuration building

5. **Common Layer (common module)**
   - Shared utilities
   - Constants
   - Extensions
   - Base classes

## Protocols Supported

- **VLESS**: Full support including Reality, XTLS, various transports (TCP, WS, gRPC, HTTPUpgrade, XHTTP)
- **Trojan**: TLS, Reality support with all transport options
- **Shadowsocks**: SIP002 and legacy format, AEAD ciphers
- **Hysteria 2**: UDP, port hopping, obfuscation
- **VMess**: AES-GCM and CHACHA20-POLY1305 ciphers
- **WireGuard**: Modern VPN protocol
- **SOCKS**: SOCKS4, SOCKS5

## Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- JDK 17 or later
- Android SDK 26+
- Kotlin 1.9+

### Build Instructions

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Build and run

## Dependencies

- **AndroidX**: Core KTX, AppCompat, Lifecycle
- **Jetpack Compose**: Material3, Navigation, Foundation
- **Coroutines**: Async operations
- **Koin**: Dependency injection
- **MMKV**: Fast key-value storage
- **Gson/Kotlinx Serialization**: JSON parsing
- **WorkManager**: Background tasks
- **Xray Core**: Proxy engine

## License

MIT License

## Credits

Based on v2rayNG project architecture and Xray core capabilities.
