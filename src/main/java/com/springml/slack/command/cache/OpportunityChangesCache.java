package com.springml.slack.command.cache;

import static com.springml.slack.util.Constants.*;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.util.StringUtils;

import com.springml.salesforce.wave.api.WaveAPI;
import com.springml.salesforce.wave.impl.WaveAPIImpl;
import com.springml.salesforce.wave.model.QueryResult;
import com.springml.salesforce.wave.model.Results;
import com.springml.salesforce.wave.util.SFConfig;

/**
 * A Cache to load all the OpportunityChanges dataset for
 * 1. Better performance
 * 2. Avoid Wave API limit issue
 * @author sam
 *
 */
public class OpportunityChangesCache {
    private static final Logger LOG = LoggerFactory.getLogger(OpportunityChangesCache.class);

    @Value("${sf.username}")
    private String sfUsername;
    @Value("${sf.password}")
    private String sfPassword;
    @Value("${sf.login.url}")
    private String sfLoginURL;

    @Value("${opportunity.changes.saql}")
    private String saql;
    @Value("${sf.batch.size}")
    private int batchSize;
    @Value("${opportunity.changes.saql.var}")
    private String resultVar;
    @Value("${sf.api.version}")
    private String sfAPIVersion;
    @Autowired
    private CacheManager cacheManager;
    private Cache cache;

    @PostConstruct
    public void init() throws Exception {
        cache = cacheManager.getCache(this.getClass().getSimpleName());
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> get(String teamId, String opprId) throws Exception {
        String key = getKey(teamId, opprId);

        ValueWrapper valueWrapper = cache.get(key);
        if (valueWrapper == null) {
            populateCache(teamId);
            valueWrapper = cache.get(key);
        }

        Map<String, String> value = null;
        if (valueWrapper != null) {
            value = (Map<String, String>) valueWrapper.get();
        }

        return value;
    }

    private String getKey(String teamId, String opprId) {
        StringBuilder key = new StringBuilder(128);
        key.append(teamId).append(STR_UNDERSCORE).append(opprId);
        return key.toString();
    }

    private void populateCache(String teamId) throws Exception {
        LOG.info("SAQL to be executed \n" + saql);
        WaveAPI waveAPI = getWaveAPI(teamId);
        QueryResult result = waveAPI.queryWithPagination(saql, resultVar, batchSize);
        addToCache(teamId, result);
        while (!result.isDone()) {
            result = waveAPI.queryMore(result);
            addToCache(teamId, result);
        }
    }

    private WaveAPI getWaveAPI(String teamId) throws Exception {
        // TODO : Need to get corresponding salesforce Id using the teamId
        // Currently it is configured via application.properties
        SFConfig sfConfig = new SFConfig(sfUsername, sfPassword, sfLoginURL, sfAPIVersion);
        return new WaveAPIImpl(sfConfig);
    }

    private void addToCache(String teamId, QueryResult result) {
        Results results = result.getResults();
        List<Map<String, String>> records = results.getRecords();
        if (CollectionUtils.isNotEmpty(records)) {
            for (Map<String, String> fields : records) {
                String opprId = fields.get(SAQL_FIELD_OPPOR_ID);
                if (!StringUtils.isEmpty(opprId)) {
                    LOG.debug("Adding details for Opportunity " + opprId + " to cache");
                    cache.put(opprId, fields);
                } else {
                    LOG.warn("Found a record without opportunity id in OppurtunityChanges dataset " + fields);
                }
            }
        }
    }
}
