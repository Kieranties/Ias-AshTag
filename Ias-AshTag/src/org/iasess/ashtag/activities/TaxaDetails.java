package org.iasess.ashtag.activities;

import org.iasess.ashtag.AshTagApp;
import org.iasess.ashtag.R;
import org.iasess.ashtag.data.ImageStore;
import org.iasess.ashtag.data.TaxonStore;
import org.iasess.ashtag.handlers.ActivityResultHandler;
import org.iasess.ashtag.handlers.ClickHandler;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

/**
 * Controls the 'TaxaDetails' Activity view
 */
public class TaxaDetails extends InvadrActivityBase {
	
	private ImageStore imgStore = new ImageStore(this);
	private String image = null;
	
	/**
	 * Initialises the content of this Activity
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.taxa_details);	
		getSupportActionBar().setTitle(R.string.id_guide);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		long taxonId = getIntent().getExtras().getLong("taxonId");
		
		TaxonStore store = new TaxonStore(this);
		Cursor cursor = store.getByPk(taxonId);
		
		cursor.moveToFirst();
		String source = cursor.getString(cursor.getColumnIndex(TaxonStore.COL_SOURCE));	
		String detail = cursor.getString(cursor.getColumnIndex(TaxonStore.COL_DETAIL));
		String title = cursor.getString(cursor.getColumnIndex(TaxonStore.COL_TITLE));
		cursor.close();
		store.close();
		
		TextView tvSource = (TextView)findViewById(R.id.textSource);
		String sourceStub = getResources().getString(R.string.source);
		tvSource.setText(sourceStub + " " + source);
		
		TextView tvDetail = (TextView)findViewById(R.id.textDetail);
		tvDetail.setText(detail);
		
		TextView tvTitle = (TextView)findViewById(R.id.textTitle);
		tvTitle.setText(title);
				
	
		new PopulateImages().execute(taxonId);
	}
	

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
       MenuInflater inflater = getSupportMenuInflater();
       inflater.inflate(R.menu.image_only, menu);
       return super.onCreateOptionsMenu(menu);
    }   
    
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		imgStore.close();
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
		    case R.id.btnAddSighting:
		    	new ClickHandler(this).onAddSightingClick(item);
		    	return true;
	    }
	
		return super.onOptionsItemSelected(item);
	}
	
	private ImageView getImageView(){
		return (ImageView) findViewById(R.id.imageView);
	}
	/**
	 * Class to handle the population of image details in a separate thread
	 */
	private class PopulateImages extends AsyncTask<Long, Void, String> {
		
		/**
		 * The progress dialog to display while processing
		 */
		private ProgressDialog _dlg;

		/**
		 * Display the progress dialog to the user before processing the AsyncTask
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		protected void onPreExecute() {
			// display the dialog to the user
			_dlg = ProgressDialog.show(TaxaDetails.this, "", "Fetching images...", true,true, new OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					PopulateImages.this.cancel(true);	
					finish();
				}
			});
		}
		
		/**
		 * Fetches an image from the API
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		protected String doInBackground(Long... params) {
			return imgStore.getImage(params[0], "1200");
		}

		/**
		 * Process the results of the AsyncTask
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		protected void onPostExecute(String result) {		
			if(result == null){
				AshTagApp.makeToast("No images found...");
				_dlg.dismiss();
			} else {
				image = result;
				final ImageView iv = getImageView();
				ImageLoader.getInstance().displayImage(image, iv, new ImageLoadingListener() {
				
					
					public void onLoadingFailed(FailReason failReason) {
						AshTagApp.makeToast("No images found...");
						_dlg.dismiss();
					}

					
					public void onLoadingComplete(Bitmap loadedImage) {
						iv.setVisibility(View.VISIBLE);
						_dlg.dismiss();
					}

					
					public void onLoadingCancelled() {
						// Do nothing
					}


					public void onLoadingStarted() {
						// TODO Auto-generated method stub
						
					}
				});				
			}			
		}
	}
}
