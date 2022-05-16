package com.pdguru.transportmapper

import android.app.Application
import com.pdguru.transportmapper.di.KoinGraph
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class TransportMapperApp: Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin()
        Timber.plant(Timber.DebugTree())
    }

    private fun initKoin() {
        startKoin {
            androidContext(this@TransportMapperApp)
            modules(KoinGraph.mainModule)
        }
    }
}
