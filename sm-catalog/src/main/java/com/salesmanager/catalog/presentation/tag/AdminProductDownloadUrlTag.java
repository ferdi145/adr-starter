package com.salesmanager.catalog.presentation.tag;

import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;

import com.salesmanager.catalog.business.integration.core.service.MerchantStoreInfoService;
import com.salesmanager.catalog.model.integration.core.MerchantStoreInfo;
import com.salesmanager.catalog.presentation.util.CatalogFilePathUtils;
import com.salesmanager.core.integration.merchant.MerchantStoreDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.tags.RequestContextAwareTag;

import com.salesmanager.catalog.model.product.file.DigitalProduct;
import com.salesmanager.common.presentation.constants.Constants;



public class AdminProductDownloadUrlTag extends RequestContextAwareTag {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6319855234657139862L;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AdminProductDownloadUrlTag.class);

	private DigitalProduct digitalProduct;
	
	@Inject
	private CatalogFilePathUtils filePathUtils;

	@Autowired
	private MerchantStoreInfoService merchantStoreInfoService;




	public DigitalProduct getDigitalProduct() {
		return digitalProduct;
	}

	public void setDigitalProduct(DigitalProduct digitalProduct) {
		this.digitalProduct = digitalProduct;
	}

	public int doStartTagInternal() throws JspException {
		try {
			
			if (filePathUtils==null) {
	            WebApplicationContext wac = getRequestContext().getWebApplicationContext();
	            AutowireCapableBeanFactory factory = wac.getAutowireCapableBeanFactory();
	            factory.autowireBean(this);
	        }


			HttpServletRequest request = (HttpServletRequest) pageContext
					.getRequest();

			MerchantStoreDTO storeDTO = (MerchantStoreDTO) request.getAttribute(Constants.MERCHANT_STORE_DTO);
			MerchantStoreInfo merchantStore = this.merchantStoreInfoService.findbyCode(storeDTO.getCode());
			
			HttpSession session = request.getSession();

			StringBuilder filePath = new StringBuilder();
			
			//TODO domain from merchant, else from global config, else from property (localhost)
			
			// example -> "/files/{storeCode}/{fileName}.{extension}"
			

			@SuppressWarnings("unchecked")
			Map<String,String> configurations = (Map<String, String>)session.getAttribute(Constants.STORE_CONFIGURATION);
			String scheme = Constants.HTTP_SCHEME;
			if(configurations!=null) {
				scheme = (String)configurations.get("scheme");
			}
			

			
			filePath.append(scheme).append("://")
			.append(merchantStore.getDomainName())
			//.append("/")
			.append(request.getContextPath());
			
			filePath
				.append(filePathUtils.buildAdminDownloadProductFilePath(merchantStore, digitalProduct)).toString();

			

			pageContext.getOut().print(filePath.toString());


			
		} catch (Exception ex) {
			LOGGER.error("Error while getting content url", ex);
		}
		return SKIP_BODY;
	}

	public int doEndTag() {
		return EVAL_PAGE;
	}





	

}
