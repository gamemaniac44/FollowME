package com.example.followMe.obj;

@SuppressWarnings("serial")
public class InvalidUserDataException extends Exception{

	String mssg = null;
	
	public InvalidUserDataException (String mssg) 
	{ 
		this.mssg = mssg; 
	}
	
	@Override
	public String toString( ) 
	{ 
		return mssg;
	}
}
