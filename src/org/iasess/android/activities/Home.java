package org.iasess.android.activities;

import org.iasess.android.SubmitParcel;
import org.iasess.android.ImageHandler;
import org.iasess.android.Logger;
import org.iasess.android.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

/**
 * Controls the 'Home' Activity view
 */
public class Home extends InvadrActivityBase {
	
    /**
     * Initialises the content of the Activity
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        
        //TODO: Change this to a first run event only
        //check we have a username saved
        //if(IasessApp.getUsernamePreferenceString().equals("")){
        //	//if we don't, show the settings screen.
        //}
    }
    
    /**
	 * Handler for the display of the menu
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.home_menu, menu);
		return true;
	}
    
	/**
	 * Handler for the selection of a menu option
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.menu_about:
				intent = new Intent(this, About.class);
				break;
			case R.id.menu_settings:
				intent = new Intent(this, Settings.class);
				break;
		}

		if(intent != null){
			startActivity(intent);
			return true;
		}
		else{
			return super.onOptionsItemSelected(item);
		}    	
	}
	
    /**
     * Handler to pass control to the image selection process
     * 
     * @param v The {@link View} which fired the event handler
     */
    public void onAddPhotoClick(View v) {
    	new ImageHandler(this).showChooser();
    }
    
    
    /**
     * Handler to populate and process an Intent to 
     * pass control to the gallery view of the application
     * 
     * @param v The {@link View} which fired the event handler
     */
    public void onViewGalleryClick(View v) {
    	Intent intent = new Intent(this, TaxaListing.class);
    	intent.putExtra("gallery", true);
    	startActivity(intent);
    }
    
    /**
     * Handles the response of an ActivityResult fired in the context of
     * this Activity
     * 
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
     */
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == Activity.RESULT_OK){
			//we're expecting the intent to be an image intent initiated by this app
			String selected = ImageHandler.getImagePathFromIntentResult(resultCode, requestCode, data);
			if(selected != null){
				//pass data to next activity
				Intent intent = new Intent(this, AddPhoto.class);
				intent.putExtra(SubmitParcel.SUBMIT_PARCEL_EXTRA, new SubmitParcel(selected));
				startActivity(intent);
			} 
			else{
				Logger.warn("Could not get a selected image");
			}
		}		
	}
}