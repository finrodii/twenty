package org.byu.cs.gen;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.byu.cs.gen.global.HttpInterface;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

/**
 * This displays all todo lists owned by the current user.
 * These lists can be selected for viewing or deleted.
 * There is also an option to create new lists.
 * @author Seth Dickson
 */
public class Todo extends ListActivity {
	
	private Todo instanceRef = this;
	private EditText mCreateListText;
	
	private Intent searchIntent;
	private Intent homeScreenIntent;
	private Intent show_list;
	private ArrayList<String> list_pos_id;
	private ArrayList<String> list_pos_name;
	private ArrayList<Integer> searches;

	private static final int DELETE_LIST = Menu.FIRST;

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.todo_lists);
        
        searchIntent = new Intent(this, SearchResults.class); //Use this Intent for any searches, just change the extras.
        homeScreenIntent = new Intent(this, TwentyMinuteGen.class);
        show_list = new Intent(this, ShowList.class);
        searches = new ArrayList<Integer>();
        
        //Create the thread that performs the search and displays results.
        new SearchLoader().execute(new String[0]);
        
        final ListView lv = getListView();
        registerForContextMenu(lv);
        lv.setTextFilterEnabled(true);
        lv.setOnItemClickListener(new OnItemClickListener() {
          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        	  show_list.putExtra("list_id", list_pos_id.get(position));
        	  show_list.putExtra("list_name", list_pos_name.get(position));
        	  startActivity(show_list);
          }
        });
        
        Button createListButton = (Button) findViewById(R.id.create);
        mCreateListText = (EditText) findViewById(R.id.create_list);
        createListButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        	    
	            // Add your data  
	            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);  
	            nameValuePairs.add(new BasicNameValuePair("name", mCreateListText.getText().toString())); 
				try {
					HttpResponse response = HttpInterface.getInstance().executePost("todo/lists.json", nameValuePairs);
					HttpInterface.getResponseBody(response);				
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {}
				mCreateListText.setText("");
                Intent intent = instanceRef.getIntent();
                startActivity(intent);
                finish();
        	}
        });
	}
	
	/**
	 * This class finds this user's todo lists and displays the results in a list.
	 * @author Scott Slaugh
	 */
	private class SearchLoader extends AsyncTask<String,String,ArrayList<HashMap<String,String>>> {
		
		private ProgressDialog searchProgress;
		
		@Override
		public void onPreExecute() {
			//Display an indeterminate progress dialog while retrieving lists.
			searchProgress = ProgressDialog.show(instanceRef, "", "Retrieving Todo Lists . .");
		}

		@SuppressWarnings("unchecked")
		@Override
		protected ArrayList<HashMap<String,String>> doInBackground(String... arg0) {
			
			ArrayList<HashMap<String,String>> result = new ArrayList<HashMap<String,String>>();
			try {
				//Call the search endpoint on the webservice.
				HttpResponse response = HttpInterface.getInstance().executeGet("todo/lists.json");
				String httpResult = HttpInterface.getResponseBody(response);
				//Create a map object from the result.
				List<String> parseResult = (List<String>)HttpInterface.parseJSON(httpResult);
				
				list_pos_id = new ArrayList<String>();
				list_pos_name = new ArrayList<String>();
				//Iterate over each list in the list
				for (String cList : parseResult) {
					//The way the simpleJSON library works is that each subobject in a list
					//is just a string, so we need to parse that string as well.
					Map<String,Object> listInfo = (Map<String,Object>)HttpInterface.parseJSON(cList);
					HashMap<String,String> newLine = new HashMap<String,String>();
					//Get the list name
					String listName = listInfo.get("name").toString();
					if (listName.compareTo("completed_tasks")==0)
						continue;
					//Set line 1 of the list item to be the list name.
					newLine.put("line1", listName);
					result.add(newLine);

					list_pos_id.add(listInfo.get("id").toString());
					list_pos_name.add(listName);
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
			//Create a list adapter which will display each list item.
			SimpleAdapter listItemsAdapter = new SimpleAdapter(instanceRef, result, android.R.layout.simple_list_item_1, new String[] {"line1"}, new int[]{android.R.id.text1});
			instanceRef.setListAdapter(listItemsAdapter);
		}
		
	}
	
    @Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_LIST, 0, R.string.todo_delete_list);
	}

    @Override
	public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch(item.getItemId()) {
    	case DELETE_LIST:
			try {
				HttpResponse response = HttpInterface.getInstance().executeDelete("todo/lists/"+list_pos_id.get(info.position)+".json");
				HttpInterface.getResponseBody(response);	
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {}
			mCreateListText.setText("");
            Intent intent = instanceRef.getIntent();
            startActivity(intent);
            finish();
		}
		return super.onContextItemSelected(item);
	}
    
    @SuppressWarnings("unchecked")
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	menu.removeItem(R.id.todo);
    	SubMenu sbmenu = menu.addSubMenu("Searches");
    	sbmenu.setIcon(R.drawable.suggestion);
		String httpResult = "";
		try {
			HttpResponse response = HttpInterface.getInstance().executeGet("family/searches.json");
			httpResult = HttpInterface.getResponseBody(response);

		} catch (IOException e) { e.printStackTrace(); }
		
		List<String> parseResult = (List<String>)HttpInterface.parseJSON(httpResult);
		for (String cSearch : parseResult) {
			Map<String,Object> searchInfo = (Map<String,Object>)HttpInterface.parseJSON(cSearch);
			String search_name = searchInfo.get("name").toString();
			int search_id = Integer.parseInt(searchInfo.get("id").toString());
			
			sbmenu.add(Menu.NONE, search_id, search_id, search_name);
			searches.add(search_id);
		}
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.genmenu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
        	case R.id.home:
        		startActivity(homeScreenIntent);
        		return true;        
        }
        if(searches.contains(item.getItemId())){
			searchIntent.putExtra("progress_dialog", "Searching . .");
			searchIntent.putExtra("search_id", item.getItemId());
			startActivity(searchIntent);
			return true;
        }
    	return super.onOptionsItemSelected(item);
    }  
    
}