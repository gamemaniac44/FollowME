package com.example.followMe.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.util.Log;

public class HttpClient {
	private static final String TAG = "HttpClient";

	public static JSONObject sendPost(String URL, JSONObject jsonObjSend) {

		try {
			// Package JSON object into a StringEntity
			StringEntity stringEntity;
			stringEntity = new StringEntity(jsonObjSend.toString());
			
			// Create HttpClient and HttpPost objects
			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(URL);

			// Set HttpPost parameters
			httpPost.setEntity(stringEntity);
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");

			// Execute HttpPost on the HttpClient and get the HttpResponse
			HttpResponse httpResponse = (HttpResponse) httpclient.execute(httpPost);

			// Get the HttpEntity from the HttpResponse
			HttpEntity httpEntity = httpResponse.getEntity();
			Log.i(TAG, "Response: " + httpEntity.toString());
			
			if (httpEntity != null) {
				// Convert the HttpEntity to a String
				InputStream instream = httpEntity.getContent();
				String entityString= convertInputStreamToString(instream);
				instream.close();
				Log.i(TAG, "Input stream: (" + entityString + ")");
				
				if (entityString == null || entityString.equals("")) {
					Log.i(TAG, "Entity string is null");
					return null;
				}
				
				// Convert the String into a JSONObject
				JSONObject jsonObjRecv = new JSONObject(entityString);
				Log.i(TAG,"Returned JSON: " + jsonObjRecv.toString());

				// Return the JSONObject
				return jsonObjRecv;
			} 
		}
		catch (Exception e)
		{
			Log.i(TAG,"HTTPResponse: caught exception, returning null entity");
			Log.i(TAG, e.toString());
			e.printStackTrace();
		}

		return null;
	}


	private static String convertInputStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder builder = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				builder.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return builder.toString();
	}

}


