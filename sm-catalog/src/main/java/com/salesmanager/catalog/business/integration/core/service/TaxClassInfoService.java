package com.salesmanager.catalog.business.integration.core.service;

import com.salesmanager.catalog.business.integration.core.repository.TaxClassInfoRepository;
import com.salesmanager.catalog.model.integration.core.TaxClassInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaxClassInfoService {

    private TaxClassInfoRepository taxClassInfoRepository;

    @Autowired
    public TaxClassInfoService(TaxClassInfoRepository taxClassInfoRepository) {
        this.taxClassInfoRepository = taxClassInfoRepository;
    }

    public TaxClassInfo findById(Long id) {
        return this.taxClassInfoRepository.findById(id);
    }

    public List<TaxClassInfo> listByStore(Integer id) {
        return this.taxClassInfoRepository.listByStore(id);
    }

}
