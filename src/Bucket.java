/**
 * 
 * @author dsuskin
 * 
 */
public class Bucket {

    /**
     * We want a maximum number of divisions of the token refill rate, so we
     * adjust tick times, rounding down to the nearest increment. Also limits
     * the maximum number of ticks per second.
     */
    private static final long NANOS_PER_SECOND = 1000000000L;
    private static final long NANOS_PER_MILLI = 1000000;
    private static final long MILLIS_PER_SECOND = 1000;
    private static final long TICK_SNAP_INCREMENT_NANOS = NANOS_PER_MILLI * 100;
    private static final int MAX_TICKS_PER_SECOND = (int) (NANOS_PER_SECOND / TICK_SNAP_INCREMENT_NANOS);

    private final String operationName;
    private final int refillTokensPerSecond;
    private final int refillTokensPerTickIncrement;
    private final int tokenCapacity;

    private Integer tokenCount;
    private Long lastTickTime;

    /**
     * 
     * @param operationName
     * @param refillTokensPerSecond
     * @param tokenCapacity
     */
    public Bucket(final String operationName, final int refillTokensPerSecond,
            final int tokenCapacity) {
        this.operationName = operationName;
        this.refillTokensPerSecond = refillTokensPerSecond;
        this.tokenCapacity = tokenCapacity;

        tokenCount = tokenCapacity;
        lastTickTime = null;
        refillTokensPerTickIncrement = refillTokensPerSecond
                / MAX_TICKS_PER_SECOND;
    }

    /**
     * Occasionally tokens should be added to the bucket, at the rate to which
     * this bucket is configured.
     */
    public synchronized void tick() {
        long elapsedNanosSinceLastTick = getNanosSinceLastTickSnappedToGrid();

        long elapsedMillisSinceLastTick = elapsedNanosSinceLastTick
                / NANOS_PER_MILLI;
        double tickIncrementsSinceLastTick = elapsedMillisSinceLastTick
                / (double) MILLIS_PER_SECOND * MAX_TICKS_PER_SECOND;

        synchronized (tokenCount) {
            tokenCount += (int) (tickIncrementsSinceLastTick * refillTokensPerTickIncrement);
            tokenCount = Math.min(tokenCapacity, tokenCount);
        }

        synchronized (lastTickTime) {
            lastTickTime += elapsedNanosSinceLastTick;
        }
    }

    /**
     * 
     * @return time elapsed since the last time this was called, rounded down to
     *         the nearest specified increment
     */
    private long getNanosSinceLastTickSnappedToGrid() {
        long currentNanos = System.nanoTime();

        synchronized (lastTickTime) {
            if (lastTickTime == null) {
                lastTickTime = currentNanos;
            }
        }

        long elapsedNanosSinceLastTick = Math.max(0, currentNanos
                - lastTickTime);

        long roundedNanosSinceLastTick = (elapsedNanosSinceLastTick / TICK_SNAP_INCREMENT_NANOS)
                * TICK_SNAP_INCREMENT_NANOS;

        return roundedNanosSinceLastTick;
    }

    /**
     * Calls should only go through successfully while there are tokens left in
     * the bucket.
     * 
     * @return true if the call tracked by this bucket should be throttled,
     *         false otherwise
     */
    public boolean throttle() {
        boolean shouldThrottle;
        synchronized (tokenCount) {
            shouldThrottle = tokenCount == 0;
            tokenCount = Math.max(tokenCount - 1, 0);
        }

        return shouldThrottle;
    }

    public String getOperationName() {
        return operationName;
    }

    public int getRefillTokensPerSecond() {
        return refillTokensPerSecond;
    }

    public int getTokenCapacity() {
        return tokenCapacity;
    }
}