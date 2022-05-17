package com.pdguru.transportmapper.model

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AvailableVehicles(
	val data: List<Vehicle?>? = null
) : Parcelable

@Parcelize
data class Vehicle(
	val attributes: Attributes,
	val id: String,
	val type: String
) : Parcelable

@Parcelize
data class Attributes(
	val lng: Double,
	val hasHelmetBox: Boolean,
	val maxSpeed: Int,
	val lat: Double,
	val vehicleType: String,
	val batteryLevel: Int
) : Parcelable, ClusterItem {
	override fun getPosition(): LatLng {
		return LatLng(lat, lng)
	}

	override fun getTitle(): String {
		return vehicleType
	}

	override fun getSnippet(): String {
		return ""
	}
}
