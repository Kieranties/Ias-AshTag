package org.iasess.ashtag.activities;

import org.iasess.ashtag.AshTagApp;
import org.iasess.ashtag.R;
import org.iasess.ashtag.api.ApiHandler;
import org.iasess.ashtag.api.CampaignModel;
import org.iasess.ashtag.handlers.ActivityResultHandler;
import org.iasess.ashtag.handlers.ClickHandler;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

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
        getSupportActionBar().setDisplayShowTitleEnabled(false);   
        
        setUsernameText(findViewById(R.id.editUsername)); 
        
		//Check to see if we've fetched the campaign details before
		String site = CampaignModel.getInstance().getSite(); 
		if(site == null || site.length() == 0) new CampaignUpdate(false).execute();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       MenuInflater inflater = getSupportMenuInflater();
       inflater.inflate(R.menu.home, menu);
       return super.onCreateOptionsMenu(menu);
    }   
    
    @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		new ActivityResultHandler(this).onActivityResult(requestCode, resultCode, data);
	}
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
	    switch (item.getItemId()) {
		    case android.R.id.home:
		    	this.finish();
	            return true;
		    case R.id.btnAbout:
		    	new CampaignUpdate().execute();
		    	return true;
		    case R.id.btnAddSighting:
		    	new ClickHandler(this).onAddSightingClick(item);
		    	return true;
	    }
	
		return super.onOptionsItemSelected(item);
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
    
    public void onUsernameClick(View v){
    	boolean isValid = new ClickHandler(this).onUsernameClick();
    	if(isValid) setUsernameText(v);
    }

    private void setUsernameText(View v){
    	((TextView)v).setText(AshTagApp.getUsernamePreferenceString());
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
			_dlg = ProgressDialog.show(Home.this, "", getResources().getString(R.string.get_details), true,true, new OnCancelListener() {
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
	    		AshTagApp.makeToast(getResources().getString(R.string.get_details_fail));
	    	}
	    	
	    	_dlg.dismiss();
	    	
	    	if(_displayDetails){
	    		Intent intent = new Intent(Home.this, About.class);
	    		startActivity(intent);
	    	}
	    }
	}
}