package org.iasess.ashtag.activities;

import org.iasess.ashtag.AshTagApp;
import org.iasess.ashtag.ExtendedImageDownloader;
import org.iasess.ashtag.R;
import org.iasess.ashtag.api.ApiHandler;
import org.iasess.ashtag.data.GuideStore;
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
	/**
	 * Class to handle the processing of the list content in a separate thread
	 */
	private class PopulateGrid extends AsyncTask<String, Void, Cursor> {

		/**
		 * The progress dialog to display to the user during processing
		 */
		private ProgressDialog _dlg;

		/**
		 * Performs a query against the cached data store. Fetches from the API
		 * if the store is empty or 'refresh' has been passed in params
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected Cursor doInBackground(String... params) {
			return guideStore.getAll();
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
			SimpleCursorAdapter adapter = new SimpleCursorAdapter(Home.this, // Context.
				R.layout.image_grid_item, result, // Pass in the cursor to bind
													// to.
				new String[] { GuideStore.COL_SMALL_IMAGE }, // Array of cursor columns
													// to bind to.
				new int[] { R.id.image }); // Parallel array of which template
											// objects to bind to those columns.

			ViewBinder viewBinder = new ViewBinder(){
				@Override
				public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
					if (view.getId() == R.id.image) {

						// Use UIL to handle caching/image binding
						ImageView imageSpot = (ImageView)view;
						String uri = ExtendedImageDownloader.PROTOCOL_ASSETS_PREFIX + cursor.getString(columnIndex);
						ImageLoader.getInstance().displayImage(uri, imageSpot);

						// return true to say we handled to binding
						return true;
					}
					return false;
				}
			};

			// Bind to our new adapter.
			adapter.setViewBinder(viewBinder);
			GridView gridView = (GridView)findViewById(R.id.gridview);
			gridView.setAdapter(adapter);
			gridView.setOnItemClickListener(new OnItemClickListener(){
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Intent intent = new Intent(Home.this, DetailsPager.class);
					intent.putExtra("position", position);
					startActivity(intent);
				}
			});

			_dlg.dismiss();
		}

		/**
		 * Displays the progress dialog to the user before processing the taxa
		 * items
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			// display the dialog to the user
			_dlg = ProgressDialog.show(Home.this, "", getResources().getString(R.string.get_details), true, true,
				new OnCancelListener(){
					@Override
					public void onCancel(DialogInterface dialog) {
						PopulateGrid.this.cancel(true);
						finish();
					}
				});
		}
	}

	private GuideStore guideStore = new GuideStore(Home.this);

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		new ActivityResultHandler(this).onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * Initialises the content of the Activity
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		new PopulateGrid().execute(""); // <- TODO: ugly!

		setUsernameText();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.home, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		guideStore.close();
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			case R.id.btnAbout:
				Intent intent = new Intent(this, About.class);
				startActivity(intent);
				return true;
			case R.id.btnAddSighting:
				new ClickHandler(this).onAddSightingClick(item);
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public void onUsernameClick(View v) {
		new ClickHandler(this).onUsernameClick(new OnDismissListener(){
			@Override
			public void onDismiss(DialogInterface dialog) {
				if (EmailHandler.isValid()) setUsernameText();
			}
		});

	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		setUsernameText();
	}

	private void setUsernameText() {
		((TextView)findViewById(R.id.editUsername)).setText(AshTagApp.getUsernamePreferenceString());
	}
}