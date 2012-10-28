package org.iasess.ashtag.activities;

import org.iasess.ashtag.AshTagApp;
import org.iasess.ashtag.R;
import org.iasess.ashtag.SubmitParcel;
import org.iasess.ashtag.TaxonParcel;
import org.iasess.ashtag.api.ApiHandler;
import org.iasess.ashtag.data.ImageStore;
import org.iasess.ashtag.data.TaxaStore;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;

import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Controls the 'TaxaListing' Activity view
 */
public class TaxaListing extends InvadrActivityBase {

	private TaxaStore taxaStore = new TaxaStore(TaxaListing.this);
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
		new PopulateList().execute(""); // <- TODO: ugly!
		
		ListView lv = (ListView) findViewById(R.id.listTaxa);
		
		//check to see if we are display in a gallery view
		Bundle extras = getIntent().getExtras();
		if(extras != null && extras.containsKey("gallery") && extras.getBoolean("gallery")){
			lv.setOnItemClickListener(new GalleryViewListener());
		} else {
			lv.setOnItemClickListener(new SightingSubmissionListener());	
		}
	}

	/**
	 * Clean up the resources of this Activity when destroyed
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		taxaStore.close();
		imgStore.close();
	}
		
	/**
	 * Class to listen to ListView item selection events when
	 * the user is in the process of submitting a sighting
	 */
	private class SightingSubmissionListener implements  AdapterView.OnItemClickListener{

		/**
		 * Handler to capture and process the selection of a ListView item
		 * 
		 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
		 */
		public void onItemClick(AdapterView<?> adapter, View view, int position, long rowId) {				
			Intent intent = new Intent(AshTagApp.getContext(), Summary.class);
			
			// set the selected image
			Intent orig = getIntent();
			SubmitParcel submitPackage = orig.getParcelableExtra(SubmitParcel.SUBMIT_PARCEL_EXTRA);
			
			// set the selected taxa
			Cursor cursor = (Cursor) adapter.getItemAtPosition(position);
			String name = cursor.getString(cursor.getColumnIndex(TaxaStore.COL_COMMON_NAME));
			
			submitPackage.setTaxon(rowId, name);
			intent.putExtra(SubmitParcel.SUBMIT_PARCEL_EXTRA, submitPackage);
			startActivityForResult(intent, 0);				
		}		
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
			TaxonParcel parcel = new TaxonParcel(rowId, null);
			intent.putExtra(TaxonParcel.TAXON_PARCEL_EXTRA, parcel);
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
			_dlg = ProgressDialog.show(TaxaListing.this, "", "Fetching taxa...", true,true, new OnCancelListener() {
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
				taxaStore.update(ApiHandler.getTaxa());
				return taxaStore.getAll();
			} else {
				Cursor taxaCursor = taxaStore.getAll();
				if (!taxaCursor.moveToFirst()) { // is an empty set
					taxaCursor.close();
					
					// get from the api as not initialised
					taxaStore.update(ApiHandler.getTaxa());

					// re-fetch cursor data
					taxaCursor = taxaStore.getAll();
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
			String[] columns = new String[] { TaxaStore.COL_COMMON_NAME, TaxaStore.COL_SCIENTIFIC_NAME, TaxaStore.COL_PK };
			int[] to = new int[] { R.id.textPrimary, R.id.textSecondary, R.id.icon };

			SimpleCursorAdapter adapter = new SimpleCursorAdapter(TaxaListing.this, R.layout.image_list_item, result, columns, to);
			adapter.setViewBinder(new ViewBinder() {
				public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
					if (view.getId() == R.id.icon) {
						
						// Use UIL to handle caching/image binding
						ImageView imageSpot = (ImageView) view;							
						String uri = imgStore.getListingImage(cursor.getInt(columnIndex));
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