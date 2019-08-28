package com.jackson.foo.mygeofencing

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.jackson.foo.mygeofencing.model.MyGeofencing
import com.jackson.foo.mygeofencing.model.MyLocation
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_maps.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class MapsActivity : BaseActivity(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var locationManager: LocationManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val disposable = CompositeDisposable()
    private var petronasStationList: MutableList<MyGeofencing> = ArrayList()

    companion object {
        private const val MY_LOCATION_REQUEST_CODE = 1
        private const val MY_PERMISSIONS_REQUEST_ACCESS_LOCATION = 1234
        private const val PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
        private const val EXTRA_LAT_LNG = "EXTRA_LAT_LNG"

        fun newIntent(context: Context, latLng: LatLng): Intent {
            val intent = Intent(context, MapsActivity::class.java)
            intent.putExtra(EXTRA_LAT_LNG, latLng)
            return intent
        }
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
                fusedLocationClient.lastLocation.addOnSuccessListener {
                    it
                    initialPetronasStation(MyLocation(it.latitude, it.longitude))
                }.addOnFailureListener {
                    initialPetronasStation(MyLocation(3.1138174, 101.7242323))
                }
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

    override fun onResume() {
        super.onResume()
        if (checkPlayServices()) {

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == MY_LOCATION_REQUEST_CODE) {
            onMapAndPermissionReady()
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

        showAllGeo()
    }

    private fun initialPetronasStation(location: MyLocation) {
        val httpClient: OkHttpClient = OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor()
                    .setLevel(HttpLoggingInterceptor.Level.BODY)
            ).build()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .client(httpClient)
            .build()
        disposable.add(
            retrofit.create(HttpClient::class.java).getNearbyPlace(
                location.toString(),
                "distance",
                "Petronas Station",
                "AIzaSyDcsMd_l4tOQ62EMBfyGH3CLigzZY4Rr6A"
            ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer {
                    getRepository().removeAll(success = {

                    }, failure = {
                        Snackbar.make(main, it, Snackbar.LENGTH_LONG).show()
                    })
                    for (item in it.result) {
                        petronasStationList.add(
                            MyGeofencing(
                                latLng = LatLng(
                                    item.geometry.myLocation.lat,
                                    item.geometry.myLocation.lng
                                ),
                                radius = 100.0, message = item.name
                            )
                        )
                    }
                    getRepository().addAll(petronasStationList, success = {
                        Snackbar.make(main, "Loaded Successfully", Snackbar.LENGTH_LONG).show()
                        showAllGeo()
                    }, failure = {
                        Snackbar.make(main, it, Snackbar.LENGTH_LONG).show()
                    })
                })
        )
    }

    private fun showAllGeo() {
        mMap.run {
            clear()
            for (myGeo in getRepository().getAll()) {
                showReminderInMap(this@MapsActivity, this, myGeo)
            }
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private fun checkPlayServices(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                    .show()
            } else {
                //This device is not supported
                finish()
            }
            return false
        }
        return true
    }

}
