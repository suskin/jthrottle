package com.dsuskin.jthrottle;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This test demonstrates some of the delimiters that can be used
 * to get longest-prefix matching behavior. It also shows the restriction
 * imposed by the current implementation.
 *
 * @author dsuskin
 *
 */
public class ThrottlerLongestPrefixTest {

    private Rule ruleA;
    private Rule ruleB;
    private ConcurrentNavigableMap<String, Rule> throttlerRules;
    private Throttler throttler;

    @Before
    public void setUp() {
        ruleA = new Rule("a", 100, 10);
        ruleB = new Rule("ab", 200, 20);
        throttlerRules = new ConcurrentSkipListMap<String, Rule>();
        throttlerRules.put(ruleA.getOperationName(), ruleA);
        throttlerRules.put(ruleB.getOperationName(), ruleB);
        throttler = new Throttler(throttlerRules);
    }

    @After
    public void tearDown() {
        ruleA = null;
        ruleB = null;
        throttlerRules = null;
        throttler = null;
    }

    @Test
    public void shouldSelectLongestPrefixWhenForwardSlashIsTheDelimiter() {
        String operationName = "a/c";

        depleteTokensForSingleOperation(operationName, ruleA);
    }

    @Test
    public void shouldSelectLongestPrefixWhenHyphenIsTheDelimiter() {
        String operationName = "a-c";

        depleteTokensForSingleOperation(operationName, ruleA);
    }

    /**
     * This is imposed because we don't use a real Trie for rule matching.
     * Fortunately, we can get Trie-like behavior by using delimiters.
     */
    @Test
    public void shouldNotMatchWhenThereIsPrefixMatchIfThereIsNoDelimiter() {
        String operationName = "ac";

        assertTrue("Expected the operation to not match a bucket", throttler.throttle(operationName));
    }

    /**
     * Check the throttle status of a single operation, starting with it having
     * all tokens and ending with it having none.
     * 
     * @param operationName
     */
    private void depleteTokensForSingleOperation(String operationName, Rule ruleOperationInheritedFrom) {
        assertFalse("New operation bucket should not be throttled",
                throttler.throttle(operationName));

        for (int i = 0; i < ruleOperationInheritedFrom.getTokenBucketCapacity() - 1; i++) {
            assertFalse("Bucket should have more tokens",
                    throttler.throttle(operationName));
        }

        assertTrue("Bucket should be empty", throttler.throttle(operationName));
    }
}
