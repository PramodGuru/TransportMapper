package com.pdguru.transportmapper.di

import com.pdguru.transportmapper.BASE_URL
import com.pdguru.transportmapper.MapActivityViewModel
import com.pdguru.transportmapper.networking.ConnectivityHelper
import com.pdguru.transportmapper.networking.HttpClientProvider
import com.pdguru.transportmapper.networking.MoshiFactory
import com.pdguru.transportmapper.networking.RetrofitFactory
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

object KoinGraph {

    private val clientProvider = HttpClientProvider().getOkHttpClient()

    val mainModule = module {
        single { MoshiFactory.createMoshi() }
        single { RetrofitFactory.createRetrofit(get(), clientProvider, BASE_URL) }
        single { ConnectivityHelper(androidContext()) }

        viewModel { MapActivityViewModel(get(), get()) }
    }


}
