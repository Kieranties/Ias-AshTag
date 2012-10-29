package org.iasess.ashtag.activities;

import org.iasess.ashtag.AshTagApp;
import org.iasess.ashtag.ImageHandler;
import org.iasess.ashtag.Logger;
import org.iasess.ashtag.R;
import org.iasess.ashtag.SubmitParcel;
import org.iasess.ashtag.api.ApiHandler;
import org.iasess.ashtag.api.CampaignModel;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

/**
 * Controls the 'Home' Activity view
 */
public class Home extends InvadrActivityBase {//implements OnEditorActionListener {
    
	private EditText _username;
	
	/**
     * Initialises the content of the Activity
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);              
    	        
        //populate the username box
        _username = (EditText) findViewById(R.id.editUsername);
        _username.setText(AshTagApp.getPreferenceString(AshTagApp.PREFS_USERNAME));
		CheckUsername();
		
        //hack to calculate when the keyboard is displayed
        // http://stackoverflow.com/questions/2150078/how-to-check-visibility-of-software-keyboard-in-android
		final View activityRootView = findViewById(R.id.rootView);
		activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			boolean checkOnNext;
			public void onGlobalLayout() {
				if(checkOnNext){
					CheckUsername();
					checkOnNext = false;
				} else {
				    Rect r = new Rect();
				    activityRootView.getWindowVisibleDisplayFrame(r);		
				    int heightDiff = activityRootView.getRootView().getHeight() - (r.bottom - r.top);
				    checkOnNext = heightDiff > 100;
				}
			 }
		});
		
		// Hook up the click listener for the campaign info
		ImageView img = (ImageView) findViewById(R.id.info);
		img.setOnClickListener(new OnClickListener() {
		    public void onClick(View v) {
		    	new CampaignUpdate().execute();
		    }
		});
		
		//Check to see if we've fetched the campaign details before
		String site = CampaignModel.getInstance().getSite(); 
		if(site == null || site.length() == 0) new CampaignUpdate(false).execute();
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
    
    private void CheckUsername(){
    	String text = _username.getText().toString().trim();
    	Button btn = (Button) findViewById(R.id.buttonSubmit);
    	if(android.util.Patterns.EMAIL_ADDRESS.matcher(text).matches()){
    		AshTagApp.setPreferenceString(AshTagApp.PREFS_USERNAME, text);    		
    		btn.setEnabled(true);    		
    		btn.setBackgroundColor(getResources().getColor(R.color.ias_main));
    	} else {
    		btn.setEnabled(false);
    		btn.setBackgroundColor(getResources().getColor(R.color.ias_main_fade));
    		AshTagApp.makeToast("Invalid email address");
    	}
    }
    
    /**
	 * Class to process the checking of a username in a separate thread
	 */
	private class CampaignUpdate extends AsyncTask<Void, Void, CampaignModel> {	
		
		/**
		 * The progress dialog to display while processing
		 */
		private ProgressDialog _dlg;
		private boolean _displayDetails;
		
		public CampaignUpdate(boolean displayDetails){
			_displayDetails = displayDetails;
		}
		
		public CampaignUpdate(){
			_displayDetails = true;
		}
		/**
		 * Displays the progress dialog before executing the async task
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		protected void onPreExecute() {
			//display the dialog to the user
			_dlg = ProgressDialog.show(Home.this, "", "Getting details...", true,true, new OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					CampaignUpdate.this.cancel(true);	
					finish();
				}
			});
	    }
        
		/**
		 * Executes a request to the API to check the username
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		protected CampaignModel doInBackground(Void... textValue) {
	    	//return the response from the api
	        return ApiHandler.GetCampaignDetails();
	    }
	    
	    /**
	     * Dismisses the dialog and updates the UI with the results
	     * of the API request
	     * 
	     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	     */
	    protected void onPostExecute(CampaignModel result) {
	    	if(result != null){
	    		result.save();	    		
	    	} else {
	    		AshTagApp.makeToast("Could not get campaign details");
	    	}
	    	
	    	_dlg.dismiss();
	    	
	    	if(_displayDetails){
	    		Intent intent = new Intent(Home.this, About.class);
	    		startActivity(intent);
	    	}
	    }
	}
}