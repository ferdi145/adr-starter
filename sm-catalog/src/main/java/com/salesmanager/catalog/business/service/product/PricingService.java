package com.salesmanager.catalog.business.service.product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import com.salesmanager.catalog.model.integration.core.CustomerInfo;
import com.salesmanager.catalog.model.integration.core.MerchantStoreInfo;
import com.salesmanager.common.business.exception.ServiceException;
import com.salesmanager.catalog.model.product.Product;
import com.salesmanager.catalog.model.product.attribute.ProductAttribute;
import com.salesmanager.catalog.model.product.price.FinalPrice;


/**
 * Services for Product item price calculation. 
 * @author Carl Samson
 *
 */
public interface PricingService {

	/**
	 * Calculates the FinalPrice of a Product taking into account
	 * all defined prices and possible rebates
	 * @param product
	 * @return
	 * @throws ServiceException
	 */
	FinalPrice calculateProductPrice(Product product) throws ServiceException;

	/**
	 * Calculates the FinalPrice of a Product taking into account
	 * all defined prices and possible rebates. It also applies other calculation
	 * based on the customer
	 * @param product
	 * @param customer
	 * @return
	 * @throws ServiceException
	 */
	FinalPrice calculateProductPrice(Product product, CustomerInfo customer)
			throws ServiceException;

	/**
	 * Calculates the FinalPrice of a Product taking into account
	 * all defined prices and possible rebates. This method should be used to calculate
	 * any additional prices based on the default attributes or based on the user selected attributes.
	 * @param product
	 * @param attributes
	 * @return
	 * @throws ServiceException
	 */
	FinalPrice calculateProductPrice(Product product,
			List<ProductAttribute> attributes) throws ServiceException;

	/**
	 * Calculates the FinalPrice of a Product taking into account
	 * all defined prices and possible rebates. This method should be used to calculate
	 * any additional prices based on the default attributes or based on the user selected attributes.
	 * It also applies other calculation based on the customer
	 * @param product
	 * @param attributes
	 * @param customer
	 * @return
	 * @throws ServiceException
	 */
	FinalPrice calculateProductPrice(Product product,
			List<ProductAttribute> attributes, CustomerInfo customer)
			throws ServiceException;

	/**
	 * Method to be used to print a displayable formated amount to the end user
	 * @param amount
	 * @param store
	 * @return
	 * @throws ServiceException
	 */
	String getDisplayAmount(BigDecimal amount, MerchantStoreInfo store)
			throws ServiceException;
	
	/**
	 * Converts a String amount to BigDecimal
	 * Takes care of String amount validation
	 * @param amount
	 * @return
	 * @throws ServiceException
	 */
	BigDecimal getAmount(String amount) throws ServiceException;
	
	/**
	 * String format of the money amount without currency symbol
	 * @param amount
	 * @param store
	 * @return
	 * @throws ServiceException
	 */
	String getStringAmount(BigDecimal amount, MerchantStoreInfo store)
			throws ServiceException;

}
