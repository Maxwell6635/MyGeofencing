package com.jackson.foo.mygeofencing.model

import com.google.gson.annotations.SerializedName

data class MyLocation(
    @SerializedName("lat")
    var lat: Double = 0.0,

    @SerializedName("lng")
    var lng: Double = 0.0
) {

    override fun toString(): String {
        return String.format("%f,%f", lat, lng)
    }
}