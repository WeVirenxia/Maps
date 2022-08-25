package com.example.maps

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.maps.commonMethod.CommonMethod
import com.example.maps.commonMethod.GPSTracker
import com.example.maps.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.firestore.core.View
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    var initialClick: LatLng? = null
    var secondClick: LatLng? = null
    private var initialLocation: LatLng? = null
    private var gpsTracker: GPSTracker? = null
    var coordinates: Coordinates? = null
    var isStopped = false
    var mapFragment: SupportMapFragment? = null
    private val coordinatesList = mutableListOf<LatLng>()
    private lateinit var mapViewModel: MapViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        if (CommonMethod.isNetworkConnected(applicationContext)){
            permissionSetup()
        }else{
            Toast.makeText(applicationContext,"Turn On Internet Connection and Restart",Toast.LENGTH_LONG).show()
        }

        mapViewModel = ViewModelProvider(this).get(MapViewModel::class.java)
        setObservers()
        //weatherViewModel.clearResultSet()
        //Handler().postDelayed({ mapFragment?.getMapAsync(this) }, 5000)
        //
        // permissionSetup()
        binding.button.setOnClickListener {
            //  isStopped = !isStopped
            //mMap.clear()
            //mapFragment?.getMapAsync(this)
            //plotPoints(coordinatesList)
            //coordinatesList.subList(1,coordinatesList.size).clear()
            //coordinatesList.clear()
            getCurrentLocation()
            Toast.makeText(applicationContext,"latitude:${coordinates?.latitude},logitude:${coordinates?.longitude}",Toast.LENGTH_LONG).show()
        }
        binding.menuItem.setOnClickListener {
            isStopped = !isStopped
            if (isStopped) {
                binding.menuItem.labelText = "Stop"
                binding.menuItem.setImageDrawable(resources.getDrawable(R.drawable.ic_stop))
            } else {
                binding.menuItem.labelText = "Start"
                binding.menuItem.setImageDrawable(resources.getDrawable(R.drawable.ic_start))
            }

        }
        binding.menuItem2.setOnClickListener {
            coordinatesList.clear()
            mMap.clear()
            mapFragment?.getMapAsync(this)
        }

        binding.menuItem3.setOnClickListener {
            if (isStopped) {
                binding.menuItem3.isEnabled=false
                if (CommonMethod.isNetworkConnected(applicationContext)) {
                    //mapViewModel.getCurrentLocation(applicationContext)
                    getCurrentLocation()
                    Toast.makeText(applicationContext,"latitude:${coordinates?.latitude},logitude:${coordinates?.longitude}",Toast.LENGTH_LONG).show()
                }else{
                    Toast.makeText(applicationContext,"Turn On Internet",Toast.LENGTH_LONG).show()
                }
               // Handler().postDelayed({ binding.menuItem3.isEnabled = true },4000)
                /* val currentLocation = LatLng(coordinates!!.latitude, coordinates!!.longitude)
                 coordinatesList.add(currentLocation)*/
                //plotPoints(coordinatesList)
            }else{
                Toast.makeText(applicationContext,"Click On Start Button to Start Plotting",Toast.LENGTH_LONG).show()
            }
            Log.d("TAG", "size of the list ${coordinatesList.size}")
        }

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    private fun setObservers() {
        mapViewModel.coordinatesLiveDataList.observe(this, Observer {
            Log.d("TAG", "List of coordinates $it")
            initialLocation = LatLng(it[0].latitude,it[0].longitude)
            mMap.addMarker(MarkerOptions().position(initialLocation!!).title("Marker in Vashi"))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation!!, 18F))
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // Add a marker in Sydney and move the camera
        // val sydney = LatLng(-34.0, 151.0)
        //val vashi = LatLng(19.0856733, 73.005094)
        if (CommonMethod.isNetworkConnected(applicationContext)) {
           // mapViewModel.getCurrentLocation(applicationContext)
            getCurrentLocation()
        }else{
            Toast.makeText(applicationContext,"Turn On Internet",Toast.LENGTH_LONG).show()
        }
        //coordinatesList.add(initialLocation)
        //mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        initialLocation = LatLng(coordinates!!.latitude,coordinates!!.longitude)
        mMap.addMarker(MarkerOptions().position(initialLocation!!).title("Marker in Vashi"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation!!, 18F))
        mMap.setOnMapClickListener {
            Log.d("TAG", "${it.latitude}, ${it.longitude}")

            //val initialClick= LatLng(it.latitude,it.longitude)
            //lateinit var secondClick:LatLng
            if (isStopped) {
                //mMap.addMarker(MarkerOptions().position(it).title("Marker in Vashi"))
                coordinatesList.add(it)
                Log.d("TAG", "lsit $coordinatesList")
                plotPoints(coordinatesList)
            }

            /*if (initialClick==null){
                initialClick= LatLng(it.latitude,it.longitude)
            }else{
                secondClick = LatLng(it.latitude,it.longitude)
                plotPoints(initialClick!!, secondClick!!)
                initialClick=secondClick
            }*/


            //plotPoints(initialClick, secondClick)
            /* val polylineOptions = PolylineOptions()
             polylineOptions.add(vashi)
             polylineOptions.add(LatLng(it.latitude,it.longitude))
             mMap.clear()
             mMap.addPolyline(polylineOptions)*/
        }

    }

    /*fun plotPoints(initialClick:LatLng,secondClick:LatLng){
        val polylineOptions = PolylineOptions()
        polylineOptions.add(initialClick)
        polylineOptions.add(secondClick)
        mMap.clear()
        mMap.addPolyline(polylineOptions)
    }*/

    private fun plotPoints(list: List<LatLng>) {
        val polylineOptions = PolylineOptions()
        if (list.size > 1) {
            for (i in list) {
                polylineOptions.add(i)
                mMap.addMarker(MarkerOptions().position(i).title("Marker"))
                mMap.clear()
                mMap.addPolyline(polylineOptions)
            }
            /*mMap.clear()
            mMap.addPolyline(polylineOptions)*/
            Log.d("TAG","inside PlotPoints")
        }
        //polylineOptions.add(secondClick)

    }

    fun getCurrentLocation() {
            gpsTracker = GPSTracker(applicationContext)
            if (gpsTracker?.isGPSTrackingEnabled!!) {
                coordinates = CommonMethod.getLocation(applicationContext)
                Log.d("TAG","Coorrdiotes recieved $coordinates")
            } else {
                gpsTracker!!.showSettingsAlert()
                // Navigation.findNavController(fragmentWeatherBinding.root).navigate(R.id.action_weather_to_category)
            }
        val currentLocation = LatLng(coordinates!!.latitude, coordinates!!.longitude)
        coordinatesList.add(currentLocation)
        Log.d("TAG","Coorrdiotes List ${coordinatesList.size}")
        if (coordinatesList.size>1){
         //   plotPoints(coordinatesList)
            binding.menuItem3.isEnabled=true
        }

    }

    private fun permissionSetup() {
        val permission = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (permission != PackageManager.PERMISSION_GRANTED) {
            permissionsResultCallback.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )

        } else {
            mapFragment?.getMapAsync(this)
            println("Permission isGranted")
        }
    }


    private val permissionsResultCallback = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all {
            it.value == true
        }
        if (granted) {
            mapFragment?.getMapAsync(this)
            println("Permission has been granted by user")
        } else {
            Toast.makeText(applicationContext, "Permission denied", Toast.LENGTH_SHORT).show()
            //requireActivity().finish()
        }
    }

}