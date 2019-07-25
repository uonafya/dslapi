package com.healthit.cache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

/**
 *
 * @author duncan
 */
public class DslCache {

    private static Cache dslCache;

    private DslCache() {
    }

    public static Cache getCache() {
        if (dslCache == null) {
            CacheManager cm = CacheManager.newInstance();
            dslCache = (Cache) cm.getCache("dslCache");
        }
        return DslCache.dslCache;
    }
}
