package com.pdguru.transportmapper

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
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
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.pdguru.transportmapper.databinding.ActivityMapsBinding
import org.koin.androidx.viewmodel.ext.android.viewModel


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
        ) {
            getUserLocation()
        }
        viewModel.getAvailableVehicles()
    }

    private fun setBottomSheetVisibility(isVisible: Boolean) {
        val updatedState =
            if (isVisible) BottomSheetBehavior.STATE_EXPANDED else BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetBehavior.state = updatedState
    }

    override fun onMapReady(googleMap: GoogleMap) {
        gMap = googleMap

        // place marker for each available vehicle
        viewModel.availableVehicles.observe(this) { vehicles ->
                vehicles.forEach {
                    val marker = LatLng(it!!.attributes.lat, it.attributes.lng)
                    gMap.addMarker(
                        MarkerOptions().position(marker).title(it.attributes.vehicleType)
                    )
                }
        }

        //get users location
        val userLatLng = getUserLocation()

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
                showVehicleInfo(marker)
                false
            }
        }
    }

    private fun showVehicleInfo(marker: Marker) {
        bottomSheetView.findViewById<TextView>(R.id.tv_vehicle_type).text = marker.title
        setBottomSheetVisibility(true)
    }

    @SuppressLint("MissingPermission")
    private fun getUserLocation(): LatLng {
        var latLng = LatLng(52.52519, 13.36935) // default to Berlin Hbf
        if (hasLocationPermission()) {
            val location = fusedLocationProviderClient.lastLocation
            location.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val lastKnownLocation = task.result
                    latLng = LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude)
                } else {
                    toast("Could not determine your location")
                }
            }
        } else {
            toast("Location permission not granted.")
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        return latLng
    }

    private fun hasLocationPermission(): Boolean {
        return (ContextCompat.checkSelfPermission(
            this.applicationContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }

    private fun toast(message: String){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
