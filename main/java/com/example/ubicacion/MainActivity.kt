package com.example.ubicacion

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class MainActivity : AppCompatActivity() {

    private val locationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            permissions ->
            when {
                permissions.getOrDefault(ACCESS_FINE_LOCATION, false) -> {
                    Log.d(javaClass.simpleName, "Precise location access granted.")
                }
                permissions.getOrDefault(ACCESS_COARSE_LOCATION, false) -> {
                    Log.d(javaClass.simpleName, "Approximate location access granted.")
                } else -> {
                    Log.d(javaClass.simpleName, "No location access granted.")
                }
            }
        }

        private var locationListener: LocationListener? = null
        private var locationManager: LocationManager? = null
        //private var map: MapView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val centertext: TextView = findViewById(R.id.centertext)
        var marker: Marker? = null
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED && ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            locationPermissionRequest.launch(arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION))
        }

        locationListener = LocationListener { location ->
            // Check location fields here.
            //Log.v("location", location.altitude.toString())
            val geoInfo: String = "altitude: ${location.altitude}\nlongitude: ${location.longitude}\nspeed: ${location.speed}\nbearing: ${location.bearing}"
            centertext.text = geoInfo

            val map: MapView = findViewById(R.id.mapView)
            map.setTileSource(TileSourceFactory.MAPNIK)
            map.setMultiTouchControls(true)
            Configuration.getInstance().userAgentValue = applicationContext.packageName
            map.controller.setZoom(15.5)
            val centerPoint = GeoPoint(location.latitude, location.longitude)
            map.controller.setCenter(centerPoint)

            // Add a marker
            marker?.let {
                map.overlays.remove(it)
            }
            marker = Marker(map)

            marker!!.position = GeoPoint(location.latitude, location.longitude)
            marker!!.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker!!.title = "Here"
            marker!!.snippet = "You are here"

            map.overlays.add(marker)
            map.invalidate()
        }
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

    }

    override fun onResume(){
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (locationListener != null)
                locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener!!)
        }
                Log.v("request", "requested updates")
    }

    override fun onPause() {
        super.onPause()
        if (locationListener != null)
            locationManager?.removeUpdates(locationListener!!)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}