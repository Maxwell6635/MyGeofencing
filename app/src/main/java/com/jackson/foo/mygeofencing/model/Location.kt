package com.android.raywenderlich.remindmethere.model

import com.google.gson.annotations.SerializedName

class Location {

    @SerializedName("lat")
    var lat : Double = 0.0

    @SerializedName("lng")
    var lng : Double = 0.0

    override fun toString(): String {
        return String.format("%.1f,%.1f", lat, lng)
    }
}