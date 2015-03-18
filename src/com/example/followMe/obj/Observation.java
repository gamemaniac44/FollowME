package com.example.followMe.obj;

import java.text.DateFormat;
import java.util.Date;

import com.example.followMe.util.AppData;

public class Observation {
	
	public static final String INVALID_NAME = "Invalid name.";
	public static final String INVALID_DESCRIPTION = "Invalid description.";
	
	public static final int MAX_TITLE_LEN = 40;
	public static final int MAX_DESC_LEN = 250;
	
	private String author = null;
	private String title = null;
	private String description = null;
	private GPSPoint location = null;
	private String creationDate = null;
	
	/********************************************************************
	 *  Constructors
	 *********************************************************************/
	
	/*** 
	 * Data is validated; author and date are set automatically; throws exception 
	 ***/
	
	public Observation(String title, String description, double longitude, double latitude) 
	throws InvalidObservationDataException
	{
		this.author = AppData.getCurrentUser().getUserName();
		setTitle(title);
		setDescription(description);
		this.location = new GPSPoint(longitude, latitude);
		this.creationDate = DateFormat.getDateTimeInstance().format(new Date());
	}
	
	/*** 
	 * No data validation is performed - used only by DB 
	 ***/
	
	public Observation(String author, String title, String description, double longitude, double latitude, String date) 
	{
		this.author = author;
		this.title = title;
		this.description = description;
		this.location = new GPSPoint(longitude, latitude);
		this.creationDate = date;
	}
	
	/********************************************************************
	 *  Modifiers
	 *  
	 *  We do not allow the author or creationDate to be modified.
	 *********************************************************************/
	
	public void setTitle(String title) 		
	throws InvalidObservationDataException 
	{ 
		if (title.length() > 0 && title.length() <= MAX_TITLE_LEN)
			this.title = title; 
		else
			throw new InvalidObservationDataException(INVALID_NAME);
	}
	
	public void setDescription(String description) 
	throws InvalidObservationDataException 
	{
		if (description.length() > 0 && description.length() <= MAX_DESC_LEN)
			this.description = description;
		else
			throw new InvalidObservationDataException(INVALID_DESCRIPTION);
	}
	
	public void setLongitude(double longitude) 
	{ 
		location.setLongitude(longitude); 
	}
	
	public void setLatitude(double latitude) 
	{ 
		location.setLatitude(latitude); 
	}
	
	/********************************************************************
	 *  Accessors
	 *********************************************************************/
	
	public String getAuthor()			{ return author;					}
	public String getTitle() 			{ return title; 					}
	public String getDescription() 		{ return description; 				}
	public double getLongitude() 		{ return location.getLongitude(); 	}
	public double getLatitude()			{ return location.getLatitude();	}
	public String getCreationDate() 	{ return creationDate; 				}
	
}