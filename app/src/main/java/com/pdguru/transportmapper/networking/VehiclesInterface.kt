package com.pdguru.transportmapper.networking

import com.pdguru.transportmapper.model.AvailableVehicles
import retrofit2.Response
import retrofit2.http.GET

interface VehiclesInterface {
    @GET("public/take_home_test_data.json")
    suspend fun getAllAvailableVehicles(): Response<AvailableVehicles>
}
