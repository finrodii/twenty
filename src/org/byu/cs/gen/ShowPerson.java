package org.byu.cs.gen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.byu.cs.gen.global.HttpInterface;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * This class displays the information for a single person.
 * It also provides links to the person's direct relatives.
 * @author Seth Dickson
 */
public class ShowPerson extends Activity {

	private ShowPerson instanceRef = this;
	private Intent this_intent;
	private Bundle extras;
	
	private Intent searchIntent;
	private Intent homeScreenIntent;
	private Intent todoIntent;
	private Intent personIntent;
	private ArrayList<Integer> searches;
	private ArrayList<String> child_ids;
	
	private String mPersonId;
	private String mFatherId;
	private String mMotherId;
	private String mSpouseId;
	private String person_name = null; private TextView mPersonName;
	private SpannableString father_name = null; private TextView mFatherName;
	private SpannableString mother_name = null; private TextView mMotherName;
	private SpannableString spouse_name = null; private TextView mSpouseName;
	private String birth_date = null; private TextView mBirth_date;
	private String death_date = null; private TextView mDeath_date;
	private String marriage_date = null; private TextView mMarriage_date;
	private ListView mChildList;

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_person);
        
        this_intent = getIntent();
        searchIntent = new Intent(this, SearchResults.class); //Use this Intent for any searches, just change the extras.
        homeScreenIntent = new Intent(this, TwentyMinuteGen.class);
        todoIntent = new Intent(this, Todo.class);
        personIntent = new Intent(this, ShowPerson.class);
        
        searches = new ArrayList<Integer>();
        child_ids = new ArrayList<String>();
        
        mPersonName = (TextView) findViewById(R.id.person_name);
    	mFatherName = (TextView) findViewById(R.id.father_name);
    	mMotherName = (TextView) findViewById(R.id.mother_name);
    	mSpouseName = (TextView) findViewById(R.id.spouse_name);
    	mBirth_date = (TextView) findViewById(R.id.birth_date);
    	mDeath_date = (TextView) findViewById(R.id.death_date);
    	mMarriage_date = (TextView) findViewById(R.id.marriage_date); 
    	mChildList = (ListView) findViewById(R.id.children_list);
    	
    	extras = this_intent.getExtras();
		if (extras != null)
			mPersonId = extras.containsKey("person_id") ? extras.get("person_id").toString() : null;

		new SearchLoader().execute(new String[0]);

		mChildList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	    		personIntent.putExtra("person_id", child_ids.get(position));
	    		startActivity(personIntent);
			}
		});
    }

	/**
	 * This class retrieves person data from twenty.
	 * @author Seth Dickson
	 */
	private class SearchLoader extends AsyncTask<String,String,ArrayList<HashMap<String,String>>> {
		private ProgressDialog searchProgress;
		
		@Override
		public void onPreExecute() {
			//Display an indeterminate progress dialog while the search is performed.
			String dialog = "Retrieving Person data . .";
			searchProgress = ProgressDialog.show(instanceRef, "", dialog); 
		}

		@SuppressWarnings("unchecked")
		@Override
		protected ArrayList<HashMap<String,String>> doInBackground(String... arg0) {
	        if (mPersonId != null) {
				HttpResponse response;
				String httpResult = "";
				try {
					response = HttpInterface.getInstance().executeGet("family/people/"+mPersonId+".json");
					httpResult = HttpInterface.getResponseBody(response);
				} 
				catch (ClientProtocolException e) {e.printStackTrace(); } 
				catch (IOException e) {e.printStackTrace();}
				
				Log.i("twenty","GET /people/mPersonId.json: " + httpResult);
				//Create a map object from the result.
				Map<String,Object> personInfo = (Map<String,Object>)HttpInterface.parseJSON(httpResult);
				person_name = personInfo.get("full_name")!=null ? personInfo.get("full_name").toString() : personInfo.get("id").toString();
				birth_date = personInfo.get("birthday")!=null ? personInfo.get("birthday").toString() : "missing";
				death_date = personInfo.get("death_date")!=null ? personInfo.get("death_date").toString() : "missing";
				marriage_date = personInfo.get("marriage_date")!=null ? personInfo.get("marriage_date").toString() : "missing";
	            
				SpannableString content;
				Map<String,Object> relativeInfo;
				String[] family = {"father","mother","spouse"};
				for (String relative : family){
					content = new SpannableString("none");
					String id = "none";
					if(personInfo.get(relative)!=null){
						relativeInfo = (Map<String,Object>)HttpInterface.parseJSON(personInfo.get(relative).toString());
						id = relativeInfo.get("id").toString();
						content = new SpannableString(relativeInfo.get("name")!=null ? relativeInfo.get("name").toString() : id);
						content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
					}
					if(relative.compareTo("father")==0){ father_name = content; mFatherId = id; }
					if(relative.compareTo("mother")==0){ mother_name = content; mMotherId = id; }
					if(relative.compareTo("spouse")==0){ spouse_name = content; mSpouseId = id; }								
				}
	            

				if(personInfo.get("children")!=null){
					ArrayList<HashMap<String,String>> result = new ArrayList<HashMap<String,String>>();
					HashMap<String,String> newLine = new HashMap<String,String>();					
		            List<String> parseChildren = (List<String>)personInfo.get("children");
					//Iterate over every entry in the list
					for (String child : parseChildren) {
						relativeInfo = (Map<String,Object>)HttpInterface.parseJSON(child);
						String id = relativeInfo.get("id").toString();
						Log.i("twenty", "parsed child: " + id);
						child_ids.add(id);
						newLine.put("line1", relativeInfo.get("name")!=null ? relativeInfo.get("name").toString() : id);
						result.add(newLine);
					}
					return result;
				}
	        }
	        return null;
		}
		
		@Override
		protected void onPostExecute(ArrayList<HashMap<String,String>> result) {
			mPersonName.setText(person_name);
	        mBirth_date.setText(birth_date);
	        mDeath_date.setText(death_date);
	        mMarriage_date.setText(marriage_date);
	        mFatherName.setText(father_name);
	        mMotherName.setText(mother_name);
	        mSpouseName.setText(spouse_name);

			if(mFatherName.getText().toString().compareTo("none")!=0)
				mFatherName.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View arg0) {
			    		personIntent.putExtra("person_id", mFatherId);
			    		startActivity(personIntent);
					}}); 
			if(mMotherName.getText().toString().compareTo("none")!=0)
				mMotherName.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View arg0) {
			    		personIntent.putExtra("person_id", mMotherId);
			    		startActivity(personIntent);
					}});
			if(mSpouseName.getText().toString().compareTo("none")!=0)
				mSpouseName.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View arg0) {
			    		personIntent.putExtra("person_id", mSpouseId);
			    		startActivity(personIntent);
					}});
			
			if(result!=null){
				SimpleAdapter listItemsAdapter = new SimpleAdapter(instanceRef, result, R.layout.my_simple_list_item, new String[] {"line1"}, new int[]{android.R.id.text1});
				mChildList.setAdapter(listItemsAdapter);
			}
			
			//Get rid of the progress dialog.
			searchProgress.dismiss();
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
