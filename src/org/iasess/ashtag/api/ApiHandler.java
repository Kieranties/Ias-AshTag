package org.iasess.ashtag.api;

import java.lang.reflect.Type;
import java.util.ArrayList;

import org.iasess.ashtag.AshTagApp;
import org.iasess.ashtag.R;
import org.iasess.ashtag.SubmitParcel;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Class to handle interactions with the API
 */
public class ApiHandler {

	/**
	 * The base url for all API requests
	 */
	private static String API_BASE = AshTagApp.getResourceString(R.string.ias_base);
	
	/**
	 * The version base for all API requests 
	 */
	private static String API_VERSION = AshTagApp.getResourceString(R.string.ias_api_version);
	
	/**
	 * The URL partial for Taxa Gallery requests
	 */
	private static String API_TAXA_GALLERY = AshTagApp.getResourceString(R.string.ias_taxa_gallery);
		
	/**
	 * The URL partial for submission requests
	 */
	private static String API_SIGHTING = AshTagApp.getResourceString(R.string.ias_sighting);
		
	
	private static String ASHTAG_CAPAIGN = AshTagApp.getResourceString(R.string.ashtag_campaign);
	/**
	 * Requests the collection of Taxa from the API
	 * 
	 * @return and ArrayList of {@link TaxaItem}
	 */
	public static ArrayList<TaxonItem> getTaxa(){
		try {
			//get service response
			String resp = HttpHandler.getResponseString(composeApiUrl(API_TAXA_GALLERY));
			
			//process through gson
			Gson gson = new Gson();
        	Type collectionType = new TypeToken<ArrayList<TaxonItem>>(){}.getType();
        	ArrayList<TaxonItem> items = gson.fromJson(resp, collectionType);
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
	
	public static CampaignModel GetCampaignDetails(){
		try {
			//get service response
			String resp = HttpHandler.getResponseString(composeApiUrl(ASHTAG_CAPAIGN));
			
			//process through gson
			Gson gson = new Gson();
        	Type modelType = new TypeToken<CampaignModel>(){}.getType();
        	CampaignModel model = gson.fromJson(resp, modelType);
        	return model;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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