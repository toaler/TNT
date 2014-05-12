package tnt.stability.timeout;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

/**
 * {@code TimeBudget} is used to demarcate a series of tasks with a fixed
 * set of time to complete. No additional behavior is incurred when the tasks
 * finish within the allowable time allocated. However tasks taking longer than
 * the allocated time will result in a {@link TimeoutException} being thrown.
 * 
 * {@code TimeBudget} provides a retry mechanism that provide a best effort
 * strategy resolve transient failures.
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
public class TimeBudget<T> {
	private final Callable<T> task;
	private final WaitTime waitTime;
	private final int retries;

	private TimeBudget(Callable<T> task, int retries, WaitTime waitTime) {
		this.waitTime = waitTime;
		this.task = task;
		this.retries = retries;
	}

	/**
	 * {@code TimeBudget} creator
	 * 
	 * @param callable
	 *            - work that must finish within the corresponding
	 *            {@code waitTime}
	 * @param waitTime
	 *            - Allocated time budget for {@code callable}
	 * @return a {@code TimeBudget<T>} instance
	 */
	public static <T> TimeBudget<T> newInstance(Callable<T> callable,
			WaitTime waitTime) {
		return new TimeBudget<T>(callable, 1, waitTime);
	}

	/**
	 * {@code TimeBudget} creator
	 * 
	 * @param callable
	 *            - work that must finish within the corresponding
	 *            {@code waitTime}
	 * @param waitTime
	 *            - Allocated time budget for {@code callable}
	 * @param retries
	 *            - number of retry attempts
	 * @return a {@code TimeBudget<T>} instance
	 */
	public static <T> TimeBudget<T> newInstance(Callable<T> callable,
			WaitTime waitTime, int retries) {
		return new TimeBudget<T>(callable, retries, waitTime);
	}

	/**
	 * Executes {@code callable} for a given time budget. If the budget and
	 * maximum retries are exceeded {@link TimeoutException} is thrown. 
	 * 
	 * @return
	 * @throws Throwable
	 */
	public T execute() throws Throwable {
		ExecutorService executor = Executors.newSingleThreadExecutor();

		T result = null;
		try {
			for (int i = 0; i <= retries; i++) {
				Future<T> future = null;
				try {
					future = executor.submit(task);
					result = future.get((long) waitTime.getWaitTime(),
							waitTime.getUnit());
					break;
				} catch (InterruptedException e) {
					throw e;
				} catch (ExecutionException e) {
					throw e.getCause();
				} catch (TimeoutException e) {
					future.cancel(true);
				}
				waitTime.inc();
			}
		} finally {
			executor.shutdown();
		}

		if (result == null) {
			throw new TimeoutException();
		}

		return result;
	}
}