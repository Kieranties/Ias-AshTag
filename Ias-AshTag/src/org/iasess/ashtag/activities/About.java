package org.iasess.ashtag.activities;

import org.iasess.ashtag.R;
import org.iasess.ashtag.api.CampaignModel;
import org.iasess.ashtag.handlers.ActivityResultHandler;
import org.iasess.ashtag.handlers.ClickHandler;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

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
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		//Load the HTML content from resources
		TextView tv = (TextView)findViewById(R.id.textAboutBlurb);
		CampaignModel model = CampaignModel.getInstance();
		tv.setText(model.getAbout());
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
       MenuInflater inflater = getSupportMenuInflater();
       inflater.inflate(R.menu.about, menu);
       return super.onCreateOptionsMenu(menu);
    }
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
	    switch (item.getItemId()) {
		    case android.R.id.home:
		    	this.finish();
	            return true;
		    case R.id.btnMail:
		    	Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		        emailIntent.setType("plain/text");
		        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{getResources().getString(R.string.contact_email)});
		        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.contact_email_subject));
		        startActivity(Intent.createChooser(emailIntent, getResources().getString(R.string.contact)));
		    	return true;
		    case R.id.btnAddSighting:
		    	new ClickHandler(this).onAddSightingClick(item);
		    	return true;
	    }
	
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		new ActivityResultHandler(this).onActivityResult(requestCode, resultCode, data);
	}
}