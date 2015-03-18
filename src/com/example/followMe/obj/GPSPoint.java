package com.example.followMe.obj;

public class GPSPoint 
{
	private double longitude = 0;
	private double latitude = 0;
	
	public GPSPoint(double longitude, double latitude) 
	{
		this.longitude = longitude;
		this.latitude = latitude;
	}
	
	public double getLongitude() { return longitude; }
	public double getLatitude() { return latitude; }
	
	public void setLongitude(double longitude) { this.longitude = longitude; }
	public void setLatitude(double latitude) { this.latitude = latitude; }
	
}
