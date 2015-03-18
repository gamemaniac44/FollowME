package com.example.followMe.obj;

public class ExcursionListEntry 
{	
	private int excursionId;
	private String title = null;
	private String originalAuthor = null;
	private String creationDate = null;
	
	public ExcursionListEntry(int excursionId, String title, String originalAuthor, String creationDate)
	{
		this.excursionId = excursionId;
		this.title = title;
		this.originalAuthor = originalAuthor;
		this.creationDate = creationDate;
	}
	
	public int getExcursionId() 		{ 	return excursionId; 	}
	public String getTitle() 			{	return title;			}
	public String getOriginalAuthor() 	{	return originalAuthor;	}
	public String getCreationDate() 	{ 	return creationDate; 	}
	
}

