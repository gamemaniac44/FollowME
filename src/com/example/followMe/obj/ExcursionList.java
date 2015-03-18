package com.example.followMe.obj;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class ExcursionList extends ArrayList<ExcursionListEntry>{

	public ExcursionList()
	{
		super();
	}
	
	public void addEntry(int excursionId, String title, String originalAuthor, String creationDate)
	{
		super.add(new ExcursionListEntry(excursionId, title, originalAuthor, creationDate));
	}

}