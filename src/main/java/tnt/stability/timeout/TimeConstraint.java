package tnt.stability.timeout;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TimeConstraint<T> {
	private final Callable<T> task;
	private final int timeout;
	private final TimeUnit timeoutUnit;
	private final int tries;

	public TimeConstraint(Callable<T> task, int timeout, TimeUnit timeoutUnit,
			int tries) {
		this.task = task;
		this.timeout = timeout;
		this.timeoutUnit = timeoutUnit;
		this.tries = tries;
	}

	public static <T> TimeConstraint<T> newInstance(Callable<T> callable,
			int timeout, TimeUnit timeoutUnit) {
		return new TimeConstraint<T>(callable, timeout, timeoutUnit, 1);
	}

	public static <T> TimeConstraint<T> newInstance(Callable<T> callable,
			int timeout, TimeUnit timeoutUnit, int retries) {
		return new TimeConstraint<T>(callable, timeout, timeoutUnit, retries);
	}

	public T execute() throws Throwable {
		ExecutorService executor = Executors.newSingleThreadExecutor();

		T result = null;
		try {
			for (int i = 0; i < tries; i++) {
				Future<T> future = null;
				try {
					future = executor.submit(task);
					result = future.get(timeout, timeoutUnit);
					break;
				} catch (InterruptedException e) {
					// TODO fix this
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO fix this
					throw e.getCause();
				} catch (TimeoutException e) {
					future.cancel(true);
				}
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
