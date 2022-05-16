package com.pdguru.transportmapper.networking

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dev.zacsweers.moshix.reflect.MetadataKotlinJsonAdapterFactory

class MoshiFactory {
    companion object {
        fun createMoshi(): Moshi {
            return Moshi.Builder()
                .add(MetadataKotlinJsonAdapterFactory())
                .add(KotlinJsonAdapterFactory())
                .build()
        }
    }
}
