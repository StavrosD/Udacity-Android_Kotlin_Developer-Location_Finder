// Modified 2022 Stavros Dimopoulos
// This is a customized version of the original code
// https://github.com/googlemaps/android-samples/blob/main/ApiDemos/kotlin/app/src/v3/java/com/example/kotlindemos/PermissionUtils.kt

// Copyright 2020 Google LLC
// Modified 2022 Stavros Dimopoulos
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.udacity.project4.utils

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import com.udacity.project4.R

/**
 * Utility class for access to runtime permissions.
 */
object PermissionUtils {
    /**
     * Requests the fine location permission. If a rationale with an additional explanation should
     * be shown to the user, displays a dialog that triggers the request.
     */

    private var toastMessageId = 0
    @JvmStatic
    fun requestPermission(
        activity: AppCompatActivity,
        requestId: Int,
        permission: String,
        rationaleMessageId: Int,
        toastMessageId: Int,
        finishActivity: Boolean,
    ) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            // Display a dialog with rationale.
            RationaleDialog.newInstance(requestId, finishActivity, rationaleMessageId, toastMessageId  , permission)
                .show(activity.supportFragmentManager, "dialog")
        } else {
            // Permission has not been granted yet, request it.
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(permission),
                requestId
            )
        }
    }

    /**
     * Checks if the result contains a [PackageMGFanager.PERMISSION_GRANTED] result for a
     * permission from a runtime permissions requFFest.
     *
     * @see androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
     */
    @JvmStatic
    fun isPermissionGranted(
        grantPermissions: Array<String>, grantResults: IntArray,
        permission: String
    ): Boolean {
        for (i in grantPermissions.indices) {
            if (permission == grantPermissions[i]) {
                return grantResults[i] == PackageManager.PERMISSION_GRANTED
            }
        }
        return false
    }

    /**
     * A dialog that displays a permission denied message.
     */
    class PermissionDeniedDialog(
        private val permissionDeniedMessageId:Int,
        private val permissionRequiredToastId:Int
    ) : DialogFragment() {
        private var finishActivity = false
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            finishActivity =
                arguments?.getBoolean(ARGUMENT_FINISH_ACTIVITY) ?: false
            return AlertDialog.Builder(activity)
                .setMessage(permissionDeniedMessageId)
                .setPositiveButton(android.R.string.ok, null)
                .create()

        }

        override fun onDismiss(dialog: DialogInterface) {
            super.onDismiss(dialog)
            if (finishActivity) {
                arguments?.getInt("rationale")?.let {
                    Toast.makeText(
                        activity,
                        it,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                activity?.finish()
            }
        }

        companion object {
            private const val ARGUMENT_FINISH_ACTIVITY = "finish"

            /**
             * Creates a new instance of this dialog and optionally finishes the calling Activity
             * when the 'Ok' button is clicked.
             */
            @JvmStatic
            fun newInstance(finishActivity: Boolean,permissionDeniedMessageId:Int, permissionRequiredToastId:Int): PermissionDeniedDialog {
                val arguments = Bundle().apply {
                    putBoolean(ARGUMENT_FINISH_ACTIVITY, finishActivity)
                }
                return PermissionDeniedDialog(permissionDeniedMessageId, permissionRequiredToastId).apply {
                    this.arguments = arguments
                }
            }
        }
    }

    /**
     * A dialog that explains the use of the location permission and requests the necessary
     * permission.
     *
     *
     * The activity should implement
     * [androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback]
     * to handle permit or denial of this permission request.
     */
    class RationaleDialog : DialogFragment() {
        private var finishActivity = false
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val requestCode =
                arguments?.getInt(ARGUMENT_PERMISSION_REQUEST_CODE) ?: 0
            finishActivity =
                arguments?.getBoolean(ARGUMENT_FINISH_ACTIVITY) ?: false
            return AlertDialog.Builder(activity)
                .setMessage(arguments?.getInt(ARGUMENT_RATIONALE_MESSAGE_ID)!!)
                .setPositiveButton(android.R.string.ok) { dialog, which -> // After click on Ok, request the permission.
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(arguments?.getString(ARGUMENT_PERMISSION)!!),
                        requestCode
                    )
                    // Do not finish the Activity while requesting permission.
                    finishActivity = false
                }
                .setNegativeButton(android.R.string.cancel, null)
                .create()

        }

        override fun onDismiss(dialog: DialogInterface) {
            super.onDismiss(dialog)

            if (finishActivity) {
                arguments?.getInt(ARGUMENT_TOAST_MESSAGE_ID)?.let {
                    Toast.makeText(
                        activity,
                        it,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                activity?.finish()
            }
        }

        companion object {
            private const val ARGUMENT_PERMISSION_REQUEST_CODE = "requestCode"
            private const val ARGUMENT_FINISH_ACTIVITY = "finish"
            private const val ARGUMENT_RATIONALE_MESSAGE_ID = "rationale"
            private const val ARGUMENT_PERMISSION = "permission"
            private const val ARGUMENT_TOAST_MESSAGE_ID = "toast"
            /**
             * Creates a new instance of a dialog displaying the rationale for the use of the location
             * permission.
             *
             *
             * The permission is requested after clicking 'ok'.
             *
             * @param requestCode    Id of the request that is used to request the permission. It is
             * returned to the
             * [androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback].
             * @param finishActivity Whether the calling Activity should be finished if the dialog is
             * cancelled.
             */
            fun newInstance(requestCode: Int, finishActivity: Boolean, rationaleMessageId:Int, toastMessageId:Int, permission: String): RationaleDialog {
                val arguments = Bundle().apply {
                    putInt(ARGUMENT_PERMISSION_REQUEST_CODE, requestCode)
                    putBoolean(ARGUMENT_FINISH_ACTIVITY, finishActivity)
                    putInt(ARGUMENT_RATIONALE_MESSAGE_ID,rationaleMessageId)
                    putInt(ARGUMENT_TOAST_MESSAGE_ID,toastMessageId)
                    putString(ARGUMENT_PERMISSION,permission)
                }
                return RationaleDialog().apply {
                    this.arguments = arguments
                }
            }
        }
    }
}