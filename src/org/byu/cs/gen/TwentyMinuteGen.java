package org.byu.cs.gen;


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
import android.view.View;
import android.widget.Button;

public class TwentyMinuteGen extends Activity {
	
	private Button loginButton;
	private final Context context = this;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        loginButton = (Button)findViewById(R.id.loginbutton);
        loginButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new LoginTask().execute(new String[0]);
			}
		});
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.genmenu, menu);
        return true;
    }
    
    private class LoginTask extends AsyncTask<String,String,Intent> {
    	
    	private ProgressDialog loginDialog;
    	
    	@Override
    	protected void onPreExecute() {
    		loginDialog = ProgressDialog.show(context, "", "Logging in . . .");
    	}

		@Override
		protected Intent doInBackground(String... params) {
			
			try {
				HttpResponse response = HttpInterface.getInstance().executeGet("login/login.json?callback_url=twentymingen-app:///");
				String result = HttpInterface.getResponseBody(response);
				Map<String,Object> parseResult = HttpInterface.parseJSON(result);
				
				String token = parseResult.get("oauthtok").toString();
				String url = parseResult.get("loginurl").toString();
				
				Settings.getInstance().setSessionToken(token);
				
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				return i;
				
			} catch (Exception e) {
				Log.e("Error", "Problem with HttpClient!", e);
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Intent result) {
			loginDialog.dismiss();
			startActivity(result);
		}
    	
    }
}