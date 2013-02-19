import java.util.Timer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * 
 * @author dsuskin
 * 
 */
public class ThrottlerTickTest {

    private Throttler throttler;
    private ThrottlerTick throttlerTick;

    @Before
    public void setUp() {
        throttler = Mockito.mock(Throttler.class);
        throttlerTick = new ThrottlerTick(throttler);
    }

    @After
    public void tearDown() {
        throttler = null;
        throttlerTick = null;
    }

    @Test
    public void shouldAddTokensInBackgroundThread() throws Exception {
        long period = 500;
        int desiredInvocations = 2;
        // sleep for one period fewer than desired invocations because
        // the first invocation happens at time 0
        long testSleepTime = period * (desiredInvocations - 1) + period / 2;
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(throttlerTick, 0, period);
        Thread.sleep(testSleepTime);

        timer.cancel();

        Mockito.verify(throttler, Mockito.times(desiredInvocations)).tick();
    }
}
