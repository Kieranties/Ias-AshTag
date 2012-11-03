package org.iasess.ashtag.handlers;

import org.iasess.ashtag.AshTagApp;
import org.iasess.ashtag.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import com.actionbarsherlock.view.MenuItem;

public class ClickHandler {

	public static final int CAMERA_OPTION = 1000;
	public static final int GALLERY_OPTION = 1001;
	
	private Activity _activity;
	
	public ClickHandler(Activity activity){
		_activity = activity;
	}
	public void onAddSightingClick(MenuItem mi) {
    	if(!(android.util.Patterns.EMAIL_ADDRESS.matcher(AshTagApp.getUsernamePreferenceString()).matches())){
    		AlertDialog.Builder builder = new AlertDialog.Builder(_activity);
    		// set items from resource list
    		String[] options = _activity.getResources().getStringArray(R.array.camera_options);
    		builder.setItems(options, new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int item) {
    				if (item == 0) cameraIntent();
    				else galleryIntent();
    			}
    		});
    		builder.create().show();
    	} else {
    		//TODO: ask for email address
    	}
    } 
	
	private void cameraIntent() {
		Uri lastCreatedImageUri;
		String fileName = "ias-" + System.currentTimeMillis() + ".jpg";
		ContentValues values = new ContentValues();
		values.put(MediaStore.Images.Media.TITLE, fileName);
		values.put(MediaStore.Images.Media.DESCRIPTION, "Taken for invadr");
		lastCreatedImageUri = _activity.getContentResolver().insert(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

		// create intent with extra output to grab uri later
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, lastCreatedImageUri);

		_activity.startActivityForResult(
				Intent.createChooser(intent, "Select Picture"), CAMERA_OPTION);
	}

	private void galleryIntent() {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);

		_activity.startActivityForResult(intent, GALLERY_OPTION);
	}
}
