package com.jackson.foo.mygeofencing.model

import com.google.gson.annotations.SerializedName

class PlaceResult {
    @SerializedName("geometry")
    var geometry : Geometry = Geometry()

    @SerializedName("icon")
    var icon : String = ""

    @SerializedName("id")
    var id : String = ""

    @SerializedName("name")
    var name : String = ""

    @SerializedName("place_id")
    var placeId : String = ""

}