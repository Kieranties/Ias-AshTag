package org.iasess.ashtag.activities;

import java.util.ArrayList;
import java.util.List;

import org.iasess.ashtag.AshTagApp;
import org.iasess.ashtag.ImageHandler;
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
import android.location.LocationListener;
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
import com.google.android.maps.OverlayItem;

/**
 * Controls the 'Summary' Activity view
 */
public class SetLocation extends SherlockMapActivity {

	private static final int GPS_INTENT = 948484;

	private SubmitParcel _submitParcel;

	private MapController _mapController;
	private LocationManager _locationManager;
	private SightingOverlay _sightingOverlay;
	private MapView _mapView;
	private Location _gpsLocation;

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
		_mapView.setBuiltInZoomControls(true);
		
		_mapController = _mapView.getController();
		
		_locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

		Drawable marker = getResources().getDrawable(R.drawable.marker);
		marker.setBounds(0, 0, marker.getIntrinsicWidth(),marker.getIntrinsicHeight());
		
		_sightingOverlay = new SightingOverlay(marker);
		_mapView.getOverlays().add(_sightingOverlay);

		setLastBestLocation();
		registerForGPSUpdates();
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
			GeoPoint point = _sightingOverlay.getMarkerLocation();
			if(point != null) new SubmitSightingTask().execute(point);
			else AshTagApp.makeToast("Please set a location");
			return true;
		case R.id.btnImageLocation:
			float[] locationData = ImageHandler.getImageLocation(_submitParcel.getImagePath());
			if(locationData != null){
				_sightingOverlay.setMarker(locationData[0], locationData[1]);
			} else {
				AshTagApp.makeToast("Could not read location from image");
			}
			return true;
		case R.id.btnMyLocation:
			if(_locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
				registerForGPSUpdates();
			} else {
				AshTagApp.makeToast(this, getResources().getString(R.string.enable_gps));
				Intent gpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivityForResult(gpsIntent, GPS_INTENT);
			}
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == GPS_INTENT) {
			if(_locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
				_gpsLocation = _locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			} else {
				AshTagApp.makeToast("GPS not enabled");
			}
			setLastBestLocation();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		AshTagApp.unbindDrawables(findViewById(R.id.rootView));
		System.gc();
	}
	
	
	private void registerForGPSUpdates(){		
		LocationListener locationListener = new LocationListener() {
		    public void onLocationChanged(Location location) {
		    	if(_gpsLocation == null) _gpsLocation = location;
		    	else{
		    		if((location.getAccuracy() - _gpsLocation.getAccuracy()) < 0){
		    			_gpsLocation = location;
		    		}
		    	}
		    }

		    public void onStatusChanged(String provider, int status, Bundle extras) {}

		    public void onProviderEnabled(String provider) {}

		    public void onProviderDisabled(String provider) {}
		  };
				  
		// Register the listener with the Location Manager to receive location updates
		_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
			
				
	}
	
	private void setLastBestLocation(){
		if(_gpsLocation == null){
			Criteria criteria = new Criteria();
			criteria.setAccuracy(Criteria.ACCURACY_FINE);
			String provider = _locationManager.getBestProvider(criteria, true);
			
			_gpsLocation = _locationManager.getLastKnownLocation(provider);
		}
		_sightingOverlay.setMarker(_gpsLocation.getLatitude(), _gpsLocation.getLongitude());
	}
	
	/**
	 * Class to handle the submission of details in a separate thread
	 * 
	 */
	private class SubmitSightingTask extends
			AsyncTask<GeoPoint, Void, SubmissionResponse> {

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

		@Override
		protected SubmissionResponse doInBackground(GeoPoint... params) {
			double latitude = params[0].getLatitudeE6() / 1E6;
			double longitude = params[0].getLongitudeE6() / 1E6;
			_submitParcel.setLocation(latitude, longitude);			
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
			this.marker = marker;
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

		public void setMarker(double lat, double lon) {		
			GeoPoint point = new GeoPoint((int) (lat * 1E6), (int) (lon * 1E6));
			OverlayItem item = new OverlayItem(point, null, null);
			items.clear();
			items.add(item);
			populate();
			_mapController.animateTo(point);
			_mapController.setZoom(18);
			AshTagApp.makeToast("Drag to refine position");

		}
		
		public GeoPoint getMarkerLocation() {
			if(items.size() < 1) return null;
			
			return items.get(0).getPoint();

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