package org.iasess.ashtag.api;

import org.iasess.ashtag.AshTagApp;

import com.google.gson.annotations.SerializedName;

/**
 * Simple wrapper object for data returned in campaign requests
 */
public class CampaignModel {
	private static final String TAXON_COMMON_NAME = "taxon_common_name";
	private static final String ABOUT = "about";
	private static final String TAXON_SCIENTIFIC_NAME = "taxon_scientific_name";
	private static final String SITE = "site";
	private static final String TITLE = "title";

	public static CampaignModel getInstance() {
		CampaignModel model = new CampaignModel();
		model._taxonCommonName = AshTagApp.getPreferenceString(TAXON_COMMON_NAME);
		model._about = AshTagApp.getPreferenceString(ABOUT);
		model._taxonScientificName = AshTagApp.getPreferenceString(TAXON_SCIENTIFIC_NAME);
		model._site = AshTagApp.getPreferenceString(SITE);
		model._title = AshTagApp.getPreferenceString(TITLE);

		return model;
	}

	@SerializedName(TAXON_COMMON_NAME)
	private String _taxonCommonName;

	@SerializedName(ABOUT)
	private String _about;

	@SerializedName(TAXON_SCIENTIFIC_NAME)
	private String _taxonScientificName;

	@SerializedName(SITE)
	private String _site;

	@SerializedName(TITLE)
	private String _title;

	public String getAbout() {
		return _about;
	}

	public String getSite() {
		return _site;
	}

	public String getTaxonCommonName() {
		return _taxonCommonName;
	}

	public String getTaxonScientificName() {
		return _taxonScientificName;
	}

	public String getTitle() {
		return _title;
	}

	public void save() {
		AshTagApp.setPreferenceString(TAXON_COMMON_NAME, _taxonCommonName);
		AshTagApp.setPreferenceString(ABOUT, _about);
		AshTagApp.setPreferenceString(TAXON_SCIENTIFIC_NAME, _taxonScientificName);
		AshTagApp.setPreferenceString(SITE, _site);
		AshTagApp.setPreferenceString(TITLE, _title);
	}

}