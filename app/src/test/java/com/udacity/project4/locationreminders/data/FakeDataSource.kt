package com.udacity.project4.locationreminders.data

import android.content.res.Resources

import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import java.util.*
import kotlin.collections.ArrayList

//import com.udacity.project4.locationreminders.data.ReminderDataSource.

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {
    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

//    COMPLETED: Create a fake data source to act as a double to the real data source
    private val reminder1 = ReminderDTO("Udacity","Udacity office location from google","Udacity headquarters",37.39923655968083, -122.10775103357292,
    UUID.randomUUID().toString())

    private val reminder2 = ReminderDTO("Google","Google office location from google","Google headquarters",37.422102367165316, -122.08411992451298,
        UUID.randomUUID().toString())

    private val reminder3 = ReminderDTO("Parthenon","Parthenon, Athens, Greece","Athens",37.971594568852474, 23.726589697854397,
        UUID.randomUUID().toString())

    private val localReminders = mutableListOf<ReminderDTO>(reminder1,reminder2,reminder3)

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
     //   COMPLETED ("Return the reminders")
        if (shouldReturnError) {
            return Result.Error("Test exception - getReminders") // there is no need for localizing a test message so it is OK to hardcode it.
        }

        if (localReminders.isEmpty()) {
            return Result.Error(Resources.getSystem().getString(R.string.no_reminders))
        } else {
            return Result.Success(localReminders)
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
      //  COMPLETED ("save the reminder")
        localReminders.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        //  COMPLETED ("return the reminder with the id")
        if(shouldReturnError) {
            return Result.Error( "Test exception - getReminder")
        }

        val result = localReminders.firstOrNull{it.id == id}
            result?.let {
            return Result.Success(it)
        }
        return Result.Error(
            "Reminder with id $id is not available"
        )
    }

    override suspend fun deleteAllReminders() {
        //  COMPLETED ("delete all the reminders")
        localReminders.clear()
    }

    override suspend fun deleteReminderWithId(reminderID: String) {
        for (reminder in localReminders) {
            if (reminder.id == reminderID) {
                localReminders.remove(reminder)
                return
            }
        }
    }

    fun getFirstReminder() : ReminderDataItem {
        val reminderDTO = localReminders.first()
        return (ReminderDataItem(reminderDTO.title, reminderDTO.description,reminderDTO.location, reminderDTO.latitude, reminderDTO.longitude, reminderDTO.id))
    }

}