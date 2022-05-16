package com.pdguru.transportmapper

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.pdguru.transportmapper.databinding.ActivityMapsBinding
import org.koin.androidx.viewmodel.ext.android.viewModel


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var gMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val viewModel by viewModel<MapActivityViewModel>()

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        viewModel.getAvailableVehicles()

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            getUserLocation()
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        gMap = googleMap

        // place marker for each available vehicle
        viewModel.availableVehicles.observe(this) { vehicles ->
            vehicles.forEach {
                val marker = LatLng(it!!.attributes.lat, it.attributes.lng)
                gMap.addMarker(MarkerOptions().position(marker).title(it.attributes.vehicleType))
            }
        }

        //get users location
        val userLatLng = getUserLocation()
        gMap.addMarker(MarkerOptions()
            .position(userLatLng)
            .title("Current location")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
        gMap.moveCamera(CameraUpdateFactory.newLatLng(userLatLng))
        gMap.animateCamera(CameraUpdateFactory.zoomTo(18f))
    }

    @SuppressLint("MissingPermission")
    private fun getUserLocation(): LatLng {
        var latLng = LatLng(52.525, 13.369) // default to Berlin Hbf
        if (hasLocationPermission()){
        val location = fusedLocationProviderClient.lastLocation
            location.addOnCompleteListener { task ->
                if(task.isSuccessful){
                    val lastKnownLocation = task.result
                    latLng = LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude)
                }
            }
        } else requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        return latLng
    }

    private fun hasLocationPermission(): Boolean {
        return (ContextCompat.checkSelfPermission(
            this.applicationContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }
}
