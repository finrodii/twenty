package org.byu.cs.gen;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.byu.cs.gen.global.HttpInterface;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.SimpleAdapter;

public class SearchResults extends ListActivity {
	
	private SearchResults instanceRef = this;

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        new SearchLoader().execute(new String[0]);
	}
	
	private class SearchLoader extends AsyncTask<String,String,ArrayList<HashMap<String,String>>> {
		
		private ProgressDialog searchProgress;
		
		@Override
		public void onPreExecute() {
			searchProgress = ProgressDialog.show(instanceRef, "", "Searching for missing ancestors . .");
		}

		@SuppressWarnings("unchecked")
		@Override
		protected ArrayList<HashMap<String,String>> doInBackground(String... arg0) {
			
			ArrayList<HashMap<String,String>> result = new ArrayList<HashMap<String,String>>();
			try {
				HttpResponse response = HttpInterface.getInstance().executeGet("people/search.json");
				String httpResult = HttpInterface.getResponseBody(response);
				Map<String,Object> parseResult = HttpInterface.parseJSON(httpResult);
				
				LinkedList<String> temp = (LinkedList<String>)parseResult.get("people");
				
				for (String cPerson : temp) {
					Map<String,Object> personInfo = HttpInterface.parseJSON(cPerson);
					HashMap<String,String> newLine = new HashMap<String,String>();
					String personName = personInfo.get("full_name").toString();
					
					if (personName.equals(""))
						personName = "Unknown Name (ID: " + personInfo.get("id") + ")";
					
					newLine.put("line1", personName);
					String parentLine = "";
					if (personInfo.get("father_id") == null && personInfo.get("mother_id") == null) {
						parentLine = "Missing mother and father";
					}
					else if (personInfo.get("father_id") == null) {
						parentLine = "Missing father";
					}
					else {
						parentLine = "Missing mother";
					}
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
			searchProgress.dismiss();
			SimpleAdapter listItemsAdapter = new SimpleAdapter(instanceRef, result, android.R.layout.two_line_list_item, new String[] {"line1", "line2"}, new int[]{android.R.id.text1, android.R.id.text2});
			instanceRef.setListAdapter(listItemsAdapter);
		}
		
	}
	
}
