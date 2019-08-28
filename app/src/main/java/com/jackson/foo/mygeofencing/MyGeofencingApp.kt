package com.jackson.foo.mygeofencing

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics

class MyGeofencingApp : Application() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var repository: MyGeoRepository

    override fun onCreate() {
        super.onCreate()
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        repository = MyGeoRepository(this)
    }

    fun getRepository() = repository
}