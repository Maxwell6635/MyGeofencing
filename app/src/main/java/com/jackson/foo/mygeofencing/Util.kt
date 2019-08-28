package com.jackson.foo.mygeofencing

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.jackson.foo.mygeofencing.model.MyGeofencing


fun displayLocationSettingsRequest(context: Context): Task<LocationSettingsResponse> {
    val locationRequest = createLocationRequest()
    val builder = LocationSettingsRequest.Builder()
    builder.addLocationRequest(locationRequest)
    builder.setAlwaysShow(true)

    val mLocationSettingsRequest = builder.build()
    val mSettingsClient = LocationServices.getSettingsClient(context)
    return mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
}

fun createLocationRequest(): LocationRequest {
    val locationRequest = LocationRequest.create()
    locationRequest.interval = (60 * 1000 * 5).toLong()
    locationRequest.fastestInterval = 1000
    locationRequest.smallestDisplacement = 30f
    locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    return locationRequest
}

fun EditText.requestFocusWithKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    if (!hasFocus()) {
        requestFocus()
    }

    post { imm.showSoftInput(this, InputMethodManager.SHOW_FORCED) }
}

fun hideKeyboard(context: Context, view: View) {
    val keyboard = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    keyboard.hideSoftInputFromWindow(view.windowToken, 0)
}

fun vectorToBitmap(resources: Resources, @DrawableRes id: Int): BitmapDescriptor {
    val vectorDrawable = ResourcesCompat.getDrawable(resources, id, null)
    val bitmap = Bitmap.createBitmap(vectorDrawable!!.intrinsicWidth,
        vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
    vectorDrawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

fun showReminderInMap(context: Context,
                      map: GoogleMap,
                      myGeofencing: MyGeofencing) {
    if (myGeofencing.latLng != null) {
        val latLng = myGeofencing.latLng as LatLng
        val vectorToBitmap = vectorToBitmap(context.resources, R.drawable.ic_twotone_location_on_48px)
        val marker = map.addMarker(MarkerOptions().position(latLng).icon(vectorToBitmap))
        marker.tag = myGeofencing.id
        if (myGeofencing.radius != null) {
            val radius = myGeofencing.radius as Double
            map.addCircle(
                CircleOptions()
                    .center(myGeofencing.latLng)
                    .radius(radius)
                    .strokeColor(ContextCompat.getColor(context, R.color.colorWhite))
                    .fillColor(ContextCompat.getColor(context, R.color.colorMyGeo)))
        }
    }
}

private const val NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".channel"

fun sendNotification(context: Context, message: String, latLng: LatLng) {
    val notificationManager = context
        .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        && notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null) {
        val name =  context.getString(R.string.app_name)
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID,
            name,
            NotificationManager.IMPORTANCE_DEFAULT)

        notificationManager.createNotificationChannel(channel)
    }

    val intent = MapsActivity.newIntent(context.applicationContext, latLng)

    val stackBuilder = TaskStackBuilder.create(context)
        .addParentStack(MapsActivity::class.java)
        .addNextIntent(intent)
    val notificationPendingIntent = stackBuilder
        .getPendingIntent(getUniqueId(), PendingIntent.FLAG_UPDATE_CURRENT)

    val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle("You are nearby ".plus(message))
        .setContentIntent(notificationPendingIntent)
        .setAutoCancel(true)
        .setDefaults(Notification.DEFAULT_ALL)
        .build()

    notificationManager.notify(getUniqueId(), notification)
}

private fun getUniqueId() = ((System.currentTimeMillis() % 10000).toInt())