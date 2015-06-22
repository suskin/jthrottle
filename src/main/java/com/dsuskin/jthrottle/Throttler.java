package com.dsuskin.jthrottle;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;

/**
 * We need a way of managing a set of buckets which are created based on a set
 * of throttling rules.
 * 
 * @author dsuskin
 * 
 */
public class Throttler {

    /**
     * 
     */
    private final ConcurrentMap<String, Bucket> buckets;

    /**
     * Technically this could be a {@link NavigableSet}, but with the String
     * keys it makes the rules easier to grab when we're initializing buckets
     * and only have the operation name.
     */
    private final ConcurrentNavigableMap<String, Rule> rules;

    /**
     * 
     * @param rules
     */
    public Throttler(final ConcurrentNavigableMap<String, Rule> rules) {
        super();
        this.buckets = new ConcurrentHashMap<String, Bucket>();
        this.rules = rules;
    }

    /**
     * Should a call to an operation be throttled?
     * 
     * @param operationName
     * @return
     */
    public boolean throttle(final String operationName) {
        if (!buckets.containsKey(operationName)) {
            Bucket newBucket = tryCreateBucket(operationName);

            if (newBucket != null) {
                buckets.putIfAbsent(operationName, newBucket);
            }
        }

        return !buckets.containsKey(operationName)
                || buckets.get(operationName).throttle();
    }

    /**
     * Update the numbers of tokens in the buckets.
     */
    void tick() {
        final Collection<Bucket> bucketsToUpdate = buckets.values();

        if (bucketsToUpdate != null) {
            for (Bucket bucket : bucketsToUpdate) {
                bucket.tick();
            }
        }
    }

    /**
     * We need a new bucket. A bucket for an operation is created from the rule
     * whose operation name is the longest matching prefix of the bucket's
     * operation name.
     * 
     * For example, let there be two rules: one with an operation name of "rule"
     * and another with an operation name of "ruleLong". When a new bucket is
     * created for an operation with a name of "ruleLong/child", it takes the
     * rules from the rule with the operation name of "ruleLong".
     * 
     * @param sourceOperationName
     * @return A new bucket to throttle calls to the given operation, or null if
     *         no matching rule was found.
     */
    private Bucket tryCreateBucket(final String sourceOperationName) {
        Entry<String, Rule> operationRuleEntry = rules
                .floorEntry(sourceOperationName);

        if (operationRuleEntry == null
                || !sourceOperationName.startsWith(operationRuleEntry
                        .getValue().getOperationName())) {
            return null;
        }

        Rule operationRule = operationRuleEntry.getValue();

        return new Bucket(sourceOperationName,
                operationRule.getTokenBucketRefilledTokensPerSecond(),
                operationRule.getTokenBucketCapacity());
    }
}
