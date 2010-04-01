package org.byu.cs.gen.global;

public class Settings {

	private static Settings instance;
	
	private String userName;
	private String sessionToken;
	
	private Settings() {
		setUserName("");
		setSessionToken("");
	}
	
	public static Settings getInstance() {
		if (instance == null)
			instance = new Settings();
		
		return instance;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserName() {
		return userName;
	}

	public void setSessionToken(String sessionToken) {
		this.sessionToken = sessionToken;
	}

	public String getSessionToken() {
		return sessionToken;
	}
	
}
