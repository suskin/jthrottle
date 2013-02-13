import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.List;

/**
 * Convenient utilities for generating data and whatnot.
 * 
 * @author dsuskin
 * 
 */
public class TestUtils {

    public static List<Rule> generateRules(int howMany) {
        List<Rule> rules = new ArrayList<Rule>(howMany);
    
        for (int i = 0; i < howMany; i++) {
            String operationName = "foo/test" + i;
            int refillRate = i;
            int capacity = i;
            rules.add(new Rule(operationName, refillRate, capacity));
        }
    
        return rules;
    }

    static void useAllTokensFromBucket(Bucket bucket) {
        for (int i = 0; i < bucket.getTokenCapacity(); i++) {
            assertFalse("Emptying bucket; should still have tokens",
                    bucket.throttle());
        }
    }

}
