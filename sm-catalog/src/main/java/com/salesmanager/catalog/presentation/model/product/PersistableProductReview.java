package com.salesmanager.catalog.presentation.model.product;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

public class PersistableProductReview extends ProductReviewEntity implements
		Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@NotNull
	private Long customerId;
	public Long getCustomerId() {
		return customerId;
	}
	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}
	


}
