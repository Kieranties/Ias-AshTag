package org.iasess.android.api;

import com.google.gson.annotations.SerializedName;

/**
 * Simple response object for username validation API requests
 */
public class UserCheckResponse {

	/**
	 * The answer from the request
	 */
	@SerializedName("answer")
	private String _answer;

	/**
	 * The username returned from the server
	 * when matching email addresses
	 */
	@SerializedName("username")
	private String _username;
	
	/**
	 * Gets the answer the server provided
	 * @return
	 */
	public String getAnswer(){ return _answer; }
	
	/**
	 * Gets the username the server provided
	 * @return
	 */
	public String getUsername(){ return _username; }
	
}
