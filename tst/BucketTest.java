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
public class BucketTest {

    private static final String OPERATION = "test/foo";
    private static final int CAPACITY = 1;
    private static final int REFILL_RATE = 10;

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
    public void shouldBeNonEmptyWhenInitialized() {
        assertFalse("No throttle when initialized", bucket.throttle());
    }

    @Test
    public void shouldThrottleWhenEmpty() {
        TestUtils.useAllTokensFromBucket(bucket);
        assertTrue("Throttle when empty", bucket.throttle());
    }

    @Test
    public void shouldAddTokensOnTick() throws Exception {
        TestUtils.useAllTokensFromBucket(bucket);
        assertTrue("Bucket empty", bucket.throttle());
        bucket.tick();
        Thread.sleep(500);
        bucket.tick();
        assertFalse("Tokens added during tick", bucket.throttle());
    }

    @Test
    public void shouldStopAddingTokensAtCapacity() throws Exception {
        TestUtils.useAllTokensFromBucket(bucket);
        assertTrue("Bucket empty", bucket.throttle());
        bucket.tick();
        Thread.sleep(500);
        bucket.tick();
        TestUtils.useAllTokensFromBucket(bucket);
        assertTrue("Bucket contained number of tokens equaling capacity",
                bucket.throttle());
    }

    @Test
    public void shouldDecrementTokensInThreadSafeWay() {
        throw new UnsupportedOperationException("Test not implemented");
    }
}
