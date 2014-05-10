package tnt.stability.timeout;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import junit.framework.TestCase;

public class TimeConstraintTest extends TestCase {

	public void testSuccessfulUnitOfWork() throws Throwable {
		assertEquals(Boolean.TRUE,
				TimeConstraint.newInstance(new Callable<Boolean>() {

					public Boolean call() throws Exception {
						Thread.sleep(1);
						return true;
					}
				}, 10, TimeUnit.MILLISECONDS).execute());
	}

	public void testUnsuccessfulUnitOfWork() throws Throwable {
		try {
			TimeConstraint.newInstance(new Callable<Boolean>() {
				public Boolean call() throws Exception {
					Thread.sleep(11);
					return true;
				}
			}, 10, TimeUnit.MILLISECONDS).execute();
			fail();
		} catch (TimeoutException e) {
			// success
		}
	}
	
	public void testUnsuccessfulUnitOfWorkWithRetries() throws Throwable {
		try {
			TimeConstraint.newInstance(new Callable<Boolean>() {
				public Boolean call() throws Exception {
					Thread.sleep(11);
					return true;
				}
			}, 5, TimeUnit.MILLISECONDS, 1).execute();
			fail();
		} catch (TimeoutException e) {
			// success
		}
	}
	
	public void testSuccessfulUnitOfWorkWithRetries() throws Throwable {
		try {
			TimeConstraint.newInstance(new Callable<Boolean>() {
				public Boolean call() throws Exception {
					Thread.sleep(6);
					return true;
				}
			}, 5, TimeUnit.MILLISECONDS, 1).execute();
			fail();
		} catch (TimeoutException e) {
			// success
		}
	}
}