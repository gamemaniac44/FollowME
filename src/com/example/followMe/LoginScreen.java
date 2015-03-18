package com.example.followMe;

import com.example.followMe.obj.InvalidUserDataException;
import com.example.followMe.obj.User;
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
import android.widget.EditText;
import android.widget.Toast;

import com.example.followMe.R;

public class LoginScreen extends Activity {

	private static final String TAG = "LoginScreen";
	private int messageDuration = 80;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		// Allow AppToPHP to connect to server on main thread.
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        
		//open the database when the first screen of the application is displayed
		LocalDB.openDB(this);
	}

	public void verifyAccount(View V) {

		//get userName and password fields from layout file
		EditText nameField = (EditText) findViewById(R.id.loginUserNameEditText);
		EditText passwordField = (EditText) findViewById(R.id.loginPasswordEditText);

		//convert the userName and password input fields to strings for if check
		String userName = nameField.getText().toString();
		String password = passwordField.getText().toString();

		//check to make sure the user has entered a username and password
		if((userName.matches("")) || (password.matches("")))
		{
			Toast.makeText(getApplicationContext(), "Please enter your username and password.", Toast.LENGTH_SHORT).show();
			return;
		}

		// Attempt to verify the user account on application server
		int servLoginResult = CloudDB.verifyLogin(userName, password);
		Log.d(TAG, "Cloud login result: " +  CloudDB.sendResult(servLoginResult));

		// If user is found and credentials are validated, the current user is set 
		// and taken to the main screen

		switch (servLoginResult) {
		case CloudDB.SUCCESS:
			if (LocalDB.userNameExists(userName) == false) {
				User newUser = new User(userName, password);
				LocalDB.addUser(newUser);
			}
			break;
			
		case CloudDB.MISSINGDATA:
			Toast.makeText(getApplicationContext(), "Error sending data to the cloud server. Please contact support.", messageDuration).show();
			return;
		
		case CloudDB.USERNAMEDNE:
			Toast.makeText(getApplicationContext(), "Username does not exist.  Please create an account.", messageDuration).show();
			nameField.setText("");
			passwordField.setText("");
			return;
		
		case CloudDB.BADPASSWORD:
			Toast.makeText(getApplicationContext(), "Invalid Password, Please try again.", messageDuration).show();
			passwordField.setText("");
			return;

		case CloudDB.DBERROR:
		case CloudDB.CONNECTIONERROR:
			// No worries, they might not have cell service.  Check locally for an account.
			if (LocalDB.userNameExists(userName) == false) 
			{
				Toast.makeText(getApplicationContext(), "Username does not exist.  Please create an account.", messageDuration).show();
				nameField.setText("");
				passwordField.setText("");
				return;
			}
			else if (LocalDB.validPassword(userName, password) == false)
			{
				Toast.makeText(getApplicationContext(), "Invalid Password, Please try again.", messageDuration).show();
				passwordField.setText("");
				return;
			}
			// If here their username and password have been verified locally.
		}
		
		AppData.setCurrentUser(userName);

		Toast.makeText(getApplicationContext(), AppData.getCurrentUser().getUserName() + " signed in.", messageDuration).show();
		Intent intent = new Intent(getApplicationContext(), MainScreen.class);
		startActivity(intent);
	}

	/*
	 * This method will load the screen for registering for a new account
	 */
	public void newUserAccount(View V){
		setContentView(R.layout.create_account);
	}

	/*
	 * This method makes sure that all of the required fields for the user's information are filled out.
	 * The user's information is added to the database.
	 */
	public void registerAccount(View V)
	{
		//the user name field
		EditText userNameEdit = (EditText) findViewById(R.id.createAccountUserNameEditText);
		String userName = userNameEdit.getText().toString();

		//the email field
		EditText emailEdit = (EditText) findViewById(R.id.createAccountEmailEditText);
		String email = emailEdit.getText().toString();

		//the first name field
		EditText fNameEdit = (EditText) findViewById(R.id.createAccountFirstNameEditText);
		String firstName = fNameEdit.getText().toString();

		//the last name field
		EditText lNameEdit = (EditText) findViewById(R.id.createAccountLastNameEditText);
		String lastName = lNameEdit.getText().toString();

		//the password field
		EditText passwordEdit = (EditText) findViewById(R.id.createAccountPasswordEditText);
		String password = passwordEdit.getText().toString();

		//the confirm password field
		EditText passwordConfirmEdit = (EditText) findViewById(R.id.createAccountConfirmPasswordEditText);  // change to passwordConfirmEdit - REM
		String passwordConfirm = passwordConfirmEdit.getText().toString();

		/*** Check that the passwords match ***/
		if(!password.equals(passwordConfirm))
		{
			Toast.makeText(getApplicationContext(), "Passwords do not match.", messageDuration).show();
			passwordEdit.setText("");
			passwordConfirmEdit.setText("");
			return;
		}

		/*** Try to create a new user and if not, display toast and clear fields ***/
		User newUser = null;

		try {
			newUser = new User(userName, email, firstName, lastName, password);
		}
		catch (InvalidUserDataException e) 
		{
			switch (e.toString()) {

			case User.INVALID_USERNAME:
				Toast.makeText(getApplicationContext(), "Please use only alphanumeric characters and \n" +
						"make sure the username is at least 3 characters long and no more than 15 characters " +
						"long.", messageDuration).show();
				userNameEdit.setText("");
				return;

			case User.INVALID_EMAIL:
				Toast.makeText(getApplicationContext(), "Please make sure you are using a valid email address.", messageDuration).show();
				emailEdit.setText("");
				return;

			case User.INVALID_PASSWORD:
				Toast.makeText(getApplicationContext(), "Please make sure your password meets the requirements: \n " +
						"-no special characters \n " +
						"-be at least 8 characters long and no more than 25 characters\n " +
						"-start with a letter \n " +
						"-include at least 1 number", messageDuration).show();
				passwordEdit.setText("");
				passwordConfirmEdit.setText("");		
				return;
				
			default:
				Log.d(TAG, "Unspecified InvalidUserDataException caught in registeraccount()");
				finish();
			}
		}

		// All data is formatted correctly, attempt to add to cloud db.

		int servRegisterResult = CloudDB.registerUser(newUser);
		Toast.makeText(getApplicationContext(), CloudDB.sendResult(servRegisterResult), 
				messageDuration).show();

		switch (servRegisterResult) {
		
		case CloudDB.USERNAMEEXISTS:
			Toast.makeText(getApplicationContext(), "Username already exists, please try again.", 
					messageDuration).show();
			userNameEdit.setText("");
			return;

		case CloudDB.EMAILEXISTS:
			Toast.makeText(getApplicationContext(), "Email already exists, please try again.", 
					messageDuration).show();
			emailEdit.setText("");
			return;
			
		case CloudDB.DBERROR:
			Toast.makeText(getApplicationContext(), "Cloud server error. Please try again later.", 
					messageDuration).show();
			return;
			
		case CloudDB.CONNECTIONERROR:
			Toast.makeText(getApplicationContext(), "Can not get internet access. Please enable WI-FI or Data.", 
					messageDuration).show();
			return;
			
		case CloudDB.MISSINGDATA:
			Toast.makeText(getApplicationContext(), "Error sending data to the cloud server. Please contact support.", 
					messageDuration).show();
			return;
			
		case CloudDB.SUCCESS:
			if (LocalDB.userNameExists(userName) == true) {
				LocalDB.deleteUser(userName);
			}

			/*** We have a valid user.  Add user to database ***/
			int result = LocalDB.addUser(newUser);

			/*** The user was successfully added to the database ***/
			if (result != LocalDB.SUCCESS)
			{
				Toast.makeText(getApplicationContext(), "Local server error. Please reinstall the app.", 
						messageDuration).show();
				return;
			}
				
			Toast.makeText(getApplicationContext(), "Your Account has been created and logged in!", 
					messageDuration).show();
			Intent intent = new Intent(getApplicationContext(), MainScreen.class);
			startActivity(intent);

			AppData.setCurrentUser(newUser);
		}
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
		if (id == R.id.actionSettings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		LocalDB.closeDB();
	}
}