package org.byu.cs.gen.global;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.util.Log;

public class HttpInterface {

	private static HttpInterface instance;

	private DefaultHttpClient httpClient;

	// public final static String BASEURL =
	// "http://10.0.2.2:8000/twentyMinGen/";
	public final static String BASEURL = "https://twenty.cs.byu.edu/twenty/";

	public static HttpInterface getInstance() {
		if (instance == null)
			instance = new HttpInterface();

		return instance;
	}

	private HttpInterface() {
		//This code was taken from http://code.google.com/p/android/issues/detail?id=1946
		DefaultHttpClient ret = null;

		// sets up parameters
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, "utf-8");
		params.setBooleanParameter("http.protocol.expect-continue", false);

		// registers schemes for both http and https
		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		registry.register(new Scheme("https", new EasySSLSocketFactory(), 443));
		ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(
				params, registry);
		ret = new DefaultHttpClient(manager, params);
		httpClient = ret;
	}

	public DefaultHttpClient getHttpClient() {
		return httpClient;
	}

	public HttpResponse executeGet(String url) throws ClientProtocolException,
			IOException {
		if (url.startsWith("/"))
			url.replaceFirst("/", "");

		HttpGet get = new HttpGet(BASEURL + url);
		HttpResponse response = httpClient.execute(get);
		return response;
	}

	public static String getResponseBody(HttpResponse response)
			throws IOException {
		HttpEntity entity = response.getEntity();

		StringBuilder result = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(entity
				.getContent()));

		String cLine = "";

		while ((cLine = reader.readLine()) != null)
			result.append(cLine + "\n");

		return result.toString();
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> parseJSON(String jsonString) {
		JSONParser parser = new JSONParser();

		ContainerFactory containerFactory = new ContainerFactory() {
			public List<String> creatArrayContainer() {
				return new LinkedList<String>();
			}

			public Map<String, Object> createObjectContainer() {
				return new LinkedHashMap<String, Object>();
			}

		};

		Map<String, Object> json = null;
		try {
			json = (Map<String, Object>) parser.parse(jsonString,
					containerFactory);
		} catch (ParseException e) {
			Log.i("ReceivedJSON", jsonString);
			Log.e("ParseError", "An error occured while parsing JSON!", e);
		}

		return json;
	}

}
