package tnt.stability.timeout;

import java.util.concurrent.TimeUnit;

/**
 * 
 * Value class used to express wait time in {@link TimeUnit} units.
 * 
 * @author toal
 *
 */
public class WaitTime {
	private final double initWaitTime;
	private final TimeUnit unit;
	private final double backoffFactor;
	
	private double waitTime;

	private WaitTime(double waitTime, TimeUnit unit, double backoffFactor) {
		if (waitTime < 0)
			throw new IllegalArgumentException();
		
		this.initWaitTime = waitTime;
		this.waitTime = waitTime;
		this.unit = unit;
		this.backoffFactor = backoffFactor;
	}

	/**
	 * @param waitTime
	 * @param unit
	 * @return instance of {@code WaitTime}
	 */
	public static WaitTime newInstance(double waitTime, TimeUnit unit) {
		return WaitTime.newInstance(waitTime, unit, 1);
	}
	
	/**
	 * @param waitTime
	 * @param unit
	 * @param backoffFactor
	 * @return instance of {@code WaitTime}
	 */
	public static WaitTime newInstance(double waitTime, TimeUnit unit, double backoffFactor) {
		return new WaitTime(waitTime, unit, backoffFactor);
	}

	public double getWaitTime() {
		return waitTime;
	}
	
	public double getWaitTime(TimeUnit targetTimeUnit) {
		return targetTimeUnit.convert((long) waitTime, unit);
	}

	/**
	 * @return updated wait time after the backoff factor was applied.
	 */
	public double inc() {
		return waitTime = waitTime * backoffFactor;
	}

	/**
	 * Sets wait time back to original specification defined at {@code WaitTime} creation time.
	 */
	public void reset() {
		waitTime = initWaitTime;
	}

	/**
	 * @return time unit specified at creation time.
	 */
	public TimeUnit getUnit() {
		return unit;
	}
}
