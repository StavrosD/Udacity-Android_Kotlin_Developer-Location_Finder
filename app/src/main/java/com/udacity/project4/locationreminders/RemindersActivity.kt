package com.udacity.project4.locationreminders
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.navigation.fragment.NavHostFragment
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityRemindersBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {
    lateinit var binding : ActivityRemindersBinding
    private val _viewModel by viewModel<SaveReminderViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRemindersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val navHostFragment = supportFragmentManager
                    .findFragmentById(R.id.nav_host_fragment) as NavHostFragment


                navHostFragment.navController.popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        requireLocationEnabled()
    }

    private fun requireLocationEnabled(){
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gps_enabled = false
        var network_enabled = false

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(ex : Exception) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(ex : Exception) {}

        if(!gps_enabled && !network_enabled) {
            // notify user
            AlertDialog.Builder(this)
                .setMessage(R.string.location_disabled)
                .setPositiveButton(R.string.open_location_settings) {_,_->
                    startActivity( Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
                .setNegativeButton(R.string.cancel){_,_->
                    Toast.makeText(this,R.string.location_enabled_required, Toast.LENGTH_LONG).show()
                    finish()
                }
                .show();
        }
    }
}
