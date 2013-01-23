import java.util.Collection;
import java.util.Collections;
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
        this.rules = (ConcurrentNavigableMap<String, Rule>) Collections
                .unmodifiableMap(rules);
    }

    /**
     * Should a call to an operation be throttled?
     * 
     * @param operationName
     * @return
     */
    public boolean throttle(final String operationName) {
        if (!buckets.containsKey(operationName)) {
            buckets.putIfAbsent(operationName, createBucket(operationName));
        }

        return buckets.get(operationName).throttle();
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
     * We need a new bucket.
     * 
     * @param sourceOperationName
     * @return
     */
    private Bucket createBucket(final String sourceOperationName) {
        Entry<String, Rule> operationRuleEntry = rules
                .floorEntry(sourceOperationName);

        if (operationRuleEntry == null) {
            throw new IllegalArgumentException(
                    "No rule found matching operation '" + sourceOperationName
                            + "'");
        }

        Rule operationRule = operationRuleEntry.getValue();

        return new Bucket(sourceOperationName,
                operationRule.getTokenBucketRefilledTokensPerSecond(),
                operationRule.getTokenBucketCapacity());
    }
}
