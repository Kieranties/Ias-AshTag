package org.iasess.ashtag;

import android.app.Application;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * Extends the base Application with static accessors
 * and globally useful methods *
 */
public class AshTagApp extends Application {

	/**
	 * Reference to application context, initialised in constructor
	 */
	private static ContextWrapper _context;
	
	/**
	 * The key used to reference a stored username 
	 */
	public static final String PREFS_USERNAME = "username";
	
	/**
	 * @return the Application context as a ContextWrapper
	 * @see ContextWrapper
	 */
	public static final ContextWrapper getContext(){
		return _context;
	}
	
	/**
	 * @return The SharedPreferences for the Application Context
	 * @see SharedPreferences
	 */
	public static final SharedPreferences getPreferences(){
		return PreferenceManager.getDefaultSharedPreferences(_context);
	}
	
	/**
	 * Fetch the requested key value from the Shared Preferences
	 * 
	 * @param key The key name of the value to return from the SharedPreferences
	 * @param defValue The value to return if no entry is found for the key
	 * @return The value of key in the SharedPreferences, otherwise defValue
	 * @see SharedPreferences
	 */
	public static final String getPreferenceString(String key, String defValue){
		return getPreferences().getString(key, defValue);
	}

	/**
	 * Fetch the requested key value from the Shared Preferences
	 * 
	 * @param key The key name of the value to return from the SharedPreferences
	 * @return The value of key in the SharedPreferences, otherwise an empty string
	 * @see SharedPreferences
	 */
	public static final String getPreferenceString(String key){
		return getPreferenceString(key, "");
	}
	
	/**
	 * Store the given key/value pair in the SharedPreferences
	 * @param key The key with which the data can later be accessed
	 * @param value The value to store at key
	 * @see SharedPreferences
	 */
	public static final void setPreferenceString(String key, String value){
		Editor editor = getPreferences().edit();
		editor.putString(key, value);
		editor.commit();
	}	
	
	/**
	 * Returns the requested resource string from the application context
	 * 
	 * @param id The resource identifier to return
	 * @return the string value of the requested resource
	 */
	public static final String getResourceString(int id){
		return _context.getString(id);
	}
	
	/**
	 * Sets the username preference string to the given value
	 * @param value
	 */
	public static final void setUsernamePreferenceString(String value){
		setPreferenceString(PREFS_USERNAME, value);
	}
	
	/**
	 * Gets the username preference string
	 * @return - The value set as the username
	 */
	public static final String getUsernamePreferenceString(){
		return getPreferenceString(PREFS_USERNAME);
	}
	
	/**
	 * Helper method to display {@link Toast} messages
	 * 
	 * @param context The ContextWrapper to show the message within
	 * @param message The text to display
	 * @see Toast
	 */
	public static void makeToast(ContextWrapper context, String message){
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	}
		
	/**
	 * Helper method to display {@link Toast} messages
	 * <p>
	 * Available to all classes in the application domain
	 * 
	 * @param message The text to display
	 * @see Toast
	 */
	public static void makeToast(String message){
		makeToast(_context, message);
	}
	
	/**
	 * Goes through all views and unbinds drawable objects.
	 * Provides better performance when pausing/disposing of activities
	 * @param view
	 */
	public static void unbindDrawables(View view){
		if(view == null || view instanceof AdapterView){
			return;
		}
		
	    if (view.getBackground() != null) {
	        view.getBackground().setCallback(null);
	    }
	    
	    if (view instanceof ViewGroup) {
	        for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
	            unbindDrawables(((ViewGroup) view).getChildAt(i));
	        }
	        ((ViewGroup) view).removeAllViews();
	    }
	}
	
	/**
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		_context = (ContextWrapper) getApplicationContext();
		prepareImageLoader();
	}
	
	private void prepareImageLoader(){		
		// Enable caching for images
		DisplayImageOptions displayOptions = new DisplayImageOptions.Builder()
			.cacheInMemory()
			.cacheOnDisc()
			.build();
		
		// Configure loader with display options and other settings
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
				.offOutOfMemoryHandling()
				.defaultDisplayImageOptions(displayOptions)
				//.memoryCache(new UsingFreqLimitedMemoryCache(2 * 1024 * 1024))
				//.discCache(new UnlimitedDiscCache(cacheDir))
				//.discCacheFileNameGenerator(new HashCodeFileNameGenerator())
				//.imageDownloader(new URLConnectionImageDownloader(5 * 1000, 20 * 1000))				
				.build();
		
		// Init instance
		ImageLoader.getInstance().init(config);	
	}
}