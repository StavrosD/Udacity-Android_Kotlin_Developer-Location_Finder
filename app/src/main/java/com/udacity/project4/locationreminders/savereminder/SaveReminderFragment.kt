package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.geofence.GeofencingConstants.GEOFENCE_EXPIRATION_IN_MILLISECONDS
import com.udacity.project4.locationreminders.geofence.GeofencingConstants.GEOFENCE_RADIUS_IN_METERS
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.PermissionUtils.PermissionDeniedDialog.Companion.newInstance
import com.udacity.project4.utils.PermissionUtils.isPermissionGranted
import com.udacity.project4.utils.PermissionUtils.requestPermission
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject


class SaveReminderFragment : BaseFragment(), ActivityCompat.OnRequestPermissionsResultCallback {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient


    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireActivity(), GeofenceBroadcastReceiver::class.java)
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        PendingIntent.getBroadcast(requireActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        geofencingClient = LocationServices.getGeofencingClient(requireActivity())
        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (_viewModel.geofencePremissionDenied.value == true) enableGeofences()
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }


        enableGeofences()
        binding.saveReminder.setOnClickListener {


            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude
            val longitude = _viewModel.longitude.value

//             COMPLETED: use the user entered reminder details to:
//             1) add a geofencing request
//             2) save the reminder to the local db


            val reminderDataItem = ReminderDataItem(title, description.value ,location,latitude.value,longitude)
            _viewModel.validateAndSaveReminder(reminderDataItem)
            if (_viewModel.validateEnteredData(reminderDataItem)) {
                val geofence = Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId(reminderDataItem.id)
                    // Set the circular region of this geofence.
                    .setCircularRegion(
                        reminderDataItem.latitude!!,
                        reminderDataItem.longitude!!,
                        GEOFENCE_RADIUS_IN_METERS
                    )
                    // Set the expiration duration of the geofence. This geofence gets automatically
                    // removed after this period of time.
                    .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                    // Set the transition types of interest. Alerts are only generated for these
                    // transition. We track entry and exit transitions in this sample.
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                    // Create the geofence.
                    .build()


                val geofencingRequest = GeofencingRequest.Builder().apply {
                    setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                    addGeofence(geofence)
                }.build()

                geofencingClient.addGeofences(geofencingRequest,geofencePendingIntent).run {
                    addOnSuccessListener {
                        lifecycleScope.launchWhenResumed{
                            _viewModel.showToast.postValue(resources.getString(R.string.geofence_added))
                        }
                    }
                    addOnFailureListener {
                        lifecycleScope.launchWhenResumed {
                            _viewModel.showErrorMessage.postValue("${getString(R.string.geofences_not_added)}\n${it.localizedMessage}")
                        }
                    }
                }
            }

        }
    }


    private fun enableGeofences() {
        // [START maps_check_location_permission]
        if (ContextCompat.checkSelfPermission(this.requireActivity(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_DENIED
        ) {
            // Permission to access the location is missing. Show rationale and request permission
            requestPermission(
                requireActivity() as AppCompatActivity,
                GEOFENCE_PERMISSION_REQUEST_CODE,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                R.string.permission_rationale_geofence,
                R.string.permission_geofence_required_toast,
                true)
        }
        // [END maps_check_location_permission]
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode != GEOFENCE_PERMISSION_REQUEST_CODE){
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }
        if (isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableGeofences()
        } else {
            // Permission was denied. Display an error message
            // [START_EXCLUDE]
            // Display the missing permission error dialog when the fragments resume.
            _viewModel.showErrorMessage.postValue(R.string.location_permission_denied.toString())
            _viewModel.geofencePremissionDenied.postValue(true)
            // [END_EXCLUDE]
        }
    }

    override fun onResume() {
        super.onResume()
        _viewModel.geofencePremissionDenied.value?.let {
            if (it) {
                showMissingPermissionError()
                _viewModel.geofencePremissionDenied.postValue(false)
            }
        }

    }

    private fun showMissingPermissionError() {
        newInstance(true, R.string.geofence_permission_denied, R.string.permission_geofence_required_toast).show(childFragmentManager, "dialog")
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }



    companion object {
        internal const val ACTION_GEOFENCE_EVENT = "SaveReminderFragment.savereminder.action.ACTION_GEOFENCE_EVENT"
        private const val GEOFENCE_PERMISSION_REQUEST_CODE = 2
    }
}
