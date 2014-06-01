package tnt.stability.timeout;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import junit.framework.TestCase;

public class FaultTolerantExecutorTest extends TestCase {
	public void testSuccessfulUnitOfWork() throws Throwable {
		ExecutorService e = Executors.newSingleThreadExecutor();
		FaultTolerantExecutor<Boolean> executor = FaultTolerantExecutor
				.newInstance(10, TimeUnit.MILLISECONDS, 1, e);

		try {

			assertEquals(Boolean.TRUE,
					executor.execute(new Callable<Boolean>() {

						public Boolean call() throws Exception {
							Thread.sleep(1);
							return true;
						}
					}));

		} finally {
			e.shutdown();
		}
	}

	public void testUnsuccessfulUnitOfWork() throws Throwable {
		ExecutorService e = Executors.newSingleThreadExecutor();
		FaultTolerantExecutor<Boolean> executor = FaultTolerantExecutor
				.newInstance(10, TimeUnit.MILLISECONDS, 1, e);

		try {
			executor.execute(new Callable<Boolean>() {
				public Boolean call() throws Exception {
					Thread.sleep(11);
					return true;
				}
			});
			fail();
		} catch (TimeoutException x) {
			// success
		} finally {
			e.shutdown();
		}
	}

	public void testUnsuccessfulUnitOfWorkWithRetries() throws Throwable {
		ExecutorService e = Executors.newSingleThreadExecutor();
		FaultTolerantExecutor<Boolean> executor = FaultTolerantExecutor
				.newInstance(5, TimeUnit.MILLISECONDS, 1, e);

		try {
			executor.execute(new Callable<Boolean>() {
				public Boolean call() throws Exception {
					Thread.sleep(11);
					return true;
				}
			});
			fail();
		} catch (TimeoutException x) {
			// success
		} finally {
			e.shutdown();
		}
	}

	public void testUnsuccessfulUnitOfWorkWithRetryDelay() throws Throwable {
		ExecutorService e = Executors.newSingleThreadExecutor();
		FaultTolerantExecutor<Boolean> executor = FaultTolerantExecutor
				.newInstance(5, TimeUnit.MILLISECONDS, 1, e);

		try {
			executor.execute(new Callable<Boolean>() {
				public Boolean call() throws Exception {
					Thread.sleep(6);
					return true;
				}
			});
			fail();
		} catch (TimeoutException x) {
			// success
		} finally {
			e.shutdown();
		}
	}

	public void testSuccessfulUnitOfWorkWithRetryExponentialBackoff()
			throws Throwable {

		ExecutorService e = Executors.newSingleThreadExecutor();
		FaultTolerantExecutor<Boolean> executor = FaultTolerantExecutor
				.newInstance(5, TimeUnit.MILLISECONDS, 2, e);

		try {
			assertEquals(Boolean.TRUE,
					executor.execute(new Callable<Boolean>() {
						public Boolean call() throws Exception {
							Thread.sleep(6);
							return true;
						}
					}));
		} finally {
			e.shutdown();
		}
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
		ExecutorService e = Executors.newSingleThreadExecutor();
		FaultTolerantExecutor<Boolean> executor = FaultTolerantExecutor
				.newInstance(5, TimeUnit.MILLISECONDS, 2, 1, e);

		try {

			assertEquals(Boolean.TRUE,
					executor.execute(new RetriableCallable()));

		} finally {
			e.shutdown();
		}
	}

	public void testInterruptedExceptionHandling() {

		ExecutorService e = Executors.newSingleThreadExecutor();
		FaultTolerantExecutor<Boolean> executor = FaultTolerantExecutor
				.newInstance(5, TimeUnit.MILLISECONDS, 1, e);

		try {
			executor.execute(new Callable<Boolean>() {
				public Boolean call() throws Exception {
					throw new InterruptedException(
							"testInterruptedExceptionHandling");
				}
			});
			fail();
		} catch (InterruptedException x) {
			// success
			assertEquals("testInterruptedExceptionHandling", x.getMessage());
		} catch (Exception x) {
			fail();
		} catch (Throwable x) {
			fail();
		} finally {
			e.shutdown();
		}

	}

	@SuppressWarnings("serial")
	public class Foo extends Exception {
	}

	public void testExecutionExceptionHandling() {
		ExecutorService e = Executors.newSingleThreadExecutor();
		FaultTolerantExecutor<Boolean> executor = FaultTolerantExecutor
				.newInstance(5, TimeUnit.MILLISECONDS, 1, e);

		try {
			executor.execute(new Callable<Boolean>() {
				public Boolean call() throws Exception {
					throw new Foo();
				}
			});
			fail();
		} catch (Foo x) {
		} catch (Exception x) {
			fail();
		} finally {
			e.shutdown();
		}
	}
}