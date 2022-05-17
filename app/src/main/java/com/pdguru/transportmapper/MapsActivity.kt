package com.pdguru.transportmapper

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.pdguru.transportmapper.databinding.ActivityMapsBinding
import com.pdguru.transportmapper.model.Vehicle
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var gMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val viewModel by viewModel<MapActivityViewModel>()

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    private val bottomSheetView by lazy { findViewById<ConstraintLayout>(R.id.bottomSheet) }
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView)
        setBottomSheetVisibility(false)

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { permit ->
            if (permit) getUserLocation()
            else useDefaultLocation()
        }
        viewModel.getAvailableVehicles()
    }

    private fun useDefaultLocation(): LatLng {
        return LatLng(52.52519, 13.36935) // berlin Hbf
    }

    private fun setBottomSheetVisibility(isVisible: Boolean) {
        val updatedState =
            if (isVisible) BottomSheetBehavior.STATE_EXPANDED else BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetBehavior.state = updatedState
    }

    override fun onMapReady(googleMap: GoogleMap) {
        gMap = googleMap

        // place marker for each available vehicle
        viewModel.state.observe(this) { state ->
            state.availableVehicles.forEach {
                val marker = LatLng(it!!.attributes.lat, it.attributes.lng)
                gMap.addMarker(
                    MarkerOptions().position(marker).title(it.attributes.vehicleType).snippet(it.id)
                )
            }
            state.message?.let { toast(it) }
        }

        //get users location
        getUserLocation()
    }

    private fun showVehicleInfo(vehicle: Vehicle?) {
        if (vehicle != null) {
            bottomSheetView.findViewById<TextView>(R.id.tv_vehicle_type).text =
                vehicle.attributes.vehicleType
            bottomSheetView.findViewById<TextView>(R.id.tv_battery_level).text =
                "${vehicle.attributes.batteryLevel}%"
            bottomSheetView.findViewById<CheckBox>(R.id.cb_helmet).isChecked =
                vehicle.attributes.hasHelmetBox
            setBottomSheetVisibility(true)
        } else {
            setBottomSheetVisibility(false)
            toast("Could not get vehicle information")
        }

    }

    @SuppressLint("MissingPermission")
    private fun getUserLocation() {
        if (hasLocationPermission()) {
            val location = fusedLocationProviderClient.lastLocation
            location.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val lastKnownLocation = task.result
                    Timber.d("currLoc: ${lastKnownLocation.latitude}, ${lastKnownLocation.longitude}")
                    showUserLocation(LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude))
                } else {
                    toast("Could not determine your location")
                    showUserLocation(useDefaultLocation()) // default Berlin Hbf
                }
            }
        } else {
            toast("Location permission not granted.")
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun showUserLocation(userLatLng: LatLng) {
        gMap.apply {
            addMarker(
                MarkerOptions()
                    .position(userLatLng)
                    .title("Current location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
            moveCamera(CameraUpdateFactory.newLatLng(userLatLng))
            animateCamera(CameraUpdateFactory.zoomTo(16f))
            setOnMapClickListener { setBottomSheetVisibility(false) }
            setOnMarkerClickListener { marker ->
                showVehicleInfo(viewModel.getVehicleInfo(marker))
                false
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        return (ContextCompat.checkSelfPermission(
            this.applicationContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
