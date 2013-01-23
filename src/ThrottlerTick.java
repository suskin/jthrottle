import java.util.TimerTask;

/**
 * A {@link Throttler} needs to be poked at a regular interval so that it can
 * update the numbers of tokens in its buckets.
 * 
 * @author dsuskin
 * 
 */
public class ThrottlerTick extends TimerTask {

    /**
     * 
     */
    private final Throttler throttler;

    /**
     * @param throttler
     */
    public ThrottlerTick(Throttler throttler) {
        super();
        this.throttler = throttler;
    }

    /**
     * 
     */
    @Override
    public void run() {
        throttler.tick();
    }
}
