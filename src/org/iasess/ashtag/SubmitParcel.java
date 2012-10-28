package org.iasess.ashtag;

import java.io.File;
import java.util.HashMap;

import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

import android.os.Parcel;
import android.os.Parcelable;

public class SubmitParcel extends TaxonParcel implements Parcelable {
	public static final String SUBMIT_PARCEL_EXTRA = "org.iasess.android.submitparcel";
	private String _imgPath;	
	private double _latitude;
	private double _longitude;
	private boolean _isExternal;

	public SubmitParcel(String imgPath) {
		super(-1, null);
		setImagePath(imgPath);
	}

	private SubmitParcel(Parcel in) {
		//these need to be taken out in the order they were added in!
		super(in);
		_imgPath = in.readString();
        _latitude = in.readDouble();
        _longitude = in.readDouble();
        _isExternal = in.readByte() == 1; 
    }
	
	public void setImagePath(String imgPath) {
		_imgPath = imgPath;
	}

	public void setLocation(double latitude, double longitude) {
		_latitude = latitude;
		_longitude = longitude;
	}

	public void setIsExternal(boolean isExternal){
		_isExternal = isExternal;
	}
	
	public String getImagePath() {
		return _imgPath;
	}
	
	public double getLatitiude() {
		return _latitude;
	}

	public double getLongitude() {
		return _longitude;
	}

	public boolean getIsExternal(){
		return _isExternal;
	}
	
	public HashMap<String, ContentBody> getSubmitContent() {
		HashMap<String, ContentBody> map = new HashMap<String, ContentBody>();
		try {
			map.put("email",
					new StringBody(AshTagApp.getUsernamePreferenceString()));
			map.put("location", new StringBody("POINT(" + _longitude + " "
					+ _latitude + ")"));
			map.put("taxon", new StringBody(Long.toString(_taxonId)));
			File image = new File(_imgPath);
			map.put("photo", new FileBody(image));
		} catch (Exception ex) {
			Logger.debug(ex.getMessage());
		}
		return map;
	}

	/** Parcelable implementation **/

	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeString(_imgPath);
		dest.writeDouble(_latitude);
		dest.writeDouble(_longitude);
		dest.writeByte((byte) (_isExternal ? 1 : 0)); 
	}

	public static final Parcelable.Creator<SubmitParcel> CREATOR = new Parcelable.Creator<SubmitParcel>() {
		public SubmitParcel createFromParcel(Parcel in) {
			return new SubmitParcel(in);
		}

		public SubmitParcel[] newArray(int size) {
			return new SubmitParcel[size];
		}
	};
}
