package org.iasess.ashtag.activities;

import org.iasess.ashtag.ImageHandler;
import org.iasess.ashtag.Logger;
import org.iasess.ashtag.R;
import org.iasess.ashtag.SubmitParcel;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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