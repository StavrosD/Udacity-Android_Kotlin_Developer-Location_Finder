package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.room.Room.inMemoryDatabaseBuilder
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

//    COMPLETED: Add testing implementation to the RemindersLocalRepository.kt

    private lateinit var remindersLocalRepository : RemindersLocalRepository
    private lateinit var remindersDatabase : RemindersDatabase

    // Store the values in variables to prevent any possible error caused by spelling error in test
    private val title = "title"
    private val description = "description"
    private val location = "location"
    private val latitude = 10.1
    private val longitude = 20.2

    // Executes each reminder synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        // using an in-memory database for testing, since it doesn't survive killing the process
        remindersDatabase = inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        remindersLocalRepository =
            RemindersLocalRepository(
                remindersDatabase.reminderDao(),
                Dispatchers.Main
            )
    }

    @After
    fun cleanUp() {
        remindersDatabase.close()
    }

    // runBlocking used here because of https://github.com/Kotlin/kotlinx.coroutines/issues/1204
    // TODO replace with runBlockingTest once issue is resolved
    @Test
    fun saveReminder_retrievesReminder() = runBlocking {
        // GIVEN - a new reminder saved in the database
        val newReminder = ReminderDTO(title,description,location,latitude,longitude)
        remindersLocalRepository.saveReminder(newReminder)

        // WHEN  - Reminder retrieved by ID
        val result = remindersLocalRepository.getReminder(newReminder.id)

        // THEN - Same reminder is returned
        assertThat(result is Result.Success, `is`(true))
        result as Result.Success
        assertThat(result.data.title, `is`(title))
        assertThat(result.data.description, `is`(description))
        assertThat(result.data.location, `is`(location))
        assertThat(result.data.latitude, `is`(latitude))
        assertThat(result.data.longitude, `is`(longitude))

    }

    @Test
    fun deleteAllReminders_getReminderById() = runBlocking {
        // Given a new reminder in the persistent repository
        val newReminder = ReminderDTO(title,description,location,latitude,longitude)
        remindersLocalRepository.saveReminder(newReminder)
        remindersLocalRepository.deleteAllReminders()
        val result = remindersLocalRepository.getReminder(newReminder.id)

        // Then the reminder should not be retrieved from the persistent repository
        assertThat(result is Result.Error, `is`(true))
        result as Result.Error
        assertThat(result.message, `is`("Reminder not found"))
    }

    @Test
    fun deleteReminderWithId_getReminderbyId() = runBlocking {
        val newReminder = ReminderDTO(title,description,location,latitude,longitude)
        remindersLocalRepository.saveReminder(newReminder)
        remindersLocalRepository.deleteReminderWithId(newReminder.id)
        val result = remindersLocalRepository.getReminder(newReminder.id)

        // Then the reminder should not be retrieved from the persistent repository
        assertThat(result is Result.Error, `is`(true))
        result as Result.Error
        assertThat(result.message, `is`("Reminder not found"))
    }

}