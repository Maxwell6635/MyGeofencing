package com.jackson.foo.mygeofencing

import android.content.Context
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.tasks.Task


fun displayLocationSettingsRequest(context: Context): Task<LocationSettingsResponse> {
    val locationRequest = createLocationRequest()
    val builder = LocationSettingsRequest.Builder()
    builder.addLocationRequest(locationRequest)
    builder.setAlwaysShow(true)

    val mLocationSettingsRequest = builder.build()
    val mSettingsClient = LocationServices.getSettingsClient(context)
    return mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
}

fun createLocationRequest(): LocationRequest {
    val locationRequest = LocationRequest.create()
    locationRequest.interval = (60 * 1000 * 5).toLong()
    locationRequest.fastestInterval = 1000
    locationRequest.smallestDisplacement = 30f
    locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    return locationRequest
}