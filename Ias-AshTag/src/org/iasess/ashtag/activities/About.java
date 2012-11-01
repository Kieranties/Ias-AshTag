package org.iasess.ashtag.activities;

import org.iasess.ashtag.R;
import org.iasess.ashtag.api.CampaignModel;

import android.os.Bundle;
import android.widget.TextView;

/**
 * Controls the 'About' Activity view
 */
public class About extends InvadrActivityBase {
	
	/**
	 * Initialises the content of the Activity
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		getSupportActionBar().setTitle(R.string.about);
		
		//Load the HTML content from resources
		TextView tv = (TextView)findViewById(R.id.textAboutBlurb);
		CampaignModel model = CampaignModel.getInstance();
		tv.setText(model.getAbout());
	}
}