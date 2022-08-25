package com.example.maps

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.maps.commonMethod.CommonMethod
import com.example.maps.commonMethod.GPSTracker
import com.google.android.gms.maps.model.LatLng
import io.grpc.Context
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class MapViewModel:ViewModel() {
    private val TAG: String = MapViewModel::class.java.simpleName
    var coordinatesLiveDataList: MutableLiveData<MutableList<LatLng>> = MutableLiveData()
    private var gpsTracker: GPSTracker? = null
    private val coordinatesList = mutableListOf<LatLng>()
    var coordinates: Coordinates? = null

    fun getCurrentLocation(context: android.content.Context) {
        Completable.fromAction {
            gpsTracker = GPSTracker(context)
            if (gpsTracker?.isGPSTrackingEnabled!!) {
                coordinates = CommonMethod.getLocation(context)

            } else {
                gpsTracker!!.showSettingsAlert()
                // Navigation.findNavController(fragmentWeatherBinding.root).navigate(R.id.action_weather_to_category)
            }
        }
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe(object : CompletableObserver {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onComplete() {
                    val currentLocation = LatLng(coordinates!!.latitude, coordinates!!.longitude)
                    coordinatesList.add(currentLocation)
                    coordinatesLiveDataList.postValue(coordinatesList)
                    //plotPoints(coordinatesList)
                }

                override fun onError(e: Throwable) {
                    Log.d("TAG", "Error in location ${e.message}")
                }
            })

    }}