package com.pdguru.transportmapper.networking

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class HttpClientProvider {
    fun getOkHttpClient(): OkHttpClient {

        val builder = OkHttpClient.Builder()
            .writeTimeout(NETWORK_TIMEOUT, TimeUnit.SECONDS)
            .connectTimeout(NETWORK_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(NETWORK_TIMEOUT, TimeUnit.SECONDS)

        builder.addNetworkInterceptor(getLoggingInterceptor())

        return builder.build()
    }

    private fun getLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor()
            .apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
    }
}

const val NETWORK_TIMEOUT = 20L
