package org.inventivetalent.trashapp.common;

import com.android.billingclient.api.SkuDetails;

public class SkuInfo {

	private final SkuDetails     skuDetails;
	private final PaymentHandler paymentHandler;

	public SkuInfo(SkuDetails skuDetails, PaymentHandler paymentHandler) {
		this.skuDetails = skuDetails;
		this.paymentHandler = paymentHandler;
	}

	public SkuDetails getSkuDetails() {
		return skuDetails;
	}

	public PaymentHandler getPaymentHandler() {
		return paymentHandler;
	}

	public void launchBilling() {
		getPaymentHandler().launchBilling(getSkuDetails());
	}

}
