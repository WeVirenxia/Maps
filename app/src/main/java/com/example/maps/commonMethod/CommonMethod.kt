package com.example.maps.commonMethod

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.util.Log
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import com.example.maps.Coordinates
import com.example.maps.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.core.View

class CommonMethod {

    companion object {
        private val TAG = CommonMethod::class.java.simpleName

        fun getLocation(context: Context): Coordinates {
            var gpsTracker: GPSTracker? = null
            val coordinates = Coordinates()
            gpsTracker = GPSTracker(context)
            if (gpsTracker.isGPSTrackingEnabled) {
                coordinates.latitude = gpsTracker.getCurrentLatitude()
                coordinates.longitude = gpsTracker.getCurrentLongitude()
                coordinates.address = gpsTracker.getAddressLine(context).toString()
                val postalCode = gpsTracker.getPostalCode(context)
                val country = gpsTracker.getCountryName(context)
                Log.d("TAG","Latitude:${coordinates.latitude} & Longitude:${coordinates.longitude}")
                println("Latitude:${coordinates.latitude} & Longitude:${coordinates.longitude}")
                //weatherViewModel.clearResultSet()
                //weatherViewModel.getWeatherDetail(latitude,longitude)

            }
            return coordinates
        }


        fun isNetworkAvailable(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }

        fun isNetworkConnected(context: Context): Boolean {
            return if (isNetworkAvailable(context)) {
                true
            } else {
                showAlert(context)
                false
            }
        }

        private fun showAlert(context: Context?) {
            val alertDialog = AlertDialog.Builder(context)

            //Setting Dialog Title
            alertDialog.setTitle("Internet Permission")

            //Setting Dialog Message
            alertDialog.setMessage("Please Turn on Internet Connection")

            //On pressing cancel button
            alertDialog.setNegativeButton("OK") { dialog, _ -> dialog.cancel() }
            alertDialog.show()
        }

         fun showSnackBar(view: android.view.View,message:String){
            val snackbar= Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
            val snackbarView=snackbar.view
            val params = snackbarView.layoutParams as FrameLayout.LayoutParams
            params.gravity = Gravity.TOP
            snackbarView.layoutParams = params
            val textView =
                snackbarView.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
            textView.setTextColor(Color.BLACK)
            snackbarView.setBackgroundResource(R.color.cardBackground)
            snackbar.show()
        }
    }
}