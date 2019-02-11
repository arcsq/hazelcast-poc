package com.arcsq.poc.hazelcast;

import com.hazelcast.cache.impl.HazelcastServerCachingProvider;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import javax.cache.CacheManager;
import javax.cache.configuration.Factory;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;
import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
public class HazelcastConfig {

    private static final String CACHE_ID = "fst.cache.group.id";
    private static final String CACHE_PW = "fst.cache.group.password";
    private static final String CACHE_SPECS = "fst.cache.specs";
    private static final String CACHE_DEFAULT = "fst.default-expiry";

    @Bean("hazelcastCache")
    public HazelcastInstance initializeHazelcast(@Autowired Environment environment) {
        GroupConfig groupConfig = new GroupConfig();
        groupConfig.setName(environment.getProperty(CACHE_ID))
                .setPassword(environment.getProperty(CACHE_PW));

        // Create a member configuration
        Config config = new Config();
        config.setGroupConfig(groupConfig);

        // Start the server instance once per invocation
        log.debug("Starting hazelcast server");
        HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);

        // Initialize cache
        log.debug("Initializing hazelcast cache");
        initCache(instance, environment);

        // Create a client configuration
        ClientConfig clientConfig = new ClientConfig();
        // Initialize the group with name and password
        clientConfig.setGroupConfig(groupConfig);

        // Initialize client once per container
        log.debug("Creating hazelcast client");
        return HazelcastClient.newHazelcastClient(clientConfig);
    }

    /**
     * Method to initialize caches based on cache specs
     * @param instance
     * @param environment
     */
    private void initCache(HazelcastInstance instance, Environment environment) {
        String[] cacheSpecs = StringUtils.tokenizeToStringArray(environment.getProperty(CACHE_SPECS), ",");
        CacheManager cm = HazelcastServerCachingProvider.createCachingProvider(instance)
                .getCacheManager();
        String defaultExpiry = environment.getProperty(CACHE_DEFAULT);

        // Initialize cache with the names provided from environment
        for (String cacheSpec: cacheSpecs) {
            String[] specs = StringUtils.tokenizeToStringArray(cacheSpec, ":");
            Integer expiry = Integer.parseInt(specs.length > 1 ? specs[1] : defaultExpiry);
            Duration duration = new Duration(TimeUnit.MINUTES, expiry);
            Factory<ExpiryPolicy> expiryPolicy = CreatedExpiryPolicy.factoryOf(duration);
            MutableConfiguration cacheConfig = new MutableConfiguration<>().setExpiryPolicyFactory(expiryPolicy);
            cm.createCache(specs[0], cacheConfig);
        }
    }

}
