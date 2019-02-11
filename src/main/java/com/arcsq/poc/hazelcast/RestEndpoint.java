package com.arcsq.poc.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Slf4j
public class RestEndpoint {

    @Autowired
    private HazelcastInstance hazelcastCache;

    @Autowired
    private CachedTestService cachedTestService;

    @GetMapping("/thread-safe-invocation")
    public String invoke() {
        ILock lock = hazelcastCache.getLock("fst-scheduled-call-lock");
        // Now create a lock and execute some guarded code.
        // In case the lock was already acquired, just exit the function
        if (lock.isLocked() == false) {
            lock.lock();
            try {
                // Performed the core of the scheduled task
                System.out.println("Started...");
                log.info("Calling service that takes 15 seconds...");
                // Thread.sleep is only kept here for testing by running two instances of this project
                // and calling those from different browsers. Actual implementation should remove these
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                // In the actual implementation this will go away as Thread.sleep will be
                e.printStackTrace();
            } finally {
                lock.unlock();
                return "Performed thread safe operation1";
            }
        }
        else {
            return "Another thread is performing this operation";
        }
    }

    @GetMapping("/cached-service")
    public void callCachedService() {
        log.debug("calling cached service...");
        cachedTestService.testMethod("test1");
        cachedTestService.testMethod2("test2");
    }

}
