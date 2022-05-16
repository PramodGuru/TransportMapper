package com.pdguru.transportmapper

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.Marker
import com.pdguru.transportmapper.model.AvailableVehicles
import com.pdguru.transportmapper.model.Vehicle
import com.pdguru.transportmapper.networking.VehiclesInterface
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import timber.log.Timber

class MapActivityViewModel(retrofitClient: Retrofit) : ViewModel() {
    private val vehiclesInterface: VehiclesInterface =
        retrofitClient.create(VehiclesInterface::class.java)

    private val _state = MutableLiveData<UIState>()
    val state = _state as LiveData<UIState>

    private val currentState: UIState
        get() {
            return state.value ?: UIState()
        }

    init {
        _state.value = currentState
    }

    fun getAvailableVehicles() {
        try {
            viewModelScope.launch {
                handleResponse(vehiclesInterface.getAllAvailableVehicles())
            }
        } catch (exception: Exception) {
            Timber.e(exception)
        }
    }

    private fun handleResponse(response: Response<AvailableVehicles>) {
        if (response.isSuccessful) _state.postValue(
            currentState.copy(
                availableVehicles = response.body()?.data?.toList() ?: listOf()
            )
        )
        else _state.postValue(currentState.copy(message = "Something went wrong"))
    }

    fun getVehicleInfo(marker: Marker): Vehicle? {
        return currentState.availableVehicles.find { vehicle ->
            vehicle?.id == marker.snippet
        }
    }

    data class UIState(
        val availableVehicles: List<Vehicle?> = listOf(),
        val message: String? = null
    )
}
