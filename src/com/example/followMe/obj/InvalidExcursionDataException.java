package com.example.followMe.obj;

@SuppressWarnings("serial")
public class InvalidExcursionDataException extends Exception {

	String mssg = null;
	
	public InvalidExcursionDataException (String mssg) 
	{ 
		this.mssg = mssg; 
	}
	
	@Override
	public String toString( ) 
	{ 
		return mssg;
	}
}