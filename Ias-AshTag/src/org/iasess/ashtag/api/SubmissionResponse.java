package org.iasess.ashtag.api;

import com.google.gson.annotations.SerializedName;

/**
 * Simple response object for API submissions
 */
public class SubmissionResponse {

	/**
	 * The URL of a successful response, where a user may be directed to
	 */
	@SerializedName("url")
	private String _url;

	/**
	 * The identifier associated with the sighting submission
	 */
	@SerializedName("id")
	private int _id;

	/**
	 * Gets the Id of the successful submission
	 * 
	 * @return
	 */
	public int getId() {
		return _id;
	}

	/**
	 * Gets the URL returned in a successful submission
	 * 
	 * @return The URL as a String
	 */
	public String getUrl() {
		return _url;
	}

}
