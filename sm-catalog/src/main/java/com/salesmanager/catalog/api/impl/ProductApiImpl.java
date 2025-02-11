package com.salesmanager.catalog.api.impl;

import com.salesmanager.catalog.api.ProductApi;
import com.salesmanager.catalog.business.integration.core.service.LanguageInfoService;
import com.salesmanager.catalog.business.integration.core.service.TaxClassInfoService;
import com.salesmanager.catalog.business.service.product.ProductService;
import com.salesmanager.catalog.model.integration.core.LanguageInfo;
import com.salesmanager.catalog.model.integration.core.TaxClassInfo;
import com.salesmanager.catalog.model.product.Product;
import com.salesmanager.common.business.exception.ServiceException;
import com.salesmanager.core.integration.language.LanguageDTO;
import com.salesmanager.core.integration.tax.TaxClassDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class ProductApiImpl implements ProductApi {

    private ProductService productService;

    private LanguageInfoService languageInfoService;

    private TaxClassInfoService taxClassInfoService;

    @Autowired
    public ProductApiImpl(ProductService productService, LanguageInfoService languageInfoService, TaxClassInfoService taxClassInfoService) {
        this.productService = productService;
        this.languageInfoService = languageInfoService;
        this.taxClassInfoService = taxClassInfoService;
    }

    @Override
    public Product getByCode(String productCode, LanguageDTO language) {
        LanguageInfo languageInfo = this.languageInfoService.findbyCode(language.getCode());
        return this.productService.getByCode(productCode, languageInfo);
    }

    @Override
    public Product getProductForLocale(long productId, LanguageDTO language, Locale locale) throws ServiceException {
        LanguageInfo languageInfo = this.languageInfoService.findbyCode(language.getCode());
        return this.productService.getProductForLocale(productId, languageInfo, locale);
    }

    @Override
    public List<Product> listByTaxClass(TaxClassDTO taxClass) {
        TaxClassInfo taxClassInfo = this.taxClassInfoService.findById(taxClass.getId());
        return this.productService.listByTaxClass(taxClassInfo);
    }
}
