package org.iasess.ashtag.activities;

import org.iasess.ashtag.AshTagApp;
import org.iasess.ashtag.ImageHandler;
import org.iasess.ashtag.R;
import org.iasess.ashtag.SubmitParcel;
import org.iasess.ashtag.api.ApiHandler;
import org.iasess.ashtag.api.CampaignModel;
import org.iasess.ashtag.api.SubmissionResponse;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

/**
 * Controls the 'Summary' Activity view
 */
public class Summary extends MapActivity{
    
	/**
	 * Request Code for the GPS intent
	 */
	private static final int GPS_INTENT = 948484;
	
	private SubmitParcel _submitParcel;
	
	/**
	 * The {@link MapController} used to manage the users location
	 */
	private MapController _mapController;
	
	/**
	 * The {@link MyLocationOverlay} used to manage the users location
	 */
	private MyLocationOverlay _locationOverlay;
	
	/**
	 * The {@link LocationManager} used to manage the users location 
	 */
	private LocationManager _locationManager;
	
	/**
	 * The {@link MapView} used to manage the users location 
	 */
	private MapView _mapView;
	

    /**
     * Initialises the content of the Activity
     * 
     * @see com.google.android.maps.MapActivity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.summary);
        _submitParcel = getIntent().getParcelableExtra(SubmitParcel.SUBMIT_PARCEL_EXTRA);
        		
        initMapComponents();
        setTaxa();
        setImageView();	    
    }
       
	/**
	 * Required override when using MapView
	 * 
	 * @see com.google.android.maps.MapActivity#isRouteDisplayed()
	 */
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	    
    /**
     * Reinstates the mapping functionality when the view
     * is bought back into focus
     * 
     * @see com.google.android.maps.MapActivity#onResume()
     */
    @Override
    protected void onResume() {
    	super.onResume();   	
    	renderMapView();
    }
    
    /**
     * Pauses the mapping functionality when the view loses
     * focus
     * 
     * @see com.google.android.maps.MapActivity#onPause()
     */
    @Override
    protected void onPause() {
    	super.onPause();
    	_locationOverlay.disableMyLocation();
    }
    
	/**
	 * Handlesthe response of an ActivityResult fired in the context
	 * of this Activity
	 * 
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == Activity.RESULT_OK && requestCode == GPS_INTENT){
			renderMapView();
		}		
	}
	
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	 
	    AshTagApp.unbindDrawables(findViewById(R.id.RootView));
	    System.gc();
	}
	
    /**
     * Executes an {@link AsyncTask} to submit a sighting
     * 
     * @param v The {@link View} which fired the event handler
     */
    public void onDoneClick(View v){
    	new SubmitSightingTask().execute("");
    }
    
	/**
	 * Initialises the map components of this Activity
	 */
	private void initMapComponents(){
		//init map related properties for pause/resume events
		_mapView = (MapView) findViewById(R.id.mapView);
		_mapView.setSatellite(true);		
		_mapController = _mapView.getController();
		_locationOverlay = new MyLocationOverlay(this, _mapView);		
		
		//check gps is enabled
		_locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
		if (!_locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			AshTagApp.makeToast(this, "Please enable GPS");
			Intent gpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivityForResult(gpsIntent, GPS_INTENT);
		} else{
			renderMapView();
		}		
	}
	
	/**
	 * Performs the rendering functions for the {@link MapView} contained
	 * in this Activity
	 */
	private void renderMapView(){		
		if(!_locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) return;
		
		//inform user we are waiting for a fix
		final ProgressDialog dialog = ProgressDialog.show(this, "", "Finding location...", true);
		
		//prepare the default overlay to fetch current location	
		_locationOverlay.enableMyLocation();
		_locationOverlay.runOnFirstFix(new Runnable(){
			public void run() {				
				//get the location and set properties
				GeoPoint loc = _locationOverlay.getMyLocation();
				_mapController.animateTo(loc);
				_mapController.setZoom(18);
				
				//we're done fetching initial fix
				dialog.cancel();
			}			
		});
		
		//add overlays to map view
		_mapView.getOverlays().add(_locationOverlay);
	}
	
	/**
	 * Sets the details for the selected Taxa on the page
	 */
	private void setTaxa(){		
		TextView tv = (TextView)findViewById(R.id.textSelectedTaxa);
		tv.setText(CampaignModel.getInstance().getTaxonCommonName());
	}
    
    /**
     * Sets the details for the selected image on the page
     */
    private void setImageView(){
    	ImageView iv = (ImageView)findViewById(R.id.imageView);
    	Bitmap bm = ImageHandler.getBitmap(_submitParcel.getImagePath());
    	
    	iv.setImageBitmap(bm);   
    }
    
    /**
     * Class to handle the submission of details in a separate thread
     *
     */
    private class SubmitSightingTask extends AsyncTask<String, Void, SubmissionResponse> {	
		
		/**
		 * The progress dialog to display while processing
		 */
		private ProgressDialog _dlg;
				
		/**
		 * Displays a progress dialog prior to the start of any processing
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		protected void onPreExecute() {
			//display the dialog to the user
			_dlg = ProgressDialog.show(Summary.this, "", "Submitting...", true,true, new OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					SubmitSightingTask.this.cancel(true);	
					finish();
				}
			});
	    }
        		
	    /**
	     * Submits the details of a sighting through the API
	     * 
	     * @see android.os.AsyncTask#doInBackground(Params[])
	     */
	    protected SubmissionResponse doInBackground(String... params) {
	    	//don't need params    
	    	Location fix = _locationOverlay.getLastFix();
	    	_submitParcel.setLocation(fix.getLatitude(), fix.getLongitude());
        	return ApiHandler.submitSighting(_submitParcel);
	    }
	    	    
	    /**
	     * Processes the result of the submission
	     * 
	     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	     */
	    protected void onPostExecute(SubmissionResponse result) {	    	
	    	_dlg.dismiss();
	    	if(result.getId() != Integer.MIN_VALUE){
	    		AshTagApp.makeToast("Submitted!");    		
	    		 	            
	    		if(_submitParcel.getIsExternal()){
	    			setResult(InvadrActivityBase.CLOSE_ALL);
	    			finish();
	    		} else {
	    			//we're done for this submission so return the app to the start
		    		Intent home = new Intent(Summary.this, Home.class);
		            home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //resets the activity stack
		            startActivity(home);
	    		}	    		 
	    	}  
	    	else
	    	{
	    		AshTagApp.makeToast("Sorry, please try again later :-(");
	    	}
	    }
	}
}