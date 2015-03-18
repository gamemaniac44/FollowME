package com.example.followMe;

import com.example.followMe.obj.InvalidObservationDataException;
import com.example.followMe.obj.Observation;
import com.example.followMe.obj.ObservationList;
import com.example.followMe.util.AppData;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AddObservation extends Activity 
{
	private final static int RESET = -1;
	public static int observationIndex = RESET;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_observation);
		populateFields();
		Log.d("followMe", "Adding observation onCreate ...");
	}
	
	@SuppressLint("CutPasteId")
	private void populateFields() 
	{
		if (observationIndex < 0) {
			//get position data from location server and google maps
			Intent locationData = getIntent();
			
			double latitude = locationData.getDoubleExtra("latitude", 0.0);
			double longitude = locationData.getDoubleExtra("longitude", 0.0);
			
			TextView gpsTextView = (TextView) findViewById(R.id.addObservationGPSDataTextView);
			gpsTextView.setText("" + latitude + " " +longitude);			
			
			return;			
		}
	
		ObservationList list = AppData.getCurrentExcursion().getObservationList();
		
		if (observationIndex >= list.size()) {
			Toast.makeText(getApplicationContext(), "Error setting observation index", Toast.LENGTH_LONG).show();
			observationIndex = RESET;
			return;
		}
		
		Observation o = list.get(observationIndex);
		
		EditText titleEditText = (EditText) findViewById(R.id.addObservationTitleEditText);
		if (o.getTitle() != null)
			titleEditText.setText(o.getTitle());
		
		EditText descEditText = (EditText) findViewById(R.id.addObservationDescriptionEditText);
		if (o.getDescription() != null)
			descEditText.setText(o.getDescription());
		
		String gps = "" + o.getLatitude() + " " +  o.getLongitude();
		TextView GPSTextView = (TextView) findViewById(R.id.addObservationGPSDataTextView);
		GPSTextView.setText(gps);
	}
	
	public void saveObservation(View v)
	{
		EditText titleEditText = (EditText) findViewById(R.id.addObservationTitleEditText);
		String title = titleEditText.getText().toString();

		EditText descEditText = (EditText) findViewById(R.id.addObservationDescriptionEditText);
		String description = descEditText.getText().toString();
		
		if (observationIndex < 0) 
			addNewObservation(title, description);
		else
			editExistingObservation(title, description);
	}
	
	private void addNewObservation(String title, String description) 
	{
		// TODO: get GPS from satelite.  Set variables below.

		Intent locationData = getIntent();
		
		double latitude = locationData.getDoubleExtra("latitude", 0.0);
		double longitude = locationData.getDoubleExtra("longitude", 0.0);

		try 
		{
			AppData.getCurrentExcursion().addObservation(title, description, longitude, latitude);
		} 
		catch (InvalidObservationDataException e) 
		{
			Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
			return;
		}

		Toast.makeText(getApplicationContext(), "New observation saved.", Toast.LENGTH_LONG).show();
		Intent showMain = new Intent(getApplicationContext(), MainScreen.class);
		startActivity(showMain);
	}

	private void editExistingObservation(String title, String description)
	{
		try {
			AppData.getCurrentExcursion().getObservationList().get(observationIndex).setTitle(title);
			AppData.getCurrentExcursion().getObservationList().get(observationIndex).setDescription(description);
		} 
		catch (InvalidObservationDataException e) 
		{
			Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
			observationIndex = RESET;
		}
		
		Toast.makeText(getApplicationContext(), "Observation updated", Toast.LENGTH_LONG).show();
		AppData.saveCurrentExcursion();
		observationIndex = RESET;
		Intent showMain = new Intent(getApplicationContext(), MainScreen.class);
		startActivity(showMain);
	}

	public void deleteObservation(View v)
	{
		if (observationIndex == RESET)
			return;
		
		AppData.getCurrentExcursion().getObservationList().remove(observationIndex);
		AppData.saveCurrentExcursion();
		observationIndex = RESET;
		Intent showMain = new Intent(getApplicationContext(), MainScreen.class);
		startActivity(showMain);		
	}
	
	public void backMain(View v)
	{
		Intent showMain = new Intent(getApplicationContext(), MainScreen.class);
		startActivity(showMain);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_observation, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.actionSettings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
