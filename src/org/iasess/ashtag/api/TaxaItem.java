package org.iasess.ashtag.api;

import java.util.Map;

import com.google.gson.annotations.SerializedName;

/**
 * Simple wrapper object for data returned in taxa/gallery requests
 */
public class TaxaItem {
	
	/**
	 * The key text for an item
	 */
	@SerializedName("key_text")
	private String _keyText;
	
	/**
	 * The scientific name for an item
	 */
	@SerializedName("scientific_name")
	private String _scientificName;
	
	/**
	 * The rank of the item
	 */
	@SerializedName("rank")
	private String _rank;
	
	/**
	 * The collection of key images associated with the item
	 */
	@SerializedName("key_images")
	private Map<String, String[]> _keyImages;
	
	/**
	 * The common name for the item
	 */
	@SerializedName("common_name")
	private String _commonName;
	
	/**
	 * The unique identifier for the item
	 */
	@SerializedName("pk")
	private int _pk;	
		
	/**
	 * Gets the key text for the item
	 * @return
	 */
	public String getKeyText(){ return _keyText; }
	
	/**
	 * Gets the scientific name for the item
	 * @return
	 */
	public String getScientificName(){ return _scientificName; }
	
	/**
	 * Gets the rank of the item
	 * @return
	 */
	public String getRank(){ return _rank; }
	
	/**
	 * Gets the key images for the item
	 * @return
	 */
	public Map<String, String[]> getKeyImages(){ return _keyImages; }
	
	/**
	 * Gets the common name for the item
	 * @return
	 */
	public String getCommonName(){ return _commonName; }
	
	/**
	 * Gets the primary key (unique identifier) for the item
	 * @return
	 */
	public int getPk(){ return _pk;	}
	
	/**
	 * Gets the path of the image to use when displaying the item in a list view
	 * @return
	 */
	public String getListingImagePath(){
		return getImagePath("100");
	}	
	
	/**
	 * Gets the path of a large item to use when displaying details of the item
	 * @return
	 */
	public String getLargeImagePath(){
		return getImagePath("800");
	}
	
	/**
	 * Helper method to return the image path from a given subset
	 * in the items key images
	 */
	private String getImagePath(String size){
		if(_keyImages.containsKey(size)){
			String[] vals = _keyImages.get(size);
			if(vals.length > 0){
				return vals[0];
			}
		}
		return null;
	}
}