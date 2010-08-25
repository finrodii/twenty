package org.byu.cs.gen.global;

import android.util.Log;

/**
 * This singleton class provides access to global application settings.
 * @author Scott Slaugh & Seth Dickson
 *
 */
public class Settings {

	private static Settings instance;
	
	private String userName;
	private String sessionID;
	private String oauthToken;
	
	/**
	 * Construct the settings instance and set default values.
	 */
	private Settings() {
		setUserName("");
		setSessionID("");
		setOauthToken("");
	}
	
	/**
	 * Get the settings instance.
	 * @return The settings instance.
	 */
	public static Settings getInstance() {
		if (instance == null){
			Log.i("twenty","Created new Settings instance");
			instance = new Settings();
		}
		
		return instance;
	}
	
	/**
	 * Clear settings instance on logout
	 */
	public void clearSettings(){
		userName = "";
		sessionID = "";
		oauthToken = "";
	}

	/**
	 * Set the username for the application.
	 * @param userName The username.
	 * @throws IllegalArgumentException If username is null
	 */
	public void setUserName(String userName) {
		if (userName == null)
			throw new IllegalArgumentException("Username cannot be null!");
		
		this.userName = userName;
	}

	/**
	 * Get the username.
	 * @return
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Set the session id for the application.
	 * @param session_id The current session id.
	 * @throws IllegalArgumentException If session_id is null
	 */
	public void setSessionID(String session_id) {
		if (session_id == null)
			throw new IllegalArgumentException("Session id cannot be null!");
		
		this.sessionID = session_id;
	}

	/**
	 * Get the current session id.
	 * @return
	 */
	public String getSessionID() {
		return sessionID;
	}	
	
	/**
	 * Set the current session token.
	 * @param sessionToken The session token.
	 * @throws IllegalArgumentException If session token is null
	 */
	public void setOauthToken(String oauthToken) {
		if (oauthToken == null)
			throw new IllegalArgumentException("Session token cannot be null!");
		
		this.oauthToken = oauthToken;
	}

	/**
	 * Get the current session token.
	 * @return The current session token.
	 */
	public String getOauthToken() {
		return oauthToken;
	}
	
}
