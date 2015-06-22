package com.dsuskin.jthrottle;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * TODO should ideally reuse all of the Bucket tests
 * 
 * TODO ideally should also have correctness tests which ensure that the
 * throttle rates match desired throttle rates
 * 
 * @author dsuskin
 * 
 */
public class ThrottlerTest {

    private Rule rule;
    private ConcurrentNavigableMap<String, Rule> throttlerRules;
    private Throttler throttler;

    @Before
    public void setUp() {
        rule = new Rule("test", 100, 10);
        throttlerRules = new ConcurrentSkipListMap<String, Rule>();
        throttlerRules.put(rule.getOperationName(), rule);
        throttler = new Throttler(throttlerRules);
    }

    @After
    public void tearDown() {
        rule = null;
        throttlerRules = null;
        throttler = null;
    }

    /**
     * Operations not covered by a rule are throttled.
     */
    @Test
    public void shouldThrottleWhenUnknownOperationIsCalled() {
        assertTrue("Unknown operation should be throttled",
                throttler.throttle("unknown"));
    }

    /**
     * Buckets should start with full tokens. To fully validate this test
     * without examining the internal state of the throttler, we need to use a
     * rule with a capacity N greater than 1, to distinguish between a new
     * bucket starting with a non-zero number of tokens and a new bucket
     * starting with N tokens, where N equals the bucket's token capacity.
     */
    @Test
    public void shouldTreatNewBucketsAsFull() {
        depleteTokensForSingleOperation(rule.getOperationName() + "/foo");
    }

    /**
     * A throttler shouldn't create a new bucket every time throttle is called.
     */
    @Test
    public void shouldReuseBucketsItCreates() {
        depleteTokensForSingleOperation(rule.getOperationName() + "/foo");
    }

    /**
     * We want each operation to have an independent bucket which doesn't share
     * tokens with other buckets.
     */
    @Test
    public void shouldCreateSeparateBucketsBasedOnInput() {
        depleteTokensForSingleOperation(rule.getOperationName() + "/foo");

        depleteTokensForSingleOperation(rule.getOperationName() + "/bar");
    }

    /**
     * Because the throttler creates buckets internally, this test can be
     * validated by checking to make sure a bucket runs out of tokens after
     * throttle() is called enough. However, a more robust validation would
     * check the internal state of the throttler to make sure the bucket is not
     * replaced in this case.
     */
    @Test
    public void shouldNotOverwriteBucketWhenTwoThreadsInitializeIt() {
        depleteTokensForSingleOperation(rule.getOperationName() + "/foo");
    }

    /**
     * Check the throttle status of a single operation, starting with it having
     * all tokens and ending with it having none.
     * 
     * @param operationName
     */
    private void depleteTokensForSingleOperation(String operationName) {
        assertFalse("New operation bucket should not be throttled",
                throttler.throttle(operationName));

        for (int i = 0; i < rule.getTokenBucketCapacity() - 1; i++) {
            assertFalse("Bucket should have more tokens",
                    throttler.throttle(operationName));
        }

        assertTrue("Bucket should be empty", throttler.throttle(operationName));
    }
}
