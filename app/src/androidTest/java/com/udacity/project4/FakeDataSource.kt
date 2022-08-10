package com.udacity.project4

import android.content.res.Resources
import android.provider.Settings.Global.getString
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.local.RemindersDao
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import org.koin.core.component.getScopeName
import java.util.*
import kotlin.collections.ArrayList

//import com.udacity.project4.locationreminders.data.ReminderDataSource.

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

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
        if (localReminders.isEmpty()) {
            return Result.Error("There are no reminders") // no localization needed, hardcoded text is OK here
        } else {
            return Result.Success(ArrayList(localReminders))
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
      //  COMPLETED ("save the reminder")
        localReminders.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        //  COMPLETED ("return the reminder with the id")
        val result = localReminders.firstOrNull{it.id == id}
        result?.let {
            return Result.Success(result)
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