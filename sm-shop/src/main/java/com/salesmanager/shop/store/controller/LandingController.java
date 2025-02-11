package com.salesmanager.shop.store.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.salesmanager.catalog.api.CatalogImageFilePathApi;
import com.salesmanager.catalog.api.ProductPriceApi;
import com.salesmanager.catalog.api.ProductRelationshipApi;
import com.salesmanager.catalog.presentation.util.CatalogImageFilePathUtils;
import com.salesmanager.core.business.services.customer.CustomerService;
import com.salesmanager.shop.populator.catalog.ReadableProductPopulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.salesmanager.catalog.business.service.product.PricingService;
import com.salesmanager.core.business.services.content.ContentService;
import com.salesmanager.core.business.services.merchant.MerchantStoreService;
import com.salesmanager.catalog.model.product.Product;
import com.salesmanager.catalog.model.product.relationship.ProductRelationship;
import com.salesmanager.catalog.model.product.relationship.ProductRelationshipType;
import com.salesmanager.core.model.content.Content;
import com.salesmanager.core.model.content.ContentDescription;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.common.presentation.constants.Constants;
import com.salesmanager.catalog.presentation.model.product.ReadableProduct;
import com.salesmanager.common.presentation.model.Breadcrumb;
import com.salesmanager.common.presentation.model.BreadcrumbItem;
import com.salesmanager.common.presentation.model.BreadcrumbItemType;
import com.salesmanager.common.presentation.model.PageInformation;
import com.salesmanager.common.presentation.util.DateUtil;
import com.salesmanager.common.presentation.util.LabelUtils;



@Controller
public class LandingController {
	
	
	private final static String LANDING_PAGE = "LANDING_PAGE";
	
	
	@Inject
	private ContentService contentService;
	
	@Inject
	private ProductRelationshipApi productRelationshipApi;

	
	@Inject
	private LabelUtils messages;
	
	@Inject
	private PricingService pricingService;
	
	@Inject
	private MerchantStoreService merchantService;
	
	@Autowired
	private CatalogImageFilePathUtils imageUtils;

	@Autowired
	private CatalogImageFilePathApi imageFilePathApi;

	@Autowired
	private ProductPriceApi productPriceApi;

	@Autowired
	private CustomerService customerService;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LandingController.class);
	private final static String HOME_LINK_CODE="HOME";
	
	@RequestMapping(value={Constants.SHOP_URI + "/home.html",Constants.SHOP_URI +"/", Constants.SHOP_URI}, method=RequestMethod.GET)
	public String displayLanding(Model model, HttpServletRequest request, HttpServletResponse response, Locale locale) throws Exception {
		
		Language language = (Language)request.getAttribute(Constants.LANGUAGE);
		
		MerchantStore store = (MerchantStore)request.getAttribute(Constants.MERCHANT_STORE);

		request.setAttribute(Constants.LINK_CODE, HOME_LINK_CODE);
		
		Content content = contentService.getByCode(LANDING_PAGE, store, language);
		
		/** Rebuild breadcrumb **/
		BreadcrumbItem item = new BreadcrumbItem();
		item.setItemType(BreadcrumbItemType.HOME);
		item.setLabel(messages.getMessage(Constants.HOME_MENU_KEY, locale));
		item.setUrl(Constants.HOME_URL);

		
		Breadcrumb breadCrumb = new Breadcrumb();
		breadCrumb.setLanguage(language.getCode());
		
		List<BreadcrumbItem> items = new ArrayList<BreadcrumbItem>();
		items.add(item);
		
		breadCrumb.setBreadCrumbs(items);
		request.getSession().setAttribute(Constants.BREADCRUMB, breadCrumb);
		request.setAttribute(Constants.BREADCRUMB, breadCrumb);
		/** **/
		
		if(content!=null) {
			
			ContentDescription description = content.getDescription();

			model.addAttribute("page",description);
			
			PageInformation pageInformation = new PageInformation();
			pageInformation.setPageTitle(description.getName());
			pageInformation.setPageDescription(description.getMetatagDescription());
			pageInformation.setPageKeywords(description.getMetatagKeywords());
			
			request.setAttribute(Constants.REQUEST_PAGE_INFORMATION, pageInformation);
			
		}
		
		ReadableProductPopulator populator = new ReadableProductPopulator();
		populator.setPricingService(pricingService);
		populator.setimageUtils(imageUtils);
		populator.setProductPriceApi(productPriceApi);
		populator.setImageFilePathApi(imageFilePathApi);
		populator.setCustomerService(customerService);

		
		//featured items
		List<ProductRelationship> relationships = productRelationshipApi.getByType(store.toDTO(), ProductRelationshipType.FEATURED_ITEM, language.toDTO());
		List<ReadableProduct> featuredItems = new ArrayList<ReadableProduct>();
		Date today = new Date();
		for(ProductRelationship relationship : relationships) {
			Product product = relationship.getRelatedProduct();
			if(product.isAvailable() && DateUtil.dateBeforeEqualsDate(product.getDateAvailable(), today)) {
				ReadableProduct proxyProduct = populator.populate(product, new ReadableProduct(), store, language);
			    featuredItems.add(proxyProduct);
			}
		}

		
		model.addAttribute("featuredItems", featuredItems);
		
		/** template **/
		StringBuilder template = new StringBuilder().append("landing.").append(store.getStoreTemplate());
		return template.toString();
	}
	
	@RequestMapping(value={Constants.SHOP_URI + "/stub.html"}, method=RequestMethod.GET)
	public String displayHomeStub(Model model, HttpServletRequest request, HttpServletResponse response, Locale locale) throws Exception {
		return "index";
	}
	
	@RequestMapping( value=Constants.SHOP_URI + "/{store}", method=RequestMethod.GET)
	public String displayStoreLanding(@PathVariable final String store, HttpServletRequest request, HttpServletResponse response) {
		
		try {
			
			request.getSession().invalidate();
			request.getSession().removeAttribute(Constants.MERCHANT_STORE);
			
			MerchantStore merchantStore = merchantService.getByCode(store);
			if(merchantStore!=null) {
				request.getSession().setAttribute(Constants.MERCHANT_STORE, merchantStore);
				request.getSession().setAttribute(Constants.MERCHANT_STORE_DTO, merchantStore.toDTO());
			} else {
				LOGGER.error("MerchantStore does not exist for store code " + store);
			}
			
		} catch(Exception e) {
			LOGGER.error("Error occured while getting store code " + store, e);
		}
		

		
		return "redirect:" + Constants.SHOP_URI;
	}
		

}
