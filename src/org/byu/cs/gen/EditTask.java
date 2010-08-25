package org.byu.cs.gen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.byu.cs.gen.global.HttpInterface;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * This class provides the interface for editing a task.
 * On confirming or simply on exit the state is saved and updated back to twenty.
 * @author Seth Dickson
 */
public class EditTask extends Activity {

	private EditText mNameText;
	private TextView mPersonText;
    private EditText mNotesText;
    private String mRowId;
    private String person;
    private String entry_id;
    private Spinner s;
    private CheckBox checkbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.edit_task);

        mNameText = (EditText) findViewById(R.id.task_name);
        mPersonText = (TextView) findViewById(R.id.task_person);
        mNotesText = (EditText) findViewById(R.id.task_notes);
      
        Button confirmButton = (Button) findViewById(R.id.confirm_task);
		if (mRowId == null) {
			Bundle extras = getIntent().getExtras();
			if (extras != null){
				mRowId = extras.containsKey("task_id") ? extras.get("task_id").toString() : null;
				person = extras.containsKey("person") ? extras.get("person").toString() : null;
				entry_id = extras.containsKey("entry_id") ? extras.get("entry_id").toString() : null;
			}
		}
		
		Log.i("twenty","Editing task: "+mRowId);

	    s = (Spinner) findViewById(R.id.spinner);
	    ArrayAdapter<?> adapter = ArrayAdapter.createFromResource(
	            this, R.array.priorities, android.R.layout.simple_spinner_item);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    s.setAdapter(adapter); 

	    checkbox = (CheckBox) findViewById(R.id.checkbox);

		try {
			populateFields();
		} catch (ClientProtocolException e) {e.printStackTrace(); }
		  catch (IOException e) {e.printStackTrace(); }
		
        confirmButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        	    setResult(RESULT_OK);
        	    finish();
        	}
        });
    }
    
    @SuppressWarnings("unchecked")
	private void populateFields() throws ClientProtocolException, IOException {
        mPersonText.setText("			for "+person);
        if (mRowId != null) {
			HttpResponse response = HttpInterface.getInstance().executeGet("todo/tasks/"+mRowId+".json");
			String httpResult = HttpInterface.getResponseBody(response);
			Log.i("twenty","GET /tasks/mRowId.json: " + httpResult);
			//Create a map object from the result.
			Map<String,Object> taskInfo = (Map<String,Object>)HttpInterface.parseJSON(httpResult);

            mNameText.setText(taskInfo.get("name").toString());
            mNotesText.setText(taskInfo.get("notes").toString());
        	int priority = Integer.parseInt(taskInfo.get("priority").toString());
        	s.setSelection(priority-1);
			String complete = taskInfo.get("complete").toString();
			if (complete.compareTo("true")==0)
				checkbox.setChecked(true);
			else
				checkbox.setChecked(false);
        }
        else
        	s.setSelection(2);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
		try {
			populateFields();
		} catch (ClientProtocolException e) {e.printStackTrace(); }
		  catch (IOException e) {e.printStackTrace(); }
    }
    
    private void saveState() {
        String name = mNameText.getText().toString();
        String notes = mNotesText.getText().toString();
        String priority = Integer.toString(s.getSelectedItemPosition() + 1);
        String complete = "False";
        if (checkbox.isChecked())
        	complete = "True";

		try {
			HttpResponse response;
			if (mRowId == null) { //POST a request for a new task
				// Add your data  
		        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		        nameValuePairs.add(new BasicNameValuePair("entry_id", entry_id));
		        nameValuePairs.add(new BasicNameValuePair("name", name));
		        nameValuePairs.add(new BasicNameValuePair("notes", notes));
		        nameValuePairs.add(new BasicNameValuePair("priority", priority));
		        nameValuePairs.add(new BasicNameValuePair("complete", complete));  
				response = HttpInterface.getInstance().executePost("todo/tasks.json", nameValuePairs);
			}
			else { //PUT a request to update a task
				// Add your data  
		        String vars = "?entry_id="+entry_id+"&name="+name+"&notes="+notes+"&priority="+priority+"&complete="+complete;
		        vars = vars.replace(" ", "%20");	
				response = HttpInterface.getInstance().executePut("todo/tasks/"+mRowId+".json"+vars);
			}
			String httpResult = HttpInterface.getResponseBody(response);
			Log.i("twenty", httpResult);
				
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {}        	

    }
}
