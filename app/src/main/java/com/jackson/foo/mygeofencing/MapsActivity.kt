package com.jackson.foo.mygeofencing

import android.Manifest
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

class MapsActivity : BaseActivity(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var locationManager: LocationManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val MY_LOCATION_REQUEST_CODE = 1
        private const val MY_PERMISSIONS_REQUEST_ACCESS_LOCATION = 1234
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        setTitle("My Petronas Geofencing")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_LOCATION_REQUEST_CODE
            )
        } else {
            displayLocationSettingsRequest(this).addOnSuccessListener { locationSettingsResponse ->

            }.addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    try {
                        startIntentSenderForResult(
                            exception.resolution.intentSender,
                            MY_PERMISSIONS_REQUEST_ACCESS_LOCATION, null, 0, 0, 0, null
                        )
                    } catch (sendEx: IntentSender.SendIntentException) {
                        // Ignore the error.
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == MY_LOCATION_REQUEST_CODE) {

        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.run {
            uiSettings.isMyLocationButtonEnabled = true
            uiSettings.isMapToolbarEnabled = true
            setOnMarkerClickListener(this@MapsActivity)
            setOnMyLocationButtonClickListener(this@MapsActivity)
            setOnMyLocationClickListener(this@MapsActivity)
        }

        onMapAndPermissionReady()
    }

    override fun onMyLocationButtonClick(): Boolean {
        return false
    }

    override fun onMyLocationClick(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        return false
    }

    private fun onMapAndPermissionReady() {
        if (mMap != null && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        }
    }

}
