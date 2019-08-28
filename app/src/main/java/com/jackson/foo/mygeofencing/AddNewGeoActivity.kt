package com.jackson.foo.mygeofencing

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.jackson.foo.mygeofencing.model.MyGeofencing
import kotlinx.android.synthetic.main.activity_add_new_geo.*
import kotlin.math.roundToInt

class AddNewGeoActivity : BaseActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap

    private var myGeofencing = MyGeofencing(latLng = null, radius = null, message = null)

    private val radiusBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onStartTrackingTouch(seekBar: SeekBar?) {}

        override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            updateRadiusWithProgress(progress)
            updateMaps()
        }
    }

    private fun updateRadiusWithProgress(progress: Int) {
        val radius = getRadius(progress)
        myGeofencing.radius = radius
        radiusDescription.text =
            getString(R.string.radius_description, radius.roundToInt().toString())
    }

    companion object {
        private const val EXTRA_LAT_LNG = "EXTRA_LAT_LNG"
        private const val EXTRA_ZOOM = "EXTRA_ZOOM"

        fun newIntent(context: Context, latLng: LatLng, zoom: Float): Intent {
            val intent = Intent(context, AddNewGeoActivity::class.java)
            intent
                .putExtra(EXTRA_LAT_LNG, latLng)
                .putExtra(EXTRA_ZOOM, zoom)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_geo)


        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        instructionTitle.visibility = View.GONE
        instructionSubtitle.visibility = View.GONE
        radiusBar.visibility = View.GONE
        radiusDescription.visibility = View.GONE
        message.visibility = View.GONE

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.run {
            uiSettings.isMyLocationButtonEnabled = true
            uiSettings.isMapToolbarEnabled = true
        }

        onMapAndPermissionReady()
    }

    private fun onMapAndPermissionReady() {
        if (map != null && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true

            centerCamera()

            showConfigureLocationStep()
        }
    }

    private fun centerCamera() {
        val latLng = intent.extras!!.get(EXTRA_LAT_LNG) as LatLng
        val zoom = intent.extras!!.get(EXTRA_ZOOM) as Float
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    private fun showConfigureLocationStep() {
        marker.visibility = View.VISIBLE
        instructionTitle.visibility = View.VISIBLE
        instructionSubtitle.visibility = View.VISIBLE
        radiusBar.visibility = View.GONE
        radiusDescription.visibility = View.GONE
        message.visibility = View.GONE
        instructionTitle.text = getString(R.string.instruction_where_description)
        next.setOnClickListener {
            myGeofencing.latLng = mMap.cameraPosition.target
            showConfigureRadiusStep()
        }

        updateMaps()
    }

    private fun showConfigureRadiusStep() {
        marker.visibility = View.GONE
        instructionTitle.visibility = View.VISIBLE
        instructionSubtitle.visibility = View.GONE
        radiusBar.visibility = View.VISIBLE
        radiusDescription.visibility = View.VISIBLE
        message.visibility = View.GONE
        instructionTitle.text = getString(R.string.instruction_radius_description)
        next.setOnClickListener {
            showConfigureMessageStep()
        }
        radiusBar.setOnSeekBarChangeListener(radiusBarChangeListener)
        updateRadiusWithProgress(radiusBar.progress)

        mMap.animateCamera(CameraUpdateFactory.zoomTo(15f))

        updateMaps()
    }

    private fun getRadius(progress: Int) = 100 + (2 * progress.toDouble() + 1) * 100

    private fun showConfigureMessageStep() {
        marker.visibility = View.GONE
        instructionTitle.visibility = View.VISIBLE
        instructionSubtitle.visibility = View.GONE
        radiusBar.visibility = View.GONE
        radiusDescription.visibility = View.GONE
        message.visibility = View.VISIBLE
        instructionTitle.text = getString(R.string.instruction_message_description)
        next.setOnClickListener {
            hideKeyboard(this, message)

            myGeofencing.message = message.text.toString()

            if (myGeofencing.message.isNullOrEmpty()) {
                message.error = getString(R.string.error_required)
            } else {
                addNewGeofencing(myGeofencing)
            }
        }
        message.requestFocusWithKeyboard()

        updateMaps()
    }

    private fun addNewGeofencing(myGeofencing: MyGeofencing) {
        getRepository().add(myGeofencing,
            success = {
                setResult(Activity.RESULT_OK)
                finish()
            },
            failure = {
                Snackbar.make(main, it, Snackbar.LENGTH_LONG).show()
            })
    }

    private fun updateMaps() {
        mMap.clear()
        showReminderInMap(this, mMap, myGeofencing)
    }
}

