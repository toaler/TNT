package tnt.stability.timeout;

import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

public class WaitTimeTest extends TestCase {
	public void testWaitTimeInvalidArgs() {
		try {
			WaitTime.newInstance(-1, TimeUnit.MILLISECONDS);
			fail();
		} catch (IllegalArgumentException x) {
			// success
		}
	}

	public void testGetWaitTimeUnitConvertion() {
		WaitTime waitTime = WaitTime.newInstance(2, TimeUnit.MILLISECONDS);
		assertEquals(2.0, waitTime.getWaitTime());
		assertEquals(2000.0, waitTime.getWaitTime(TimeUnit.MICROSECONDS));
	}

	public void testGetWaitTimeUnit() {
		WaitTime waitTime = WaitTime.newInstance(2, TimeUnit.MILLISECONDS);
		assertEquals(TimeUnit.MILLISECONDS, waitTime.getUnit());
	}

	public void testGetWaitTime() {
		WaitTime waitTime = WaitTime.newInstance(2, TimeUnit.MILLISECONDS);
		assertEquals(2.0, waitTime.getWaitTime());
		assertEquals(2.0, waitTime.inc());
		assertEquals(2.0, waitTime.getWaitTime());
	}

	public void testInc() {
		WaitTime waitTime = WaitTime.newInstance(2, TimeUnit.MILLISECONDS, 2);
		
		assertEquals(2.0, waitTime.getWaitTime());
		waitTime.inc();
		assertEquals(4.0, waitTime.getWaitTime());
		waitTime.inc();
		assertEquals(8.0, waitTime.getWaitTime());
	}

	public void testReset() {
		WaitTime waitTime = WaitTime.newInstance(2, TimeUnit.MILLISECONDS, 2);
		assertEquals(2.0, waitTime.getWaitTime());
		waitTime.inc();
		assertEquals(4.0, waitTime.getWaitTime());
		waitTime.reset();
		assertEquals(2.0, waitTime.getWaitTime());
	}
}
