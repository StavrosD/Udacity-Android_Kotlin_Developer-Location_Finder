package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.view.View
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.locationreminders.ReminderDescriptionActivity
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorFragment
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.*
import org.junit.Before
import org.junit.Rule
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {
    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private val dataBindingIdlingResource = DataBindingIdlingResource()
    private lateinit var reminderViewModel: SaveReminderViewModel



    /*
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()

        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }

            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()
        reminderViewModel = get()
        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    //    COMPLETED: test the navigation of the fragments.
    @Test
    fun remindersListFragment_NavigateToReminderDescription()  {
        val reminder = ReminderDataItem("title", "description", "location", 11.1, 22.2)
        reminderViewModel.saveReminder(reminder)
        Intents.init()

        val scenario = launchFragmentInContainer<ReminderListFragment>()
        dataBindingIdlingResource.monitorFragment(scenario as FragmentScenario<Fragment>)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        onView(withId(R.id.remindersRecyclerView)).perform(
            RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText(reminder.title)), click()
            )
        )
        Intents.intended(hasComponent(ReminderDescriptionActivity::class.java.name))
    }

    @Test
    fun remindersListFragment_NavigateToSaveReminderFragment() {

        val scenario = launchFragmentInContainer<ReminderListFragment>()

        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        onView(withId(R.id.addReminderFAB)).perform(click())

        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }


    //    COMPLETED: test the dispΦΦlayed data on the UI.
    @Test
    fun remindersListFragment_DisplayedInUi() = runBlocking {
        val reminder = ReminderDTO("title", "description", "location", 11.1, 22.2)
        repository.deleteAllReminders()
        repository.saveReminder(reminder)
        launchFragmentInContainer<ReminderListFragment>()
        checkRecyclerSubViews(R.id.remindersRecyclerView, 0, withText(reminder.title), R.id.title)
        checkRecyclerSubViews(
            R.id.remindersRecyclerView,
            0,
            withText(reminder.description),
            R.id.description
        )
        checkRecyclerSubViews(
            R.id.remindersRecyclerView,
            0,
            withText(reminder.location),
            R.id.location
        )
    }


    //    COMPLETED: add testing for the error messages.

    @Test
    fun snackbar_noTitleError() = runBlocking {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        onView(withId(R.id.addReminderFAB)).perform(click())

        onView(withId(R.id.saveReminder)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.err_enter_title)))

        activityScenario.close()
    }

    @Test
    fun snackbar_noDescriptionError() = runBlocking {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        onView(withId(R.id.addReminderFAB)).perform(click())

        onView(withId(R.id.reminderTitle)).perform(typeText("Title"),closeSoftKeyboard())
        onView(withId(R.id.saveReminder)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.err_enter_description)))

        activityScenario.close()
    }

    @Test
    fun snackbar_noLocationError() = runBlocking {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        //Verify title missing snackbar
        onView(withId(R.id.addReminderFAB)).perform(click())

        onView(withId(R.id.reminderTitle)).perform(typeText("Title"),closeSoftKeyboard())
        onView(withId(R.id.reminderDescription)).perform(typeText("Description"),closeSoftKeyboard())
        onView(withId(R.id.saveReminder)).perform(click(), closeSoftKeyboard())
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.err_select_location)))

        activityScenario.close()
    }

    // https://stackoverflow.com/questions/31394569/how-to-assert-inside-a-recyclerview-in-espresso

    fun checkRecyclerSubViews(
        recyclerViewId: Int,
        position: Int,
        itemMatcher: Matcher<View?>?,
        subViewId: Int
    ) {
        onView(withId(recyclerViewId)).perform(scrollToPosition<RecyclerView.ViewHolder>(position))
            .check(matches(atPositionOnView(position, itemMatcher!!, subViewId)))
    }

    fun atPositionOnView(
        position: Int, itemMatcher: Matcher<View?>, targetViewId: Int
    ): Matcher<View?> {
        return object : BoundedMatcher<View?, RecyclerView>(RecyclerView::class.java) {
            override fun describeTo(description: Description) {
                description.appendText("has view id $itemMatcher at position $position")
            }

            override fun matchesSafely(recyclerView: RecyclerView): Boolean {
                val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
                    ?: return false // no item on such position
                val targetView = viewHolder.itemView.findViewById<View>(targetViewId)
                return itemMatcher.matches(targetView)
            }
        }
    }
}