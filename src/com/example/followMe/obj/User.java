package com.example.followMe.obj;


public class User {
	
	public static final String INVALID_USERNAME = "Invalid username";
	public static final String INVALID_EMAIL = "Invalid email";
	public static final String INVALID_PASSWORD = "Invalid password";
	
	private String userName = null;
	private String email = null;
	private String firstName = null;
	private String lastName = null;
	private String password = null;
	
	
	/*** Data validation is performed  ***/
	public User(String userName, String email, String firstName, String lastName, String password) 
	throws InvalidUserDataException
	{
		setUserName(userName);
		setEmail(email);
		setPassword(password);
		
		this.firstName = firstName;
		this.lastName = lastName;
	}
	
	/*** No data verification is performed. Used by application server ***/
	public User(String userName, String password)
	{
		this.userName = userName;
		this.password = password;
		// TODO: Have app server send all user data 
		this.email = "unknown@email";
		this.firstName = "unknown";
		this.lastName = "unknown";
	}
	
	public void setUserName(String userName) 
	throws InvalidUserDataException
	{
		if (userName.length() > 3 && userName.length() <= 15 && userName.matches("[a-zA-Z0-9]*")) {
			this.userName = userName;
			return;
		}
		else
			throw new InvalidUserDataException(INVALID_USERNAME);
	}
	
	public void setEmail(String email) 
	throws InvalidUserDataException
	{
		if(email.contains("@")) {
			this.email = email;
			return;
		}
		else
			throw new InvalidUserDataException(INVALID_EMAIL);
	}
	
	public void setPassword(String password)
	{
		// TODO: FixMe. This does not accept the password "password1"
		this.password = password;
		return;  
		
		/*if((password.substring(0, 1) == "[a-zA-Z]*") && 
		   (password.length() <= 25) && 
		   (password.length() >= 8) &&
		   (password.matches("[a-zA-Z0-9 ]*")) &&
		   (password.contains("[0-9]*")))
			return;
		else
			throw new InvalidUserDataException(INVALID_PASSWORD);
		*/
	}
	
	public void setFirstName(String firstName) { this.firstName = firstName; }
	public void setLastName(String lastName) { this.lastName = lastName; }	
	
	public String getUserName() { return userName; }
	public String getEmail() { return email; }
	public String getFirstName() { return firstName; }
	public String getLastName() { return lastName; }
	public String getPassword() { return password; }
	
}