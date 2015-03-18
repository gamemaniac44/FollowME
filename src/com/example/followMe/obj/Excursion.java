package com.example.followMe.obj;

import java.text.DateFormat;
import java.util.Date;

import com.example.followMe.util.AppData;

public class Excursion {

	public static final String INVALID_TRAVEL_MODE 			= "Invalid travel mode.";
	public static final String INVALID_SHARE_MODE 			= "Invalid share mode.";
	public static final String INVALID_TITLE 				= "Invalid title.";
	public static final String INVALID_DESCRIPTION 			= "Invalid description.";
	public static final String NULL_VALUE_IN_CONSTRUCTOR 	= "Null value passed to constructor.";

	public static final int WALKING = 0;
	public static final int DRIVING = 1;
	
	public static final int PRIVATE = 0;
	public static final int PUBLIC  = 1;
	
	public static final int MAX_TITLE_LEN = 40;
	public static final int MAX_DESC_LEN = 250;
	
	private String publisher = null;
	private int travelMode = WALKING;
	private int shareMode = PRIVATE;
	private String title = null;
	private String description = null;
	private Route route = null;
	private ObservationList observations = null;
	private String originalAuthor = null;
	private String creationDate = null;
	
	/********************************************************************
	 *  Constructors
	 *********************************************************************/
	
	/***
	 * Default Excursion used on start-up. 
	 ***/
	
	public Excursion()
	{
		this.publisher = AppData.getCurrentUser().getUserName();
		this.travelMode = WALKING;
		this.shareMode = PRIVATE;
		this.title = "My Excursion";
		this.description = "";
		this.route = new Route();
		this.observations = new ObservationList();
		this.originalAuthor = AppData.getCurrentUser().getUserName();
		this.creationDate = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
				.format(new Date());		
	}
	
	/*** 
	 * Data is validated; publisher, originalAuthor and creationDate are set automatically.
	 * Publisher is set to current user to identify person who published to the cloud.
	 * Should be used by Activities to create New excursions.
	 * @throws InvalidExcursionDataException 
	 ***/
	
	public Excursion(int travelMode, int shareMode, String title, String description) 
	throws InvalidExcursionDataException 
	{
		this.publisher = AppData.getCurrentUser().getUserName();
		setTravelMode(travelMode);
		setShareMode(shareMode);
		setTitle(title);
		setDescription(description);
		this.route = new Route();
		this.observations = new ObservationList();
		this.originalAuthor = AppData.getCurrentUser().getUserName();
		this.creationDate = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
				.format(new Date());
	}
	
	/*** 
	 * All fields are passed except for publisher who is the current user - for use by LocalDB.
	 * Publisher is set to current user to identify who in the future publishes to the cloud.
	 * We don't allow null strings.  Empty strings are fine.
	 * @throws InvalidExcursionDataException 
	 ***/
	
	public Excursion(int travelMode, int shareMode, String title, String description, Route route, 
			ObservationList observations, String originalAuthor, String creationDate) 
	throws InvalidExcursionDataException 
	{	
		this.publisher = AppData.getCurrentUser().getUserName();
		setTravelMode(travelMode);
		setShareMode(shareMode);
		setTitle(title);
		setDescription(description);
		this.route = route;
		this.observations = observations;
		this.originalAuthor = originalAuthor;
		this.creationDate = creationDate;
		
		if (this.publisher == null || this.originalAuthor == null || this.creationDate == null)
			throw new InvalidExcursionDataException(NULL_VALUE_IN_CONSTRUCTOR);
	}
	
	/********************************************************************
	 *  Modifiers:  
	 *  We can not change the publisher, originalAuthor or creationDate
	 *********************************************************************/
	
	public void setTravelMode(int travelMode) 
	throws InvalidExcursionDataException
	{
		if (travelMode != WALKING && travelMode != DRIVING)
			throw new InvalidExcursionDataException(INVALID_TRAVEL_MODE);
		else
			this.travelMode = travelMode;
	}
	
	public void setShareMode(int shareMode) 
	throws InvalidExcursionDataException
	{
		if (shareMode != PUBLIC && shareMode != PRIVATE)
			throw new InvalidExcursionDataException(INVALID_SHARE_MODE);
		else
			this.shareMode = shareMode;
	}
	
	public void setTitle(String title) 
	throws InvalidExcursionDataException 
	{	
		if (title == null || title.length() == 0 || title.length() > MAX_TITLE_LEN)
			throw new InvalidExcursionDataException(INVALID_TITLE);
		else 
			this.title = title;
	}
	
	public void setDescription(String description) 
	throws InvalidExcursionDataException 
	{	
		if (description == null)
			this.description = "";
		else if (description.length() > MAX_DESC_LEN)
			throw new InvalidExcursionDataException(INVALID_DESCRIPTION);
		else	
			this.description = description;
	}	
	
	public void addGPSPoint(double longitude, double latitude)
	{
		route.addGPSPoint(longitude, latitude);
	}
	
	/*** 
	 * Data is validated; author and date are set automatically; throws exception 
	 ***/
	
	public void addObservation(String title, String description, double longitude, double latitude) 
	throws InvalidObservationDataException 
	{
		observations.addObservation(title, description, longitude, latitude);
	}
	
	/*** 
	 * No data validation is performed - used by DB 
	 ***/
	
	public void addObservation(String author, String title, String description, double longitude, 
			double latitude, String date) 
	{
		observations.addObservation(author, title, description, longitude, latitude, date);
	}
	
	/********************************************************************
	 * Access Methods
	 *********************************************************************/
	
	public String getPublisher()				{ return publisher; 		}
	public int getTravelMode()					{ return travelMode;		}
	public int getShareMode() 					{ return shareMode; 		}
	public String getTitle() 					{ return title; 			}
	public String getDescription() 				{ return description; 		}
	public Route getRoute() 					{ return route; 			}
	public ObservationList getObservationList() { return observations; 		}
	public String getOriginalAuthor()			{ return originalAuthor;	}
	public String getCreationDate() 			{ return creationDate; 		}	
}