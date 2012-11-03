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
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

/**
 * Controls the 'Home' Activity view
 */
public class Home extends InvadrActivityBase implements OnEditorActionListener {
    
	private EditText _username;
	private boolean _checkOnKeyboardHide = false;
	
	/**
     * Initialises the content of the Activity
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);              
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    	        
        //populate the username box
        _username = (EditText) findViewById(R.id.editUsername);
        _username.setText(AshTagApp.getUsernamePreferenceString());
        _username.setOnEditorActionListener(this);
		CheckUsername();
		
        //hack to calculate when the keyboard is displayed
        // http://stackoverflow.com/questions/2150078/how-to-check-visibility-of-software-keyboard-in-android
		final View activityRootView = findViewById(R.id.rootView);
		activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			
			public void onGlobalLayout() {
				if(_checkOnKeyboardHide){
					CheckUsername();					
				} else {
				    Rect r = new Rect();
				    activityRootView.getWindowVisibleDisplayFrame(r);		
				    int heightDiff = activityRootView.getRootView().getHeight() - (r.bottom - r.top);
				    _checkOnKeyboardHide = heightDiff > 100;
				}
			 }
		});
		
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
    
    private void CheckUsername(){
    	String text = _username.getText().toString().trim();
    	AshTagApp.setUsernamePreferenceString(text);
    	if(!android.util.Patterns.EMAIL_ADDRESS.matcher(text).matches()){    				
    		AshTagApp.makeToast("Invalid email address");
    	}
    	_checkOnKeyboardHide = false;
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