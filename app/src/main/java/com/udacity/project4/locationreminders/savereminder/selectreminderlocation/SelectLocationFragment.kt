package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.*


class SelectLocationFragment : BaseFragment(),OnMapReadyCallback {
    private val TAG = "SelectLocationFragment"

    private var permissionDenied = false
    private var locationDisabled = false

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by sharedViewModel()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map : GoogleMap
    private var poi : PointOfInterest? = null
    private lateinit var fusedLocationProviderClient : FusedLocationProviderClient
    private val defaultZoom = 15
    private var returnFromEnableLocationSetting = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        // setHasOptionsMenu(true)
        // Add menu items without overriding methods in the Activity
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.map_options, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                when (menuItem.itemId) {
                    // COMPLETED: Change the map type based on the user's selection.
                    R.id.normal_map -> {
                        map.mapType = GoogleMap.MAP_TYPE_NORMAL
                    }
                    R.id.hybrid_map -> {
                        map.mapType = GoogleMap.MAP_TYPE_HYBRID
                    }
                    R.id.satellite_map -> {
                        map.mapType = GoogleMap.MAP_TYPE_SATELLITE
                    }
                    R.id.terrain_map -> {
                        map.mapType = GoogleMap.MAP_TYPE_TERRAIN
                    }
                }
                return true
            }
        },this.viewLifecycleOwner)


        setDisplayHomeAsUpEnabled(true)

//        COMPLETED: add the map setup implementation
        val mapFragment : SupportMapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

//        COMPLETED: zoom to the user location after taking his permission
        // implemented in requestPermissionLauncher, if the location permission is granted

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

        poi?.let {
            _viewModel.longitude.postValue(poi!!.latLng.longitude)
            _viewModel.latitude.postValue(poi!!.latLng.latitude)
            _viewModel.reminderSelectedLocationStr.postValue(poi!!.name)
            _viewModel.selectedPOI.postValue(poi)
        }
        _viewModel.navigationCommand.postValue(NavigationCommand.Back)
    }

    override fun onMapReady(p0: GoogleMap) {
    //        COMPLETED("Not yet implemented")
        map = p0
        setMapStyle(map)
        setMapLongClick(map)
        setPOIClick(map)
        setOnMyLocationButtonClick(map)   // if location service is disabled is disabled, ask the user if he wants to enable it.

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
        checkFineLocationPermission()
    }
    @SuppressLint("MissingPermission")
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission())
        { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
                permissionDenied = false
                map.isMyLocationEnabled = true
                getDeviceLocation()
            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
                _viewModel.showErrorMessage.postValue(getString(R.string.location_permission_denied))
                permissionDenied = true
                map.isMyLocationEnabled = false
            }
        }
    @SuppressLint("MissingPermission")
    private fun checkFineLocationPermission() {
        if (!::map.isInitialized) return
        // [START maps_check_location_permission]
        when {
            ContextCompat.checkSelfPermission(requireContext(), REQUIRED_PERMISSION) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
                permissionDenied = false
                map.isMyLocationEnabled = true
            }

            shouldShowRequestPermissionRationale(REQUIRED_PERMISSION) -> {
            // In an educational UI, explain to the user why your app requires this
            // permission for a specific feature to behave as expected. In this UI,
            // include a "cancel" or "no thanks" button that allows the user to
            // continue using your app without granting the permission.
            AlertDialog.Builder(requireContext())
                .setMessage(getString(R.string.permission_rationale_location))
                .setPositiveButton(android.R.string.ok) { _, _ -> requestPermissionLauncher.launch(REQUIRED_PERMISSION) }
                .setNegativeButton(android.R.string.cancel) { _, _ ->
                    _viewModel.showErrorMessage.postValue(getString(R.string.permission_rationale_location))
                    permissionDenied = true
                    map.isMyLocationEnabled = false
                }
                .create().show()
                }

            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(REQUIRED_PERMISSION)
            }
        }
        // [END maps_check_location_permission]
    }


    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        /*

         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */


        // val locationEnabled = !permissionDenied && !locationDisabled
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
            _viewModel.showErrorMessage.postValue(e.localizedMessage)
            Log.e("Exception: %s", e.message, e)
        }
    }

    var checkIfLocationServiceIsEnabledActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){_ ->
        checkIfLocationEnabled(false)
    }


    @SuppressLint("MissingPermission")
    private fun checkIfLocationEnabled(showLocationNeededDialog:Boolean = true){
        val lm = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gps_enabled = false
        var network_enabled = false

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch(ex : Exception) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch(ex : Exception) {}

        if(!gps_enabled && !network_enabled) {
            // notify user
            val errorMessage = getString(R.string.location_enabled_required)
            locationDisabled = true
            if (showLocationNeededDialog) {
                android.app.AlertDialog.Builder(requireContext())
                    .setMessage(R.string.map_location_disabled)
                    .setPositiveButton(R.string.open_location_settings) { _, _ ->
                        checkIfLocationServiceIsEnabledActivityResultLauncher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                    .setNegativeButton(R.string.cancel) { _, _ ->
                        _viewModel.showSnackBar.value = errorMessage
                    }
                    .show()
            }
        } else {
            if (permissionDenied.not()) getDeviceLocation()
            locationDisabled = false
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

    private fun setOnMyLocationButtonClick(map:GoogleMap) {
        map.setOnMyLocationButtonClickListener() {
            checkIfLocationEnabled(false)
            if (locationDisabled) {
                checkIfLocationEnabled()
                return@setOnMyLocationButtonClickListener true
            }
            return@setOnMyLocationButtonClickListener false
        }
    }

    companion object {
        /**
         * Request code for location permission request.
         *
         * @see .onRequestPermissionsResult
         */
        private const val REQUIRED_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION
    }
}
