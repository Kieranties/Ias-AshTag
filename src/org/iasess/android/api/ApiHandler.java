package org.iasess.android.api;

import java.lang.reflect.Type;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.iasess.android.IasessApp;
import org.iasess.android.SubmitParcel;
import org.iasess.android.Logger;
import org.iasess.android.R;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Class to handle interactions with the API
 */
public class ApiHandler {

	/**
	 * The base url for all API requests
	 */
	private static String API_BASE = IasessApp.getResourceString(R.string.ias_base);
	
	/**
	 * The version base for all API requests 
	 */
	private static String API_VERSION = IasessApp.getResourceString(R.string.ias_api_version);
	
	/**
	 * The URL partial for Taxa Gallery requests
	 */
	private static String API_TAXA_GALLERY = IasessApp.getResourceString(R.string.ias_taxa_gallery);
	
	/**
	 * The URL partial for Username validation requests 
	 */
	private static String API_USER_CHECK = IasessApp.getResourceString(R.string.ias_user_check);
	
	/**
	 * The URL partial for submission requests
	 */
	private static String API_SIGHTING = IasessApp.getResourceString(R.string.ias_sighting);
		
	/**
	 * Requests the collection of Taxa from the API
	 * 
	 * @return and ArrayList of {@link TaxaItem}
	 */
	public static ArrayList<TaxaItem> getTaxa(){
		try {
			//get service response
			String resp = HttpHandler.getResponseString(composeApiUrl(API_TAXA_GALLERY));
			
			//process through gson
			Gson gson = new Gson();
        	Type collectionType = new TypeToken<ArrayList<TaxaItem>>(){}.getType();
        	ArrayList<TaxaItem> items = gson.fromJson(resp, collectionType);
        	return items;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Processes a sighting submission
	 * 
	 * @param img The physical path to an image to be submitted
	 * @param taxa The primary key identity for the sighting
	 * @param lat The latitude of the sighting
	 * @param lon The longitude of the sighting
	 * @param user The user performing the submission
	 * @return a {@link SubmissionResponse} of the submission results
	 */
	public static SubmissionResponse submitSighting(SubmitParcel submitPackage){
		try {			
			String url = composeApiUrl(API_SIGHTING);			
			String resp = HttpHandler.executeMultipartPost(url, submitPackage.getSubmitContent());
			Gson gson = new Gson();
			return gson.fromJson(resp, SubmissionResponse.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Validates a username against the API
	 * @param textValue The username or email to validate
	 * @return a {@link UserCheckResponse} of the validation results
	 */
	public static UserCheckResponse checkUser(String textValue){
		try {
			//get service response
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("un_or_e", textValue));
			String resp = HttpHandler.getResponseString(composeApiUrl(API_USER_CHECK), params);
			
			//process through gson
			Gson gson = new Gson();
			return gson.fromJson(resp, UserCheckResponse.class);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Fetches a byte array for a given image from the site
	 * 
	 * @param url The URL of the requested image
	 * @param isRelative If true the API_BASE will be prepended to the url
	 * @return The byte[] of image data
	 */
	public static byte[] getByteArray(String url, boolean isRelative){
		try {
			if(isRelative) url = API_BASE + url;
			
			return HttpHandler.getResponseByteArray(url);
		} catch (Exception e) {
			Logger.debug("Failed fetching image at: " + url);
		}
		return null;
	}
	
 	/**
 	 * Helper method to compose the full URL for a request
 	 * 
 	 * @param string The partial URL to complete
 	 * @return The full URL for the request
 	 */
 	private static String composeApiUrl(String string){
		return API_BASE + API_VERSION + string;
	}
}