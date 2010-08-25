package org.byu.cs.gen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.byu.cs.gen.global.HttpInterface;
import org.byu.cs.gen.global.Settings;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * This is the main activity of the Twenty Minute Genealogist application.
 * It takes care of logging in and out. If logged in it makes options available to initiate other activities.
 * @author Scott Slaugh & Seth Dickson
 *
 */
public class TwentyMinuteGen extends Activity {
	
	private Button loginButton;
	private Button logoutButton;
	
	private final Context context = this;
	private final TwentyMinuteGen instanceRef = this;
	private TextView mainText;
	private Intent searchIntent;
	private Intent todoIntent;
	private ArrayList<Integer> searches;
	
	private boolean verifying = false;
	private boolean verified = false;
	
//	private Button testButton;	
//	private Intent testIntent;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        todoIntent = new Intent(this, Todo.class);
        searchIntent = new Intent(this, SearchResults.class); //Use this Intent for any searches, just change the extras.
        searches = new ArrayList<Integer>();
        
        mainText = (TextView)findViewById(R.id.mainlabel);

        loginButton = (Button)findViewById(R.id.loginbutton);
        loginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//Create a new thread to handle the login so that the UI doesn't freeze.
				new LoginTask().execute(new String[0]);
			}
		});
        logoutButton = (Button)findViewById(R.id.logoutbutton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Settings.getInstance().clearSettings();
				//Create a new thread to handle the logout task
				new LogoutTask().execute(new String[0]);
			}
		});
        
        //testIntent = new Intent(this, Search.class);
//        testButton = (Button)findViewById(R.id.testbutton);
//        testButton.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				startActivity(testIntent);
//			}
//        });

    	loginButton.setVisibility(View.GONE);
        logoutButton.setVisibility(View.GONE);
        
        Log.i("twenty","Username: " + Settings.getInstance().getUserName());
        if(Settings.getInstance().getUserName().length() > 0){ //if there is a user name, should be logged in.
        	new CheckLoggedInTask().execute(new String[0]);
        }
        else
        	loginButton.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume(){
    	super.onResume();
    	
    	if(verifying){
    		new VerifyLoginTask().execute(new String[0]);
    	}
    	else if(verified){
	        Log.i("twenty","Starting at root*******************");
	        if(Settings.getInstance().getUserName().length() > 0){
	        	Log.i("twenty","Username: " + Settings.getInstance().getUserName());
	        	new CheckLoggedInTask().execute(new String[0]);
	        }    		
    	}
    }
 
    private class CheckLoggedInTask extends AsyncTask<String,String,String> {
    	private ProgressDialog checkDialog;
    	
    	@Override
    	protected void onPreExecute() {
    		//Show an indeterminate progress dialog while preparing to login.
    		Log.i("twenty", "starting checkloggedin");
    		checkDialog = ProgressDialog.show(context, "", "Checking logged in . . .");
    	} 	
    	
		@SuppressWarnings("unchecked")
		@Override
		protected String doInBackground(String... params) {
			Log.i("twenty", "check doInBackground started");
			try {
	        	//Call the checkloggedin endpoint to see if we're already logged in.
				HttpResponse response = HttpInterface.getInstance().executeGet("login/checkloggedin.json?nfssessionid=" + Settings.getInstance().getSessionID());
				String result = HttpInterface.getResponseBody(response);
				Log.i("twenty", "check logged in response: " + result);
				Map<String,Object> parseResult = (Map<String,Object>)HttpInterface.parseJSON(result);
				
				//Make sure we were successful
				boolean logged_in = (Boolean)parseResult.get("logged_in");
				
				if (logged_in)
					return ("Welcome " + Settings.getInstance().getUserName() + "!");
				else 
					return null;
			} catch (Exception e) {
				Log.e("Error", "Problem with HttpClient!", e);
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {	
			checkDialog.dismiss();
			
			if (result == null) 
				setLogged(false, "A timeout or error occurred while checking logged in!");
			else 
				setLogged(true, result);
		}    	
    }    
    
    /**
     * This thread class handles the initial login process.
     * @author Scott Slaugh
     */
    private class LoginTask extends AsyncTask<String,String,Intent> {
    	private ProgressDialog loginDialog;
    	
    	@Override
    	protected void onPreExecute() {
    		//Show an indeterminate progress dialog while preparing to login.
    		loginDialog = ProgressDialog.show(context, "", "Logging in . . .");
    	}

		@SuppressWarnings("unchecked")
		@Override
		protected Intent doInBackground(String... params) {
			
			try {
				//Call the login endpoint, passing a URL that will launch the WelcomeScreen
				//activity as the callback URL
				HttpResponse response = HttpInterface.getInstance().executeGet("login/login.json?callback_url=twentymingen-app:///");
				String result = HttpInterface.getResponseBody(response);
				Map<String,Object> parseResult = (Map<String,Object>)HttpInterface.parseJSON(result);
				Log.d("twenty", "Login response: "+parseResult);
				//Grab the oauthtok and login URL values from the result
				String token = parseResult.get("oauthtok").toString();
				String url = parseResult.get("loginurl").toString();
				
				//Save the session token
				Settings.getInstance().setOauthToken(token);
				
				//Create an intent that will launch the web browser to view the login URL
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				return i;
				
			} catch (Exception e) {
				Log.e("Error", "Problem with HttpClient! in LoginTask()", e);
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Intent result) {
			//Close the progress dialog.
			loginDialog.dismiss();
			if(result != null){
				//Launch the web browser.
				startActivity(result);
				verifying = true;
			}
		}
    }
    
    private class VerifyLoginTask extends AsyncTask<String,String,String> {
    	
    	private ProgressDialog loginDialog;
    	
    	@Override
    	protected void onPreExecute() {
    		//Show an indeterminate progress dialog while preparing to login.
    		loginDialog = ProgressDialog.show(context, "", "Completing login process . . .");
    	}

		@SuppressWarnings("unchecked")
		@Override
		protected String doInBackground(String... params) {
			try {
	        	//Call the verifylogin endpoint in order to complete the login.
				HttpResponse response = HttpInterface.getInstance().executeGet("login/verifylogin.json?oauthtok=" + Settings.getInstance().getOauthToken());
				String result = HttpInterface.getResponseBody(response);
				Log.i("twenty", "verify login response: " + result);
				Map<String,Object> parseResult = (Map<String,Object>)HttpInterface.parseJSON(result);
				
				//Make sure we were successful
				boolean logged_in = (Boolean)parseResult.get("logged_in");
				
				if (logged_in) {
					//Get the session id and username
					String username = parseResult.get("username").toString();
					String session_id = parseResult.get("sessionid").toString();
					
					Settings.getInstance().setUserName(username);
					Settings.getInstance().setSessionID(session_id);
					response = HttpInterface.getInstance().executeGet("default/background");
					return ("Welcome " + Settings.getInstance().getUserName() + "!");
				}
				else {
					return null;
				}
				
			} catch (Exception e) {
				Log.e("Error", "Problem with HttpClient!", e);
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			loginDialog.dismiss();
			if (result == null) 
				setLogged(false, "An error occurred while verifying login!");
			else 
				setLogged(true, result);
		}
    }
    
    private class LogoutTask extends AsyncTask<String,String,String> {
		@SuppressWarnings("unchecked")
		@Override
		protected String doInBackground(String... params) {
			HttpResponse response;
			try {
				response = HttpInterface.getInstance().executeGet("login/logout.json");
				String result = HttpInterface.getResponseBody(response);
				Map<String,Object> parseResult = (Map<String,Object>)HttpInterface.parseJSON(result);
				
				boolean logged_in = (Boolean)parseResult.get("logged_in");
				
				if (!logged_in) {
					return "Successful Logout";
				}
				else{
					Log.e("Logout", "An error occurred while logging out!");
					return null;
				}			
				
			} catch (Exception e) { Log.e("Error", "Problem with HttpClient!", e); }
			return null;
		}

		@Override
		protected void onPostExecute(String result) {	
			
			if (result == null) 
				setLogged(false, "An error occurred while checking logged in!");
			else 
				setLogged(false, result);
			
            Intent intent = instanceRef.getIntent();
            startActivity(intent);	
		}  		
		
    }

    private void setLogged(boolean logged_in, String text){
    	if(logged_in){
    		loginButton.setVisibility(View.GONE);
    		logoutButton.setVisibility(View.VISIBLE);
    		verifying = false;
    		verified = true;
    	}
    	else{
    		loginButton.setVisibility(View.VISIBLE);
    		logoutButton.setVisibility(View.GONE); 
    		verifying = false;
    		verified = false;    		
    	}
    	if(text.length()>0) //otherwise just leave as is
    		mainText.setText(text);
    }
    

    public boolean onPrepareOptionsMenu (Menu menu){
    	if(verified)	
    		return true;
    	else
    		return false;
    }
    
    @SuppressWarnings("unchecked")
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	menu.removeItem(R.id.home);
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
