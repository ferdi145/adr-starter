package com.salesmanager.catalog.presentation.controller.admin.category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import com.salesmanager.catalog.business.integration.core.service.LanguageInfoService;
import com.salesmanager.catalog.business.integration.core.service.MerchantStoreInfoService;
import com.salesmanager.catalog.model.integration.core.LanguageInfo;
import com.salesmanager.catalog.model.integration.core.MerchantStoreInfo;
import com.salesmanager.core.integration.language.LanguageDTO;
import com.salesmanager.core.integration.merchant.MerchantStoreDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.salesmanager.catalog.business.service.category.CategoryService;
import com.salesmanager.common.business.ajax.AjaxResponse;
import com.salesmanager.catalog.model.category.Category;
import com.salesmanager.catalog.model.category.CategoryDescription;
import com.salesmanager.common.presentation.model.admin.Menu;
import com.salesmanager.common.presentation.constants.Constants;
import com.salesmanager.common.presentation.util.LabelUtils;



@Controller
public class CategoryController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CategoryController.class);

	@Inject
	CategoryService categoryService;

	@Inject
	LabelUtils messages;

	@Autowired
	private MerchantStoreInfoService merchantStoreInfoService;

	@Autowired
	private LanguageInfoService languageInfoService;

	@PreAuthorize("hasRole('PRODUCTS')")
	@RequestMapping(value="/admin/categories/editCategory.html", method=RequestMethod.GET)
	public String displayCategoryEdit(@RequestParam("id") long categoryId, Model model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		return displayCategory(categoryId,model,request,response);

	}
	
	@PreAuthorize("hasRole('PRODUCTS')")
	@RequestMapping(value="/admin/categories/createCategory.html", method=RequestMethod.GET)
	public String displayCategoryCreate(Model model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		return displayCategory(null,model,request,response);

	}
	
	
	
	private String displayCategory(Long categoryId, Model model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		

		//display menu
		setMenu(model,request);


		MerchantStoreDTO storeDTO = (MerchantStoreDTO) request.getAttribute(Constants.ADMIN_STORE_DTO);
		MerchantStoreInfo store = this.merchantStoreInfoService.findbyCode(storeDTO.getCode());
		LanguageDTO languageDTO = (LanguageDTO) request.getAttribute("LANGUAGE_DTO");
		LanguageInfo language = this.languageInfoService.findbyCode(languageDTO.getCode());
		
		//get parent categories
		List<Category> categories = categoryService.listByStore(store,language);
		

		List<LanguageInfo> languages = store.getLanguages();
		Category category = new Category();
		
		if(categoryId!=null && categoryId!=0) {//edit mode
			category = categoryService.getById(categoryId);
		
			
			
			if(category==null || category.getMerchantStore().getId().intValue()!=store.getId().intValue()) {
				return "catalogue-categories";
			}
		} else {
			
			category.setVisible(true);
			
		}
		
		List<CategoryDescription> descriptions = new ArrayList<CategoryDescription>();
		
		for(LanguageInfo l : languages) {
			
			CategoryDescription description = null;
			if(category!=null) {
				for(CategoryDescription desc : category.getDescriptions()) {
					
					
					if(desc.getLanguage().getCode().equals(l.getCode())) {
						description = desc;
					}
					
					
				}
			}
			
			if(description==null) {
				description = new CategoryDescription();
				description.setLanguage(l);
			}
			
			descriptions.add(description);

		}
		
		category.setDescriptions(descriptions);
	

		
		model.addAttribute("category", category);
		model.addAttribute("categories", categories);
		

		
		return "catalogue-categories-category";
	}
	
	@PreAuthorize("hasRole('PRODUCTS')")
	@RequestMapping(value="/admin/categories/save.html", method=RequestMethod.POST)
	public String saveCategory(@Valid @ModelAttribute("category") Category category, BindingResult result, Model model, HttpServletRequest request) throws Exception {

		LanguageDTO languageDTO = (LanguageDTO) request.getAttribute("LANGUAGE_DTO");
		LanguageInfo language = this.languageInfoService.findbyCode(languageDTO.getCode());
		
		//display menu
		setMenu(model,request);

		MerchantStoreDTO storeDTO = (MerchantStoreDTO) request.getAttribute(Constants.ADMIN_STORE_DTO);
		MerchantStoreInfo store = this.merchantStoreInfoService.findbyCode(storeDTO.getCode());

		if(category.getId() != null && category.getId() >0) { //edit entry
			
			//get from DB
			Category currentCategory = categoryService.getById(category.getId());
			
			if(currentCategory==null || currentCategory.getMerchantStore().getId().intValue()!=store.getId().intValue()) {
				return "catalogue-categories";
			}

		}

			
			Map<String,LanguageInfo> langs = languageInfoService.getLanguagesMap();
			


			List<CategoryDescription> descriptions = category.getDescriptions();
			if(descriptions!=null) {
				
				for(CategoryDescription description : descriptions) {
					
					String code = description.getLanguage().getCode();
					LanguageInfo l = langs.get(code);
					description.setLanguage(l);
					description.setCategory(category);
					
					
				}
				
			}
			
			//save to DB
			category.setMerchantStore(store);
		//}
		
		if (result.hasErrors()) {
			return "catalogue-categories-category";
		}
		
		//check parent
		if(category.getParent()!=null) {
			if(category.getParent().getId()==-1) {//this is a root category
				category.setParent(null);
				category.setLineage("/");
				category.setDepth(0);
			}
		}
		
		category.getAuditSection().setModifiedBy(request.getRemoteUser());
		categoryService.saveOrUpdate(category);

			
		//ajust lineage and depth
		if(category.getParent()!=null && category.getParent().getId()!=-1) { 
		
			Category parent = new Category();
			parent.setId(category.getParent().getId());
			parent.setMerchantStore(store);
			
			categoryService.addChild(parent, category);
		
		}
		
		
		//get parent categories
		List<Category> categories = categoryService.listByStore(store,language);
		model.addAttribute("categories", categories);
		

		model.addAttribute("success","success");
		return "catalogue-categories-category";
	}
	
	
	//category list
	@PreAuthorize("hasRole('PRODUCTS')")
	@RequestMapping(value="/admin/categories/categories.html", method=RequestMethod.GET)
	public String displayCategories(Model model, HttpServletRequest request, HttpServletResponse response) throws Exception {

		
		
		setMenu(model,request);
		
		//does nothing, ajax subsequent request
		
		return "catalogue-categories";
	}
	
	@SuppressWarnings({ "unchecked"})
	@PreAuthorize("hasRole('PRODUCTS')")
	@RequestMapping(value="/admin/categories/paging.html", method=RequestMethod.POST)
	public @ResponseBody ResponseEntity<String> pageCategories(HttpServletRequest request, HttpServletResponse response) {
		String categoryName = request.getParameter("name");
		String categoryCode = request.getParameter("code");


		AjaxResponse resp = new AjaxResponse();

		
		try {

			LanguageDTO languageDTO = (LanguageDTO) request.getAttribute("LANGUAGE_DTO");
			LanguageInfo language = this.languageInfoService.findbyCode(languageDTO.getCode());


			MerchantStoreDTO storeDTO = (MerchantStoreDTO) request.getAttribute(Constants.ADMIN_STORE_DTO);
			MerchantStoreInfo store = this.merchantStoreInfoService.findbyCode(storeDTO.getCode());
			
			List<Category> categories = null;
					
			if(!StringUtils.isBlank(categoryName)) {
				
				
				categories = categoryService.getByName(store, categoryName, language);
				
			} else if(!StringUtils.isBlank(categoryCode)) {
				
				categoryService.listByCodes(store, new ArrayList<String>(Arrays.asList(categoryCode)), language);
			
			} else {
				
				categories = categoryService.listByStore(store, language);
				
			}
					
					
			
			for(Category category : categories) {
				
				@SuppressWarnings("rawtypes")
				Map entry = new HashMap();
				entry.put("categoryId", category.getId());
				
				CategoryDescription description = category.getDescriptions().get(0);
				
				entry.put("name", description.getName());
				entry.put("code", category.getCode());
				entry.put("visible", category.isVisible());
				resp.addDataEntry(entry);
				
				
			}
			
			resp.setStatus(AjaxResponse.RESPONSE_STATUS_SUCCESS);
			

		
		} catch (Exception e) {
			LOGGER.error("Error while paging categories", e);
			resp.setStatus(AjaxResponse.RESPONSE_STATUS_FAIURE);
		}
		
		String returnString = resp.toJSONString();
		final HttpHeaders httpHeaders= new HttpHeaders();
	    httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
		
	    return new ResponseEntity<String>(returnString,httpHeaders,HttpStatus.OK);
	}
	
	@PreAuthorize("hasRole('PRODUCTS')")
	@RequestMapping(value="/admin/categories/hierarchy.html", method=RequestMethod.GET)
	public String displayCategoryHierarchy(Model model, HttpServletRequest request, HttpServletResponse response) throws Exception {

		
		
		setMenu(model,request);
		
		//get the list of categories
		LanguageDTO languageDTO = (LanguageDTO) request.getAttribute("LANGUAGE_DTO");
		LanguageInfo language = this.languageInfoService.findbyCode(languageDTO.getCode());
		MerchantStoreDTO storeDTO = (MerchantStoreDTO) request.getAttribute(Constants.ADMIN_STORE_DTO);
		MerchantStoreInfo store = this.merchantStoreInfoService.findbyCode(storeDTO.getCode());
		
		List<Category> categories = categoryService.listByStore(store, language);
		
		model.addAttribute("categories", categories);
		
		return "catalogue-categories-hierarchy";
	}
	
	@PreAuthorize("hasRole('PRODUCTS')")
	@RequestMapping(value="/admin/categories/remove.html", method=RequestMethod.POST)
	public @ResponseBody ResponseEntity<String> deleteCategory(HttpServletRequest request, HttpServletResponse response, Locale locale) {
		String sid = request.getParameter("categoryId");

		MerchantStoreDTO storeDTO = (MerchantStoreDTO) request.getAttribute(Constants.ADMIN_STORE_DTO);
		MerchantStoreInfo store = this.merchantStoreInfoService.findbyCode(storeDTO.getCode());
		
		AjaxResponse resp = new AjaxResponse();

		
		try {
			
			Long id = Long.parseLong(sid);
			
			Category category = categoryService.getById(id);
			
			if(category==null || category.getMerchantStore().getId().intValue() !=store.getId().intValue() ) {

				resp.setStatusMessage(messages.getMessage("message.unauthorized", locale));
				resp.setStatus(AjaxResponse.RESPONSE_STATUS_FAIURE);			
				
			} else {
				
				categoryService.delete(category);
				resp.setStatus(AjaxResponse.RESPONSE_OPERATION_COMPLETED);
				
			}
		
		
		} catch (Exception e) {
			LOGGER.error("Error while deleting category", e);
			resp.setStatus(AjaxResponse.RESPONSE_STATUS_FAIURE);
			resp.setErrorMessage(e);
		}
		
		String returnString = resp.toJSONString();
		final HttpHeaders httpHeaders= new HttpHeaders();
	    httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
		return new ResponseEntity<String>(returnString,httpHeaders,HttpStatus.OK);
	}
	
	@PreAuthorize("hasRole('PRODUCTS')")
	@RequestMapping(value="/admin/categories/moveCategory.html", method=RequestMethod.POST)
	public @ResponseBody ResponseEntity<String> moveCategory(HttpServletRequest request, HttpServletResponse response, Locale locale) {
		String parentid = request.getParameter("parentId");
		String childid = request.getParameter("childId");

		MerchantStoreDTO storeDTO = (MerchantStoreDTO) request.getAttribute(Constants.ADMIN_STORE_DTO);
		MerchantStoreInfo store = this.merchantStoreInfoService.findbyCode(storeDTO.getCode());
		
		AjaxResponse resp = new AjaxResponse();
		final HttpHeaders httpHeaders= new HttpHeaders();
	    httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);

		
		try {
			
			Long parentId = Long.parseLong(parentid);
			Long childId = Long.parseLong(childid);
			
			Category child = categoryService.getById(childId);
			Category parent = categoryService.getById(parentId);
			
			if(child.getParent().getId()==parentId) {
				resp.setStatus(AjaxResponse.RESPONSE_OPERATION_COMPLETED);
				String returnString = resp.toJSONString();
			}

			if(parentId!=1) {
			
				if(child==null || parent==null || child.getMerchantStore().getId()!=store.getId() || parent.getMerchantStore().getId()!=store.getId()) {
					resp.setStatusMessage(messages.getMessage("message.unauthorized", locale));
					
					resp.setStatus(AjaxResponse.RESPONSE_STATUS_FAIURE);
					String returnString = resp.toJSONString();
					return new ResponseEntity<String>(returnString,httpHeaders,HttpStatus.OK);
				}
				
				if(child.getMerchantStore().getId()!=store.getId() || parent.getMerchantStore().getId()!=store.getId()) {
					resp.setStatusMessage(messages.getMessage("message.unauthorized", locale));
					resp.setStatus(AjaxResponse.RESPONSE_STATUS_FAIURE);
					String returnString = resp.toJSONString();
					return new ResponseEntity<String>(returnString,httpHeaders,HttpStatus.OK);
				}
			
			}
			

			parent.getAuditSection().setModifiedBy(request.getRemoteUser());
			categoryService.addChild(parent, child);
			resp.setStatus(AjaxResponse.RESPONSE_OPERATION_COMPLETED);

		} catch (Exception e) {
			LOGGER.error("Error while moving category", e);
			resp.setStatus(AjaxResponse.RESPONSE_STATUS_FAIURE);
			resp.setErrorMessage(e);
		}
		
		String returnString = resp.toJSONString();
		
		return new ResponseEntity<String>(returnString,httpHeaders,HttpStatus.OK);
	}
	
	@PreAuthorize("hasRole('PRODUCTS')")
	@RequestMapping(value="/admin/categories/checkCategoryCode.html", method=RequestMethod.POST)
	public @ResponseBody ResponseEntity<String> checkCategoryCode(HttpServletRequest request, HttpServletResponse response, Locale locale) {
		String code = request.getParameter("code");
		String id = request.getParameter("id");


		MerchantStoreDTO storeDTO = (MerchantStoreDTO) request.getAttribute(Constants.ADMIN_STORE_DTO);
		MerchantStoreInfo store = this.merchantStoreInfoService.findbyCode(storeDTO.getCode());
		
		
		AjaxResponse resp = new AjaxResponse();
		
		
		final HttpHeaders httpHeaders= new HttpHeaders();
	    httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
		
		if(StringUtils.isBlank(code)) {
			resp.setStatus(AjaxResponse.CODE_ALREADY_EXIST);
			String returnString = resp.toJSONString();
			return new ResponseEntity<String>(returnString,httpHeaders,HttpStatus.OK);
		}

		
		try {
			
		Category category = categoryService.getByCode(store, code);
		
		if(category!=null && StringUtils.isBlank(id)) {
			resp.setStatus(AjaxResponse.CODE_ALREADY_EXIST);
			String returnString = resp.toJSONString();
			return new ResponseEntity<String>(returnString,httpHeaders,HttpStatus.OK);
		}
		
		
		if(category!=null && !StringUtils.isBlank(id)) {
			try {
				Long lid = Long.parseLong(id);
				
				if(category.getCode().equals(code) && category.getId().longValue()==lid) {
					resp.setStatus(AjaxResponse.CODE_ALREADY_EXIST);
					String returnString = resp.toJSONString();
					return new ResponseEntity<String>(returnString,httpHeaders,HttpStatus.OK);
				}
			} catch (Exception e) {
				resp.setStatus(AjaxResponse.CODE_ALREADY_EXIST);
				String returnString = resp.toJSONString();
				return new ResponseEntity<String>(returnString,httpHeaders,HttpStatus.OK);
			}

		}
		
		
		
		

	
		
			


			resp.setStatus(AjaxResponse.RESPONSE_OPERATION_COMPLETED);

		} catch (Exception e) {
			LOGGER.error("Error while getting category", e);
			resp.setStatus(AjaxResponse.RESPONSE_STATUS_FAIURE);
			resp.setErrorMessage(e);
		}
		
		String returnString = resp.toJSONString();

		return new ResponseEntity<String>(returnString,httpHeaders,HttpStatus.OK);
	}
	
	private void setMenu(Model model, HttpServletRequest request) throws Exception {
		
		//display menu
		Map<String,String> activeMenus = new HashMap<String,String>();
		activeMenus.put("catalogue", "catalogue");
		activeMenus.put("catalogue-categories", "catalogue-categories");
		
		@SuppressWarnings("unchecked")
		Map<String, Menu> menus = (Map<String, Menu>)request.getAttribute("MENUMAP");
		
		Menu currentMenu = (Menu)menus.get("catalogue");
		model.addAttribute("currentMenu",currentMenu);
		model.addAttribute("activeMenus",activeMenus);
		//
		
	}

}
