package com.android.raywenderlich.remindmethere.model

import com.google.gson.annotations.SerializedName

class Geometry {
    @SerializedName("location")
    var location : Location = Location()
}