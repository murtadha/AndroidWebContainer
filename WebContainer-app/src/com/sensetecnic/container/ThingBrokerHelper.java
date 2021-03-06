package com.sensetecnic.container;

import java.io.File;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class ThingBrokerHelper {

	private ThingBrokerHelper() {
		// This class is a bunch of static helpers. Instantiating shouldn't be allowed
	}
	
	/**
	 * Post an object to the thing broker
	 * @param obj Object that should be posted. obj must be convertable to JSON
	 * @param uploadURL full URL to post obj to
	 * @param eventKey key for the event (e.g. "native", "append", "prepend")
	 * @param sensorKey key for the sensor. Can be null
	 * @return response from the ThingBroker
	 */
	public static String postObject(Object obj, String uploadURL, String eventKey, String sensorKey) {
		try {
			DefaultHttpClient httpclient = new DefaultHttpClient();  		
			HttpPost httppost = new HttpPost(uploadURL);
			
			if (sensorKey != null && sensorKey.length() > 0) {
				JSONObject json = new JSONObject();
				json.put(sensorKey, obj);
				obj = json;				
			}
			
			JSONObject info = new JSONObject();
			info.put(eventKey, obj);
			
			ByteArrayEntity e = new ByteArrayEntity(info.toString().getBytes());
			e.setContentType("application/JSON");
			e.setContentEncoding("application/JSON");
			httppost.setEntity(e);
			HttpResponse response = httpclient.execute(httppost);
			return new BasicResponseHandler().handleResponse(response);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return null;
		}
	}

	/**
	 * Uploads a file to the ThingBroker
	 * @param file file to upload
	 * @param uploadURL full URL to upload to
	 * @return the content-id of the uploaded file
	 */
	//  
	public static String uploadFile(File file, String uploadURL) {
		try {			
			MultipartEntity reqEntity = new MultipartEntity();  
			FileBody bin = new FileBody(file);
			reqEntity.addPart("file", bin);
		
			DefaultHttpClient httpclient = new DefaultHttpClient();  		
			HttpPost httppost = new HttpPost(uploadURL);
			httppost.setEntity(reqEntity);
			
			// Execute HTTP Post Request  
			HttpResponse response = httpclient.execute(httppost);
			String result = new BasicResponseHandler().handleResponse(response);
			
			JSONObject json = (JSONObject) new JSONTokener(result).nextValue();
			Object jsonData = json.get("content");
			return (String)((JSONArray)jsonData).get(0);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return null;
		}
	}
}
