package com.xrayvpn.app

import android.app.Application
import com.xrayvpn.data.database.AppDatabase
import com.xrayvpn.data.remote.ConfigApiServiceImpl
import com.xrayvpn.data.remote.HttpClientFactory
import com.xrayvpn.data.repository.ServerRepositoryImpl
import com.xrayvpn.domain.repository.ServerRepository
import com.xrayvpn.domain.usecase.*
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module

/**
 * Application class for XrayVPN
 */
class XrayVpnApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidLogger()
            androidContext(this@XrayVpnApplication)
            modules(appModule)
        }
    }
}

val appModule = module {
    // Database
    single { AppDatabase.getDatabase(androidContext()) }
    
    // HTTP Client
    single { HttpClientFactory.createOkHttpClient() }
    single { ConfigApiServiceImpl(get()) }
    
    // Repository
    single<ServerRepository> { ServerRepositoryImpl(get(), get()) }
    
    // Use Cases
    factory { GetAllServersUseCase(get()) }
    factory { GetServersGroupedBySourceUseCase(get()) }
    factory { AddServerUseCase(get()) }
    factory { UpdateServerUseCase(get()) }
    factory { DeleteServerUseCase(get()) }
    factory { SetActiveServerUseCase(get()) }
    factory { GetActiveServerUseCase(get()) }
    factory { TestServerUseCase(get()) }
    factory { TestServersUseCase(get()) }
    factory { ImportFromUrlUseCase(get()) }
    factory { ParseConfigUrlUseCase(get()) }
}
