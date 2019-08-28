package com.jackson.foo.mygeofencing

import com.jackson.foo.mygeofencing.model.Places
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface HttpClient {

    //https://maps.googleapis.com/maps/api/place/nearbysearch/json?
    //
    //myLocation=3.1138174,101.7242323&rankby=distance&name=Petronas+Station&key=AIzaSyAx8HUNXu_WS4kcnlpMkxdxLlDmMoMFf3Q
    @GET("maps/api/place/nearbysearch/json?")
    @Headers("Content-Type: application/json")
    fun getNearbyPlace(
        @Query("location") location: String,
        @Query("rankby") rankBy: String,
        @Query("name") name: String,
        @Query("key") apiKey: String
    ): Observable<Places>
}
