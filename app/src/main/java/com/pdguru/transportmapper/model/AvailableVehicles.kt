package com.pdguru.transportmapper.model

import android.os.Parcelable
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
) : Parcelable
