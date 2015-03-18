package com.example.followMe.util;

import java.util.Iterator;

import com.example.followMe.obj.Excursion;
import com.example.followMe.obj.ExcursionList;
import com.example.followMe.obj.GPSPoint;
import com.example.followMe.obj.InvalidExcursionDataException;
import com.example.followMe.obj.InvalidUserDataException;
import com.example.followMe.obj.Observation;
import com.example.followMe.obj.ObservationList;
import com.example.followMe.obj.User;
import com.example.followMe.obj.Route;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class LocalDB {
	// ERROR LOGGING TAG
	private static final String TAG = "DBQuery";

	// DB INFO
	private static final String DATABASE_NAME = "BCFollowMe_DB";
	private static final int DATABASE_VERSION = 2;	

	/* TABLE NAMES - use these in the SQL statements to avoid errors */
	private static final String USERS_T = "users_T";
	private static final String EXCURSIONS_T = "excursions_T";
	private static final String OBSERVATIONS_T = "observations_T";
	private static final String ROUTES_T = "routes_T";

	/* COLUMN NAMES - use these in the SQL statements to avoid errors */
	/* COLUMN NAMES for the users table*/
	private static final String U_ID = "userID";
	private static final String U_USERNAME = "userName";
	private static final String U_EMAIL = "email";
	private static final String U_FIRSTNAME = "firstName";
	private static final String U_LASTNAME = "lastName";
	private static final String U_PASSWORD = "password";

	/*COLUMN NAMES for the excursions table*/
	private static final String E_ID = "excursionID";
	private static final String E_TRAVELMODE = "travelMode";
	private static final String E_SHAREMODE = "shareMode";
	private static final String E_TITLE = "title";
	private static final String E_DESC = "description";
	private static final String E_ORIGINALAUTHOR = "originalAuthor";
	private static final String E_CREATIONDATE = "creationDate";

	/*COLUMN NAMES for the observations table*/
	private static final String O_ID = "observationID";
	private static final String O_AUTHOR = "author";
	private static final String O_TITLE = "title";
	private static final String O_DESC = "description";
	private static final String O_LONG = "logitude";
	private static final String O_LAT = "latitude";
	private static final String O_CREATIONDATE = "observationDate";

	/*COLUMN NAMES for the observations table*/
	private static final String R_COUNTER = "counter";
	private static final String R_LONG = "longitude";
	private static final String R_LAT = "latitude";

	/* RETURN CODES - Activities should check these */
	public static final int FAILURE = -1;
	public static final int SUCCESS = 0;
	public static final int USER_NAME_EXISTS = 1;

	/* db and helper objects */
	private static DatabaseHelper dBHelper = null;
	private static SQLiteDatabase db = null;

	/**************************************************************
	 * DATABASE METHODS
	 **************************************************************/

	/*** Open and Close the Database ***/

	public static void openDB(Context c) 
	{
		if (db == null) {
			dBHelper = new DatabaseHelper(c);
			db = dBHelper.getWritableDatabase();
		}
	}

	public static void closeDB()
	{
		db = null;
		dBHelper.close();
	}

	/**************************************************************
	 * USER METHODS
	 **************************************************************/

	public static boolean userNameExists(String userName)
	{
		assert(db != null);
		String query = 
				"select " + U_USERNAME + 					// be sure to add spaces
				" from " + USERS_T +
				" where " + U_USERNAME + "= ?"; 			// don't end query with ;

		String[] data = {userName};

		Cursor c = db.rawQuery(query, data);

		if (c == null || c.getCount() == 0)
			return false;
		else
			return true;
	}

	public static boolean validPassword(String userName, String passWord)
	{
		assert(db != null);
		String query = 
				"select " + U_PASSWORD +
				" from " + USERS_T +
				" where " + U_USERNAME + "= ?" ;

		String[] data = {userName};

		Cursor c = db.rawQuery(query, data);
		if (c == null || c.getCount() == 0)
			return false;

		c.moveToFirst();
		String dbPassword = c.getString(c.getColumnIndex(U_PASSWORD));

		if (dbPassword.equals(passWord))
			return true;
		else
			return false;
	}

	public static User getUser(String userName) throws InvalidUserDataException
	{
		assert(db != null);	
		String query = 
				"select * " +
				" from " + USERS_T +
				" where " + U_USERNAME + "= ?" ;

		String[] data = {userName};

		Cursor c = db.rawQuery(query, data);
		if (c == null || c.getCount() == 0)
			return null;

		c.moveToFirst();
		
		String email = c.getString(c.getColumnIndex(U_EMAIL));
		String firstName = c.getString(c.getColumnIndex(U_FIRSTNAME));
		String lastName = c.getString(c.getColumnIndex(U_LASTNAME));
		String password = c.getString(c.getColumnIndex(U_PASSWORD));
		
		return new User(userName, email, firstName, lastName, password);
	}

	public static int addUser(User user)
	{
		assert(db != null);

		if (userNameExists(user.getUserName()))
			return USER_NAME_EXISTS;

		ContentValues values = new ContentValues(5);
		values.put(U_USERNAME, user.getUserName());
		values.put(U_EMAIL, user.getEmail());
		values.put(U_FIRSTNAME, user.getFirstName());
		values.put(U_LASTNAME, user.getLastName());
		values.put(U_PASSWORD, user.getPassword());

		long results = db.insert(USERS_T, null, values);

		if (results == -1)
			return FAILURE;
		else
			return SUCCESS;
	}
	
	public static int deleteUser(String username)
	{
		assert(db != null);
		
		Log.d(TAG, "Deleting: " + username);
		
		int userId = getUserId(username);
		Log.d(TAG, "Deleting: " + userId);
		
		int result = db.delete(USERS_T, U_ID + " = " + userId, null);
		
		if (result == 0)
			Log.d(TAG, "no rows deleted from excursion table");
		
		return result;
		
	}
	
	/**************************************************************
	 *  PRIVATE USER METHODS
	 **************************************************************/
	
	private static int getCurrentUserId() 
	{
		assert(db != null);

		return getUserId(AppData.getCurrentUser().getUserName());
	}
	
	private static int getUserId(String username) 
	{
		assert(db != null);

		String query = 
				"select " + U_ID +
				" from " + USERS_T +
				" where " + U_USERNAME + "= ?" ;

		String[] data = {username};

		Cursor c = db.rawQuery(query, data);
		if (c == null || c.getCount() == 0)
			return -1;

		c.moveToFirst();
		int userId = c.getInt(c.getColumnIndex(U_ID));
		Log.d(TAG, "userid = " + userId);
		
		return userId;
	}
	
	

	/**************************************************************
	 *  EXCURSION METHODS
	 **************************************************************/

	/***
	 * No two excursions can have the same userId, title, original author and creation date.
	 * Check if an excursion already exists with the current excursion data.
	 ***/

	public static boolean excursionExists()
	{
		assert(db != null);
		Excursion e = AppData.getCurrentExcursion();

		int excursionId = getExcursionId(getCurrentUserId(), e.getTitle(), e.getOriginalAuthor(), e.getCreationDate());

		if (excursionId > 0)
			return true;
		else
			return false;
	}

	/***
	 *  No two excursions can have the same userId, title, original author and creation date.
	 *  If the excursion exists, delete it, then add the excursion
	 ***/

	public static int saveExcursion() 
	{
		assert(db != null);
		Excursion e = AppData.getCurrentExcursion();

		int excursionId = getExcursionId(getCurrentUserId(), e.getTitle(), e.getOriginalAuthor(), e.getCreationDate());

		if (excursionId > 0) {
			deleteExcursion(excursionId);
		}

		ContentValues values = new ContentValues(6);

		values.put(U_ID, getCurrentUserId());
		values.put(E_TRAVELMODE, e.getTravelMode());
		values.put(E_SHAREMODE, e.getShareMode());
		values.put(E_TITLE, e.getTitle());
		values.put(E_DESC, e.getDescription());
		values.put(E_ORIGINALAUTHOR, e.getOriginalAuthor());
		values.put(E_CREATIONDATE, e.getCreationDate());

		long results = db.insert(EXCURSIONS_T, null, values);

		if (results == -1)
			return FAILURE;

		excursionId = getExcursionId(getCurrentUserId(), e.getTitle(), e.getOriginalAuthor(), e.getCreationDate());	

		ObservationList observations = e.getObservationList();

		if (observations.size() > 0) {
			Iterator<Observation> it = observations.iterator();
			Observation curObservation = null;
			while(it.hasNext()) {
				curObservation = it.next();

				values = new ContentValues(7);
				values.put(E_ID, excursionId);
				values.put(O_AUTHOR, curObservation.getAuthor());
				values.put(O_TITLE, curObservation.getTitle());
				values.put(O_DESC, curObservation.getDescription());
				values.put(O_LONG, curObservation.getLongitude());
				values.put(O_LAT, curObservation.getLatitude());
				values.put(O_CREATIONDATE, curObservation.getCreationDate());

				results = db.insert(OBSERVATIONS_T, null, values);

				if (results == -1)
					return FAILURE;
			}
		}

		Route route = e.getRoute();
		if (route.size() > 0) {
			Iterator<GPSPoint> it = route.iterator();
			GPSPoint curPoint = null;
			int counter = 1;
			while (it.hasNext()) {
				curPoint = it.next();

				values = new ContentValues(4);
				values.put(E_ID, excursionId);
				values.put(R_COUNTER, counter++);
				values.put(R_LONG, curPoint.getLongitude());
				values.put(R_LAT, curPoint.getLatitude());

				results = db.insert(ROUTES_T, null, values);

				if (results == -1)
					return FAILURE;
			}
		}

		return SUCCESS;
	}

	/***
	 * Returns a list of excursions for the current user.
	 * The list includes the excursionId, title, original author and creation date.
	 * No two excursions can have the same userId, title, original author and creation date.
	 ***/

	public static ExcursionList getExcursionList() 
	{	
		assert(db != null);
		ExcursionList list = new ExcursionList();

		String query = 
				"select " + E_ID + "," + E_TITLE + "," + E_ORIGINALAUTHOR + "," + E_CREATIONDATE +
				" from " + EXCURSIONS_T +
				" where " + EXCURSIONS_T + "." + U_ID + " = " + getCurrentUserId();

		Cursor c = db.rawQuery(query, null);
		if (c == null || c.getCount() == 0)
			return list;

		int excursionId = 0;
		String title = null;
		String originalAuthor = null;
		String creationDate = null;

		while (c.moveToNext()) {
			excursionId = c.getInt(c.getColumnIndex(E_ID));
			title = c.getString(c.getColumnIndex(E_TITLE));
			originalAuthor = c.getString(c.getColumnIndex(E_ORIGINALAUTHOR));
			creationDate = c.getString(c.getColumnIndex(E_CREATIONDATE));

			list.addEntry(excursionId, title, originalAuthor, creationDate);
		}

		return list;
	}

	/***
	 *  Get the excursion with the given excursion id.
	 *  This method is used by the LoadExcursion Activity.
	 * @throws InvalidExcursionDataException 
	 ***/

	public static Excursion getExcursion(int excursionId) 
	throws InvalidExcursionDataException
	{
		assert(db != null);
		// Get the Route
		String query =
				"select * " +
						" from " + ROUTES_T +
						" where " + E_ID + " = " + excursionId +
						" order by " + R_COUNTER;

		Cursor c = db.rawQuery(query, null);
		Route route = new Route();

		if (c != null && c.getCount() > 0) {
			double longitude = 0;
			double latitude = 0;
			while(c.moveToNext()) {
				longitude = c.getDouble(c.getColumnIndex(R_LONG));
				latitude = c.getDouble(c.getColumnIndex(R_LAT));
				route.addGPSPoint(longitude, latitude);
			}
		}

		// Get the Observations
		query = "select * " +
				" from " + OBSERVATIONS_T +
				" where " + E_ID + " = " + excursionId;

		c = db.rawQuery(query, null);
		ObservationList observations = new ObservationList();

		if (c != null && c.getCount() > 0) {
			String author = null;
			String title = null;
			String description = null;
			double longitude = 0;
			double latitude = 0;
			String creationDate = null;

			while(c.moveToNext()) {
				author = c.getString(c.getColumnIndex(O_AUTHOR));
				title = c.getString(c.getColumnIndex(O_TITLE));
				description = c.getString(c.getColumnIndex(O_DESC));
				longitude = c.getDouble(c.getColumnIndex(O_LONG));
				latitude = c.getDouble(c.getColumnIndex(O_LAT));
				creationDate = c.getString(c.getColumnIndex(O_CREATIONDATE));

				observations.addObservation(author, title, description, longitude, latitude, creationDate);
			}
		}

		// Get the Excursion from the database
		query = "select * " +
				" from " + EXCURSIONS_T +
				" where " + E_ID + " = " + excursionId;

		c = db.rawQuery(query, null);
		if (c == null || c.getCount() == 0)
			return null;

		c.moveToFirst();
		int travelMode = c.getInt(c.getColumnIndex(E_TRAVELMODE));
		int shareType = c.getInt(c.getColumnIndex(E_SHAREMODE));
		String title = c.getString(c.getColumnIndex(E_TITLE));
		String description = c.getString(c.getColumnIndex(E_DESC));
		String originalAuthor = c.getString(c.getColumnIndex(E_ORIGINALAUTHOR));
		String creationDate = c.getString(c.getColumnIndex(E_CREATIONDATE));

		return new Excursion(travelMode, shareType, title, description, route, observations, originalAuthor, creationDate);
	}

	/***
	 * Delete the excursion with the given excursion id.
	 ***/

	public static int deleteExcursion(Excursion e)
	{
		assert(db != null);

		int excursionId = getExcursionId(e);
		
		if (excursionId == -1)
			return 0;
			
		return deleteExcursion(excursionId);
	}
	
	/**************************************************************
	 *  PRIVATE EXCURSION METHODS
	 **************************************************************/

	private static int getExcursionId(Excursion e)
	{
		return getExcursionId(getCurrentUserId(), e.getTitle(), e.getOriginalAuthor(), e.getCreationDate());
	}
	
	/***
	 * No two excursions can have the same userId, title, original author and creation date.
	 * This private method gets the excursion id of the excursion with these values.
	 ***/

	private static int getExcursionId(int userId, String title, String originalAuthor, String creationDate)
	{
		assert(db != null);

		String query = 
				"select " + E_ID +
				" from " + EXCURSIONS_T +
				" where " + U_ID + " = " + userId + 
				" and " + E_TITLE + " = '" + title + "'" +
				" and " + E_ORIGINALAUTHOR + " = '" + originalAuthor + "'" + 
				" and " + E_CREATIONDATE + " = '" + creationDate + "'";
		
		Cursor c = db.rawQuery(query, null);
		if (c == null || c.getCount() == 0)
			return -1;

		c.moveToFirst();
		return c.getInt(c.getColumnIndex(E_ID));
	}

	/***
	 * Delete the excursion with the given excursion id.
	 ***/

	private static int deleteExcursion(int excursionId)
	{
		assert(db != null);

		Log.d(TAG, "Deleting: " + excursionId);
		
		int result = db.delete(EXCURSIONS_T, E_ID + " = " + excursionId, null);
		
		if (result == 0)
			Log.d(TAG, "no rows deleted from excursion table");
		
		return result;
	}
	
	/**************************************************************
	 *  Private class which handles database creation and upgrading.
	 **************************************************************/

	private static class DatabaseHelper extends SQLiteOpenHelper
	{
		private String CREATE_USERS_T = 
				"create table if not exists " + USERS_T + " (" +
						U_ID + " integer primary key autoincrement, " +
						U_USERNAME + " text not null, " +
						U_FIRSTNAME + " text not null, " +
						U_LASTNAME + " text not null, " +
						U_PASSWORD + " text not null, " +
						U_EMAIL + " text not null);";				// we do include ; here

		private String CREATE_EXCURSIONS_T =
				"create table if not exists " + EXCURSIONS_T + " (" +
						E_ID + " integer primary key autoincrement, " +
						U_ID + " int not null, " +
						E_TRAVELMODE + " int not null, " +
						E_SHAREMODE + " int not null, " +
						E_TITLE + " text not null, " +
						E_DESC + " text, " +
						E_ORIGINALAUTHOR + " text not null, " +
						E_CREATIONDATE + " text not null, " +
						" foreign key (" + U_ID + ") references " + USERS_T + "(" + U_ID + ") " +
						" on delete cascade);";

		private String CREATE_OBSERVATIONS_T = 
				"create table if not exists " + OBSERVATIONS_T + " (" +
						O_ID + " integer primary key autoincrement, " +
						E_ID + " integer not null, " +
						O_AUTHOR + " text not null, " +
						O_TITLE + " text, " +
						O_DESC + " text, " +
						O_LONG + " integer not null, " +
						O_LAT + " integer not null, " +
						O_CREATIONDATE + " text not null, " +
						" foreign key (" + E_ID + ") references " + EXCURSIONS_T + "(" + E_ID + ") " +
						" on delete cascade);";

		private String CREATE_ROUTES_T =
				"create table if not exists " + ROUTES_T + " (" +
						E_ID + " integer not null, " +
						R_COUNTER + " integer not null, " +
						R_LONG + " integer not null, " +
						R_LAT + " integer not null, " +
						" primary key (" + E_ID + "," + R_COUNTER + ")," +
						" foreign key (" + E_ID + ") references " + EXCURSIONS_T + "(" + E_ID + ") " +
						" on delete cascade);";

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase _db) {
			_db.execSQL(CREATE_USERS_T);
			_db.execSQL(CREATE_EXCURSIONS_T);
			_db.execSQL(CREATE_OBSERVATIONS_T);
			
			_db.execSQL(CREATE_ROUTES_T);
		}

		@Override
		public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading application's database from version " + oldVersion
					+ " to " + newVersion + ", which will destroy all old data!");

			// Destroy old database:
			_db.execSQL("DROP TABLE IF EXISTS " + USERS_T);
			_db.execSQL("DROP TABLE IF EXISTS " + EXCURSIONS_T);			
			_db.execSQL("DROP TABLE IF EXISTS " + OBSERVATIONS_T);			
			_db.execSQL("DROP TABLE IF EXISTS " + ROUTES_T);			

			// Recreate new database:
			onCreate(_db);
		}
	}
}