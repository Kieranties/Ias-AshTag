package org.iasess.ashtag.activities;

import org.iasess.ashtag.R;
import org.iasess.ashtag.data.ImageStore;
import org.iasess.ashtag.data.TaxonStore;
import org.iasess.ashtag.handlers.ActivityResultHandler;
import org.iasess.ashtag.handlers.ClickHandler;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

/**
 * Controls the 'TaxaDetails' Activity view
 */
public class DetailsPager extends InvadrActivityBase {

	private class ImagePagerAdapter extends PagerAdapter {

		private Cursor _items;
		private LayoutInflater inflater;

		public ImagePagerAdapter(Cursor items) {
			_items = items;
			inflater = getLayoutInflater();
		}

		@Override
		public void destroyItem(View container, int position, Object object) {
			((ViewPager)container).removeView((View)object);
		}

		@Override
		public void finishUpdate(View container) {}

		@Override
		public int getCount() {
			return _items.getCount();
		}

		@Override
		public Object instantiateItem(View view, int position) {
			final View detailsLayout = inflater.inflate(R.layout.details_pager_item, null);
			final TextView title = (TextView)detailsLayout.findViewById(R.id.textTitle);
			final TextView source = (TextView)detailsLayout.findViewById(R.id.textSource);
			final TextView details = (TextView)detailsLayout.findViewById(R.id.textDetail);
			final ImageView imageView = (ImageView)detailsLayout.findViewById(R.id.imageView);
			final ProgressBar progress = (ProgressBar)detailsLayout.findViewById(R.id.progress);
			final ScrollView content = (ScrollView)detailsLayout.findViewById(R.id.scrollView);

			_items.moveToPosition(position);
			title.setText(_items.getString(_items.getColumnIndex(TaxonStore.COL_TITLE)));
			source.setText(_items.getString(_items.getColumnIndex(TaxonStore.COL_SOURCE)));
			details.setText(_items.getString(_items.getColumnIndex(TaxonStore.COL_DETAIL)));
			String image = imgStore.getImage(_items.getLong(_items.getColumnIndex(TaxonStore.COL_PK)), "1200");

			_imageLoader.displayImage(image, imageView, new ImageLoadingListener(){
				@Override
				public void onLoadingCancelled() {
					// Do nothing
				}

				@Override
				public void onLoadingComplete(Bitmap loadedImage) {
					progress.setVisibility(View.GONE);
					content.setVisibility(View.VISIBLE);
				}

				@Override
				public void onLoadingFailed(FailReason failReason) {
					String message = null;
					switch (failReason) {
						case IO_ERROR:
							message = "Input/Output error";
							break;
						case OUT_OF_MEMORY:
							message = "Out Of Memory error";
							break;
						case UNKNOWN:
							message = "Unknown error";
							break;
					}
					Toast.makeText(DetailsPager.this, message, Toast.LENGTH_SHORT).show();
				}

				@Override
				public void onLoadingStarted() {
					progress.setVisibility(View.VISIBLE);
					content.setVisibility(View.GONE);
				}
			});

			((ViewPager)view).addView(detailsLayout, 0);
			return detailsLayout;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view.equals(object);
		}

		@Override
		public void restoreState(Parcelable state, ClassLoader loader) {}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View container) {}
	}

	private TaxonStore taxonStore = new TaxonStore(DetailsPager.this);
	private ImageStore imgStore = new ImageStore(DetailsPager.this);

	private ImageLoader _imageLoader = ImageLoader.getInstance();

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		new ActivityResultHandler(this).onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * Initialises the content of this Activity
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.details_pager);
		getSupportActionBar().setTitle(R.string.id_guide);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		int position = getIntent().getExtras().getInt("position");
		Cursor taxaCursor = taxonStore.getAll();
		startManagingCursor(taxaCursor);
		ViewPager pager = (ViewPager)findViewById(R.id.pager);
		pager.setAdapter(new ImagePagerAdapter(taxaCursor));
		pager.setCurrentItem(position);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.image_only, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		taxonStore.close();
		imgStore.close();
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			case R.id.btnAddSighting:
				new ClickHandler(this).onAddSightingClick(item);
				return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
