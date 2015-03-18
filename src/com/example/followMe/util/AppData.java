package com.example.followMe.util;

import android.util.Log;

import com.example.followMe.obj.Excursion;
import com.example.followMe.obj.ExcursionListEntry;
import com.example.followMe.obj.InvalidExcursionDataException;
import com.example.followMe.obj.InvalidUserDataException;
import com.example.followMe.obj.User;

public class AppData {
	
	private final static String TAG = "AppData";
	
	/*********************************************************
	 * After a user logs in, exactly one User and exactly one 
	 * Excursion is in memory at all times.
	 *********************************************************/
	
	private static User currentUser = null;
	private static Excursion currentExcursion = null;
	
	/*********************************************************
	 * Get the current User and current Excursion
	 *********************************************************/
	
	/***
	 * Get information about the current user.
	 * 
	 */
	public static User getCurrentUser()
	{
		return currentUser;
	}
	
	/***
	 * Get the current excursion.
	 * 
	 */
	public static Excursion getCurrentExcursion()
	{
		return currentExcursion;
	}
	
	/*********************************************************
	 * Set the current User
	 *********************************************************/
	
	/***
	 * User just logged in.  Username and password have been verified.
	 * Method attempts to get User and create a default Excursion
	 * If exception is thrown, db is probably down and app is of no use.
	 * There is nothing for the caller to do so we terminate the program.
	 *             
	 */
	
	public static void setCurrentUser(String userName) 
	{
		try {
			currentUser = LocalDB.getUser(userName);
			currentExcursion = new Excursion();
		} 
		catch (InvalidUserDataException e) {
			e.printStackTrace();
			android.os.Process.killProcess(android.os.Process.myPid());
		}
	}
	
	/***
	 * User just created an account. Data has been validated by LocalDB
	 * prior to this call.  Default excursion is created.
	 *              
	 */
	public static void setCurrentUser(User user) 
	{
		currentUser = user;
		currentExcursion = new Excursion();
	}
	
	/*********************************************************
	 * Create new Excursion and set it as current excursion
	 *********************************************************/
	
	/***
	 * Create a new default excursion.
	 *              
	 */
	public static void createNewExcursion() 
	{
		currentExcursion = new Excursion();	
	}
	
	/*********************************************************
	 * Save the current Excursion
	 *********************************************************/
	
	/***
	 * User want to save a change to an excursion. Returns
	 * LocalDB.SUCCESS or LocalDB.FAILURE.  We terminate if not SUCCESS.
	 * 
	 */
	public static void saveCurrentExcursion()
	{
		if (LocalDB.saveExcursion() != LocalDB.SUCCESS) {
			Log.d(TAG, "Error saving excursion in AppData");
			android.os.Process.killProcess(android.os.Process.myPid());
		}
	}
	
	/*********************************************************
	 * Delete the current Excursion, create a new one
	 *********************************************************/
	
	/***
	 * User has no use for the current excursion. This method returns the 
	 * number of rows that were deleted. It should delete just one.
	 * We terminate if not 1.
	 */
	public static void deleteCurrentExcursion() 
	{
		int result = LocalDB.deleteExcursion(currentExcursion);
		
		if (result != 1) {
			Log.d(TAG, "Error deleting excursion in AppData");
			android.os.Process.killProcess(android.os.Process.myPid());			
		}
				
		currentExcursion = new Excursion(); 
	}

	/*********************************************************
	 * Load a different Excursion
	 *********************************************************/
	
	/*** 
	 * User wants to load an excursion found in the ExcursionList. If 
	 * it can't be found in the db, there is a problem. We terminate the
	 * program here.
	 */
	 
	public static void loadExcursion(ExcursionListEntry entry)
	{
		Excursion e = null;
		try {
			e = LocalDB.getExcursion(entry.getExcursionId());
			currentExcursion = e;
		} 
		catch (InvalidExcursionDataException e1) {
			e1.printStackTrace();
			android.os.Process.killProcess(android.os.Process.myPid());
		}
		
	}
	
	/*** 
	 * User wants to load an excursion found in the ExcursionList that 
	 * was downloaded from the application server.
	 */
	
	public static void setExcursion(Excursion excursion){
		currentExcursion = excursion;
	}
}