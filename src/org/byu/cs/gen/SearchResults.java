package org.byu.cs.gen;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.byu.cs.gen.global.HttpInterface;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.SimpleAdapter;

/**
 * This class provides the basic interface for displaying the results of a search.
 * When the class is loaded, it creates a new thread which performs the search and
 * updates the results screen when the search is complete.
 * @author Scott Slaugh
 *
 */
public class SearchResults extends ListActivity {
	
	private SearchResults instanceRef = this;

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Create the thread that performs the search and displays results.
        new SearchLoader().execute(new String[0]);
	}
	
	/**
	 * This class performs a search for missing persons and displays the results in a list.
	 * @author Scott Slaugh
	 *
	 */
	private class SearchLoader extends AsyncTask<String,String,ArrayList<HashMap<String,String>>> {
		
		private ProgressDialog searchProgress;
		
		@Override
		public void onPreExecute() {
			//Display an indeterminate progress dialog while the search is performed.
			searchProgress = ProgressDialog.show(instanceRef, "", "Searching for missing ancestors . .");
		}

		@SuppressWarnings("unchecked")
		@Override
		protected ArrayList<HashMap<String,String>> doInBackground(String... arg0) {
			
			ArrayList<HashMap<String,String>> result = new ArrayList<HashMap<String,String>>();
			try {
				//Call the search endpoint on the webservice.
				HttpResponse response = HttpInterface.getInstance().executeGet("people/search.json");
				String httpResult = HttpInterface.getResponseBody(response);
				
				//Create a map object from the result.
				List<String> parseResult = (List<String>)HttpInterface.parseJSON(httpResult);
				
				//Iterate over every person in the list
				for (String cPerson : parseResult) {
					//The way the simpleJSON library works is that each subobject in a list
					//is just a string, so we need to parse that string as well.
					Map<String,Object> personInfo = (Map<String,Object>)HttpInterface.parseJSON(cPerson);
					HashMap<String,String> newLine = new HashMap<String,String>();
					//Get the person's full name
					String personName = personInfo.get("full_name").toString();
					
					//If the name is blank (unknown) display the person's ID instead.
					if (personName.equals(""))
						personName = "Unknown Name (ID: " + personInfo.get("id") + ")";
					
					//Set line 1 of the list item to be the name
					newLine.put("line1", personName);
					String parentLine = "";
					//Create an appropriate string depending on who is missing.
					if (personInfo.get("father_id") == null && personInfo.get("mother_id") == null) {
						parentLine = "Missing mother and father";
					}
					else if (personInfo.get("father_id") == null) {
						parentLine = "Missing father";
					}
					else {
						parentLine = "Missing mother";
					}
					//Set line 2 of the list item to be which parents are missing.
					newLine.put("line2", parentLine);
					result.add(newLine);
				}
				
				return result;
				
			} catch (Exception e) {
				Log.e("Error", "Problem with HttpClient!", e);
			}
			
			return result;
		}
		
		@Override
		protected void onPostExecute(ArrayList<HashMap<String,String>> result) {
			//Get rid of the progress dialog.
			searchProgress.dismiss();
			//Create a list adapter which will display the two lines for each person item.
			SimpleAdapter listItemsAdapter = new SimpleAdapter(instanceRef, result, android.R.layout.two_line_list_item, new String[] {"line1", "line2"}, new int[]{android.R.id.text1, android.R.id.text2});
			instanceRef.setListAdapter(listItemsAdapter);
		}
		
	}
	
}
