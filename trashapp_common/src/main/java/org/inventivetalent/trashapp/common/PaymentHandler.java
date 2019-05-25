package org.inventivetalent.trashapp.common;

import com.android.billingclient.api.SkuDetails;

public interface PaymentHandler {

	void launchBilling(SkuDetails details);

	boolean isPurchased(String sku);

}
