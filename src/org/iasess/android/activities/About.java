package org.iasess.android.activities;

import org.iasess.android.R;

import android.os.Bundle;
import android.text.Html;
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
		
		//Load the HTML content from resources
		TextView tv = (TextView)findViewById(R.id.textAboutBlurb);
		String content = getResources().getString(R.string.about_blurb);
		tv.setText(Html.fromHtml(content));
	}
}