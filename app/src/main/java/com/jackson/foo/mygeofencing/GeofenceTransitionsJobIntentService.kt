package com.jackson.foo.mygeofencing

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.jackson.foo.mygeofencing.model.MyGeofencing

class GeofenceTransitionsJobIntentService : JobIntentService() {

    companion object {
        private const val LOG_TAG = "GeoTrJobIntentService"

        private const val JOB_ID = 123

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                context,
                GeofenceTransitionsJobIntentService::class.java, JOB_ID,
                intent
            )
        }
    }

    override fun onHandleWork(intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceErrorMessages.getErrorString(
                this,
                geofencingEvent.errorCode
            )
            Log.e(LOG_TAG, errorMessage)
            return
        }
        handleEvent(geofencingEvent)
    }

    private fun handleEvent(event: GeofencingEvent) {
        if (event.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            val myGeofencing = getCurrentGeo(event.triggeringGeofences)
            val message = myGeofencing?.message
            val latLng = myGeofencing?.latLng
            if (message != null && latLng != null) {
                sendNotification(this, message, latLng)
            }
        }
    }

    private fun getCurrentGeo(triggeringGeofences: List<Geofence>): MyGeofencing? {
        val firstGeofence = triggeringGeofences[0]
        return (application as MyGeofencingApp).getRepository().get(firstGeofence.requestId)
    }

}