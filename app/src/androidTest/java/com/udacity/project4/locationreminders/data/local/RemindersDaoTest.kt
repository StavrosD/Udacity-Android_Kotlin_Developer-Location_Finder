package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {
    //    COMPLETED: Add testing implementation to the RemindersDao.kt

    private lateinit var database: RemindersDatabase

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb(){
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun insertReminderAndGetById() = runTest{
        val reminder = ReminderDTO("Title 1","Description 1","13.000",24.99,22.5)
        database.reminderDao().saveReminder(reminder)
        val loaded = database.reminderDao().getReminderById(reminder.id)
        assertThat(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.title, `is` (reminder.title))
        assertThat(loaded.description, `is` (reminder.description))
        assertThat(loaded.id, `is` (reminder.id))
        assertThat(loaded.latitude, `is` (reminder.latitude))
        assertThat(loaded.longitude, `is` (reminder.longitude))
        assertThat(loaded.location, `is` (reminder.location))
    }

    @Test
    fun removeReminderWithId() = runTest {
        val reminder = ReminderDTO("Title 1","Description 1","13.000",24.99,22.5)
        database.reminderDao().saveReminder(reminder)
        database.reminderDao().deleteReminderWithId(reminder.id)
        val loaded = database.reminderDao().getReminderById(reminder.id)
        assertThat(loaded, `is` (nullValue()))
    }


}