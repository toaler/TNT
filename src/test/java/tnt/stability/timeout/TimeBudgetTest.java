package tnt.stability.timeout;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import junit.framework.TestCase;

public class TimeBudgetTest extends TestCase {
	public void testSuccessfulUnitOfWork() throws Throwable {
		WaitTime waitTime = WaitTime.newInstance(10, TimeUnit.MILLISECONDS);

		assertEquals(Boolean.TRUE,
				TimeBudget.newInstance(new Callable<Boolean>() {

					public Boolean call() throws Exception {
						Thread.sleep(1);
						return true;
					}
				}, waitTime).execute());
	}

	public void testUnsuccessfulUnitOfWork() throws Throwable {
		try {
			WaitTime waitTime = WaitTime.newInstance(10, TimeUnit.MILLISECONDS);
			TimeBudget.newInstance(new Callable<Boolean>() {
				public Boolean call() throws Exception {
					Thread.sleep(11);
					return true;
				}
			}, waitTime).execute();
			fail();
		} catch (TimeoutException e) {
			// success
		}
	}

	public void testUnsuccessfulUnitOfWorkWithRetries() throws Throwable {
		try {
			WaitTime waitTime = WaitTime.newInstance(5, TimeUnit.MILLISECONDS,
					1);
			TimeBudget.newInstance(new Callable<Boolean>() {
				public Boolean call() throws Exception {
					Thread.sleep(11);
					return true;
				}
			}, waitTime).execute();
			fail();
		} catch (TimeoutException e) {
			// success
		}
	}

	public void testUnsuccessfulUnitOfWorkWithRetryDelay() throws Throwable {
		try {
			WaitTime waitTime = WaitTime.newInstance(5, TimeUnit.MILLISECONDS,
					1);
			TimeBudget.newInstance(new Callable<Boolean>() {
				public Boolean call() throws Exception {
					Thread.sleep(6);
					return true;
				}
			}, waitTime).execute();
			fail();
		} catch (TimeoutException e) {
			// success
		}
	}

	public void testSuccessfulUnitOfWorkWithRetryExponentialBackoff()
			throws Throwable {
		WaitTime waitTime = WaitTime.newInstance(5, TimeUnit.MILLISECONDS, 2);
		assertEquals(Boolean.TRUE,
				TimeBudget.newInstance(new Callable<Boolean>() {
					public Boolean call() throws Exception {
						Thread.sleep(6);
						return true;
					}
				}, waitTime, 1).execute());
	}

	private static class RetriableCallable implements Callable<Boolean> {
		private static boolean first = true;

		public Boolean call() throws Exception {
			if (first) {
				Thread.sleep(6);
				first = false;
			} else {
				Thread.sleep(1);
			}
			return true;
		}

	}

	public void testSuccessfulUnitOfWorkWithRetry() throws Throwable {
		WaitTime waitTime = WaitTime.newInstance(5, TimeUnit.MILLISECONDS, 2);
		assertEquals(Boolean.TRUE,
				TimeBudget
						.newInstance(new RetriableCallable(), waitTime, 1)
						.execute());
	}

	public void testInterruptedExceptionHandling() {
		try {
			WaitTime waitTime = WaitTime.newInstance(5, TimeUnit.MILLISECONDS);
			TimeBudget.newInstance(new Callable<Boolean>() {
				public Boolean call() throws Exception {
					throw new InterruptedException(
							"testInterruptedExceptionHandling");
				}
			}, waitTime).execute();
			fail();
		} catch (InterruptedException e) {
			// success
			assertEquals("testInterruptedExceptionHandling", e.getMessage());
		} catch (Exception e) {
			fail();
		} catch (Throwable e) {
			fail();
		}
	}

	@SuppressWarnings("serial")
	public class Foo extends Exception {
	}

	public void testExecutionExceptionHandling() {
		final Foo foo = new Foo();

		try {
			WaitTime waitTime = WaitTime.newInstance(5, TimeUnit.MILLISECONDS);
			TimeBudget.newInstance(new Callable<Boolean>() {
				public Boolean call() throws Exception {
					throw new ExecutionException(foo);
				}
			}, waitTime).execute();
			fail();
		} catch (ExecutionException e) {
			// success
			assertEquals(foo, e.getCause());
		} catch (Exception e) {
			fail();
		} catch (Throwable e) {
			fail();
		}
	}
}