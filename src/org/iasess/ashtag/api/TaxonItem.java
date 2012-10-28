package org.iasess.ashtag.api;

import java.util.Map;

import com.google.gson.annotations.SerializedName;


public class TaxonItem {

	@SerializedName("source")
	private String _source;
	
	@SerializedName("title")
	private String _title;
	
	@SerializedName("detail")
	private String _detail;
	
	@SerializedName("sizes")
	private Map<String, String> _sizes;
	

	public String getSource(){ return _source; }
	public String getTitle(){ return _title; }
	public String getDetail(){ return _detail; }
	public Map<String, String> getSizes(){ return _sizes; }
}