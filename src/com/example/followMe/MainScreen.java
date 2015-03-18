package com.example.followMe;

import java.util.ArrayList;
import java.util.Iterator;

import com.example.followMe.obj.GPSPoint;
import com.example.followMe.obj.Observation;
import com.example.followMe.obj.ObservationList;
import com.example.followMe.obj.Route;
import com.example.followMe.util.AppData;
import com.example.followMe.util.LocationServer;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class MainScreen extends FragmentActivity{

	private static SupportMapFragment mapFragment = null;
	private static GoogleMap googleMap = null;

	private static ActionBarDrawerToggle menuDrawerToggle = null;
	private static DrawerLayout drawerLayout = null;
	private static ListView drawerList = null;
	private static String[] menuSelections = null;
	private static TypedArray menuBarIcons = null;
	private static ArrayList<NavDrawerItem> navDrawerItems = null;
	private static NavDrawerListAdapter adapter = null;
	private static boolean isDrawerOpen= false;
	
	private static boolean gpsRecording = false;
	private static LatLng prevRecordedLocation = null;
	private static LatLng lastKnownLocation = null;
	private static LatLng startMarkerPos = null;
	private static LatLng endMarkerPos = null;

	private static boolean addedStartRouteMarker = false;
	private static boolean addedEndRouteMarker = false;
	private static Marker stopMarker = null;


	private final static int NEW_EXCURSION_POS = 0;
	private final static int LOAD_EXCURSION_POS = 1;
	private final static int EDIT_EXCURSION_POS = 2;
	private final static int ADD_OBSERVATION_POS = 3;
	private final static int EDIT_OBSERVATION_POS = 4;
	private final static int SETTINGS_POS = 5;
	private final static int ABOUT_POS = 6;
	private final static int LOGOUT_POS = 7;
	private final static int NUM_OPTIONS = 7;

	public static LocationServer locServer = null;

	/**************************************************************************
	 * onCreate() called when the app is loaded
	 * - set the layout
	 * - start google play services
	 * - start the location server
	 * - set up the menu bar
	 * - set up the slider
	 *************************************************************************/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.google_map);

		// Check if the phone has Google Play Services
		checkGooglePlayServices();

		// Keep the device screen turned on so that the device always shows Google Maps
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// Draw the map and the users location
		initializeGoogleMap(savedInstanceState);

		// Start the location server
		initializeLocationServer();

		//title of action bar
		getActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
		String title = AppData.getCurrentExcursion().getTitle();
		if (title != null)
			getActionBar().setTitle(AppData.getCurrentExcursion().getTitle());
		else
			getActionBar().setTitle(R.string.app_name);

		// Create an action bar slider
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerList = (ListView) findViewById(R.id.drawer_list);
		menuSelections = getResources().getStringArray(R.array.mapCommands);
		menuBarIcons = getResources().obtainTypedArray(R.array.mapCommandsIcons);

		// Slider menu callback function
		menuDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer, 
				R.string.drawerOpen, R.string.drawerClose) {

			@Override
			public void onDrawerSlide(View drawerView, float slideOffset){
				//drawer is open
				if(slideOffset > .55 && !isDrawerOpen){
					getActionBar().setTitle("Menu");
					onDrawerOpened(drawerView);
					isDrawerOpen = true;
					invalidateOptionsMenu();
				}

				//drawer is closed
				else if(slideOffset < .45 && isDrawerOpen) {
					getActionBar().setTitle(AppData.getCurrentExcursion().getTitle());
					onDrawerClosed(drawerView);
					isDrawerOpen = false;
					invalidateOptionsMenu();
				}
			}
		};

		// Set slider callback
		drawerLayout.setDrawerListener(menuDrawerToggle);

		//create a new ArrayList and add values to the array
		//the array represents the different route options available to the user
		navDrawerItems = new ArrayList<NavDrawerItem>();

		for (int i = 0; i <= NUM_OPTIONS; i++)
			navDrawerItems.add(new NavDrawerItem(menuSelections[i], menuBarIcons.getResourceId(i, -1)));

		menuBarIcons.recycle();
		adapter = new NavDrawerListAdapter(getApplicationContext(), navDrawerItems);
		drawerList.setAdapter(adapter);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		//Set item click listener for the ListView drawerList
		drawerList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent,
					View view,
					int position,
					long id) {

				//Create a Bundle object
				Bundle data = new Bundle();

				//Set the index of the currently selected item of drawer list
				data.putInt("position", position);

				if (position == NEW_EXCURSION_POS)
				{
					if (AppData.getCurrentExcursion().getObservationList().size() > 0 ||
							AppData.getCurrentExcursion().getRoute().size() > 0) {
						AppData.saveCurrentExcursion();
					}
					AppData.createNewExcursion();
					resetMapState();
				}
				if (position == LOAD_EXCURSION_POS)
				{
					if (AppData.getCurrentExcursion().getObservationList().size() > 0 ||
							AppData.getCurrentExcursion().getRoute().size() > 0) {
						AppData.saveCurrentExcursion();
					}
					resetMapState();
					Intent loadIntent = new Intent(getApplicationContext(), LoadExcursion.class);
					startActivity(loadIntent);
				}
				else if(position == EDIT_EXCURSION_POS)
				{
					Intent loadIntent = new Intent(getApplicationContext(), EditExcursion.class);
					startActivity(loadIntent);
				}
				else if(position == ADD_OBSERVATION_POS)
				{	
					if (gpsRecording == true)
					{
						Intent addObsIntent = new Intent(getApplicationContext(), AddObservation.class);

						if (lastKnownLocation == null) {
							Toast.makeText(getApplicationContext(), "Location Server Temporarily Unavailable.", 
									Toast.LENGTH_LONG).show();
							return;
						}

						addObsIntent.putExtra("latitude", lastKnownLocation.latitude);
						addObsIntent.putExtra("longitude", lastKnownLocation.longitude);

						startActivity(addObsIntent);
					}
					else
					{
						turnRecordingOnDialog();
					}
				}
				else if(position == EDIT_OBSERVATION_POS)
				{
					Intent editObsIntent = new Intent(getApplicationContext(), EditObservation.class);
					startActivity(editObsIntent);
				}
				else if(position == SETTINGS_POS)
				{
					Intent settings = new Intent();
					settings.setClassName("com.example.followMe", "com.example.followMe.Settings");
					startActivity(settings);
				}
				else if(position == ABOUT_POS)
				{
					Intent about = new Intent(getApplicationContext(), About.class);
					startActivity(about);
				}
				else if(position == LOGOUT_POS)
				{
					if (AppData.getCurrentExcursion().getObservationList().size() > 0 ||
							AppData.getCurrentExcursion().getRoute().size() > 0) {
						AppData.saveCurrentExcursion();
					}
					resetMapState();
					Intent loginScreen = new Intent(getApplicationContext(), LoginScreen.class);
					startActivity(loginScreen);
				}

				drawerLayout.closeDrawer(drawerList);
			}
		});
	}

	/**************************************************************************
	 * Start Google Play Services, location Server and initialize Google Map
	 *************************************************************************/

	private void checkGooglePlayServices() 
	{
		// TODO: I don't think this is working. Test.
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());

		if (status != ConnectionResult.SUCCESS) {
			Toast.makeText(getApplicationContext(), "Google Play Services Not Available", Toast.LENGTH_LONG).show();

			//int requestCode = 10;  //TODO: is 10 the right request code?
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, 0);
			dialog.show();
		}
	}

	private void initializeLocationServer() 
	{
		if (locServer == null)
			locServer = new LocationServer(this);	
	}

	private void initializeGoogleMap(Bundle savedInstanceState) 
	{
		// TODO: If WI-FI is off, no maps are downloaded.
		// Check status of WI-FI or cellular network and ask user to turn on.
		
		// Getting reference to the SupportMapFragment of XML file
		mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
		mapFragment.onCreate(savedInstanceState);

		// Getting GoogleMap object from the fragment
		
		if (mapFragment != null) {
			googleMap = mapFragment.getMap();
			if (googleMap != null) {
				googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

				//set user location enabled on map
				googleMap.setMyLocationEnabled(true);
			}
		}
	}

	/**************************************************************************
	 * The Start and Stop Recording Route buttons' callback method.
	 *************************************************************************/

	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		int id = item.getItemId();
		if (id == R.id.actionSettings) {
			return true;
		}

		if (menuDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		if (id == R.id.startGPS) {
			// Only start the GPS recording if it is currently off and we are able to 
			// get a valid GPS location

			if (gpsRecording == false) {

				gpsRecording = true;
				Toast.makeText(getApplicationContext(), "Recording Route On", Toast.LENGTH_LONG).show();

				if (lastKnownLocation == null)  {
					Toast.makeText(getApplicationContext(), "Oops(1): Last Known Location Unknown", Toast.LENGTH_LONG).show();
					return false;
				}

				if (addedStartRouteMarker == false) {
					//add marker on map to signify start of route
					googleMap.addMarker(new MarkerOptions()
					.position(lastKnownLocation)
					.title("Start")
					.snippet("Beginning of route")
					.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

					addedStartRouteMarker = true;
				}

				//check to see if any end marker exist on map, and if so, then remove the end marker to replace for a new one when the user presses stop recording
				if(addedEndRouteMarker == true) {
					stopMarker.remove();
					addedEndRouteMarker = false;	
				}

			}
		}

		if (id == R.id.stopGPS) {
			// Only stop GPS recording when it is currently recording.
			if (gpsRecording == true) {

				gpsRecording = false;
				Toast.makeText(getApplicationContext(), "Recording Route Off", Toast.LENGTH_LONG).show();

				if (lastKnownLocation == null)  {
					Toast.makeText(getApplicationContext(), "Oops(2): Last Known Location Unknown", Toast.LENGTH_LONG).show();
					return false;
				}			

				//add marker on map to signify end of route
				stopMarker = googleMap.addMarker(new MarkerOptions()
				.position(lastKnownLocation)
				.title("End")
				.snippet("End of route")
				.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

				addedEndRouteMarker = true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	/**************************************************************************
	 * Initialize main screen map variables for new excursion
	 **************************************************************************/
	
	private void resetMapState() 
	{
		if (googleMap != null)
			googleMap.clear();
	
		gpsRecording = false;
		prevRecordedLocation = null;
		lastKnownLocation = null;
		startMarkerPos = null;
		endMarkerPos = null;

		addedStartRouteMarker = false;
		addedEndRouteMarker = false;
		stopMarker = null;
	}
	
	/**************************************************************************
	 * Here we move the users location on the map and if recording is ON
	 * we draw the a line from the previous location to a the new location
	 * and save the new location in the excursion.
	 *************************************************************************/

	public void onLocationChanged(LatLng curLocation) 
	{		
		if (googleMap == null)
			return;
		
		if (gpsRecording == true) {

			//add route point to database
			Toast.makeText(getApplicationContext(), "Saving GPS Point", Toast.LENGTH_LONG).show();
			AppData.getCurrentExcursion().addGPSPoint(curLocation.longitude, curLocation.latitude);
			
			// If user has recorded at least one point prior to this point
			// then draw a line from the previous location to the new location,
			// otherwise just set the prevRecordedLocation to curLocation.
			if (prevRecordedLocation != null) {
				
				@SuppressWarnings("unused")
				Polyline route = googleMap.addPolyline(new PolylineOptions()
				.add(prevRecordedLocation, curLocation)
				.width(5)
				.color(Color.RED));
			}
			
			prevRecordedLocation = curLocation;
		}
		else if (gpsRecording == false) {
			prevRecordedLocation = null;
		}

		lastKnownLocation = curLocation;
		moveCamera();
	}

	/**************************************************************************
	 * Mover the camera to an appropriate location
	 **************************************************************************/

	private void moveCamera() 
	{
		LatLng cameraLocation = null;
		Route routePointsList = AppData.getCurrentExcursion().getRoute();
		int numPoints = routePointsList.size();

		if (gpsRecording == false && routePointsList.size() != 0) {
			GPSPoint lastPoint = routePointsList.get(numPoints-1);
			cameraLocation = new LatLng(lastPoint.getLatitude(), lastPoint.getLongitude());
		}
		else if (lastKnownLocation == null){
			return;
		}
		else {
			cameraLocation = lastKnownLocation;
		}

		// Showing the current location in Google Map
		googleMap.moveCamera(CameraUpdateFactory.newLatLng(cameraLocation));
		googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

		//Toast.makeText(getApplicationContext(), "Moving blue dot: " + cameraLocation.toString(), Toast.LENGTH_LONG).show();		
	}


	/**************************************************************************
	 * Draw markers for the the current excursions observations.
	 *************************************************************************/

	//draw markers on map when map is reloaded from switching between activities
	private void redrawObservationMarkers()
	{
		Observation observation = null;

		//get the list of observations from the database
		ObservationList obsList = AppData.getCurrentExcursion().getObservationList();

		if (obsList.size() == 0)
			return;

		// Go through all of the observations and draw markers
		Iterator<Observation> obsParse = obsList.iterator();

		while (obsParse.hasNext())
		{
			observation = obsParse.next();
			LatLng obsLatLng = new LatLng(observation.getLatitude(), observation.getLongitude());

			googleMap.addMarker(new MarkerOptions()
			.position(obsLatLng)
			.title(observation.getTitle())
			.snippet(observation.getDescription())
			.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
		}
	}

	/**************************************************************************
	 * Draw the current excursions route on the map
	 *************************************************************************/

	private void redrawRoute()
	{
		LatLng prevRouteLatLng = null;
		GPSPoint gpsPoints = null;

		// Get the list of gps points from the database
		Route routePointsList = AppData.getCurrentExcursion().getRoute();

		if (routePointsList.size() == 0)
			return;

		// Display the start marker
		startMarkerPos = new LatLng(routePointsList.get(0).getLatitude(), routePointsList.get(0).getLongitude());
		prevRouteLatLng = startMarkerPos;

		//redraw start marker onto map
		googleMap.addMarker(new MarkerOptions()
		.position(startMarkerPos)
		.title("Start")
		.snippet("Beginning of route")
		.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
		addedStartRouteMarker = true;

		// Go through all of the points on the route and draw them on the map
		Iterator<GPSPoint> routeParse = routePointsList.iterator();

		while (routeParse.hasNext())
		{
			gpsPoints = routeParse.next();
			LatLng routeLatLng = new LatLng(gpsPoints.getLatitude(), gpsPoints.getLongitude());

			@SuppressWarnings("unused")
			Polyline route = googleMap.addPolyline(new PolylineOptions()
			.add(prevRouteLatLng, routeLatLng)
			.width(5)
			.color(Color.RED));

			prevRouteLatLng = routeLatLng;
		}

		// Redraw the stop marker ONLY if the user has stopped recording GPS.
		if (gpsRecording == false)
		{
			//get the last point from the route points list to mark the end of the route
			endMarkerPos = new LatLng(prevRouteLatLng.latitude, prevRouteLatLng.longitude);

			//redraw end marker onto map
			stopMarker = googleMap.addMarker(new MarkerOptions()
			.position(endMarkerPos)
			.title("End")
			.snippet("End of route")
			.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
		}

	}

	/**************************************************************************
	 * Dialog to tell user to press the start recording button 
	 * in order to add an observation. 
	 **************************************************************************/

	//method to 
	private void turnRecordingOnDialog()
	{
		Drawable gpsError = this.getResources().getDrawable(R.drawable.alert_icon);

		//display error message
		new AlertDialog.Builder(this)
		.setTitle("GPS Recording Off")
		.setMessage("You must turn GPS recording ON in order to create an observation.")
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which){
				dialog.cancel();
			}
		})
		.setIcon(gpsError).show();
	}

	/**************************************************************************
	 * Odds and Ends 
	 **************************************************************************/

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.trail__track, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) 
	{
		// If the drawer is open, hide action items related to the content view
		menu.findItem(R.id.actionSettings).setVisible(false);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) 
	{
		super.onPostCreate(savedInstanceState);
		menuDrawerToggle.syncState();
	}

	//if the user presses the back button on +the device, then do nothing.
	@Override
	public void onBackPressed()
	{
		// Do nothing.
	}

	@Override
	protected void onPause() {
		super.onPause();
		mapFragment.onPause();
		//Toast.makeText(getApplicationContext(), "onPause", Toast.LENGTH_LONG).show();
	}


	@Override
	protected void onStop() {
		super.onStop();
		mapFragment.onStop();
		if (gpsRecording == false)
			locServer.stop();
		//Toast.makeText(getApplicationContext(), "onStop", Toast.LENGTH_LONG).show();

	}

	@Override
	protected void onDestroy(){
		super.onDestroy();
		mapFragment.onDestroy();
		//Toast.makeText(getApplicationContext(), "onDestroy", Toast.LENGTH_LONG).show();

	}

	@Override
	protected void onRestart() {
		super.onRestart();
		//Toast.makeText(getApplicationContext(), "onRestart", Toast.LENGTH_LONG).show();

	}

	@Override
	protected void onResume() {
		super.onResume();
		mapFragment.onResume();
		if (gpsRecording == false)
			locServer.start();
		redrawObservationMarkers();
		redrawRoute();
		moveCamera();
		//Toast.makeText(getApplicationContext(), "onResume", Toast.LENGTH_LONG).show();
	}




} // END OF CLASS

