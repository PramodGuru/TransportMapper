package com.pdguru.transportmapper

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pdguru.transportmapper.model.AvailableVehicles
import com.pdguru.transportmapper.model.Vehicle
import com.pdguru.transportmapper.networking.VehiclesInterface
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit

class MapActivityViewModel(retrofitClient: Retrofit) : ViewModel() {
    private val vehiclesInterface: VehiclesInterface =
        retrofitClient.create(VehiclesInterface::class.java)

    private val _availableVehicles = MutableLiveData<List<Vehicle?>>()
    val availableVehicles = _availableVehicles as LiveData<List<Vehicle?>>

    fun getAvailableVehicles() {
        viewModelScope.launch {
            handleResponse(vehiclesInterface.getAllAvailableVehicles())
        }
    }

    private fun handleResponse(response: Response<AvailableVehicles>) {
        _availableVehicles.postValue(response.body()?.data?.toList())
    }
}
