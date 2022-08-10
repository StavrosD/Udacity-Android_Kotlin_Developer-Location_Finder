package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*
import com.udacity.project4.utils.PermissionUtils.PermissionDeniedDialog.Companion.newInstance
import com.udacity.project4.utils.PermissionUtils.isPermissionGranted
import com.udacity.project4.utils.PermissionUtils.requestPermission


class SelectLocationFragment : BaseFragment(),OnMapReadyCallback,
    ActivityCompat.OnRequestPermissionsResultCallback {
    private val TAG = "SelectLocationFragment"

    private var permissionDenied = false
    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map : GoogleMap
    private var poi : PointOfInterest? = null
    private lateinit var fusedLocationProviderClient : FusedLocationProviderClient
    private val defaultZoom = 15

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

//        COMPLETED: add the map setup implementation
        val mapFragment : SupportMapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

//        COMPLETED: zoom to the user location after taking his permission

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())


//      COMPLETED: add style to the map
//      style set inside onMapReady function


//      COMPLETED: put a marker to location that the user selected
//      market set inside onMapReady function

//        COMPLETED: call this function after the user confirms on the selected location
       binding.saveLocation.setOnClickListener {
           onLocationSelected()
       }
        return binding.root
    }

    private fun onLocationSelected() {
        //        COMPLETED: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
        _viewModel.longitude.postValue(poi!!.latLng.longitude)
        _viewModel.latitude.postValue(poi!!.latLng.latitude)
        _viewModel.reminderSelectedLocationStr.postValue(poi!!.name)
        _viewModel.navigationCommand.postValue(NavigationCommand.Back)
        _viewModel.selectedPOI.postValue(poi)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // COMPLETED: Change the map type based on the user's selection.
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(p0: GoogleMap) {
    //        COMPLETED("Not yet implemented")
        map = p0
        setMapStyle(map)
        setMapLongClick(map)
        setPOIClick(map)

        // if the user reopens the view, display the selected point
        _viewModel.selectedPOI.value?.let {
                map.addMarker(
                    MarkerOptions()
                        .position(it.latLng)
                        .title(it.name)
                        .snippet(it.name)
                )
                binding.saveLocation.isEnabled = true
        }
        enableMyLocation()
        if (!permissionDenied) {
            getDeviceLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (!::map.isInitialized) return
        // [START maps_check_location_permission]
        if (ContextCompat.checkSelfPermission(this.requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            requestPermission(
                requireActivity() as AppCompatActivity,
                LOCATION_PERMISSION_REQUEST_CODE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                R.string.permission_rationale_location,
                R.string.permission_location_required_toast,
                true)
        }
        // [END maps_check_location_permission]
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }
        if (isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation()
        } else {
            // Permission was denied. Display an error message
            // [START_EXCLUDE]
            // Display the missing permission error dialog when the fragments resume.
            _viewModel.showErrorMessage.postValue(R.string.location_permission_denied.toString())
            permissionDenied = true
            // [END_EXCLUDE]
        }
    }

    override fun onResume() {
        super.onResume()
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError()
            permissionDenied=false
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private fun showMissingPermissionError() {
        newInstance(true,R.string.location_permission_denied, R.string.permission_location_required_toast).show(childFragmentManager, "dialog")
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        /*

         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (!permissionDenied) {

                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        val lastKnownLocation = task.result

                        if (lastKnownLocation != null) {
                            map.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(lastKnownLocation.latitude,
                                        lastKnownLocation.longitude), defaultZoom.toFloat()))
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception: %s", task.exception)
                         val defaultLocation = LatLng(0.0, 0.0)
                        map.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(defaultLocation, defaultZoom.toFloat()))
                        map.uiSettings.isMyLocationButtonEnabled = false
                    }

                    // move to selected location, if reopening the map
                    poi.let {
                        if (it != null) {
                            map.moveCamera(
                                CameraUpdateFactory
                                    .newLatLngZoom(it.latLng, defaultZoom.toFloat()))
                        }
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireActivity(),
                    R.raw.map_style
                )
            )
            if (!success) {
                Log.e(tag, "Could not apply map style")
            }
        } catch (e:Exception) {
            Log.e(tag, "Exception: ${e.message}")
        }
    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            // A Snippet is Additional text that's displayed below the title.
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )
            val locationStr : String

            val geoCoder = Geocoder(context, Locale.getDefault())
            locationStr = try {
                geoCoder.getFromLocation(
                    latLng.latitude,
                    latLng.longitude,
                    1
                )[0].getAddressLine(0)
            } catch (e: Exception) {
                // geoCoder did not return a feature name.
                getString(R.string.dropped_pin)
            }
            poi = PointOfInterest(latLng,"1", locationStr)
            map.clear()
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
            )
            binding.saveLocation.isEnabled = true
        }
    }

    private fun setPOIClick(map:GoogleMap){
        map.setOnPoiClickListener {
            map.clear()
            poi = it
            map.clear()
            map.addMarker(
                MarkerOptions()
                    .position(it.latLng)
                    .title(it.name)
                    .snippet(it.name)
            )?.showInfoWindow()

            binding.saveLocation.isEnabled = true
        }
    }

    companion object {
        /**
         * Request code for location permission request.
         *
         * @see .onRequestPermissionsResult
         */
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}
