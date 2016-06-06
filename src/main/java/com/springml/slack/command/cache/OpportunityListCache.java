package com.springml.slack.command.cache;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;

public class OpportunityListCache {
    @Autowired
    private CacheManager cacheManager;
    private Cache cache;

    @PostConstruct
    public void init() throws Exception {
        cache = cacheManager.getCache(this.getClass().getSimpleName());
    }

    public void add(String teamId, List<String> opprId) {
        cache.put(teamId, opprId);
    }

    @SuppressWarnings("unchecked")
    public List<String> get(String teamId) {
        List<String> opprIds= new ArrayList<>();
        ValueWrapper valueWrapper = cache.get(teamId);
        if (valueWrapper != null) {
            opprIds = (List<String>) valueWrapper.get();
        }

        return opprIds;
    }

    public String get(String teamId, int pos) {
        List<String> opprIds = get(teamId);
        String opprId = null;
        if (pos < opprIds.size()) {
            opprId = opprIds.get(pos);
        }

        return opprId;
    }
}
