package org.iasess.ashtag.activities;

import org.iasess.ashtag.AshTagApp;
import org.iasess.ashtag.ImageHandler;
import org.iasess.ashtag.R;
import org.iasess.ashtag.api.ApiHandler;
import org.iasess.ashtag.data.ImageStore;
import org.iasess.ashtag.data.TaxonStore;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;

import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Controls the 'TaxaListing' Activity view
 */
public class TaxaListing extends InvadrActivityBase {

	private TaxonStore taxonStore = new TaxonStore(TaxaListing.this);
	private ImageStore imgStore = new ImageStore(TaxaListing.this);
	
	/**
	 * Initialises the content of this Activity
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.taxa_listing);
		getSupportActionBar().setTitle(R.string.select_taxa);
		
		if(!(android.util.Patterns.EMAIL_ADDRESS.matcher(AshTagApp.getUsernamePreferenceString()).matches())){
    		Button btn = (Button) findViewById(R.id.buttonSubmit);
    		btn.setVisibility(View.GONE);
    	}
		
		new PopulateList().execute(""); // <- TODO: ugly!
		
		ListView lv = (ListView) findViewById(R.id.listTaxa);
		lv.setOnItemClickListener(new GalleryViewListener());
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
	 * Clean up the resources of this Activity when destroyed
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		taxonStore.close();
		imgStore.close();
	}
		
	
	/**
	 * Class to listen to ListView item selection events when
	 * the user is in the process of viewing the details of a Taxa
	 */
	private class GalleryViewListener implements  AdapterView.OnItemClickListener{

		/**
		 * Handler to capture and process the selection of a ListView item
		 * 
		 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
		 */
		public void onItemClick(AdapterView<?> adapter, View view, int position, long rowId) {				
			Intent intent = new Intent(AshTagApp.getContext(), TaxaDetails.class);		
			Cursor cursor = (Cursor) adapter.getItemAtPosition(position);
			long id = cursor.getLong(cursor.getColumnIndex(TaxonStore.COL_PK));
			
			intent.putExtra("taxonId", id);
			startActivity(intent);				
		}		
	}
	
	
	
	/**
	 * Class to handle the processing of the list content in a separate thread
	 */
	private class PopulateList extends AsyncTask<String, Void, Cursor> {
		
		/**
		 * The progress dialog to display  to the user during processing
		 */
		private ProgressDialog _dlg;
		
		/**
		 * Displays the progress dialog to the user before processing the taxa items
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		protected void onPreExecute() {
			// display the dialog to the user
			_dlg = ProgressDialog.show(TaxaListing.this, "", "Fetching details...", true,true, new OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					PopulateList.this.cancel(true);	
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
		protected void onPostExecute(Cursor result) {
			// Bind the adapter to the list view
			ListView listView = (ListView) findViewById(R.id.listTaxa);
			String[] columns = new String[] { TaxonStore.COL_TITLE, TaxonStore.COL_PK};
			int[] to = new int[] { R.id.textPrimary, R.id.icon };

			SimpleCursorAdapter adapter = new SimpleCursorAdapter(TaxaListing.this, R.layout.image_list_item, result, columns, to);
			adapter.setViewBinder(new ViewBinder() {
				public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
					if (view.getId() == R.id.icon) {
						
						// Use UIL to handle caching/image binding
						ImageView imageSpot = (ImageView) view;							
						String uri = imgStore.getImage(cursor.getInt(columnIndex), "100");
						ImageLoader.getInstance().displayImage(uri, imageSpot);		
						
						// return true to say we handled to binding
						return true;
					}
					return false;
				}
			});
			listView.setAdapter(adapter);
			
			_dlg.dismiss();
		}
	}
}