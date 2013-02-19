import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

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

    /**
     * When throttle() is called by multiple threads at the same time, it should
     * not allow more calls to succeed that the bucket has been configured to
     * allow.
     * 
     * @throws Exception
     */
    @Test
    public void shouldDecrementTokensInThreadSafeWay() throws Exception {
        bucket = Mockito.spy(bucket);
        int threadCount = CAPACITY + 1;
        final CountDownLatch latch = new CountDownLatch(threadCount);

        Mockito.doAnswer(new Answer<Boolean>() {
            // synchronizes calling threads on a countdown latch
            // so that they all call #throttle() at the same time
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                latch.countDown();
                latch.await();
                return (Boolean) invocation.callRealMethod();
            }
        }).when(bucket).throttle();

        final AtomicInteger successfulCalls = new AtomicInteger();
        final List<Thread> threads = new ArrayList<Thread>(threadCount);
        for (int i = 0; i < threadCount; i++) {
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    if (bucket.throttle()) {
                        successfulCalls.incrementAndGet();
                    }
                }
            });

            threads.add(thread);
            thread.start();
        }

        for (Thread t : threads) {
            t.join(1000);
        }

        // We expect one success for each token in the bucket.
        // No more, no less.
        assertEquals("Successful calls equals number of available tokens",
                successfulCalls.intValue(), CAPACITY);
    }
}
