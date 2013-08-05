package org.iasess.ashtag;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.support.v4.content.CursorLoader;
import android.util.Log;

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

			try{						
				Uri selectedUri = null;
				switch (requestCode) {
					case ImageHandler.GALLERY_OPTION:
						String path = getRealPathFromURI(data.getData());
						if(path != null){ return path; }
						
						
						//TODO: display message saying file is to be downloaded
						//if yes download the file, if no cancel action
						
						
					    final InputStream is =  AshTagApp.getContext().getContentResolver().openInputStream(data.getData());
					    File f = createImageFile();
					    OutputStream os = new FileOutputStream(f);
					    int read = 0;
						byte[] bytes = new byte[1024];
					 
						while ((read = is.read(bytes)) != -1) {
							os.write(bytes, 0, read);
						}
					 
						is.close();
						os.flush();
						os.close();
					    return f.getPath();
					    
					case ImageHandler.CAMERA_OPTION:
						selectedUri = lastCreatedImageUri;
						Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
						mediaScanIntent.setData(selectedUri);
						AshTagApp.getContext().sendBroadcast(mediaScanIntent);
						return selectedUri.getPath();
				}
			} catch (Exception e) {
				Log.e("AshTag", "Error while processing image: " + e.getMessage());
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

	private static File createImageFile() throws IOException {

		File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"AshTag");
		storageDir.mkdirs();
		if (storageDir.isDirectory() && storageDir.canWrite()) {			

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
		builder.setTitle("Image options");
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