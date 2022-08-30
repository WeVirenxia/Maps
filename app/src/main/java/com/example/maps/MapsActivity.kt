package com.example.maps

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
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
import com.google.gson.Gson
import org.json.JSONArray
import java.util.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    var initialClick: LatLng? = null
    var secondClick: LatLng? = null
    private var initialLocation: LatLng? = null
    private var gpsTracker: GPSTracker? = null
    var coordinates: Coordinates? = null
    private var isStopped = false
    var isAuto = false
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
            //getCurrentLocation()
            /*val toast= Toast.makeText(applicationContext,"latitude:${coordinates?.latitude},logitude:${coordinates?.longitude}",Toast.LENGTH_LONG)
            toast.setGravity(Gravity.TOP,0,0)
            toast.show()*/
            getJson(coordinatesList)
            CommonMethod.showSnackBar(binding.root,"latitude:${coordinates?.latitude},logitude:${coordinates?.longitude}")
        }
        // start and stop
        binding.menuItem.setOnClickListener {
            isStopped = !isStopped
            if (isStopped) {
                binding.menuItem.labelText = "Stop"
                binding.menuItem.setImageDrawable(resources.getDrawable(R.drawable.ic_stop))
                binding.menuItem3.visibility=View.VISIBLE
                binding.menuItem4.visibility=View.VISIBLE
            } else {
                binding.menuItem.labelText = "Start"
                binding.menuItem.setImageDrawable(resources.getDrawable(R.drawable.ic_start))
                binding.menuItem3.visibility=View.GONE
                binding.menuItem4.visibility=View.GONE
                isAuto=false
                binding.menuItem4.isEnabled=true
                if (coordinatesList.size>2){
                    jointLastPoints()
                }
            }

        }
        //clear
        binding.menuItem2.setOnClickListener {
            coordinatesList.clear()
            mMap.clear()
            mapFragment?.getMapAsync(this)
        }
        //add point
        binding.menuItem3.setOnClickListener {
            if (isStopped) {
                binding.menuItem3.isEnabled=false
                if (CommonMethod.isNetworkConnected(applicationContext)) {
                    //mapViewModel.getCurrentLocation(applicationContext)
                    getCurrentLocation()
                    /*val toast:Toast=Toast.makeText(applicationContext,"latitude:${coordinates?.latitude},logitude:${coordinates?.longitude}",Toast.LENGTH_LONG)
                    toast.setGravity(Gravity.TOP,0,0)
                    toast.show()*/
                    Handler().postDelayed({binding.menuItem3.isEnabled = true },5000)
                    CommonMethod.showSnackBar(binding.root,"latitude:${coordinates?.latitude},logitude:${coordinates?.longitude}")
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
        //auto
        binding.menuItem4.setOnClickListener {
            isAuto=true
            binding.menuItem4.isEnabled=false
            binding.menuItem3.visibility=View.GONE
            autoPlotting()
        }

    }
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
        mMap.isMyLocationEnabled=true
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

        }

    }

    private fun plotPoints(list: MutableList<LatLng>) {
        val polylineOptions = PolylineOptions()
        if (list.size > 1) {
            for (i in list) {
                polylineOptions.width(10F)
                polylineOptions.add(i).color(resources.getColor(R.color.colorVirenxia))
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
            }
        val currentLocation = LatLng(coordinates!!.latitude, coordinates!!.longitude)
        coordinatesList.add(currentLocation)
        Log.d("TAG","Coorrdiotes List ${coordinatesList.size}")
        if (coordinatesList.size>1){
            plotPoints(coordinatesList)
            //binding.menuItem3.isEnabled=true
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
 private fun getJson(coordinateList:MutableList<LatLng>){
     //val jsArray = JSONArray(coordinateList)
     val jsArray = Gson().toJson(coordinateList)
     val newJSONObject= JSONArray()
     val jsonArray=JSONArray(jsArray)
    /* for (i in 1..jsonObject.length()){
         val mainObj:JSONObject= jsonObject.get(i.toString()) as JSONObject
         mainObj.put("Sr No.",i)
         newJSONObject.put(i.toString(),mainObj)
         Log.d("TAG","newvalue : ${mainObj.toString()}")
     }*/
     for (i in 0 until jsonArray.length()){
         val mainObj=jsonArray.getJSONObject(i)
         mainObj.put("Sr No.",i+1)
         newJSONObject.put(mainObj)
     }
     Log.d("TAG","New Json: ${newJSONObject.toString()}")
     Log.d("TAG","Json length : ${jsonArray.length()}")
     //Log.d("TAG","Json : ${jsArray.toString()}")
 }

    private fun jointLastPoints(){
        val firstPoint= coordinatesList[0]
        coordinatesList.add(firstPoint)
        plotPoints(coordinatesList)
    }

    private fun autoPlotting(){
            val timer=Timer()
            timer.schedule(object : TimerTask() {
                override fun run() {
                    runOnUiThread {
                        if (isAuto){
                            getCurrentLocation()
                            Log.d("TAG","hello world")
                        }
                    }
                }
            }, 0, 10000)
        }

}