package org.iasess.android.activities;

import org.iasess.android.IasessApp;
import org.iasess.android.R;
import org.iasess.android.api.ApiHandler;
import org.iasess.android.api.UserCheckResponse;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

/**
 * Controls the 'Settings' Activity view
 */
public class Settings extends InvadrActivityBase implements OnEditorActionListener {

	/**
	 * The {@link EditText} for the username
	 */
	private EditText _editText;

	/**
	 * Initialises the content of the Activity
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.settings);
		
		//find the edit box
		_editText = (EditText) findViewById(R.id.editUsername);

		// set from application preferences
		_editText.setText(IasessApp.getPreferenceString(IasessApp.PREFS_USERNAME));

		// attach listener for focus lost events
		_editText.setOnEditorActionListener(this);
	}

	/**
	 * Stores the given username and ends this Activity
	 * 
	 * @param v The {@link View} which fired this event handler
	 */
	public void onDoneClick(View v) {
		IasessApp.setPreferenceString(IasessApp.PREFS_USERNAME, _editText.getText().toString());
		finish();
	}

	/**
	 * Handler for key events on the username {@link EditText}
	 * 
	 * @see android.widget.TextView.OnEditorActionListener#onEditorAction(android.widget.TextView, int, android.view.KeyEvent)
	 */
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		
		switch(actionId){
			case EditorInfo.IME_ACTION_DONE:
			case EditorInfo.IME_ACTION_GO:
			case EditorInfo.IME_ACTION_NEXT:
				new UsernameCheckTask().execute(_editText.getText().toString());
				return false;
		}
		
		if(event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
			new UsernameCheckTask().execute(_editText.getText().toString());
		}
		
		return false;
	}
	
	
	/**
	 * Class to process the checking of a username in a separate thread
	 */
	private class UsernameCheckTask extends AsyncTask<String, Void, UserCheckResponse> {	
		
		/**
		 * The progress dialog to display while processing
		 */
		private ProgressDialog _dlg;
		
		
		/**
		 * Displays the progress dialog before executing the async task
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		protected void onPreExecute() {
			//display the dialog to the user
			_dlg = ProgressDialog.show(Settings.this, "", "Checking...", true,true, new OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					UsernameCheckTask.this.cancel(true);	
					finish();
				}
			});
	    }
        
		/**
		 * Executes a request to the API to check the username
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		protected UserCheckResponse doInBackground(String... textValue) {
	    	//return the response from the api
	        return ApiHandler.checkUser(textValue[0]);
	    }
	    
	    /**
	     * Dismisses the dialog and updates the UI with the results
	     * of the API request
	     * 
	     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	     */
	    protected void onPostExecute(UserCheckResponse result) {
	    	_dlg.dismiss();
	    	
	    	if(result != null){
	    		//inform the user of the response
		    	IasessApp.makeToast(result.getAnswer());
		    	
		    	//update ui if required
				String username = result.getUsername();
				if(username != null && username != ""){
					_editText.setText(username);
				}
				_editText.clearFocus();		
	    	} else {
	    		IasessApp.makeToast("Could not contact website.  Please try again later.");
	    	}	    		
	    }
	}
}