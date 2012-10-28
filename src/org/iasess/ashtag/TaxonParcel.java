package org.iasess.ashtag;

import android.os.Parcel;
import android.os.Parcelable;

public class TaxonParcel implements Parcelable{
	public static final String TAXON_PARCEL_EXTRA = "org.iasess.android.taxonparcel";
	protected long _taxonId;
	protected String _taxonName;
	
	public TaxonParcel(long taxonId, String taxonName) {
		_taxonId = taxonId;
		_taxonName = taxonName;
	}
	
	protected TaxonParcel(Parcel in) {
		//these need to be taken out in the order they were added in!
        _taxonId = in.readLong();
        _taxonName = in.readString();
    }	
	
	public void setTaxon(long taxonId, String taxonName) {
		_taxonId = taxonId;
		_taxonName = taxonName;
	}
	
	public long getTaxonId() {
		return _taxonId;
	}

	public String getTaxonName() {
		return _taxonName;
	}
	
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(_taxonId);
		dest.writeString(_taxonName);		
	}
	
	public static final Parcelable.Creator<TaxonParcel> CREATOR = new Parcelable.Creator<TaxonParcel>() {
		public TaxonParcel createFromParcel(Parcel in) {
			return new TaxonParcel(in);
		}

		public TaxonParcel[] newArray(int size) {
			return new TaxonParcel[size];
		}
	};
}
