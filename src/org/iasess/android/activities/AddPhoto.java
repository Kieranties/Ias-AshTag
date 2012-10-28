package org.iasess.android.activities;

import java.net.URI;

import org.iasess.android.SubmitParcel;
import org.iasess.android.ImageHandler;
import org.iasess.android.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

/**
 * Controls the 'AddPhoto' Activity view
 */
public class AddPhoto extends InvadrActivityBase {
    
	private SubmitParcel _package = null;
	
	/**
	 * Initialises the content of the Activity
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_photo);
		
		//check for data from state change
        if(savedInstanceState != null && savedInstanceState.containsKey(SubmitParcel.SUBMIT_PARCEL_EXTRA)){
        	_package = savedInstanceState.getParcelable(SubmitParcel.SUBMIT_PARCEL_EXTRA);
	    }
        
        //if still null check for data from intent
        if(_package == null){
        	_package = getIntent().getParcelableExtra(SubmitParcel.SUBMIT_PARCEL_EXTRA);
        }
        
        //if still null check if we have come from a share/send to action
        if(_package == null){
        	Bundle extras = getIntent().getExtras();
        	if(extras != null && extras.containsKey(Intent.EXTRA_STREAM)){
        		Uri uri = extras.getParcelable(Intent.EXTRA_STREAM);
        		_package = new SubmitParcel(ImageHandler.getPath(uri));
        		//need to set a flag to track that we have come in from external source
        		_package.setIsExternal(true);
        	}
        }                    
        
        //if we have a selected image, set it
        if(_package != null) setImageView();
    }    
	
    /**
	 * Store saved content on Activity state change
	 * 
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		
		outState.putParcelable(SubmitParcel.SUBMIT_PARCEL_EXTRA, _package);
	}
		
    /**
     * Handler to populate and execute an Intent
     * to pass control to the next stage of the application
     * 
     * @param v The {@link View} which fired the event handler
     */
    public void onNextClick(View v){
    	Intent intent = new Intent(this, TaxaListing.class);
    	intent.putExtra(SubmitParcel.SUBMIT_PARCEL_EXTRA, _package);
    	startActivityForResult(intent, 0);
    }
    
    /**
     * Handler to pass control to the image selection process
     * 
     * @param v The {@link View} which fired the event handler
     */
    public void onImageClick(View v){
    	new ImageHandler(this).showChooser();
    }
     
    /**
     * Handles the response from an ActivityResult fired in the context
     * of this Activity
     * 
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
     */
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == Activity.RESULT_OK){
			//we're expecting the intent to have been an image type initiated by this app
			String path = ImageHandler.getImagePathFromIntentResult(resultCode, requestCode, data);
			_package.setImagePath(path);
			setImageView();
		}			
	}
   
    /**
     * Populates the {@link ImageView} in this activity with the image
     * from the given {@link URI}
     * 
     * @param uri The {@link URI} to be displayed
     */
    private void setImageView(){
    	if(_package != null){
	    	ImageView iv = (ImageView)findViewById(R.id.imageView);
	    	Bitmap bm = ImageHandler.getBitmap(_package.getImagePath());    	
	    	iv.setImageBitmap(bm);
    	}
    }
}
