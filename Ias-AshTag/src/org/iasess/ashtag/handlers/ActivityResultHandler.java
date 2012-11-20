package org.iasess.ashtag.handlers;

import org.iasess.ashtag.AshTagApp;
import org.iasess.ashtag.ImageHandler;
import org.iasess.ashtag.Logger;
import org.iasess.ashtag.SubmitParcel;
import org.iasess.ashtag.activities.AddPhoto;

import android.app.Activity;
import android.content.Intent;

public class ActivityResultHandler {

	public static final int CLOSE_ALL = 909090;

	private Activity _activity;

	public ActivityResultHandler(Activity activity) {
		_activity = activity;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		// check for close all
		if (resultCode == CLOSE_ALL || requestCode == CLOSE_ALL) {
			_activity.setResult(CLOSE_ALL); // set for activities above stack
			_activity.finish(); // close this activity
		}

		if (resultCode == Activity.RESULT_OK) {
			// we're expecting the intent to be an image intent initiated by
			// this app
			String selected = ImageHandler.getImagePathFromIntentResult(resultCode, requestCode, data);
			if (selected != null) {
				// pass data to next activity
				Intent intent = new Intent(_activity, AddPhoto.class);
				intent.putExtra(SubmitParcel.SUBMIT_PARCEL_EXTRA, new SubmitParcel(selected));

				_activity.startActivity(intent);
			} else {
				AshTagApp.makeToast("Could not read image");
			}
		}
	}
}
