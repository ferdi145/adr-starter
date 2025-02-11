package com.salesmanager.catalog.presentation.api.v1.category;

import com.salesmanager.catalog.business.integration.core.service.MerchantStoreInfoService;
import com.salesmanager.catalog.business.service.category.CategoryService;
import com.salesmanager.catalog.model.integration.core.LanguageInfo;
import com.salesmanager.catalog.model.integration.core.MerchantStoreInfo;
import com.salesmanager.catalog.presentation.controller.category.facade.CategoryFacade;
import com.salesmanager.catalog.presentation.model.category.PersistableCategory;
import com.salesmanager.catalog.presentation.model.category.ReadableCategory;
import com.salesmanager.catalog.presentation.util.RestUtils;
import com.salesmanager.catalog.model.category.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/api/v1")
public class CategoryApi {
	
	@Inject
	private CategoryFacade categoryFacade;
	
	@Inject
	private CategoryService categoryService;
	
	@Inject
	private MerchantStoreInfoService merchantStoreInfoService;
	
	@Inject
	private RestUtils restUtils;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CategoryApi.class);
	
	
	@RequestMapping(value = "/category/{id}", method=RequestMethod.GET)
	@ResponseBody
	public ReadableCategory get(@PathVariable final Long id, @RequestParam(value = "lang", required=false) String lang, HttpServletRequest request, HttpServletResponse response) throws Exception {
	
		try {
			MerchantStoreInfo merchantStore = this.merchantStoreInfoService.findbyCode(com.salesmanager.common.business.constants.Constants.DEFAULT_STORE);
			LanguageInfo language = restUtils.getRESTLanguage(request, merchantStore);
			
			ReadableCategory category  = categoryFacade.getById(merchantStore, id, language);
			
			if(category==null) {
				response.sendError(404,  "Category id not found");
				return null;
			}
	
	
			return category;
		
		} catch (Exception e) {
			LOGGER.error("Error while getting category",e);
			try {
				response.sendError(503, "Error while getting category " + e.getMessage());
			} catch (Exception ignore) {
			}
			return null;
		}
	}
	
	/**
	 * Get all category starting from root
	 * filter can be used for filtering on fields
	 * only featured is supported
	 * @param lang
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/category", method=RequestMethod.GET)
	@ResponseBody
	public List <ReadableCategory> getFiltered(
			@RequestParam(value = "filter", required=false) String filter,
			@RequestParam(value = "lang", required=false) 
			String lang, HttpServletRequest request, HttpServletResponse response) throws Exception {
	
		try {
			MerchantStoreInfo merchantStore = this.merchantStoreInfoService.findbyCode(com.salesmanager.common.business.constants.Constants.DEFAULT_STORE);

			LanguageInfo language = restUtils.getRESTLanguage(request, merchantStore);
			
			List <ReadableCategory> category  = categoryFacade.getCategoryHierarchy(merchantStore, 0, language, filter);


			return category;
		
		} catch (Exception e) {
			LOGGER.error("Error while getting root category",e);
			try {
				response.sendError(503, "Error while getting root category " + e.getMessage());
			} catch (Exception ignore) {
			}
			return null;
		}
	}
	
	/**
	 * Category creation
	 */
	@RequestMapping( value="/private/category", method=RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	public PersistableCategory createCategory(@Valid @RequestBody PersistableCategory category, HttpServletRequest request, HttpServletResponse response) {
		
		
		try {


			MerchantStoreInfo merchantStore = this.merchantStoreInfoService.findbyCode(com.salesmanager.common.business.constants.Constants.DEFAULT_STORE);

			categoryFacade.saveCategory(merchantStore, category);

			
			category.setId(category.getId());

			return category;
		
		} catch (Exception e) {
			LOGGER.error("Error while creating category",e);
			try {
				response.sendError(503, "Error while creating category " + e.getMessage());
			} catch (Exception ignore) {
			}
			return null;
		}
	}
	
    @ResponseStatus(HttpStatus.OK)
	@RequestMapping( value="/private/category/{id}", method=RequestMethod.PUT)
    public @ResponseBody PersistableCategory update(@PathVariable Long id, @Valid @RequestBody PersistableCategory category, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		try {

			MerchantStoreInfo merchantStore = this.merchantStoreInfoService.findbyCode(com.salesmanager.common.business.constants.Constants.DEFAULT_STORE);

			categoryFacade.saveCategory(merchantStore, category);

			return category;
			
		} catch (Exception e) {
			LOGGER.error("Error while updating category",e);
			try {
				response.sendError(503, "Error while updating category " + e.getMessage());
			} catch (Exception ignore) {
			}
			
			return null;
		}
		
	}
    
    @ResponseStatus(HttpStatus.OK)
	@RequestMapping( value="/private/category/{id}", method=RequestMethod.DELETE)
    public void delete(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		try {
			Category category = categoryService.getById(id);
			if(category != null){
				categoryFacade.deleteCategory(category);
			}else{
				response.sendError(404, "No Category found for ID : " + id);
			}
		} catch (Exception e) {
			LOGGER.error("Error while deleting category",e);
			try {
				response.sendError(503, "Error while deleting category " + e.getMessage());
			} catch (Exception ignore) {
			}
		}
	}

}
