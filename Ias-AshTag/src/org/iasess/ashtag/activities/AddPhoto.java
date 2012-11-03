package org.iasess.ashtag.activities;

import java.net.URI;

import org.iasess.ashtag.AshTagApp;
import org.iasess.ashtag.ImageHandler;
import org.iasess.ashtag.R;
import org.iasess.ashtag.SubmitParcel;
import org.iasess.ashtag.handlers.ClickHandler;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.nostra13.universalimageloader.core.ImageLoader;

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
		getSupportActionBar().setTitle(R.string.add_photo);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
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
		
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
       MenuInflater inflater = getSupportMenuInflater();
       inflater.inflate(R.menu.add_photo, menu);
       return super.onCreateOptionsMenu(menu);
    }
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
	    switch (item.getItemId()) {
		    case android.R.id.home:
		    	this.finish();
	            return true;
		    case R.id.btnAddLocation:
		    	Intent intent = new Intent(this, SetLocation.class);
		    	intent.putExtra(SubmitParcel.SUBMIT_PARCEL_EXTRA, _package);
		    	startActivityForResult(intent, 0);
		    	return true;
		    case R.id.btnAddSighting:
		    	new ClickHandler(this).onAddSightingClick(item);
		    	return true;
	    }
	
		return super.onOptionsItemSelected(item);
	}
	
	 @Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			//super.onActivityResult(requestCode, resultCode, data);
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
	    	String imagePath = "file://" + _package.getImagePath();
	    	ImageLoader.getInstance().displayImage(imagePath, iv);
	    	AshTagApp.makeToast("Tap image to change");
    	}
    }
}
