package com.example.followMe;

import java.util.ArrayList;
import java.util.Iterator;

import com.example.followMe.obj.Excursion;
import com.example.followMe.obj.ExcursionList;
import com.example.followMe.obj.ExcursionListEntry;
import com.example.followMe.util.AppData;
import com.example.followMe.util.CloudDB;
import com.example.followMe.util.LocalDB;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class LoadExcursion extends Activity 
{
	private ListView listView = null;
	private ExcursionList excursionList = null;
	private int numLocalExcursions = 0;
	private static String[][] serverExcursionList = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.load_excursion);
		listView = (ListView)findViewById(R.id.loadExcursionListView);
		CreateListView();

		// Allows the http calls to run ---- 
		// TODO: Might work without this code, haven't tested though
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);

	}

	private void CreateListView() 
	{	
		excursionList = LocalDB.getExcursionList();
		numLocalExcursions = excursionList.size();	

		serverExcursionList = CloudDB.getExcursionList();
		
		if (serverExcursionList != null) {
			for(int i = 0; i < serverExcursionList.length; i++) {
				excursionList.addEntry(-1, 			// indicates excursion is from server
						serverExcursionList[i][0], 	// title
						serverExcursionList[i][3], 	// original author
						serverExcursionList[i][2]);	// creation date
			}
		}

		// titleList entries will be in the same order as excursionList
		ArrayList<String> titleList = new ArrayList<String>();
		Iterator<ExcursionListEntry> it = excursionList.iterator();
		ExcursionListEntry entry = null;
		
		// Add all local and remote excursions to the titleList.
		while(it.hasNext()) {
			entry = it.next();
			titleList.add("'" + entry.getTitle() + "'\n" + 
					"Published by "  + entry.getOriginalAuthor() + "\n" + 
					"Created on " + entry.getCreationDate());
		}

		listView.setAdapter(new ArrayAdapter<String>(LoadExcursion.this, 
				android.R.layout.simple_list_item_1, titleList));

		// ListView Item Click Listener
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				String label = excursionList.get(position).getTitle();
				Toast.makeText(getApplicationContext(), "Loading " + label + "(" + position +")", Toast.LENGTH_LONG).show();

				if (AppData.getCurrentExcursion().getObservationList().size() > 0 ||
							AppData.getCurrentExcursion().getRoute().size() > 0) {
					AppData.saveCurrentExcursion();
				}

				// Tries to create an excursion using data from the downloaded 
				// excursionList and calling the loadExcursion method
				
				if (position >= numLocalExcursions) {
					int serverListIndex = position - numLocalExcursions;
					
					String title = serverExcursionList[serverListIndex][0];  
					String description = serverExcursionList[serverListIndex][1];
					String createDate = serverExcursionList[serverListIndex][2];
					String oriAuthor = serverExcursionList[serverListIndex][3];
					String publisher = serverExcursionList[serverListIndex][4];
					int travelMode = Integer.parseInt(serverExcursionList[serverListIndex][5]);
					int shareMode = Integer.parseInt(serverExcursionList[serverListIndex][6]);
					
					Excursion excursion = CloudDB.loadExcursion (title, publisher, 
							description, createDate, oriAuthor, travelMode, shareMode);
					Log.d("Load Excursion", excursion.toString());

					if (excursion != null) {
						Toast.makeText(getApplicationContext(), "Setting remote excursion", Toast.LENGTH_LONG).show();
						AppData.setExcursion(excursion);
					}
					else{
						Toast.makeText(getApplicationContext(), "Failed to load excursion", 
								Toast.LENGTH_LONG).show();
					}
				}
				else {
					Toast.makeText(getApplicationContext(), "Setting local excursion", Toast.LENGTH_LONG).show();
					ExcursionListEntry chosen =  excursionList.get(position);
					AppData.loadExcursion(chosen);
				}	

				Intent showMain = new Intent(getApplicationContext(), MainScreen.class);
				startActivity(showMain);
			}
		});
	}

	public void backMain(View v)
	{
		Intent showMain = new Intent(getApplicationContext(), MainScreen.class);
		startActivity(showMain);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.load_excursion, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		int id = item.getItemId();	
		if (id == R.id.actionSettings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}