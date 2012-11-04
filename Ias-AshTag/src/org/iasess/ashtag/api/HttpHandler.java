package org.iasess.ashtag.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

/**
 * Class to perform all HTTP operations for the application
 */
public class HttpHandler {

	/**
	 * Executes a GET request
	 * 
	 * @param url
	 *            The URL to execute the request against
	 * @param qsParams
	 *            The query string parameters to be sent in the request
	 * @return An {@link HtepEntity} of the response data
	 * @throws Exception
	 */
	private static HttpEntity executeGet(String url, ArrayList<NameValuePair> qsParams) throws Exception {
		try {
			// init client
			HttpClient client = new DefaultHttpClient();
			if (qsParams != null) {
				url += "?" + URLEncodedUtils.format(qsParams, "UTF-8");
			}
			HttpGet getter = new HttpGet(url);
			// execute
			HttpResponse response = client.execute(getter);
			return response.getEntity();
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Performs a multi-part POST request
	 * 
	 * @param url
	 *            The URL for the request
	 * @param imgPath
	 *            The path to an image to be submitted
	 * @param fields
	 *            A collection of key/values to submit as fields
	 * @return A String of JSON data
	 * @throws Exception
	 */
	public static String executeMultipartPost(String url, HashMap<String, ContentBody> fields) throws Exception {
		try {
			// init client
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost poster = new HttpPost(url);

			// populate submission content from field map
			MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			for (HashMap.Entry<String, ContentBody> entry : fields.entrySet()) {
				multipartEntity.addPart(entry.getKey(), entry.getValue());
			}
			poster.setEntity(multipartEntity);

			// perform the actual post
			return httpclient.execute(poster, new ResponseHandler<String>(){
				@Override
				public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
					HttpEntity r_entity = response.getEntity();
					return EntityUtils.toString(r_entity);
				}
			});
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Fetches a byte[] for the given URL
	 * 
	 * @param url
	 *            The URL to execute the request against
	 * @return A byte[] of the response
	 * @throws Exception
	 */
	public static byte[] getResponseByteArray(String url) throws Exception {
		try {
			return EntityUtils.toByteArray(executeGet(url, null));
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Returns the response of a simple GET request
	 * 
	 * @param url
	 *            The URL to execute the request against
	 * @return A String of JSON data
	 * @throws Exception
	 */
	public static String getResponseString(String url) throws Exception {
		try {
			return getResponseString(url, null);
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Returns the response of a simple GET request
	 * 
	 * @param url
	 *            The URL to execute the request against
	 * @param qsParams
	 *            A collection of query string parameters to append to the URL
	 * @return A String of JSON data
	 * @throws Exception
	 */
	public static String getResponseString(String url, ArrayList<NameValuePair> qsParams) throws Exception {
		try {
			return EntityUtils.toString(executeGet(url, qsParams));
		} catch (Exception e) {
			throw e;
		}
	}
}
