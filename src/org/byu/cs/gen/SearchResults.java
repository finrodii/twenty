package org.byu.cs.gen;

import java.util.LinkedList;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.byu.cs.gen.global.HttpInterface;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

public class SearchResults extends ListActivity {
	
	private SearchResults instanceRef = this;

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        new SearchLoader().execute(new String[0]);
	}
	
	private class SearchLoader extends AsyncTask<String,String,String[]> {
		
		private ProgressDialog searchProgress;
		
		@Override
		public void onPreExecute() {
			searchProgress = ProgressDialog.show(instanceRef, "", "Searching for missing ancestors . . .");
		}

		@SuppressWarnings("unchecked")
		@Override
		protected String[] doInBackground(String... arg0) {
			
			HttpResponse response;
			try {
				response = HttpInterface.getInstance().executeGet("people/search.json");
				String result = HttpInterface.getResponseBody(response);
				Map<String,Object> parseResult = HttpInterface.parseJSON(result);
				
				LinkedList<String> temp = (LinkedList<String>)parseResult.get("people");
				
				String[] searchResult = new String[temp.size()];
				
				int count = 0;
				for (String cPerson : temp) {
					Map<String,Object> personInfo = HttpInterface.parseJSON(cPerson);
					searchResult[count++] = personInfo.get("full_name").toString();
				}
				
				return searchResult;
				
			} catch (Exception e) {
				Log.e("Error", "Problem with HttpClient!", e);
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(String[] result) {
			searchProgress.dismiss();
			instanceRef.setListAdapter(new ArrayAdapter<String>(instanceRef,android.R.layout.simple_list_item_1, result));
		}
		
	}
	
}
