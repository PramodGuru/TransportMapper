package com.pdguru.transportmapper

import com.pdguru.transportmapper.model.Attributes
import com.pdguru.transportmapper.model.AvailableVehicles
import com.pdguru.transportmapper.model.Vehicle
import com.pdguru.transportmapper.networking.VehiclesInterface
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test
import retrofit2.Response

class MapperTest {
    private val mockedWebService = mockk<VehiclesInterface>(relaxed = true)

    @Test
    fun `getAvailableVehicles gets list of vehicles`() {
        coEvery {
            mockedWebService.getAllAvailableVehicles()
        } returns Response.success(dummyList)

        val result = runBlocking { mockedWebService.getAllAvailableVehicles() }

        assert(result.body() == dummyList)
    }

}

// dummy values to test with

private val v1a1 = Attributes(
    52.475785,
    13.326359,
    false,
    20,
    "escooter",
    27
)

private val v2a2 = Attributes(
    52.517169,
    13.394245,
    true,
    20,
    "escooter",
    57
)

private val v1 = Vehicle("064396c0", "vehicle", v1a1)
private val v2 = Vehicle("883a94de", "vehicle", v2a2)
private val dummyList = AvailableVehicles(listOf(v1, v2))

