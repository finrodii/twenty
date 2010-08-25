package org.byu.cs.gen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
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
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

/**
 * This class displays the contents of a list, organized by task.
 * Currently this class is not being used in favor of the ShowList activity,
 *   however there should be an option to view a list either way.
 * @author Seth Dickson
 */
public class ShowTasks extends ListActivity{

	private ShowTasks instanceRef = this;
	private Intent this_intent;
	private Bundle extras;
	
	private Intent searchIntent;
	private Intent homeScreenIntent;
	private Intent todoIntent;
	private Intent edit_task;
	private ArrayList<String> entry_names;
	private ArrayList<String> entry_ids;
	private ArrayList<String> task_ids;
	private ArrayList<Integer> searches;
	
	private static final int DELETE = Menu.FIRST;

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_tasks);
        
        this_intent = getIntent();
        extras = this_intent.getExtras();
        
        entry_names = new ArrayList<String>();
        entry_ids = new ArrayList<String>();
        task_ids = new ArrayList<String>();
        searches = new ArrayList<Integer>();
        
        searchIntent = new Intent(this, SearchResults.class); //Use this Intent for any searches, just change the extras.
        homeScreenIntent = new Intent(this, TwentyMinuteGen.class);
        todoIntent = new Intent(this, Todo.class);
        edit_task = new Intent(this, EditTask.class);
        
        TextView header = (TextView) findViewById(R.id.showlist_title);
        header.setText((CharSequence) extras.get("list_name").toString());
        header.setTextSize(30);
        
	}
	
    @Override
    protected void onResume() {
        super.onResume();
        init();
    }
    private void init() {
        //Create the thread that performs the search and displays results.
        new ListLoader().execute(new String[0]);
        ListView lv = getListView();
        registerForContextMenu(lv);

        lv.setTextFilterEnabled(true);
        lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				edit_task.putExtra("task_id", task_ids.get(position));
				edit_task.putExtra("person", entry_names.get(position));
				edit_task.putExtra("entry_id", entry_ids.get(position));
				startActivity(edit_task);
			}
        });      	
    }
	
	/**
	 * This class finds this list information and displays it.
	 * @author Scott Slaugh
	 *
	 */
	private class ListLoader extends AsyncTask<String,String,ArrayList<HashMap<String,String>>> {
		
		private ProgressDialog searchProgress;
		
		@Override
		public void onPreExecute() {
			//Display an indeterminate progress dialog while the search is performed.
			searchProgress = ProgressDialog.show(instanceRef, "", "Retrieving list info . .");
		}

		@SuppressWarnings("unchecked")
		@Override
		protected ArrayList<HashMap<String,String>> doInBackground(String... arg0) {
			
			ArrayList<HashMap<String,String>> result = new ArrayList<HashMap<String,String>>();

			try {
				
				String list_id = extras.get("list_id").toString();
				//Call the search endpoint on the webservice.
				HttpResponse response = HttpInterface.getInstance().executeGet("todo/lists/"+list_id+".json");
				String httpResult = HttpInterface.getResponseBody(response);
				//Create a map object from the result.
				List<String> parseResult = (List<String>)HttpInterface.parseJSON(httpResult);
				
				if (!entry_names.isEmpty())
					entry_names.clear();
				if (!task_ids.isEmpty())
					task_ids.clear();
				//Iterate over every entry in the list
				for (String cEntry : parseResult) {
					//The way the simpleJSON library works is that each subobject in a list
					//is just a string, so we need to parse that string as well.
					Map<String,Object> entryInfo = (Map<String,Object>)HttpInterface.parseJSON(cEntry);
					
		            List<String> parseTasks = (List<String>)entryInfo.get("tasks");
		            for (String cTask : parseTasks) {
		            	Map<String,Object> taskInfo = (Map<String,Object>)HttpInterface.parseJSON(cTask);
						HashMap<String,String> newLine = new HashMap<String,String>();

		            	String task_name = taskInfo.get("name").toString();
		                String priority = taskInfo.get("priority").toString();
		                switch(Integer.parseInt(priority)){
		                case 1:
		                	priority = "top";
		                	break;
		                case 2:
		                	priority = "high";
		                	break;
		                case 3:
		                	priority = "medium";
		                	break;
		                case 4:
		                	priority = "low";
		                	break;
		                }
		                

		                String task_details = "\npriority: "+priority;
		                String notes = taskInfo.get("notes").toString();
		                if(notes.trim().length()!=0)
		                	task_details += "\n\n '"+notes+"'";
		                
		                newLine.put("line1", task_name +"\n    for "+ entryInfo.get("name").toString());
		                newLine.put("line2", task_details);
		                result.add(newLine);
		                
						entry_names.add(entryInfo.get("name").toString());
						entry_ids.add(entryInfo.get("id").toString());
		                task_ids.add(taskInfo.get("id").toString());
		            }
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
			SimpleAdapter mAdapter = new SimpleAdapter( 
					instanceRef, result, R.layout.my_expanded_list_item_2, new String[] { "line1", "line2"}, new int[] { android.R.id.text1, android.R.id.text2 });
	        setListAdapter(mAdapter);
		}
	}
	
    @Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, DELETE, 0, R.string.todo_delete_item);	
	}

    @Override
	public boolean onContextItemSelected(MenuItem item) {  
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch(item.getItemId()) {
    	case DELETE:
    		try {
    			HttpResponse response = null;
    			String httpResult;
    			Intent intent = instanceRef.getIntent();
	    		response = HttpInterface.getInstance().executeDelete("todo/tasks/"+task_ids.get(info.position)+".json");
	    		if (response != null){
					httpResult = HttpInterface.getResponseBody(response);
					Log.i("twenty", httpResult);		
	                startActivity(intent);
	                finish();
	    		}
			} 
    		catch (ClientProtocolException e) {e.printStackTrace();} 
			catch (IOException e) {e.printStackTrace();}
			break;
		}
		return super.onContextItemSelected(item);
	}
    
    @SuppressWarnings("unchecked")
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
	        case R.id.todo:
	        	startActivity(todoIntent);
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
