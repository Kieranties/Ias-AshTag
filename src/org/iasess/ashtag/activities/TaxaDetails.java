package org.iasess.ashtag.activities;

import java.util.ArrayList;

import org.iasess.ashtag.AshTagApp;
import org.iasess.ashtag.R;
import org.iasess.ashtag.TaxonParcel;
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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Controls the 'TaxaDetails' Activity view
 */
public class TaxaDetails extends InvadrActivityBase {
	
	private ImageStore imgStore = new ImageStore(this);
	private ArrayList<String> images = new ArrayList<String>();
	
	/**
	 * Initialises the content of this Activity
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.taxa_details);
		
		TaxonParcel parcel = getIntent().getParcelableExtra(TaxonParcel.TAXON_PARCEL_EXTRA);
		long taxonId = parcel.getTaxonId();
		if(taxonId != -1){
			TaxaStore store = new TaxaStore(this);
			Cursor cursor = store.getByPk(taxonId);
			
			cursor.moveToFirst();
			String description = cursor.getString(cursor.getColumnIndex(TaxaStore.COL_KEY_TEXT));	
			String name = cursor.getString(cursor.getColumnIndex(TaxaStore.COL_COMMON_NAME));
			String scientific = cursor.getString(cursor.getColumnIndex(TaxaStore.COL_SCIENTIFIC_NAME));
			String rank = cursor.getString(cursor.getColumnIndex(TaxaStore.COL_RANK));
			cursor.close();
			store.close();
			
			TextView tvDesc = (TextView)findViewById(R.id.textDescription);
			tvDesc.setText(description);
			
			TextView tvTitle = (TextView)findViewById(R.id.textBanner);
			tvTitle.setText(name);
			
			TextView tvScientific = (TextView)findViewById(R.id.scientific_name);
			tvScientific.setText(scientific);
						
			TextView tvRank = (TextView)findViewById(R.id.rank);
			String rankStub = getResources().getString(R.string.rank);
			tvRank.setText(rankStub + " " + rank);					
		
			new PopulateImages().execute(taxonId);
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
		
		imgStore.close();
	}
	
	public void startImageGalleryActivity(int position) {
		Intent intent = new Intent(this, ImagePager.class);
	    intent.putExtra("images", images);
	    intent.putExtra("position", position);
	   	startActivity(intent);
	}
	 
	public void onSingleImageClick(View v){
		startImageGalleryActivity(0);
	}
	
	private Gallery getGallery(){
		return (Gallery) findViewById(R.id.gallery);
	}
		
	private ImageView getImageView(){
		return (ImageView) findViewById(R.id.imageView);
	}
	/**
	 * Class to handle the population of image details in a separate thread
	 */
	private class PopulateImages extends AsyncTask<Long, Void, ArrayList<String>> {
		
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
		protected ArrayList<String> doInBackground(Long... params) {
			return imgStore.getLargeImages(params[0]);
		}

		/**
		 * Process the results of the AsyncTask
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		protected void onPostExecute(ArrayList<String> results) {
			images = results;			
			if(results.isEmpty()){
				AshTagApp.makeToast("No images found...");
			} else if (images.size() == 1) {
				ImageView iv = getImageView();
				ImageLoader.getInstance().displayImage(images.get(0), iv);
				getGallery().setVisibility(View.INVISIBLE);
				iv.setVisibility(View.VISIBLE);
				
			} else {
				BuildGridView();		
			}
			_dlg.dismiss();
		}
			
		private void BuildGridView(){
			Gallery gallery = getGallery();
			
			gallery.setAdapter(new ImageAdapter());
			gallery.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					startImageGalleryActivity(position);
				}
			});							
			
			gallery.setVisibility(View.VISIBLE);
			getImageView().setVisibility(View.INVISIBLE);
			
		}
	}
	
	public class ImageAdapter extends BaseAdapter {
		DisplayImageOptions options;
		
		public ImageAdapter() {
			options = new DisplayImageOptions.Builder()
			.cacheInMemory()
			.cacheOnDisc()
			.build();
		}
		
		public int getCount() {
			return images.size();
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			final ImageView imageView;
			if (convertView == null) {
				imageView = (ImageView) getLayoutInflater().inflate(R.layout.grid_image, parent, false);
			} else {
				imageView = (ImageView) convertView;
			}

			ImageLoader.getInstance().displayImage(images.get(position), imageView, options);

			return imageView;
		}
	}
}
