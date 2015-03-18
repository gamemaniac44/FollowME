package com.example.followMe.util;

import com.example.followMe.LoginScreen;
import com.example.followMe.MainScreen;
import com.example.followMe.R;
import com.google.android.gms.maps.model.LatLng;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.widget.Toast;

public class LocationServer 
implements android.location.LocationListener
{	
	private MainScreen mainScreen = null;
	private LocationManager locationManager = null;	
	private int GPSStatus = LocationProvider.OUT_OF_SERVICE;
	private Location location = null;
	
	/***********************************************
	 * Public constructor and methods for users
	 ***********************************************/
	
	public LocationServer(MainScreen mainScreen) 
	{
		this.mainScreen = mainScreen;
		Context context = mainScreen.getApplicationContext();
    	locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		
		start();
	}
	
	public void start() 
	{
		/* Don't return until GPS is activated. */
		//enableGPS();
    	//while (enableGPS() == false) ;
    	requestUpdates();
	}
	
	public void stop() 
	{
		locationManager.removeUpdates(this);
		GPSStatus = LocationProvider.OUT_OF_SERVICE;
	}
	
	/**********************************************
	 * Public LocationListener callback methods
	 **********************************************/
	
	@Override
	public void onLocationChanged(Location location) 
	{
		Toast.makeText(mainScreen.getApplicationContext(), "Location Changed", Toast.LENGTH_LONG).show();

		this.location = location;
		GPSStatus = LocationProvider.AVAILABLE;
		mainScreen.onLocationChanged(new LatLng(location.getLatitude(), location.getLongitude()));
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) 
	{		
		if (provider != LocationManager.GPS_PROVIDER) 
			return;
		
		Toast.makeText(mainScreen.getApplicationContext(), "Status Changed: " + provider, Toast.LENGTH_LONG).show();
		
		GPSStatus = status;
		
		switch(status) {
			case LocationProvider.OUT_OF_SERVICE:
				Toast.makeText(mainScreen.getApplicationContext(), "GPS Out of Service", Toast.LENGTH_LONG).show();
				GPSStatus = LocationProvider.OUT_OF_SERVICE;
				// Don't return until GPS is activated
				while (enableGPS() == false) ;
				break;
			case LocationProvider.TEMPORARILY_UNAVAILABLE:
				Toast.makeText(mainScreen.getApplicationContext(), "GPS Temporarily Unavailable", Toast.LENGTH_LONG).show();
				GPSStatus = LocationProvider.TEMPORARILY_UNAVAILABLE;
				tellUserToMoveLocation();
				location = null;
				break;
			case LocationProvider.AVAILABLE:
				Toast.makeText(mainScreen.getApplicationContext(), "GPS Available", Toast.LENGTH_LONG).show();
				requestUpdates();
		}
	}

	@Override
	public void onProviderEnabled(String provider) 
	{
		if (provider.equals(LocationManager.GPS_PROVIDER)) {
			Toast.makeText(mainScreen.getApplicationContext(), "GPS Enabled", Toast.LENGTH_LONG).show();
			requestUpdates();
		}
	}

	@Override
	public void onProviderDisabled(String provider) 
	{
		if (provider.equals(LocationManager.GPS_PROVIDER)) {
			Toast.makeText(mainScreen.getApplicationContext(), "GPS Disabled", Toast.LENGTH_LONG).show();
			while(enableGPS() == false) ;
		}
	}
	
	/***********************************************
	 * Private methods
	 ***********************************************/
	
	private boolean enableGPS() 
	{    	
    	boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    	
		// Enable GPS on the phone
		if(gpsEnabled == false)
		{
			//TODO: this does not work
			
			Drawable gpsError = mainScreen.getResources().getDrawable(R.drawable.alert_icon);
			
			//display error message
			new AlertDialog.Builder(mainScreen)
			.setTitle("Turn on GPS Settings")
			.setMessage("GPS is not enabled. Do you want to go to device settings to enable it?")

			//executed when settings button is pressed
			.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which){
					//Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					Intent intent = new Intent(Intent.ACTION_MAIN);
					intent.setClassName("com.android.phone", "com.android.phone.Settings");
					mainScreen.startActivity(intent);
				}
			})

			//executed when cancel button is pressed
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Toast.makeText(mainScreen.getApplicationContext(), "Please turn on GPS in device settings and login again.", Toast.LENGTH_LONG).show();
					Intent loginScreen = new Intent(mainScreen.getApplicationContext(), LoginScreen.class);
					mainScreen.startActivity(loginScreen);
				}
			}) 
			.setIcon(gpsError).show();
			//Toast.makeText(mainScreen.getApplicationContext(), "Please enable GPS in your phone settings.", Toast.LENGTH_LONG).show();
		}
		else
		{
			Toast.makeText(mainScreen.getApplicationContext(), "GPS Enabled (2)", Toast.LENGTH_LONG).show();
		}
		
		return gpsEnabled;
	}
	
	private void requestUpdates() 
	{
		Criteria criteria = new Criteria();
		locationManager.getBestProvider(criteria, true);
		locationManager.requestSingleUpdate(criteria, this, null);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15000, 0, this);
	}
	
	private void tellUserToMoveLocation() 
	{
		Drawable gpsError = mainScreen.getResources().getDrawable(R.drawable.alert_icon);

		// Display error message dialog box
		new AlertDialog.Builder(mainScreen.getApplicationContext())
		.setTitle("We can not get your GPS location.")
		.setMessage("Please move to a clear view of the sky then press OK.")
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which){
				dialog.cancel();
			}
		})
		.setIcon(gpsError).show();
	}
} // END OF CLASS

