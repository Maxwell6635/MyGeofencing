package com.jackson.foo.mygeofencing.model

import com.google.gson.annotations.SerializedName

class Geometry {
    @SerializedName("location")
    var myLocation : MyLocation = MyLocation()
}