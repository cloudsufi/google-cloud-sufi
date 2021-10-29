package com.google.adapter.configuration.util;

/**
 * Created by sk sanjay on 18th May 2020
 */
public class CacheProperties {

  //************************RFC List Caches
  public static final String RFC_DATA = "rfcListCache";

  //************************RFC Component Caches
  public static final String RFC_COMP_DATA = "rfcCompCache";

  public static final Long RFC_CACHE_EXPIRY = 6000L;
  public static final Integer RFC_INDX_SIZE = 45000;

  public static final Long RFC_HEAP_SIZE = 6000L;

  public static final String RFC_LIST_SUFFIX = "CachingKey_List";
  public static final String RFC_IDX_LIST_SUFFIX = "IndexingKey";
  public static final String RFC_COMP_LIST_SUFFIX = "CachingKey_ComponentList";

}
