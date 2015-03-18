package com.example.followMe.util;

//import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.followMe.obj.Excursion;
import com.example.followMe.obj.InvalidExcursionDataException;
import com.example.followMe.obj.ObservationList;
import com.example.followMe.obj.Route;
import com.example.followMe.obj.User;
import com.google.gson.Gson;

import android.annotation.SuppressLint;
import android.util.Log;


@SuppressLint("UseSparseArrays") public class CloudDB 
{
	private static final String TAG1 = "loadExcursionPHP";
	private static final String TAG2 = "getExcursionListPHP";
	private static final String TAG3 = "publishExcursionPHP";
	private static final String TAG4 = "loginPHP";
	
	private static final String URL = "http://bc-followme.azurewebsites.net/php/login.php";
	private static final String REG_URL = "http://bc-followme.azurewebsites.net/php/createAccount.php";
	private static final String EXC_URL = "http://bc-followme.azurewebsites.net/php/getExcursionList.php";
	private static final String PUB_URL = "http://bc-followme.azurewebsites.net/php/publishExcursion.php";
	private static final String LOAD_EXC_URL = "http://bc-followme.azurewebsites.net/php/loadExcursion.php";
	
	public static final int SUCCESS = 0;
	public static final int DBERROR = 1;
	public static final int BADPASSWORD = 2;
	public static final int USERNAMEEXISTS = 3;
	public static final int EMAILEXISTS = 4;
	public static final int EXCURSIONEXISTS = 5;
	public static final int EXCURSIONDNE = 6;
	public static final int USERNAMEDNE = 7;
	public static final int MISSINGDATA = 8;
	public static final int CONNECTIONERROR = -1;
	
	//private static final String URL = "http://appdbtest.azurewebsites.net/login.php";
	
	private static Map<Integer, String> map = new HashMap<Integer, String>(5);
	
	public static String sendResult (int returnCode){
		map.put(SUCCESS, "SUCCESS");
		map.put(DBERROR, "DATABASE ERROR");
		map.put(BADPASSWORD, "INVALID PASSWORD");
		map.put(USERNAMEEXISTS, "USER ALREADY EXISTS");
		map.put(EMAILEXISTS, "EMAIL ALREADY EXISTS");
		map.put(EXCURSIONEXISTS, "EXCURSION ALREADY EXISTS");
		map.put(EXCURSIONDNE, "EXCURSION DOES NOT EXIST");
		map.put(USERNAMEDNE, "USER NAME DOES NOT EXIST");
		map.put(MISSINGDATA,  "SERVER IS MISSING DATA");
		
		map.put(CONNECTIONERROR, "Could not connect to database");
		
		String text = map.get(returnCode);
		return text;
	}
	
	/************************************************************************
	 * Gets the list of available excursions from the server database
	 ************************************************************************/
	
	public static String[][] getExcursionList() {
		String[][] data = null;
		JSONObject jsonSend = new JSONObject();
		
		// Send HTTP Post Request and get the returned JSON object
		JSONObject jsonRecv = HttpClient.sendPost(EXC_URL, jsonSend);
		Log.d(TAG2, "sendPost returned: " + jsonRecv);
		
		if(jsonRecv == null){
			Log.d(TAG2, "Received null object from sendPost() call");
			return null;
		}
		
		//Gets the return code
		int returnCode = CONNECTIONERROR;
		
		try {
			returnCode = jsonRecv.getInt("returnCode");
		} 
		catch (JSONException e) {
			Log.i(TAG2, "'returnCode' not in JSON object");
			e.printStackTrace();
			return null;
		}
		
		Object jsonArrayObject = null;
		String jsonArrayString = null;
		
		//if a successful return code is retrieved, the method tries to pull the excursion list data
		if(returnCode == SUCCESS){
			try{
				jsonArrayObject = jsonRecv.get("excursion");
				Log.d(TAG2, "JSON test object: " + jsonArrayObject);
			} 
			catch(JSONException e){
				e.printStackTrace();
				return null;
			}
			
			//Json array is converted to a string
			jsonArrayString = jsonArrayObject.toString();
			Log.d(TAG2, "jsonString: " + jsonArrayString);
			
			//String is converted to a multidimensional string array
			/*******The code seems to break here, may be a problem in the conversion process**********/
			Gson gson = new Gson();
			data = gson.fromJson(jsonArrayString, String[][].class);
		}
		
		return data;
	}
	
	/************************************************************************
	 * Loads excursions from the server database
	 ************************************************************************/
	
	public static Excursion loadExcursion(	
			String title, String publisher, String description, 
			String createDate, String oriAuthor, int travelMode, int shareMode) 
	{	
		Excursion servExcursion = null;
		String[][] obsData = null;
		String[][] coordsData = null;
		// Create JSONObject
		JSONObject jsonSend = new JSONObject();
				
		try {
			// Put data in JSONObject
			jsonSend.put("username", publisher);
			jsonSend.put("title", title);
			Log.i(TAG1, jsonSend.toString());
		} 
		catch (JSONException e) {
			Log.i(TAG1, "Error packing login data");
			e.printStackTrace();
			return null;
		}
		
		JSONObject jsonRecv = HttpClient.sendPost(LOAD_EXC_URL, jsonSend);
		
		if (jsonRecv == null)
			return null;

		int returnCode = CONNECTIONERROR;
		
		try {
			returnCode = jsonRecv.getInt("returnCode");
		} 
		catch (JSONException e) {
			Log.i(TAG1, "'returnCode' in JSON object");
			e.printStackTrace();
			return null;
		}
		
		Object jsonArrayObject = null;
		if(returnCode == SUCCESS){
			//Pulls observations from json object and converts them to a string array
			try{
				jsonArrayObject = jsonRecv.get("observations");
				Log.d(TAG1, "JSON test object: " + jsonArrayObject);
			} 
			catch(JSONException e){
				e.printStackTrace();
				return null;
			}
			
			String jsonArrayString = jsonArrayObject.toString();
			
			Gson gson = new Gson();
			obsData = gson.fromJson(jsonArrayString, String[][].class);
			
			//Pulls coordinates from json object and converts them to a string array
			try{
				jsonArrayObject = jsonRecv.get("coordinates");
				Log.d(TAG1, "JSON test object: " + jsonArrayObject);
			} 
			catch(JSONException e){
				e.printStackTrace();
				return null;
			}
			
			jsonArrayString = jsonArrayObject.toString();
			coordsData = gson.fromJson(jsonArrayString, String[][].class);
		}
		
		//if the observationList and coordinate list were put into an array successfully, an excursion is created
		if(obsData != null && coordsData != null) {
			Route excRoute = new Route();
			ObservationList excObs = new ObservationList();
			
			//loops through obsData and adds observations to a new observationList
			for(int i=0; i<obsData.length; i++){
				excObs.addObservation(obsData[i][5], obsData[i][0], obsData[i][1], Double.parseDouble(obsData[i][3]), Double.parseDouble(obsData[i][2]), obsData[i][4]);
			}
			//loops through coordsData and adds coordinates to a new Route
			for(int i=0; i<coordsData.length; i++){
				excRoute.addGPSPoint(Double.parseDouble(coordsData[i][1]), Double.parseDouble(coordsData[i][0]));
			}
			//constructs the excursion
			try {
				servExcursion = new Excursion(travelMode, shareMode, title, 
						description, excRoute, excObs, publisher, createDate);
			} 
			catch (InvalidExcursionDataException e) {
				Log.d(TAG2, e.toString());
				return null;
			}
		}
		return servExcursion;
	}
	
	/************************************************************************
	 * Publishes excursions to the server database
	 ************************************************************************/
	//This whole method is a train wreck. Most of it is failed attempts to package multidimensional arrays as json
	public static int publishExcursion() 
	{
		Excursion excursion = AppData.getCurrentExcursion();
		String[] excursionData = new String[7];	
		
		excursionData[0] = excursion.getTitle();
		excursionData[1] = excursion.getDescription();
		excursionData[2] = excursion.getCreationDate();
		excursionData[3] = excursion.getPublisher();
		excursionData[4] = excursion.getOriginalAuthor();
		excursionData[5] = Integer.toString(excursion.getShareMode());
		excursionData[6] = Integer.toString(excursion.getTravelMode());
	
		ObservationList observationList = excursion.getObservationList();
		int numObservations = observationList.size();
		String[][] observationData = new String[numObservations][6];
		
		for (int i = 0; i < numObservations; i++) {
			observationData[i][0] = observationList.get(i).getTitle();
			observationData[i][1] = observationList.get(i).getDescription();
			observationData[i][2] = String.valueOf(observationList.get(i).getLatitude());
			observationData[i][3] = String.valueOf(observationList.get(i).getLongitude());
			observationData[i][4] = observationList.get(i).getCreationDate();
			observationData[i][5] = observationList.get(i).getAuthor();
		}
		
		Route route = excursion.getRoute();
		int numRoutePoints = route.size();
		String[][] routeData = new String[numRoutePoints][3];
		
		for (int i = 0; i < numRoutePoints; i++) {
			routeData[i][0] = String.valueOf(route.get(i).getLatitude());
			routeData[i][1] = String.valueOf(route.get(i).getLongitude());
			routeData[i][2] = String.valueOf(i);
		}
		
		JSONObject jsonSend = new JSONObject();
		
		try {
			jsonSend.put("excursion", new JSONArray(excursionData));
			jsonSend.put("observations", new JSONArray(observationData));
			jsonSend.put("coordinates", new JSONArray(routeData));
			Log.i(TAG3, jsonSend.toString());
		} 
		catch (JSONException e) {
			Log.i(TAG3, "Error packing login data");
			e.printStackTrace();
			return CONNECTIONERROR;
		}
		
		JSONObject jsonRecv = HttpClient.sendPost(PUB_URL, jsonSend);
		if (jsonRecv == null)
			return CONNECTIONERROR;
		
		int returnCode = CONNECTIONERROR;
		
		try {
			returnCode = jsonRecv.getInt("returnCode"); 
		} 
		catch (JSONException e) {
			Log.i(TAG3, "'returnCode' not in JSON object");
			e.printStackTrace();
		}
		
		return returnCode;
	}
	
	/************************************************************************
	 * Sends login information to the server for validation
	 ************************************************************************/
	
	public static int verifyLogin(String userName, String password) 
	{	
		JSONObject jsonSend = new JSONObject();

		try {
			// Put data in JSONObject
			jsonSend.put("username", userName);
			jsonSend.put("password", password);
			Log.i(TAG4, jsonSend.toString());
		} 
		catch (JSONException e) {
			Log.i(TAG4, "Error packing login data");
			e.printStackTrace();
			return CONNECTIONERROR;
		}

		// Send HTTP Post Request and get the returned JSON object
		JSONObject jsonRecv = HttpClient.sendPost(URL, jsonSend);
		if (jsonRecv == null)
			return CONNECTIONERROR;
		
		int returnCode = CONNECTIONERROR;
		
		try {
			returnCode = jsonRecv.getInt("returnCode"); 
		} 
		catch (JSONException e) {
			Log.i(TAG4, "'returncode' not in JSON object");
			e.printStackTrace();
		}

		return returnCode;
	}
	
	/************************************************************************
	 * Sends new user information to the server database
	 ************************************************************************/
	
	public static int registerUser(User tempUser) 
	{		
		// Create JSONObject
		JSONObject jsonSend = new JSONObject();
		
		String username, password, email, fName, lName;
		username = tempUser.getUserName();
		password = tempUser.getPassword();
		email = tempUser.getEmail();
		fName = tempUser.getFirstName();
		lName = tempUser.getLastName();
		
		try {
			// Put data in JSONObject
			jsonSend.put("username", username);
			jsonSend.put("password", password);
			jsonSend.put("email", email);
			jsonSend.put("firstName", fName);
			jsonSend.put("lastName", lName);
			Log.i(TAG4, jsonSend.toString());
		} 
		catch (JSONException e) {
			Log.i(TAG4, "Error packing login data");
			e.printStackTrace();
			return CONNECTIONERROR;
		}

		// Send HTTP Post Request and get the returned JSON object
		JSONObject jsonRecv = HttpClient.sendPost(REG_URL, jsonSend);
		if (jsonRecv == null)
			return CONNECTIONERROR;
		
		int returnCode = CONNECTIONERROR;
		
		try {
			returnCode = jsonRecv.getInt("returnCode");
		} 
		catch (JSONException e) {
			Log.i(TAG4, "'returncode' not in JSON object");
			e.printStackTrace();
		}

		return returnCode;
	}
	
	
}


