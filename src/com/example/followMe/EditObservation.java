package com.example.followMe;

import java.util.ArrayList;
import java.util.Iterator;

import com.example.followMe.obj.Observation;
import com.example.followMe.obj.ObservationList;
import com.example.followMe.util.AppData;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class EditObservation extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_observation);
		populateListView();
    }
	
	private void populateListView()
	{
		ArrayList<String> titleList = new ArrayList<String>();
		ObservationList obsList = AppData.getCurrentExcursion().getObservationList();
		Iterator<Observation> it = obsList.iterator();
		
		Observation obs = null;
		String text = null;
		
		while(it.hasNext()) {
			obs = it.next();
			text = "'" + obs.getTitle() + "'\n" + " created " + obs.getCreationDate();
			titleList.add(text);
		}
	
		ListView listView = (ListView) findViewById(R.id.editObservationsListView);
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
				android.R.layout.simple_list_item_1, android.R.id.text1, titleList);
		
		listView.setAdapter(adapter);
		
		// ListView Item Click Listener
        listView.setOnItemClickListener(new OnItemClickListener() {

              @Override
              public void onItemClick(AdapterView<?> parent, View view,
                 int position, long id) {

            	  String text = "Setting position in AddObservation for editing:  "+ position;
            	  Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
            	  
            	  AddObservation.observationIndex = position;
      			
            	  startActivity(new Intent(getApplicationContext(), AddObservation.class));          	  
              }
            }); 		
		
	}
	
	public void backMain(View v)
	{
		Intent showMain = new Intent(getApplicationContext(), MainScreen.class);
		startActivity(showMain);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.view_observation, menu);
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