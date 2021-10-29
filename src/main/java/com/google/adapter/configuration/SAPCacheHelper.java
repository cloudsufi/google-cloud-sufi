package com.google.adapter.configuration;

import com.google.adapter.configuration.util.CacheProperties;
import com.sap.conn.jco.JCoTable;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

/**
 * SAPCacheHelper class
 *
 * @author Sanjay Singh
 */
public final class SAPCacheHelper {

  private static SAPCacheHelper sapCacheHelper;

  private CacheManager cacheManager;
  private Cache<String, List> rfcListCache;
  private Cache<String, List> rfcComponentCache;

  /**
   * SAPCacheHelper Constructor
   */
  private SAPCacheHelper() {
    cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
    cacheManager.init();

    rfcListCache = cacheManager.createCache(CacheProperties.RFC_DATA, CacheConfigurationBuilder
            .newCacheConfigurationBuilder(String.class, List.class, ResourcePoolsBuilder.heap(
                    CacheProperties.RFC_HEAP_SIZE))
            .withExpiry(ExpiryPolicyBuilder
                    .timeToLiveExpiration(Duration.ofSeconds(CacheProperties.RFC_CACHE_EXPIRY))));
    rfcComponentCache = cacheManager
            .createCache(CacheProperties.RFC_COMP_DATA, CacheConfigurationBuilder
                    .newCacheConfigurationBuilder(String.class, List.class, ResourcePoolsBuilder.heap(
                            CacheProperties.RFC_HEAP_SIZE))
                    .withExpiry(ExpiryPolicyBuilder
                            .timeToLiveExpiration(Duration.ofSeconds(CacheProperties.RFC_CACHE_EXPIRY))));
  }

  /**
   * Create and returns singleton instance of SAPCacheHelper
   * @return instance of SAPCacheHelper
   */
  public static SAPCacheHelper getInstance() {

    if (Objects.isNull(sapCacheHelper)) {
      synchronized (SAPCacheHelper.class) {
        if (Objects.isNull(sapCacheHelper)) {
          sapCacheHelper = new SAPCacheHelper();
        }
      }
    }

    return sapCacheHelper;
  }

  /**
   * @return Cache with key and value
   */
  public Cache<String, List> getRfcListCache() {
    return rfcListCache;
  }

  /**
   * @return Cache with key and value
   */
  public Cache<String, List> getRfcListCacheFromCacheManager() {
    return cacheManager.getCache(CacheProperties.RFC_DATA, String.class, List.class);
  }

  /**
   * @return Cache with key and value
   */
  /*public Cache<String, JCoTable> getRfcComponentCache() {
    return rfcComponentCache;
  }*/
  public Cache<String, List> getRfcComponentCache() {
    return rfcComponentCache;
  }


  /**
   * @return Cache with key and value
   */
  public Cache<String, JCoTable> getRfcComponentCacheFromCacheManager() {
    return cacheManager.getCache(CacheProperties.RFC_COMP_DATA, String.class, JCoTable.class);
  }

  public void clearAllCache(){
    this.rfcComponentCache.clear();
    this.rfcListCache.clear();
  }

}
