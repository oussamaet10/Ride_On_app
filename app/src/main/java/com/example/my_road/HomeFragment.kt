package com.example.my_road

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng

class HomeFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var playPauseButton: Button
    private lateinit var showLocationButton: ImageButton
    private lateinit var distanceTimeInfoTextView: TextView
    private lateinit var totalFareTextView: TextView

    private var isPlaying = false
    private var totalFare: Double = 0.0

    private val baseFare: Double = 2.5
    private val farePerKm: Double = 1.5
    private val farePerMinute: Double = 0.5

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001


    private var startLocation: Location? = null
    private var totalDistance = 0.0 // in meters
    private var rideStartTime: Long = 0L
    private var handler: android.os.Handler = android.os.Handler()

    private var currentMarker: com.google.android.gms.maps.model.Marker? = null

    private var startTime: Long = 0

    private var previousLatLng: LatLng? = null







    //---------------------------------------


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.home_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createNotificationChannel()

        val mapFragment = childFragmentManager.findFragmentById(R.id.fragment_container) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        playPauseButton = view.findViewById(R.id.playPauseButton)
        showLocationButton = view.findViewById(R.id.showLocationButton)
        distanceTimeInfoTextView = view.findViewById(R.id.distanceTimeInfo)
        totalFareTextView = view.findViewById(R.id.totalFare)

        playPauseButton.setOnClickListener { togglePlayPause() }
        showLocationButton.setOnClickListener { locateMyLocation() }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }


    //---------------------------------REQST

    private fun checkAndRequestPermissions() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            locateMyLocation() // If permission is already granted, proceed with the location
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                locateMyLocation()
            } else {
                // Permission denied
                Toast.makeText(requireContext(), "Permission denied.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //---------------------------------

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
    }

    private fun togglePlayPause() {
        if (isPlaying) {
            isPlaying = false
            playPauseButton.text = "Start Ride"
            stopRide()
        } else {
            isPlaying = true
            playPauseButton.text = "Pause Ride"
            startRide()
        }
    }






    private fun startRide() {
        Toast.makeText(requireContext(), "Ride Started", Toast.LENGTH_SHORT).show()
        startTime = System.currentTimeMillis()
        totalDistance = 0.0
        startLocation = null
        startUpdatingRide()
    }

    private fun stopRide() {
        Toast.makeText(requireContext(), "Ride Stopped", Toast.LENGTH_SHORT).show()
        handler.removeCallbacksAndMessages(null)

        sendRideNotification()
    }

    private fun startUpdatingRide() {
        handler.post(object : Runnable {
            override fun run() {
                updateRideStats() // Update stats (distance, time)
                handler.postDelayed(this, 5000) // Update every 1 second
            }
        })
    }


    @SuppressLint("SetTextI18n")
    private fun updateRideStats() {
        // Check if the location permission is granted
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // If permission is not granted, request it
            checkAndRequestPermissions()
            return
        }

        // Location permission granted, proceed with updating ride stats
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)

                if (previousLatLng != null) {
                    // Calculate the distance between the previous and current location
                    val distance = calculateDistance(previousLatLng!!, currentLatLng)

                    // Update the total distance traveled
                    totalDistance += distance // Accumulate the distance

                    // Calculate the time elapsed
                    val timeElapsed = calculateTimeElapsed(startTime)

                    // Update the stats on the UI
                    updateDistanceAndTime(totalDistance, timeElapsed)

                    // Calculate the fare based on distance and time
                    calculateFare(totalDistance, timeElapsed)
                } else {
                    // First time location is fetched, initialize the previousLatLng
                    previousLatLng = currentLatLng
                }

                // Set the current location as the previous location for next calculation
                previousLatLng = currentLatLng
            } else {
                Toast.makeText(requireContext(), "Failed to get location", Toast.LENGTH_SHORT).show()
            }
        }
    }


    //--------------Calculate

    private fun calculateDistance(start: LatLng, end: LatLng): Double {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            start.latitude, start.longitude,
            end.latitude, end.longitude,
            results
        )
        return results[0].toDouble() // Return the distance in meters
    }

    private fun calculateTimeElapsed(startTime: Long): Int {
        val elapsedTime = System.currentTimeMillis() - startTime
        return (elapsedTime / 1000).toInt() // Convert to seconds
    }

    private fun calculateFare(distance: Double, timeElapsed: Int) {
        // Base fare, rate per kilometer, and rate per minute
        val baseFare = baseFare
        val farePerKm = farePerKm
        val farePerMinute = farePerMinute

        // Calculate fare based on distance and time
        val fare = baseFare + (farePerKm * distance / 1000) + (farePerMinute * timeElapsed / 60)

        // Update the UI with the total fare
        totalFareTextView.text = "Total Fare: ${"%.2f".format(fare)} DH"
    }


    //-----------------------------

    private fun updateDistanceAndTime(distance: Double, timeElapsed: Int) {
        val distanceText = "Distance: ${"%.2f".format(distance / 1000)} km"
        val timeText = "Time: ${timeElapsed / 60} min ${timeElapsed % 60} sec"

        distanceTimeInfoTextView.text = "$distanceText\n$timeText"
    }





    private fun updateMarkerPosition(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        if (currentMarker == null) {
            currentMarker = mMap.addMarker(
                com.google.android.gms.maps.model.MarkerOptions().position(latLng).title("Your Location")
            )
        } else {
            currentMarker?.position = latLng
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
    }

    private fun locateMyLocation() {
        if (::mMap.isInitialized) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Location permission is granted
                mMap.isMyLocationEnabled = true

                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val currentLatLng = LatLng(location.latitude, location.longitude)
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                        updateMarkerPosition(location)  // This will move the marker on the map
                    }
                }
            } else {
                // Permission is not granted, request permission
                checkAndRequestPermissions()
            }
        } else {
            Log.e("HomeFragment", "Google Map is not initialized yet.")
        }
    }


    //-------------------------NOTIF

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "ride_notification_channel"
            val channelName = "Ride Notification"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Channel for ride notifications"
            }

            val notificationManager = requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    private fun sendRideNotification() {
        // Calculate time in minutes and seconds
        val totalTimeElapsed = calculateTimeElapsed(startTime)
        val totalMinutes = totalTimeElapsed / 60
        val totalSeconds = totalTimeElapsed % 60

        val totalDistanceInKm = totalDistance / 1000 // Convert meters to kilometers

        // Calculate the fare based on the total distance and time
        val fare = baseFare + (farePerKm * totalDistanceInKm) + (farePerMinute * totalTimeElapsed / 60)

        // Create the notification
        val notificationManager = requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = 1
        val channelId = "ride_notification_channel"

        val notification = NotificationCompat.Builder(requireContext(), channelId)
            .setSmallIcon(R.drawable.ic_ride_notification) // Icon for the notification
            .setContentTitle("Ride Paused")
            .setContentText("Distance: ${"%.2f".format(totalDistanceInKm)} km, Time: $totalMinutes min $totalSeconds sec, Fare: ${"%.2f".format(fare)} DH")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        // Show the notification
        notificationManager.notify(notificationId, notification)
    }





}
