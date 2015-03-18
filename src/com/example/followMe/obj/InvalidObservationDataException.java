package com.example.followMe.obj;

@SuppressWarnings("serial")
public class InvalidObservationDataException extends Exception{

	String mssg = null;
	
	public InvalidObservationDataException (String mssg) 
	{ 
		this.mssg = mssg; 
	}
	
	@Override
	public String toString( ) 
	{ 
		return mssg;
	}
}
