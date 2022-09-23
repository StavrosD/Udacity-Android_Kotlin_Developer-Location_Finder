<h1> Project: Location Reminder </h1>

<p>
In this project, I created a TODO list app with location reminders that remind the user to do something when the user is at a specific location. The app will require the user to create an account and login to set and access reminders.
</p> 

<h2> Enhanced functionality </h2>

<p> Besides the project requirements, I completed some additional tasks: </p>

<ol>
<li> Used MediatorLiveData. I converted "showNoData" to MediatorLiveData in order to automatically combine the values of "error" and "empty". "invalidateShowNoData" function is no longer needed. </li>
<li> A GeoCoder is used to get the address from the coordinates, if possible. </li>
<li> Upgraded deprecated code to up to day code where possible.
   <ul>
   <li> setHasOptionsMenu -> addMenuProvider. </li>
   <li> requestPermissions() -> RequestPermission contract. </li>
   </ul>
</li>

<li> After login, require location service enabled on the device in every fragment.</li>

<li> Apply limit of 100 reminders. This is the official geofence limit for a single app, single user. If there are 100 geofences, a message is displayed when the user clicks the “Add geofence” button. </li>

<li>Experimented with string interpolation (max_geofences_reached).</li>

<li>Suppressed meaningless warning (@Suppress("UNCHECKED_CAST”)).</li>

<li>Code refactored for extensive error handling.</li>

<li> Added this readme file and some screenshots.</li>

<li>Added flowcharts for SaveReminderFragment and SelectLocationFragment. The most important functionality of these classes is displayed in these flowcharts.</li>
</ol>

<h2>Notes</h2>

<ol>
<li> Before running the project you should enter your MAPS_API_KEY. <br/>
The recommended way is to create a "local.properties" file in the projects' root folder and then add  MAPS_API_KEY=...<br/>
You can also replace the "$MAPS_API_KEY" value inside AndroidManifest.xml.</li>
<li> Long click on a reminder to delete it and remove the geofence.</li>
<li> If the geofence is not activated using the emulator:
<ul>
   <li> Go to the application settings and set "Battery optimization" to "Not optimized" </li>
   <li> Go to the save reminder screen and then on the map. While the map is open, the app receives location notifications. </li>
</ul>
<li>Before running the tests, run the app, enable the required permissions and turn the location service on.</li>
<li>Even if the access fine location permission is granted and the location service is on, clicking the "My location" button may not center the map on the device location. <br/>
In rare cases, such as right after enabling the location service or being located on a location without GPS or wifi coverage. <br/>
If you have clicked the "My location" button and a location is not available, the map will be centered automatically when the device resolves its' location.</li>
</ol>
<h2> Demo </h2>


<figure>
    <img src="screenshots/AppCapture.gif" width="256" alt="Start screen"/>
    <figcaption>App demo</figcaption>
</figure>
<br/>

<h2> Screenshots </h2>

<figure>
    <img src="screenshots/Start.png" width="256" alt="Start screen"/>
    <figcaption>Start screen.</figcaption>
</figure>
<br/>
<figure>
    <img src="screenshots/Login.png" width="256" alt="Login screen"/>
    <figcaption>Login screen.</figcaption>
</figure>
<br/>
<figure>
    <img src="screenshots/NoReminders.png" width="256" alt="No reminders"/>
    <figcaption>Reminders list. No reminders added yet.</figcaption>
</figure>
<br/>
<figure>
    <img src="screenshots/SaveReminder.png" width="256" alt="Save reminder"/>
    <figcaption>Add new reminder screen.</figcaption>
</figure>
<br/>
<figure>
   <img src="screenshots/NormalMap.png" width="256" alt="Normal map"/>
   <img src="screenshots/HybridMap.png" width="256" alt="Hybrid map"/>
   <img src="screenshots/SatelliteMap.png" width="256" alt="Satellite map"/>
   <img src="screenshots/TerrainMap.png" width="256" alt="Terrain map"/>
   <figcaption>Select location. There are four map Types (Normal, Hybrid, Satellite, Terrain).</figcaption>
</figure>
<br/>
<figure>
    <img src="screenshots/LocationSelected.png" width="256" alt="Location selected"/>
    <figcaption>Add new reminder screen. A location is selected. The address is resolved from the coordinates using a GeoCoder.</figcaption>
</figure>
<br/>
<figure>
    <img src="screenshots/BackgroundPermission.png" width="256" alt="Background permission request"/>
    <figcaption>When the user click the save button the background location permission is requested, if it not already enabled.  </figcaption>
</figure>
<br/>
<figure>
    <img src="screenshots/ReminderList.png" width="256" alt="Reminder list"/>
    <figcaption>Reminder list  </figcaption>
</figure>
<br/>
<figure>
    <img src="screenshots/ReminderDetails.png" width="256" alt="Reminder details"/>
    <figcaption>After a geofence is triggered a notification is triggered. When the user clicks on the notification, the details of this reminder are displayed. </figcaption>
</figure>
<br/>
<figure>
    <img src="screenshots/androidTest.png" width="512" alt="Integration test"/>
    <img src="screenshots/DaoTests.png" width="512" alt="DAO tests"/>
    <img src="screenshots/RepositoryTests.png" width="512" alt="DAO tests"/>
    <img src="screenshots/SaveReminderViewmodelTest.png" width="512" alt="SaveReminderViewmodel tests"/>
    <figcaption>Integration tests passed!</figcaption>
</figure>
<br/>
<figure>
    <img src="screenshots/UnitTests.png" width="1024" alt="Unit tests"/>
    <figcaption>All the unit tests (14 of 14) passed! </figcaption>
</figure>
<br/>

<h2>Flowcharts</h2>
<figure>
    <img src="screenshots/SaveReminderFragment-flowchart.png" alt="SaveReminderFragment flowchart"/>
    <figcaption>SaveReminderFragment flowchart. </figcaption>
</figure>
<br/><br/>
 <figure>
    <img src="screenshots/SelectLocationFragment-flowchart.png" alt="SelectLocationFragment flowchart"/>
    <figcaption>SelectLocationFragment flowchart (access fine location permission,mylocation layer, location setting, get location ).</figcaption>
</figure>
