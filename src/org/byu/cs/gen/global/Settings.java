package org.byu.cs.gen.global;

/**
 * This singleton class provides access to global application settings.
 * @author Scott Slaugh
 *
 */
public class Settings {

	private static Settings instance;
	
	private String userName;
	private String sessionToken;
	
	/**
	 * Construct the settings instance and set default values.
	 */
	private Settings() {
		setUserName("");
		setSessionToken("");
	}
	
	/**
	 * Get the settings instance.
	 * @return The settings instance.
	 */
	public static Settings getInstance() {
		if (instance == null)
			instance = new Settings();
		
		return instance;
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
	 * Set the current session token.
	 * @param sessionToken The session token.
	 * @throws IllegalArgumentException If session token is null
	 */
	public void setSessionToken(String sessionToken) {
		if (sessionToken == null)
			throw new IllegalArgumentException("Session token cannot be null!");
		
		this.sessionToken = sessionToken;
	}

	/**
	 * Get the current session token.
	 * @return The current session token.
	 */
	public String getSessionToken() {
		return sessionToken;
	}
	
}
