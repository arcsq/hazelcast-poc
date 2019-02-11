package com.arcsq.poc.hazelcast;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class CachedTestService {

    @Cacheable(cacheNames = "cache-1")
    public String testMethod(String param) {
        log.info("In service 1");
        return "return " + param;
    }

    @Cacheable(cacheNames = "cache-2")
    public String testMethod2(String param) {
        log.info("In service 2");
        return "return " + param;
    }

}
