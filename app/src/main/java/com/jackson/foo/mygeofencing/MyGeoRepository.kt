package com.jackson.foo.mygeofencing

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.jackson.foo.mygeofencing.model.MyGeofencing

class MyGeoRepository(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "MyGeoRepository"
        private const val MYGEOLIST = "MyGeoList"
    }

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val geofencingClient = LocationServices.getGeofencingClient(context)
    private val gson = Gson()

    fun add(
        myGeofencing: MyGeofencing,
        success: () -> Unit,
        failure: (error: String) -> Unit
    ) {
        val geofence = buildGeofence(myGeofencing)
        if (geofence != null
            && ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            geofencingClient
                .addGeofences(buildGeofencingRequest(geofence), geofencePendingIntent)
                .addOnSuccessListener {
                    saveAll(getAll() + myGeofencing)
                    success()
                }
                .addOnFailureListener {
                    failure(GeofenceErrorMessages.getErrorString(context, it))
                }
        }
    }

    fun addAll(
        myGeofencings: List<MyGeofencing>,
        success: () -> Unit,
        failure: (error: String) -> Unit
    ) {
        for (item in myGeofencings) {
            val geofence = buildGeofence(item)
            if (geofence != null
                && ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                geofencingClient
                    .addGeofences(buildGeofencingRequest(geofence), geofencePendingIntent)
                    .addOnSuccessListener {
                        saveAll(getAll() + item)
                        success()
                    }
                    .addOnFailureListener {
                        failure(GeofenceErrorMessages.getErrorString(context, it))
                    }
            }
        }
    }

    fun removeAll(
        success: () -> Unit,
        failure: (error: String) -> Unit
    ) {
        geofencingClient
            .removeGeofences(geofencePendingIntent)
            .addOnSuccessListener {
                deleteAll()
                success()
            }
            .addOnFailureListener {
                failure(GeofenceErrorMessages.getErrorString(context, it))
            }
    }


    fun remove(
        myGeofencing: MyGeofencing,
        success: () -> Unit,
        failure: (error: String) -> Unit
    ) {
        geofencingClient
            .removeGeofences(listOf(myGeofencing.id))
            .addOnSuccessListener {
                saveAll(getAll() - myGeofencing)
                success()
            }
            .addOnFailureListener {
                failure(GeofenceErrorMessages.getErrorString(context, it))
            }
    }

    private fun buildGeofence(myGeofencing: MyGeofencing): Geofence? {
        val latitude = myGeofencing.latLng?.latitude
        val longitude = myGeofencing.latLng?.longitude
        val radius = myGeofencing.radius

        if (latitude != null && longitude != null && radius != null) {
            return Geofence.Builder()
                .setRequestId(myGeofencing.id)
                .setCircularRegion(
                    latitude,
                    longitude,
                    radius.toFloat()
                )
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build()
        }

        return null
    }

    private fun buildGeofencingRequest(geofence: Geofence): GeofencingRequest {
        return GeofencingRequest.Builder()
            .setInitialTrigger(0)
            .addGeofences(listOf(geofence))
            .build()
    }

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun saveAll(list: List<MyGeofencing>) {
        preferences
            .edit()
            .putString(MYGEOLIST, gson.toJson(list))
            .apply()
    }

    fun removeAll(
        reminderId: List<String>,
        success: () -> Unit,
        failure: (error: String) -> Unit
    ) {
        geofencingClient
            .removeGeofences(reminderId)
            .addOnSuccessListener {
                deleteAll()
                success()
            }
            .addOnFailureListener {
                failure(GeofenceErrorMessages.getErrorString(context, it))
            }
    }

    fun deleteAll() {
        preferences
            .edit()
            .clear()
            .apply()
    }

    fun getAll(): List<MyGeofencing> {
        if (preferences.contains(MYGEOLIST)) {
            val remindersString = preferences.getString(MYGEOLIST, null)
            val arrayOfReminders = gson.fromJson(
                remindersString,
                Array<MyGeofencing>::class.java
            )
            if (arrayOfReminders != null) {
                return arrayOfReminders.toList()
            }
        }
        return listOf()
    }

    fun get(requestId: String?) = getAll().firstOrNull { it.id == requestId }

    fun getLast() = getAll().lastOrNull()
}