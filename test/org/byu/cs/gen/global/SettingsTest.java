package org.byu.cs.gen.global;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;


public class SettingsTest {
	
	private Settings settings;
	
	@Before
	public void prepareTest() {
		settings = Settings.getInstance();
	}
	
	@Test
	public void testUserName() {
		assertEquals("", settings.getUserName());
		
		settings.setUserName("test");
		
		assertEquals("test", settings.getUserName());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testNullUserName() {
		settings.setUserName(null);
	}
	
	@Test
	public void testSessionToken() {
		assertEquals("", settings.getOauthToken());
		
		settings.setOauthToken("12345");
		
		assertEquals("12345", settings.getOauthToken());
	}
	
	@Test( expected = IllegalArgumentException.class )
	public void testNullSessionToken() {
		settings.setOauthToken(null);
	}
	
}
