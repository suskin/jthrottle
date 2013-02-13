import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author dsuskin
 * 
 */
public class SlowFillingBucketTest {

    private static final String OPERATION = "test/foo";
    private static final int CAPACITY = 1;
    private static final int REFILL_RATE = 1;

    private Bucket bucket;

    @Before
    public void setUp() {
        bucket = new Bucket(OPERATION, REFILL_RATE, CAPACITY);
    }

    @After
    public void tearDown() {
        bucket = null;
    }

    @Test
    public void bucketShouldFillWhenEachTickIsLessThanOneToken()
            throws Exception {
        TestUtils.useAllTokensFromBucket(bucket);
        assertTrue("Bucket empty", bucket.throttle());
        bucket.tick();

        // make sure enough time has passed for a token to be expected
        for (int i = 0; i < 10; i++) {
            Thread.sleep(200);
            bucket.tick();
        }
        
        assertFalse("Bucket gained tokens", bucket.throttle());
    }
}
