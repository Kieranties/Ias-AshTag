package org.iasess.ashtag.handlers;

import org.iasess.ashtag.AshTagApp;
import org.iasess.ashtag.ImageHandler;

import android.app.Activity;

import com.actionbarsherlock.view.MenuItem;

public class ClickHandler {

	private Activity _activity;
	
	public ClickHandler(Activity activity){
		_activity = activity;
	}
	public void onAddSightingClick(MenuItem mi) {
    	if(!(android.util.Patterns.EMAIL_ADDRESS.matcher(AshTagApp.getUsernamePreferenceString()).matches())){
        	new ImageHandler(_activity).showChooser();
    	} else {
    		//TODO: ask for email address
    	}
    } 
}
