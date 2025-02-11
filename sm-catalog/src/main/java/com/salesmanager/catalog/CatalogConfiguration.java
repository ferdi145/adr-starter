package com.salesmanager.catalog;

import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.PersistenceConfigurationBuilder;
import org.infinispan.configuration.cache.SingleFileStoreConfigurationBuilder;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.tree.TreeCache;
import org.infinispan.tree.TreeCacheFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriTemplateHandler;

import javax.persistence.EntityListeners;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.salesmanager.catalog.business.service", "com.salesmanager.catalog.business.util", "com.salesmanager.catalog.business.integration", "com.salesmanager.catalog.api"})
@EnableJpaRepositories(basePackages = {"com.salesmanager.catalog.business.repository","com.salesmanager.catalog.business.integration.core.repository"})
@EntityScan(basePackages = {"com.salesmanager.catalog.model"})
@PropertySource("classpath:catalog.properties")
@EnableCaching
@ImportResource("classpath:/spring/shopizer-catalog-context.xml")
public class CatalogConfiguration {

    @Autowired
    private Environment environment;

    @Autowired
    private DefaultCacheManager defaultCacheManager;

    @Bean
    TreeCache catalogTreeCache() {
        final PersistenceConfigurationBuilder persistConfig = new ConfigurationBuilder().persistence();
        persistConfig.passivation(false);
        final SingleFileStoreConfigurationBuilder fileStore = new SingleFileStoreConfigurationBuilder(persistConfig).location(environment.getProperty("catalog.cms.location"));
        fileStore.invocationBatching().enable();
        fileStore.eviction().maxEntries(15);
        fileStore.eviction().strategy(EvictionStrategy.LRU);
        fileStore.jmxStatistics().disable();
        final org.infinispan.configuration.cache.Configuration config = persistConfig.addStore(fileStore).build();
        config.compatibility().enabled();
        defaultCacheManager.defineConfiguration("CatalogRepository", config);
        final Cache<String, String> cache = defaultCacheManager.getCache("CatalogRepository");
        TreeCacheFactory f = new TreeCacheFactory();
        TreeCache treeCache = f.createTreeCache(cache);
        cache.start();
        return treeCache;
    }

    @Bean
    public CacheManager catalogEhCacheManager() {
        return new EhCacheCacheManager(catalogEhCacheManagerFactory().getObject());
    }

    @Bean
    public EhCacheManagerFactoryBean catalogEhCacheManagerFactory() {
        EhCacheManagerFactoryBean ehCacheManagerFactoryBean = new EhCacheManagerFactoryBean();
        ehCacheManagerFactoryBean.setConfigLocation(new ClassPathResource("spring/ehcache-catalog.xml"));
        return ehCacheManagerFactoryBean;
    }

    @Bean
    public RestTemplate coreRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(100);
        connectionManager.setDefaultMaxPerRoute(6);
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(HttpClients.custom().setConnectionManager(connectionManager).build()));
        String coreUrl = environment.getProperty("core.url");
        ((DefaultUriTemplateHandler) restTemplate.getUriTemplateHandler()).setBaseUrl(coreUrl);
        return restTemplate;
    }
}
