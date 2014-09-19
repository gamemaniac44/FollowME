package com.example.followMe;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	DBAdapter appDB;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		openDatabase();
		
	}

	private void openDatabase() {
		// TODO Auto-generated method stub
		appDB = new DBAdapter(this);
		appDB.open();
	}
	
	private void closeDatabase() {
		// TODO Auto-generated method stub
		appDB.close();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void submitAccount(View V) {
		
		//get username and password fields from layout file
		EditText nameField = (EditText) findViewById(R.id.userNameInput);
		EditText passwordField = (EditText) findViewById(R.id.userPassword);
		
		//convert the username and password input fields to strings for if check
		String userNameCheck = nameField.getText().toString();
		String userPasswordCheck = passwordField.getText().toString();
		
		//check to see if the user has entered their username and password
		if((userNameCheck.matches("")) || (userPasswordCheck.matches("")))
		{
			Toast.makeText(getApplicationContext(), "Please enter your username and password", Toast.LENGTH_LONG).show();
		}
		//if the user HAS NOT entered their username and/or password, then display an error message
		else
		{
			Toast.makeText(getApplicationContext(), "Signing in", Toast.LENGTH_LONG).show();
			Intent intent = new Intent(getApplicationContext(), Trail_Track.class);
			startActivity(intent);
		}
	}
	
	public void newUserAccount(View V){
		setContentView(R.layout.register_user);
	}
	
	public void registerAccount(View V)
	{
		//setContentView(R.layout.register_user);
		//Toast.makeText(getApplicationContext(), "Got here", Toast.LENGTH_LONG).show();
		
		EditText password = (EditText) findViewById(R.id.passwordEdit);
		String userPassword = password.getText().toString();
		password = (EditText) findViewById(R.id.passwordConfirm);
		String userConfirm = password.getText().toString();
		 
		if((userPassword.equals(userConfirm)) && (!userPassword.matches("")))
		{
			
			Toast.makeText(getApplicationContext(), "Your Account has been created and logged in!", Toast.LENGTH_LONG).show();
			Intent intent = new Intent(getApplicationContext(), Trail_Track.class);
			startActivity(intent);
		}
		else
		{
			if(!userPassword.equals(userConfirm))
			{
				Toast.makeText(getApplicationContext(), "Passwords Do Not Match!!!", Toast.LENGTH_LONG).show();
			}
			if((userPassword.matches(""))|| (userConfirm.matches("")))
			{
				Toast.makeText(getApplicationContext(), "Invalid Password!", Toast.LENGTH_LONG).show();
			}
		}
	}
	
	protected void onDestroy()
	{
		super.onDestroy();
		closeDatabase();
	}
}
