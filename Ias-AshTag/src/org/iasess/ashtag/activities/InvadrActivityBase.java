package org.iasess.ashtag.activities;

import org.iasess.ashtag.AshTagApp;
import org.iasess.ashtag.R;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockActivity;

public class InvadrActivityBase extends SherlockActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
	    super.onDestroy();
	 
	    AshTagApp.unbindDrawables(findViewById(R.id.rootView));
	    System.gc();
	}
	
    
}
