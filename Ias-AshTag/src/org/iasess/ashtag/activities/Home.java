package org.iasess.ashtag.activities;

import org.iasess.ashtag.AshTagApp;
import org.iasess.ashtag.R;
import org.iasess.ashtag.api.ApiHandler;
import org.iasess.ashtag.api.CampaignModel;
import org.iasess.ashtag.data.ImageStore;
import org.iasess.ashtag.data.TaxonStore;
import org.iasess.ashtag.handlers.ActivityResultHandler;
import org.iasess.ashtag.handlers.ClickHandler;
import org.iasess.ashtag.handlers.EmailHandler;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Controls the 'Home' Activity view
 */
public class Home extends InvadrActivityBase {
	
	private TaxonStore taxonStore = new TaxonStore(Home.this);
	private ImageStore imgStore = new ImageStore(Home.this);
	
	/**
     * Initialises the content of the Activity
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);              
        getSupportActionBar().setDisplayShowTitleEnabled(false);   
        
        new PopulateGrid().execute(""); // <- TODO: ugly!
        
        setUsernameText(); 
        
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
         
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
    	super.onWindowFocusChanged(hasFocus);
    	setUsernameText();
    }
    
    @Override
	protected void onDestroy() {
		super.onDestroy();
		
		taxonStore.close();
		imgStore.close();
	}
        
    public void onUsernameClick(View v){
    	new ClickHandler(this).onUsernameClick(new OnDismissListener() {			
			@Override
			public void onDismiss(DialogInterface dialog) {
				if(EmailHandler.isValid()) setUsernameText();				
			}
		});
    	
    }

    private void setUsernameText(){
    	((TextView)findViewById(R.id.editUsername)).setText(AshTagApp.getUsernamePreferenceString());
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
		@Override
		protected void onPreExecute() {
			//display the dialog to the user
			_dlg = ProgressDialog.show(Home.this, "", getResources().getString(R.string.get_details), true,true, new OnCancelListener() {
				@Override
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
		@Override
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
	    @Override
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
	
	/**
	 * Class to handle the processing of the list content in a separate thread
	 */
	private class PopulateGrid extends AsyncTask<String, Void, Cursor> {
		
		/**
		 * The progress dialog to display  to the user during processing
		 */
		private ProgressDialog _dlg;
		
		/**
		 * Displays the progress dialog to the user before processing the taxa items
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			// display the dialog to the user
			_dlg = ProgressDialog.show(Home.this, "", getResources().getString(R.string.get_details), true,true, new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					PopulateGrid.this.cancel(true);	
					finish();
				}
			});
		}
		
		/**
		 * Performs a query against the cached data store.
		 * Fetches from the API if the store is empty or 'refresh' has been passed in params
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected Cursor doInBackground(String... params) {
			if (params.length > 0 && params[0].equals("refresh")) {
				taxonStore.update(ApiHandler.getTaxa());
				return taxonStore.getAll();
			} else {
				Cursor taxaCursor = taxonStore.getAll();
				if (!taxaCursor.moveToFirst()) { // is an empty set
					taxaCursor.close();
					
					// get from the api as not initialised
					taxonStore.update(ApiHandler.getTaxa());

					// re-fetch cursor data
					taxaCursor = taxonStore.getAll();
				}

				return taxaCursor;
			}
		}

		/**
		 * Processes the results of the AsyncTask
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		@SuppressWarnings("deprecation")
		protected void onPostExecute(Cursor result) {
			startManagingCursor(result);
			SimpleCursorAdapter adapter = new SimpleCursorAdapter(
	                 Home.this, // Context.
	                 R.layout.image_grid_item,
	                 result,                                              // Pass in the cursor to bind to.
	                 new String[] { TaxonStore.COL_PK},           // Array of cursor columns to bind to.
	                 new int[] { R.id.image });  // Parallel array of which template objects to bind to those columns.

	         ViewBinder viewBinder = new ViewBinder() {
					@Override
					public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
						if (view.getId() == R.id.image) {
							
							// Use UIL to handle caching/image binding
							ImageView imageSpot = (ImageView) view;							
							String uri = imgStore.getImage(cursor.getInt(columnIndex), "200");
							ImageLoader.getInstance().displayImage(uri, imageSpot);		
							
							// return true to say we handled to binding
							return true;
						}
						return false;
					}
				};
	         
	         // Bind to our new adapter.
			adapter.setViewBinder(viewBinder);
			GridView gridView = (GridView) findViewById(R.id.gridview);
			gridView.setAdapter(adapter);
			gridView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Intent intent = new Intent(Home.this, DetailsPager.class);
			    	intent.putExtra("position", position);
			    	startActivity(intent);
				}
			});
			
			_dlg.dismiss();
		}
	}
}