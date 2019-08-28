package com.jackson.foo.mygeofencing.model

import com.google.android.gms.maps.model.LatLng
import java.util.*

data class MyGeofencing(val id: String = UUID.randomUUID().toString(),
                    var latLng: LatLng?,
                    var radius: Double?,
                    var message: String?)