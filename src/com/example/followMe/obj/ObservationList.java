package com.example.followMe.obj;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class ObservationList extends ArrayList<Observation>{
	
	public ObservationList() 
	{
		super();
	}
	
	/********************************************************************
	 *  Modifiers
	 *********************************************************************/
	
	/*** 
	 * Data is validated, author and date are set automatically - used by Activities 
	 ***/
	
	public void addObservation(String title, String description, double longitude, double latitude) 
	throws InvalidObservationDataException 
	{
		super.add(new Observation(title, description, longitude, latitude));
	}
	
	/*** 
	 * No data validation is performed - used by DB 
	 ***/
	
	public void addObservation(String author, String title, String description, 
			double longitude, double latitude, String date) 
	{
		super.add(new Observation(author, title, description, longitude, latitude, date));
	}

	
}
