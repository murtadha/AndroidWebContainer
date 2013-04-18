package com.sensetecnic.container;

import java.io.File;
import org.json.JSONArray;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

public class MediaUploaderActivity extends Activity {

	File file;
	ProgressDialog progressDialog;
	String uploadURL;
	String eventKey;
	String sensorKey;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.html_callback);
		
		Intent intent = getIntent();
		uploadURL = intent.getStringExtra("uploadURL") + "?keep-stored=true";
		eventKey = intent.getStringExtra("eventKey");
		sensorKey = intent.getStringExtra("sensorKey");
		
		Intent newIntent = new Intent("com.nexes.manager.LAUNCH");
		startActivityForResult(newIntent, 0);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			try {
				progressDialog = ProgressDialog.show(this, "", 	"Uploading file...", true);
				file = new File(data.getStringExtra("filepath"));
				new FileUploadTask().execute();
			} catch (Exception e) {
				e.printStackTrace(System.err);
				finish();
			}
		} else {
			finish();
		}
	}
	
	// uploads file we just selected to the server, and then finishes up	
	class FileUploadTask extends AsyncTask<String, Void, String> {
		protected String doInBackground(String... params) {
			try {
				String id = ThingBrokerHelper.uploadFile(file, uploadURL, eventKey, sensorKey);
				JSONArray array = new JSONArray();

				String url = 
					"http://" + getString(R.string.thing_broker_server) + ":" 
					+ getString(R.string.thing_broker_port) + "/thingbroker/content/" + id;
				array.put(url);
				array.put(file.getName());

				return ThingBrokerHelper.postObject(array, uploadURL, eventKey, sensorKey);
			} catch (Exception e) {
				e.printStackTrace(System.err);
				return null;
			}
		}
		
		protected void onPostExecute(String result) {
			if (progressDialog != null)
				progressDialog.dismiss();

			if (result != null) {
				//System.out.println("Uploaded to " + getString(R.string.upload_url) + " with response " + result);
				//Passes the result of the upload back out of the callbackactivity
				Intent callbackactivity = getIntent();
				callbackactivity.putExtra("uploadResult", result);
				callbackactivity.putExtra("method", "upload");
				setResult(RESULT_OK, callbackactivity);
			}
			finish();
		}
	}
}
