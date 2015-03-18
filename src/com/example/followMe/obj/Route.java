package com.example.followMe.obj;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class Route extends ArrayList<GPSPoint>{

	
	public Route() 
	{
		super();
	}
	
	public void addGPSPoint(double longitude, double latitude) 
	{
		super.add(new GPSPoint(longitude, latitude));
	}
	
}