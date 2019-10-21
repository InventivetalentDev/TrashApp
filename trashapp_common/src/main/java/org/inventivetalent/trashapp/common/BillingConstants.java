package org.inventivetalent.trashapp.common;

public class BillingConstants {

	public static final String SKU_THEMES     = "themes";
	@Deprecated
	public static final String SKU_REMOVE_ADS = "remove_ads";

	public static final String SKU_AD_FREE = "ad_free";

	public static final String[] IN_APP_SKUS = new String[] {
			SKU_THEMES,
			SKU_REMOVE_ADS };
	public static final String[] SUBS_SKUS   = new String[] {
			SKU_AD_FREE
	};

	public static String getTypeForSku(String sku) {
		switch (sku) {
			case SKU_AD_FREE:
				return "subscription";
			case SKU_THEMES:
			default:
				return "product";
		}
	}

}
