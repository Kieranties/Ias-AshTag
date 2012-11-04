package org.iasess.ashtag;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.support.v4.content.CursorLoader;

/**
 * Singleton class to manage all image interactions with the device camera or
 * media storage
 */
public final class ImageHandler {

	/**
	 * The intent request code for intents returning an image from the users
	 * camera
	 */
	public static final int CAMERA_OPTION = 1000;

	/**
	 * The intent request code for intents returning an image from the users
	 * gallery
	 */
	public static final int GALLERY_OPTION = 1001;

	/**
	 * Work around for Samsung devices.
	 * <p>
	 * Holds the URI of the last created image by the devices camera
	 */
	private static Uri lastCreatedImageUri;

	/**
	 * Fetches the Bitmap of the given URI
	 * 
	 * @param uri
	 *            The URI to return a Bitmap for
	 * @return The Bitmap of the URI correctly orientated
	 */
	public static Bitmap getBitmap(String imgPath) {
		Bitmap bm = null;
		try {
			// http://stackoverflow.com/a/823966 => winner!

			// get the size of the image to scale without loading the actual
			// bitmap
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(imgPath, options);

			// Find the correct scale value. It should be the power of 2.
			int scale = 1;
			final int MAX_WIDTH = 800; // ==> set to arbitrary value as
										// measuring views is an arse
			while (options.outWidth / scale / 2 >= MAX_WIDTH)
				scale *= 2;

			// Decode with inSampleSize to save memory
			options = new BitmapFactory.Options();
			options.inSampleSize = scale;
			bm = BitmapFactory.decodeFile(imgPath, options);

			// check it's orientation
			int rotation = getImageRotation(imgPath);
			if (rotation != 0) {
				// rotate if required
				Matrix mtx = new Matrix();
				mtx.postRotate(rotation);
				bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), mtx, true);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bm;
	}

	private static ExifInterface getExifData(String imgPath) throws IOException {
		return new ExifInterface(imgPath);
	}

	public static float[] getImageLocation(String imgPath) {
		float[] latLong = new float[2];
		try {
			ExifInterface exif = getExifData(imgPath);
			if (!exif.getLatLong(latLong)) {
				latLong = null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			latLong = null;
		}
		return latLong;
	}

	/**
	 * Returns the URI selected by the user in an image selection intent
	 * 
	 * @param resultCode
	 *            The result of the Intent
	 * @param requestCode
	 *            The identity of the Intent
	 * @param data
	 *            The actual data of the Intent
	 * @return The path of the selected image
	 */
	public static String getImagePathFromIntentResult(int resultCode, int requestCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {

			Uri selectedUri = null;
			switch (requestCode) {
				case ImageHandler.GALLERY_OPTION:
					return getRealPathFromURI(data.getData());
				case ImageHandler.CAMERA_OPTION:
					selectedUri = lastCreatedImageUri;
					Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
					mediaScanIntent.setData(selectedUri);
					AshTagApp.getContext().sendBroadcast(mediaScanIntent);
					return selectedUri.getPath();
			}
		}
		return null;
	}

	/**
	 * Returns the number of degrees an image should be rotated to provide its
	 * correct orientation
	 * 
	 * @param imgPath
	 *            The path of the image
	 * @return The number if degrees to rotate the image
	 */
	public static int getImageRotation(String imgPath) {
		try {
			// check orientation
			ExifInterface exif = getExifData(imgPath);
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

			switch (orientation) {
				case ExifInterface.ORIENTATION_ROTATE_270:
					return -90;
				case ExifInterface.ORIENTATION_ROTATE_180:
					return 180;
				case ExifInterface.ORIENTATION_ROTATE_90:
					return 90;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * Gets the physical path to a given URI on the device
	 * 
	 * @param uri
	 *            The URI to find the physical path for
	 * @return The physical path to the URI
	 */

	public static String getRealPathFromURI(Uri contentUri) {
		String[] proj = { MediaColumns.DATA };
		CursorLoader loader = new CursorLoader(AshTagApp.getContext(), contentUri, proj, null, null, null);
		Cursor cursor = loader.loadInBackground();
		int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	/**
	 * The contextual Activity for instances of this class
	 */
	private Activity _activity;

	/**
	 * Constructor
	 * 
	 * @param activity
	 *            The {@link Activity} context to fetch the image within
	 */
	public ImageHandler(Activity activity) {
		_activity = activity;
	}

	/**
	 * Creates and executes and Intent to process an image selection based on a
	 * newly created image in the devices camera application
	 * 
	 * @throws IOException
	 */
	private void cameraIntent() throws IOException {

		File f = createImageFile();
		if(f == null){
			AshTagApp.makeToast("No room for new images");
			return;
		}
		lastCreatedImageUri = Uri.fromFile(f);

		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, lastCreatedImageUri);
		_activity.startActivityForResult(Intent.createChooser(intent, "Select Picture"), CAMERA_OPTION);
	}

	private File createImageFile() throws IOException {

		File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
			"AshTag");
		if (storageDir.canWrite()) {
			storageDir.mkdirs();

			// Create an image file name
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
			String imageFileName = "ias-" + timeStamp;
			File image = File.createTempFile(imageFileName, ".jpg", storageDir);
			return image;
		}
		return null;
	}

	/**
	 * Creates and executes and Intent to capture and process an image selection
	 * from the users gallery/device
	 */
	private void galleryIntent() {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);

		_activity.startActivityForResult(intent, GALLERY_OPTION);
	}

	/**
	 * Creates an AlertDialog allowing the user to select where/how they would
	 * select an image for the application
	 */
	public void showChooser() {
		AlertDialog.Builder builder = new AlertDialog.Builder(_activity);
		// set items from resource list
		String[] options = _activity.getResources().getStringArray(R.array.camera_options);
		builder.setItems(options, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int item) {
				if (item == 0)
					try {
						cameraIntent();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				else
					galleryIntent();
			}
		});
		builder.create().show();
	}

}