package com.pdguru.transportmapper

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.Marker
import com.pdguru.transportmapper.model.AvailableVehicles
import com.pdguru.transportmapper.model.Vehicle
import com.pdguru.transportmapper.networking.ConnectivityHelper
import com.pdguru.transportmapper.networking.VehiclesInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import timber.log.Timber


class MapActivityViewModel(retrofitClient: Retrofit, val connectivityHelper: ConnectivityHelper) : ViewModel() {
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
        if (connectivityHelper.isInternetConnected()) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    handleResponse(vehiclesInterface.getAllAvailableVehicles())
                } catch (exception: Exception) {
                    Timber.e(exception)
                }
            }
        } else{
            _state.postValue(currentState.copy(message = "You are not connected to the internet"))
        }
    }

    private fun handleResponse(response: Response<AvailableVehicles>) {
        if (response.isSuccessful) {
            _state.postValue(
                currentState.copy(availableVehicles = response.body()?.data?.toList() ?: listOf())
            )
            if(response.body()?.data?.toList().isNullOrEmpty()){
                _state.postValue(currentState.copy(message = "Could not fetch available vehicles"))
            }
        }
        else {
            _state.postValue(currentState.copy(message = "Something went wrong"))
            Timber.e("${response.errorBody()}")
        }
    }

    fun getVehicleInfo(marker: Marker): Vehicle? {
        return currentState.availableVehicles.find { vehicle ->
            vehicle?.attributes?.lat == marker.position.latitude
                    && vehicle.attributes.lng == marker.position.longitude
        }
    }

    fun checkInternet(): Boolean {
        return connectivityHelper.isInternetConnected()
    }

    data class UIState(
        val availableVehicles: List<Vehicle?> = listOf(),
        val message: String? = null
    )
}
