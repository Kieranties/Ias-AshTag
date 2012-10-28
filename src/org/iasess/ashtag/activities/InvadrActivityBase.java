package org.iasess.ashtag.activities;

import org.iasess.ashtag.AshTagApp;
import org.iasess.ashtag.R;

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
	}
	
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	 
	    AshTagApp.unbindDrawables(findViewById(R.id.RootView));
	    System.gc();
	}
}
