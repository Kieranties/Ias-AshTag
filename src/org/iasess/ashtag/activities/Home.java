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
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

/**
 * Controls the 'Home' Activity view
 */
public class Home extends InvadrActivityBase implements OnEditorActionListener {
	
	private EditText _editText;
	
    /**
     * Initialises the content of the Activity
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        
        //find the edit box
		_editText = (EditText) findViewById(R.id.editUsername);
	
		// set from application preferences
		_editText.setText(AshTagApp.getPreferenceString(AshTagApp.PREFS_USERNAME));
	
		// attach listener for focus lost events
		_editText.setOnEditorActionListener(this);
		CheckUsername();
		
		ImageView img = (ImageView) findViewById(R.id.info);
		img.setOnClickListener(new OnClickListener() {
		    public void onClick(View v) {
		    	new CampaignUpdate().execute();
		    }
		});
		
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
    
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		
		switch(actionId){
			case EditorInfo.IME_ACTION_DONE:
			case EditorInfo.IME_ACTION_GO:
			case EditorInfo.IME_ACTION_NEXT:
				CheckUsername();
				return false;
		}
		
		if(event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
			CheckUsername();
		}
		
		return false;
	}
    
    private void CheckUsername(){
    	String text = _editText.getText().toString().trim();
    	AshTagApp.setPreferenceString(AshTagApp.PREFS_USERNAME, text);
    	Button btn = (Button) findViewById(R.id.buttonSubmit);
    	boolean enabled = text != null && text.length() != 0;
    	if(enabled){
    		btn.setEnabled(true);    		
    		btn.setBackgroundColor(getResources().getColor(R.color.ias_main));
    	} else {
    		btn.setEnabled(false);
    		btn.setBackgroundColor(getResources().getColor(R.color.ias_main_fade));
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