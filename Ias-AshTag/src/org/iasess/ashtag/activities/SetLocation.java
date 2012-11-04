package org.iasess.ashtag.activities;

import java.util.ArrayList;
import java.util.List;

import org.iasess.ashtag.AshTagApp;
import org.iasess.ashtag.R;
import org.iasess.ashtag.SubmitParcel;
import org.iasess.ashtag.api.ApiHandler;
import org.iasess.ashtag.api.SubmissionResponse;
import org.iasess.ashtag.handlers.ActivityResultHandler;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.actionbarsherlock.app.SherlockMapActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;

/**
 * Controls the 'Summary' Activity view
 */
public class SetLocation extends SherlockMapActivity {

	private static final int GPS_INTENT = 948484;

	private SubmitParcel _submitParcel;

	private MapController _mapController;
	private MyLocationOverlay _locationOverlay;
	private LocationManager _locationManager;
	private MapView _mapView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.set_location);
		getSupportActionBar().setTitle(R.string.location);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		_submitParcel = getIntent().getParcelableExtra(
				SubmitParcel.SUBMIT_PARCEL_EXTRA);

		_mapView = (MapView) findViewById(R.id.mapView);
		_mapView.setSatellite(true);
		_mapView.displayZoomControls(true);
		_mapController = _mapView.getController();
		
		_locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

		Drawable marker = getResources().getDrawable(R.drawable.marker);

		marker.setBounds(0, 0, marker.getIntrinsicWidth(),
				marker.getIntrinsicHeight());
		SightingOverlay sightingOverlay = new SightingOverlay(marker);
		_mapView.getOverlays().add(sightingOverlay);

		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		String provider = _locationManager.getBestProvider(criteria, true);

		Location loc = _locationManager.getLastKnownLocation(provider);
		sightingOverlay.setMarker(loc);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.set_location, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			return true;
		case R.id.btnSend:
			// new SubmitSightingTask().execute("");
			return true;
		case R.id.btnImageLocation:
			return true;
		case R.id.btnMyLocation:
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Required override when using MapView
	 * 
	 * @see com.google.android.maps.MapActivity#isRouteDisplayed()
	 */
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	/**
	 * Pauses the mapping functionality when the view loses focus
	 * 
	 * @see com.google.android.maps.MapActivity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
	}

	/**
	 * Handlesthe response of an ActivityResult fired in the context of this
	 * Activity
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 *      android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK && requestCode == GPS_INTENT) {
			// renderMapView();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		AshTagApp.unbindDrawables(findViewById(R.id.rootView));
		System.gc();
	}

	/**
	 * Class to handle the submission of details in a separate thread
	 * 
	 */
	private class SubmitSightingTask extends
			AsyncTask<String, Void, SubmissionResponse> {

		/**
		 * The progress dialog to display while processing
		 */
		private ProgressDialog _dlg;

		/**
		 * Displays a progress dialog prior to the start of any processing
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			// display the dialog to the user
			_dlg = ProgressDialog.show(SetLocation.this, "", getResources()
					.getString(R.string.submitting), true, true,
					new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							SubmitSightingTask.this.cancel(true);
							finish();
						}
					});
		}

		/**
		 * Submits the details of a sighting through the API
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected SubmissionResponse doInBackground(String... params) {
			// don't need params
			Location fix = _locationOverlay.getLastFix();
			_submitParcel.setLocation(fix.getLatitude(), fix.getLongitude());
			return ApiHandler.submitSighting(_submitParcel);
		}

		/**
		 * Processes the result of the submission
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(SubmissionResponse result) {
			_dlg.dismiss();
			if (result.getId() != Integer.MIN_VALUE) {
				AshTagApp.makeToast(getResources()
						.getString(R.string.submitted));

				if (_submitParcel.getIsExternal()) {
					setResult(ActivityResultHandler.CLOSE_ALL);
					finish();
				} else {
					// we're done for this submission so return the app to the
					// start
					Intent home = new Intent(SetLocation.this, Home.class);
					home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // resets the
																	// activity
																	// stack
					startActivity(home);
				}
			} else {
				AshTagApp.makeToast(getResources().getString(
						R.string.submitting_fail));
			}
		}
	}

	private class SightingOverlay extends ItemizedOverlay<OverlayItem> {
		private List<OverlayItem> items = new ArrayList<OverlayItem>();
		private Drawable marker = null;
		private OverlayItem inDrag = null;
		private ImageView dragImage = null;
		private int xDragImageOffset = 0;
		private int yDragImageOffset = 0;
		private int xDragTouchOffset = 0;
		private int yDragTouchOffset = 0;

		public SightingOverlay(Drawable marker) {
			super(boundCenterBottom(marker));
			this.marker = boundCenterBottom(marker);

			dragImage = (ImageView) findViewById(R.id.drag);
			xDragImageOffset = dragImage.getDrawable().getIntrinsicWidth() / 2;
			yDragImageOffset = dragImage.getDrawable().getIntrinsicHeight();

			populate();
		}

		@Override
		protected OverlayItem createItem(int i) {
			return (items.get(i));
		}

		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
			super.draw(canvas, mapView, shadow);
		}

		@Override
		public int size() {
			return (items.size());
		}

		@Override
		public boolean onTouchEvent(MotionEvent event, MapView mapView) {
			final int action = event.getAction();
			final int x = (int) event.getX();
			final int y = (int) event.getY();
			boolean result = false;

			if (action == MotionEvent.ACTION_DOWN) {
				for (OverlayItem item : items) {
					Point p = new Point(0, 0);

					_mapView.getProjection().toPixels(item.getPoint(), p);

					if (hitTest(item, marker, x - p.x, y - p.y)) {
						result = true;
						inDrag = item;
						items.remove(inDrag);
						populate();

						xDragTouchOffset = 0;
						yDragTouchOffset = 0;

						setDragImagePosition(p.x, p.y);
						dragImage.setVisibility(View.VISIBLE);

						xDragTouchOffset = x - p.x;
						yDragTouchOffset = y - p.y;

						break;
					}
				}
			} else if (action == MotionEvent.ACTION_MOVE && inDrag != null) {
				setDragImagePosition(x, y);
				result = true;
			} else if (action == MotionEvent.ACTION_UP && inDrag != null) {
				dragImage.setVisibility(View.GONE);

				GeoPoint pt = _mapView.getProjection().fromPixels(
						x - xDragTouchOffset, y - yDragTouchOffset);
				OverlayItem toDrop = new OverlayItem(pt, inDrag.getTitle(),
						inDrag.getSnippet());

				items.add(toDrop);
				populate();

				inDrag = null;
				result = true;
			}

			return (result || super.onTouchEvent(event, mapView));
		}

		public void setMarker(Location loc) {
			GeoPoint point = new GeoPoint((int) (loc.getLatitude() * 1e6),
					(int) (loc.getLongitude() * 1e6));
			OverlayItem item = new OverlayItem(point, null, null);
			items.clear();
			items.add(item);
			populate();
			_mapController.animateTo(point);
			AshTagApp.makeToast("Drag to refine position");

		}

		private void setDragImagePosition(int x, int y) {
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) dragImage
					.getLayoutParams();

			lp.setMargins(x - xDragImageOffset - xDragTouchOffset, y
					- yDragImageOffset - yDragTouchOffset, 0, 0);
			dragImage.setLayoutParams(lp);
		}
	}
}