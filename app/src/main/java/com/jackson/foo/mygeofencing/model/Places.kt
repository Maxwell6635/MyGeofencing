package com.android.raywenderlich.remindmethere.model

import com.google.gson.annotations.SerializedName

class Places {
    @SerializedName("next_page_token")
    var nextPageToken : String = ""

    @SerializedName("results")
    var result : ArrayList<PlaceResult> = ArrayList()

    @SerializedName("status")
    var status : String = ""
}