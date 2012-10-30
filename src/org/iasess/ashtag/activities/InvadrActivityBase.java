package org.iasess.ashtag.activities;

import org.iasess.ashtag.AshTagApp;
import org.iasess.ashtag.ImageHandler;
import org.iasess.ashtag.Logger;
import org.iasess.ashtag.R;
import org.iasess.ashtag.SubmitParcel;

import android.app.Activity;
import android.content.Intent;

public class InvadrActivityBase extends Activity {
	
	protected static int CLOSE_ALL = 909090;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		//check for close all
		if(resultCode == CLOSE_ALL || requestCode == CLOSE_ALL){
			setResult(CLOSE_ALL); //set for activities above stack
			finish(); //close this activity
		}
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == Activity.RESULT_OK){
			//we're expecting the intent to be an image intent initiated by this app
			String selected = ImageHandler.getImagePathFromIntentResult(resultCode, requestCode, data);
			if(selected != null){
				//pass data to next activity
				Intent intent = new Intent(this, AddPhoto.class);
				intent.putExtra(SubmitParcel.SUBMIT_PARCEL_EXTRA, new SubmitParcel(selected));
				startActivity(intent);
			} 
			else{
				Logger.warn("Could not get a selected image");
			}
		}
	}

	@Override
	protected void onDestroy() {
	    super.onDestroy();
	 
	    AshTagApp.unbindDrawables(findViewById(R.id.RootView));
	    System.gc();
	}
}
