package tnt.stability.timeout;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import tnt.stability.util.ExceptionUtil;

/**
 * {@code FaultTolerantExecutor} is used to demarcate a series of tasks with a
 * fixed set of time to complete. No additional behavior is incurred when the
 * tasks finish within the allowable time allocated. However tasks taking longer
 * than the allocated time will result in a {@link TimeoutException} being
 * thrown.
 * 
 * {@code FaultTolerantExecutor} provides a retry mechanism that provide a best
 * effort strategy resolve transient failures.
 * 
 * {@link WaitTime} attribute can optionally be specified that provides a wait
 * behavior between retries.
 * 
 * <ul>
 * <li>Synchronous code that depends on a remote system shouldn't block forever.
 * Adding a timeout is a mechanism used to proactively cancel a synchronous unit
 * of work that may not finish in appropriate time. Many blocking third party
 * API's do not provide a timeout</li>
 * 
 * <li>Adding timeouts provide fault isolation</li>
 * 
 * <li>Useful for blocking API's, external system integration and slow requests
 * reducing cascading failures</li>
 * 
 * <li>Provide mechanism to recover from unplanned external issues giving the
 * client a way to deal with this situation</li>
 * </ul>
 * 
 * @author toal
 * 
 * @param <T>
 */
public class FaultTolerantExecutor<T> {
	private final long waitTime;
	private final TimeUnit waitTimeUnit;
	private final int retries;
	private ExecutorService executor;
	private double backoffFactor;

	private FaultTolerantExecutor(int retries, long waitTime,
			TimeUnit waitTimeUnit, double backoffFactor,
			ExecutorService executor) {
		this.backoffFactor = backoffFactor;
		this.waitTime = waitTime;
		this.waitTimeUnit = waitTimeUnit;
		this.retries = retries;
		this.executor = executor;
	}

	/**
	 * {@code FaultTolerantExecutor} creator
	 * 
	 * @param callable
	 *            - work that must finish within the corresponding
	 *            {@code waitTime}
	 * @param waitTime
	 *            - Allocated time budget for {@code callable}
	 * @return a {@code FaultTolerantExecutor<T>} instance
	 */
	public static <T> FaultTolerantExecutor<T> newInstance(long waitTime,
			TimeUnit waitTimeUnit, double backoffFactor,
			ExecutorService executor) {
		return new FaultTolerantExecutor<T>(1, waitTime, waitTimeUnit,
				backoffFactor, executor);
	}

	/**
	 * {@code FaultTolerantExecutor} creator
	 * 
	 * @param callable
	 *            - work that must finish within the corresponding
	 *            {@code waitTime}
	 * @param waitTime
	 *            - Allocated time budget for {@code callable}
	 * @param retries
	 *            - number of retry attempts
	 * @return a {@code FaultTolerantExecutor<T>} instance
	 */
	public static <T> FaultTolerantExecutor<T> newInstance(long waitTime,
			TimeUnit waitTimeUnit, double backoffFactor, int retries,
			ExecutorService executor) {
		return new FaultTolerantExecutor<T>(retries, waitTime, waitTimeUnit,
				backoffFactor, executor);
	}

	/**
	 * Executes {@code callable} for a given time budget. If the budget and
	 * maximum retries are exceeded {@link TimeoutException} is thrown.
	 * 
	 * @return
	 * @throws Throwable
	 */
	public T execute(Callable<T> task) throws Exception {
		long wait = waitTime;
		T result = null;
		for (int i = 0; i <= retries; i++) {
			Future<T> future = null;
			try {
				future = executor.submit(task);
				result = future.get(wait, waitTimeUnit);
				break;
			} catch (InterruptedException e) {
				throw e;
			} catch (ExecutionException e) {
				ExceptionUtil.propagate(e.getCause(), Exception.class);
			} catch (TimeoutException e) {
				future.cancel(true);
			}
			wait = (long) (wait * backoffFactor);
		}

		if (result == null) {
			throw new TimeoutException();
		}

		return result;
	}
}