package com.example.followMe;

import com.example.followMe.obj.Excursion;
import com.example.followMe.obj.InvalidExcursionDataException;
import com.example.followMe.util.AppData;
import com.example.followMe.util.CloudDB;
import com.example.followMe.util.LocalDB;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

public class EditExcursion extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_excursion);
		setExcursionFields();
		
		// Allows the http calls to run ---- 
		// TODO: Might work without this code, haven't tested though
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
	}
	
	private void setExcursionFields()
	{
		Excursion e = AppData.getCurrentExcursion();
		
		EditText titleEditText = (EditText) findViewById(R.id.editExcursionTitleEditText);
		titleEditText.setText(e.getTitle());
		
		EditText descEditText = (EditText) findViewById(R.id.editExcursionDescEditText);
		if (e.getDescription() != null)
			descEditText.setText(e.getDescription());
		
		Switch travelModeSwitch = (Switch) findViewById(R.id.editExcursionTravelModeSwitch);
		if (e.getTravelMode() == Excursion.DRIVING)
			travelModeSwitch.setChecked(true);
		else
			travelModeSwitch.setChecked(false);
		
		Switch shareModeSwitch = (Switch) findViewById(R.id.editExcursionShareModeSwitch);
		if (e.getShareMode() == Excursion.PUBLIC)
			shareModeSwitch.setChecked(true);
		else
			shareModeSwitch.setChecked(false);
	}
	
	public void saveExcursion(View v)
	{
		Excursion e = AppData.getCurrentExcursion();
		
		EditText titleEditText = (EditText) findViewById(R.id.editExcursionTitleEditText);
		
		String title = titleEditText.getText().toString();
		if (title.length() == 0) {
			Toast.makeText(getApplicationContext(), "The excursion must have a title.", Toast.LENGTH_LONG).show();
			return;
		}

		EditText descEditText = (EditText) findViewById(R.id.editExcursionDescEditText);
		String desc = descEditText.getText().toString();
		
		Switch travelModeSwitch = (Switch) findViewById(R.id.editExcursionTravelModeSwitch);
		int travelMode;
		if (travelModeSwitch.isChecked())
			travelMode = Excursion.DRIVING;
		else
			travelMode = Excursion.WALKING;			
		
		Switch shareModeSwitch = (Switch) findViewById(R.id.editExcursionShareModeSwitch);
		int shareMode;
		if (shareModeSwitch.isChecked())
			shareMode = Excursion.PUBLIC;
		else
			shareMode = Excursion.PRIVATE;	
		
		try {
			e.setTitle(title);
			e.setDescription(desc);
			e.setTravelMode(travelMode);
			e.setShareMode(shareMode);
		} 
		catch (InvalidExcursionDataException e1) 
		{
			Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
			return;
		}

		Toast.makeText(getApplicationContext(), "Saving Excursion", Toast.LENGTH_LONG).show();
		LocalDB.saveExcursion();
		
		if (AppData.getCurrentExcursion().getShareMode() == Excursion.PUBLIC) {
			int returnCode = CloudDB.publishExcursion();
			String result = CloudDB.sendResult(returnCode);
			Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
		}
		
		Intent loadMainScreen = new Intent(getApplicationContext(), MainScreen.class);
		startActivity(loadMainScreen);
	}
	
	public void deleteExcursion(View v)
	{
		AppData.deleteCurrentExcursion();
		Toast.makeText(getApplicationContext(), "Deleting Excursion", Toast.LENGTH_LONG).show();
		Intent loadMainScreen = new Intent(getApplicationContext(), MainScreen.class);
		startActivity(loadMainScreen);	
	}
	
	public void backMain(View v)
	{
		Intent showMain = new Intent(getApplicationContext(), MainScreen.class);
		startActivity(showMain);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_excursion, menu);
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