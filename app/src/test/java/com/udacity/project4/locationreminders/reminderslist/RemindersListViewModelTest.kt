package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    //COMPLETED: provide testing to the RemindersListViewModel and its live data objects
    private lateinit var  remindersListViewModel : RemindersListViewModel
    private lateinit var fakeDataSource: FakeDataSource

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupRemindersListViewModel() {
        stopKoin()
        // We initialise the repository with no reminders
        fakeDataSource = FakeDataSource()
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(),fakeDataSource)
    }

    @Test
    fun loadReminders_loading(){
        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showLoading.value, `is`(true))
        mainCoroutineRule.resumeDispatcher()
        assertThat(remindersListViewModel.showLoading.value, `is`(false))
    }

    @Test
    fun loadReminders() = runBlocking{
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.remindersList.value, `is` (not(nullValue())))
    }


    @Test
    fun getReminder() = runBlocking {
        remindersListViewModel.loadReminders()
        val reminder = fakeDataSource.getFirstReminder()
        assertThat(remindersListViewModel.getReminder(reminder.id), `is`(not(nullValue())))     // verify that getReminder() works as expected

    }

    @Test
    fun deleteReminder() = runBlocking{
        remindersListViewModel.loadReminders()
        val reminder = fakeDataSource.getFirstReminder()
        remindersListViewModel.deleteReminder(reminder)
        assertThat(remindersListViewModel.getReminder(reminder.id), `is`(nullValue()))          // verify that the reminder is deleted
        assertThat(remindersListViewModel.remindersList.value!!.count(), `is`(2))           // verify that only 2 elements are left
    }




}