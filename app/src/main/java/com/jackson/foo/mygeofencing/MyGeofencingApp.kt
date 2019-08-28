package com.jackson.foo.mygeofencing

import android.app.Application

class MyGeofencingApp : Application() {

    private lateinit var repository: MyGeoRepository

    override fun onCreate() {
        super.onCreate()
        repository = MyGeoRepository(this)
    }

    fun getRepository() = repository
}