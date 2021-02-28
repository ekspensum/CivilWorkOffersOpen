package pl.aticode.civilworkoffers.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.sf.ehcache.config.CacheConfiguration;

@Configuration
@EnableCaching
public class CacheConfig extends CachingConfigurerSupport {
    
    @Bean
    public net.sf.ehcache.CacheManager ehCacheManager() {

............................
        
        net.sf.ehcache.config.Configuration config = new net.sf.ehcache.config.Configuration();
        config.addCache(allAdminsCache);
        config.addCache(roleForAdminEmployeeCache);
        config.addCache(allEmployeesCache);
        config.addCache(allCustomerTypesCache);
        config.addCache(allCustomersCache);
        config.addCache(mainPagesCache);
        config.addCache(logoSloganFooterCache);
        return net.sf.ehcache.CacheManager.newInstance(config);
    }
    
    @Bean
    @Override
    public CacheManager cacheManager() {
        return new EhCacheCacheManager(ehCacheManager());
    }
    
}
