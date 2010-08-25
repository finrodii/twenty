package org.byu.cs.gen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.byu.cs.gen.global.HttpInterface;

import android.app.Dialog;
import android.app.ExpandableListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
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
import android.widget.ArrayAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ExpandableListView.OnChildClickListener;

/**
 * This class displays the contents of a list, organized by entry(person).
 * To view just tasks use the ShowTasks activity.
 * @author Seth Dickson
 */
public class ShowList extends ExpandableListActivity  {

	private ShowList instanceRef = this;
	private Intent this_intent;
	private Context context = this;
	private Bundle extras;
	
	private Intent searchIntent;
	private Intent homeScreenIntent;
	private Intent todoIntent;
	private Intent edit_task;
	private Intent show_person;
	private ArrayList<String> person_ids;
	private ArrayList<String> entry_names;
	private ArrayList<String> entry_ids;
	private ArrayList<String> entry_priorities;
	private ArrayList<ArrayList<String>> child_ids;
	private ExpandableListAdapter mAdapter;
	private ArrayList<Integer> searches;
	private String active_entry;
	private int active_group;
	private boolean creating_dialog = true;
	private boolean prep_dialog = false;
	
	private static final int DELETE = Menu.FIRST;
	private static final int ADD_TASK = Menu.FIRST+1;
	private static final int SHOW_PERSON = Menu.FIRST+2;
	private static final int CHANGE_PRIORITY = Menu.FIRST+3;
	private static final int DIALOG_PRIORITY = 0;

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_expandable_list_content);
        
        this_intent = getIntent();
        extras = this_intent.getExtras();
        
        person_ids = new ArrayList<String>();
        entry_names = new ArrayList<String>();
        entry_ids = new ArrayList<String>();
        entry_priorities = new ArrayList<String>();
        child_ids = new ArrayList<ArrayList<String>>();
        searches = new ArrayList<Integer>();
        active_entry = "";
        active_group = 0;
        
        searchIntent = new Intent(this, SearchResults.class); //Use this Intent for any searches, just change the extras.
        homeScreenIntent = new Intent(this, TwentyMinuteGen.class);
        todoIntent = new Intent(this, Todo.class);
        edit_task = new Intent(this, EditTask.class);
        show_person = new Intent(this, ShowPerson.class);
        
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
        ExpandableListView lv = getExpandableListView();
        registerForContextMenu(lv);
        
        lv.setTextFilterEnabled(true);
        lv.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				Toast.makeText(getApplicationContext(), "task_id: "+child_ids.get(groupPosition).get(childPosition), Toast.LENGTH_LONG).show();
				edit_task.putExtra("task_id", child_ids.get(groupPosition).get(childPosition));
				edit_task.putExtra("person", entry_names.get(groupPosition));
				edit_task.putExtra("entry_id", entry_ids.get(groupPosition));
				startActivity(edit_task);
				return true;
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
		List<List<Map<String, String>>> childData;
		private static final String IS_EVEN = "IS_EVEN";
		
		@Override
		public void onPreExecute() {
			//Display an indeterminate progress dialog while the search is performed.
			searchProgress = ProgressDialog.show(instanceRef, "", "Retrieving list info . .");
		}

		@SuppressWarnings("unchecked")
		@Override
		protected ArrayList<HashMap<String,String>> doInBackground(String... arg0) {
			
			ArrayList<HashMap<String,String>> result = new ArrayList<HashMap<String,String>>();
			childData = new ArrayList<List<Map<String, String>>>();
			try {
				
				String list_id = extras.get("list_id").toString();
				//Call the search endpoint on the webservice.
				HttpResponse response = HttpInterface.getInstance().executeGet("todo/lists/"+list_id+".json");
				String httpResult = HttpInterface.getResponseBody(response);
				//Create a map object from the result.
				List<String> parseResult = (List<String>)HttpInterface.parseJSON(httpResult);
				
				if (!person_ids.isEmpty())
					person_ids.clear();
				if (!entry_names.isEmpty())
					entry_names.clear();
				if (!entry_ids.isEmpty())
					entry_ids.clear();
				if (!entry_priorities.isEmpty())
					entry_priorities.clear();
				if (!child_ids.isEmpty())
					child_ids.clear();
				//Iterate over every entry in the list
				for (String cEntry : parseResult) {
					//The way the simpleJSON library works is that each subobject in a list
					//is just a string, so we need to parse that string as well.
					Map<String,Object> entryInfo = (Map<String,Object>)HttpInterface.parseJSON(cEntry);
					HashMap<String,String> newLine = new HashMap<String,String>();
					//Set line 1 of the list item to be the name
					String name = entryInfo.get("name")!=null ? entryInfo.get("name").toString() : entryInfo.get("person_id").toString();
					newLine.put("line1", name);
					newLine.put(IS_EVEN, "");
					result.add(newLine);
					person_ids.add(entryInfo.get("person_id").toString());
					entry_names.add(name);
					entry_ids.add(entryInfo.get("id").toString());
					entry_priorities.add(entryInfo.get("priority").toString());
					
					ArrayList entry_children = new ArrayList<String>();
		            List<String> parseTasks = (List<String>)entryInfo.get("tasks");
					List<Map<String, String>> children = new ArrayList<Map<String, String>>();
		            for (String cTask : parseTasks) {
		            	Map<String,Object> taskInfo = (Map<String,Object>)HttpInterface.parseJSON(cTask);
		                Map<String, String> curChildMap = new HashMap<String, String>();
		                curChildMap.put("line2", " " + taskInfo.get("name").toString());
		                String priority = taskInfo.get("priority").toString();
		                switch(Integer.parseInt(priority)){
			                case 1: priority = "top";    break;
			                case 2: priority = "high";   break;
			                case 3: priority = "medium"; break;
			                case 4: priority = "low";    break;
		                }
		                String notes = taskInfo.get("notes").toString();
		                String task_details = " priority: "+priority;
		                if(notes.trim().length()!=0)
		                	task_details += "\n\n '"+notes+"'";
		                curChildMap.put(IS_EVEN, task_details);
		                children.add(curChildMap);
		                
		                entry_children.add(taskInfo.get("id").toString());
		            }
		            childData.add(children);
		            child_ids.add(entry_children);
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
	        mAdapter = new SimpleExpandableListAdapter( 
	        		instanceRef, result, R.layout.my_expanded_list_item_1, new String[] { "line1", IS_EVEN}, new int[] { android.R.id.text1, android.R.id.text2 },
	        					 childData, R.layout.my_expanded_list_item_2, new String[] { "line2", IS_EVEN}, new int[] { android.R.id.text1, android.R.id.text2 }
	                );
	        setListAdapter(mAdapter);
		}
	}
	
    @Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuInfo;
		int type = ExpandableListView.getPackedPositionType(info.packedPosition);

		//Context menu for child items
		if (type == 1) {
			menu.add(0, DELETE, 0, R.string.todo_delete_item);
		}
		else { //for group items. don't want children to be able to create a task.
			menu.add(0, DELETE, 0, R.string.todo_delete_item);
			menu.add(0,ADD_TASK,0, R.string.todo_add_task);
			menu.add(0, SHOW_PERSON, 0, R.string.show_person);
			menu.add(0, CHANGE_PRIORITY,0,"Change Priority");
		}
	}

    @Override
	public boolean onContextItemSelected(MenuItem item) {
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
		long packed_position = info.packedPosition;
		int packed_pos_type = ExpandableListView.getPackedPositionType(packed_position);
		int group_pos = ExpandableListView.getPackedPositionGroup(packed_position);
		int child_pos = ExpandableListView.getPackedPositionChild(packed_position);    	
		switch(item.getItemId()) {
	    	case DELETE:
	    		try {
	    			HttpResponse response = null;
	    			String httpResult;
	    			Intent intent = instanceRef.getIntent();
		    		switch(packed_pos_type){
			    		case ExpandableListView.PACKED_POSITION_TYPE_GROUP:
		    				response = HttpInterface.getInstance().executeDelete("todo/entries/"+entry_ids.get(group_pos)+".json");
			    			break;
			    		case ExpandableListView.PACKED_POSITION_TYPE_CHILD:
		    				response = HttpInterface.getInstance().executeDelete("todo/tasks/"+child_ids.get(group_pos).get(child_pos)+".json");
			    			break;
		    		}
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
	    	case ADD_TASK:
				if(edit_task.hasExtra("task_id"))
					edit_task.removeExtra("task_id");
				edit_task.putExtra("person", entry_names.get(group_pos));
				edit_task.putExtra("entry_id", entry_ids.get(group_pos));
				startActivity(edit_task);	    		
	    		break;
	    	case SHOW_PERSON:
	    		show_person.putExtra("person_id", person_ids.get(group_pos));
	    		startActivity(show_person);
	    		break;
	    	case CHANGE_PRIORITY:
	    		active_group = group_pos;
	    		active_entry = entry_ids.get(group_pos);
	    		showDialog(DIALOG_PRIORITY);
	    		break;
		}
		return super.onContextItemSelected(item);
	}

	/** Create a Dialog for changing entry priority
	 */
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;

		switch(id) {
		    case DIALOG_PRIORITY:
		    	dialog = new Dialog(context);
		    	dialog.setContentView(R.layout.priority_dialog);
		    	dialog.setTitle("Choose a priority...");
		    	dialog.setOnDismissListener(new OnDismissListener(){
					@Override
					public void onDismiss(DialogInterface arg0) {
						init();
					}
		    	});		    	
		    	break;
	    	default:
	    		Log.i("twenty", "default: set to null");
	    		dialog = null;
	    		break;
	    }
		Log.i("twenty", "dialog returned");
	    return dialog;
	}	    
	protected void onPrepareDialog(int id, final Dialog dialog){
		switch(id) {
	    	case DIALOG_PRIORITY:
	    		
			    final Spinner s = (Spinner) dialog.findViewById(R.id.spinner);
			    ArrayAdapter<?> adapter = ArrayAdapter.createFromResource(
			            this, R.array.priorities, android.R.layout.simple_spinner_item);
			    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			    s.setAdapter(adapter);
			    
                String priority = entry_priorities.get(active_group).toString();
                Log.i("twenty", "active_group: "+active_group);
                Log.i("twenty", "setting selection to: "+ priority);
                
                prep_dialog = true;
			    s.setSelection(Integer.parseInt(priority)-1, true);
			    
			    Log.i("twenty", "setting item selected listener...");
			    s.setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
						if(creating_dialog || prep_dialog){
							creating_dialog=false;
							prep_dialog = false;
							return;
						}
				        String priority = Integer.toString(s.getSelectedItemPosition() + 1);
						try {
							HttpResponse response;
							//PUT a request to update a task
							// Add your data  
					        String vars = "?priority="+priority;
					        vars = vars.replace(" ", "%20");	
							response = HttpInterface.getInstance().executePut("todo/entries/"+active_entry+".json"+vars);
		
							String httpResult = HttpInterface.getResponseBody(response);
							Log.i("twenty", "set priority result: "+httpResult);
								
						} catch (ClientProtocolException e) {} 
						  catch (IOException e) {}
						Log.i("twenty", "dialog dismissed");
						dialog.dismiss();						
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
						Log.i("twenty", "Nothing selected");
					}
			    });	    		
	    	break;
		}
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
