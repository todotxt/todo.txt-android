package com.todotxt.todotxttouch.test;

import java.lang.reflect.Field;

import com.todotxt.todotxttouch.Constants;
import com.todotxt.todotxttouch.TodoApplication;
import com.todotxt.todotxttouch.remote.RemoteClientManager;
import com.todotxt.todotxttouch.task.TaskBag;

import android.content.Intent;
import android.test.ApplicationTestCase;

public class TodoApplicationTest extends ApplicationTestCase<TodoApplication> {

	public TodoApplicationTest() {
		super(TodoApplication.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		createApplication();
		TodoApplication app = getApplication();

		RemoteClientManager mgr = new RemoteClientManagerStub(app);
		try {
			Field mgrField = TodoApplication.class
					.getDeclaredField("remoteClientManager");
			mgrField.setAccessible(true);
			mgrField.set(app, mgr);
		} catch (Exception e) {
			fail(e.toString());
		}

	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	private void setTaskBag(TaskBag bag) {
		TodoApplication app = getApplication();

		try {
			Field bagField = TodoApplication.class.getDeclaredField("taskBag");
			bagField.setAccessible(true);
			bagField.set(app, bag);
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	public class Waiter {
		private static final long sleepInterval = 60;
		private static final long maxWait = 5000;

		public boolean doWait() {
			long elapsed = 0;
			boolean res = false;
			while (!(res = test()) && elapsed < maxWait) {
				try {
					Thread.sleep(sleepInterval);
				} catch (InterruptedException e) {
				}
				elapsed += sleepInterval;
			}
			return res;
		}

		public boolean test() {
			return false;
		}
	}

	public void testPushToRemote() {
		final TaskBagStub bag = new TaskBagStub();
		setTaskBag(bag);

		TodoApplication app = getApplication();
		app.sendBroadcast(new Intent(Constants.INTENT_START_SYNC_TO_REMOTE));

		new Waiter() {
			public boolean test() {
				return bag.pullFromRemoteCalled == 1;
			};
		}.doWait();

		assertEquals("Should have called pushToRemote once", 1,
				bag.pushToRemoteCalled);
		// Remember that pull is called automatically after a successful push
		assertEquals("Should have called pullToRemote once", 1,
				bag.pullFromRemoteCalled);
	}

	public void testPushToRemoteMultiple() {
		final TaskBagStub bag = new TaskBagStub();
		setTaskBag(bag);

		TodoApplication app = getApplication();
		app.sendBroadcast(new Intent(Constants.INTENT_START_SYNC_TO_REMOTE));
		app.sendBroadcast(new Intent(Constants.INTENT_START_SYNC_TO_REMOTE));

		new Waiter() {
			public boolean test() {
				return bag.pullFromRemoteCalled == 1;
			};
		}.doWait();

		assertEquals("Should have called pushToRemote twice", 2,
				bag.pushToRemoteCalled);
		// Remember that pull is called automatically after a successful push
		assertEquals("Should have called pullToRemote twice", 1,
				bag.pullFromRemoteCalled);
	}

	public void testPushToRemoteMultipleDelayed() {
		final TaskBagStub bag = new TaskBagStub() {
			@Override
			public void pushToRemote(boolean overridePreference,
					boolean overwrite) {
				super.pushToRemote(overridePreference, overwrite);
				if (pushToRemoteCalled <= 1) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
					}
				}
			}
		};
		setTaskBag(bag);

		TodoApplication app = getApplication();
		app.sendBroadcast(new Intent(Constants.INTENT_START_SYNC_TO_REMOTE));

		new Waiter() {
			public boolean test() {
				return bag.pushToRemoteCalled == 1;
			};
		}.doWait();

		app.sendBroadcast(new Intent(Constants.INTENT_START_SYNC_TO_REMOTE));
		app.sendBroadcast(new Intent(Constants.INTENT_START_SYNC_TO_REMOTE));
		app.sendBroadcast(new Intent(Constants.INTENT_START_SYNC_TO_REMOTE));
		app.sendBroadcast(new Intent(Constants.INTENT_START_SYNC_TO_REMOTE));
		app.sendBroadcast(new Intent(Constants.INTENT_START_SYNC_TO_REMOTE));

		new Waiter() {
			public boolean test() {
				return bag.pullFromRemoteCalled == 1;
			};
		}.doWait();

		assertEquals("Should have called pushToRemote twice", 2,
				bag.pushToRemoteCalled);
		// Remember that pull is called automatically after a successful push
		assertEquals("Should have called pullToRemote once", 1,
				bag.pullFromRemoteCalled);
	}

	public void testPushToRemoteMultipleDelayedWithPull() {
		final TaskBagStub bag = new TaskBagStub() {
			@Override
			public void pushToRemote(boolean overridePreference,
					boolean overwrite) {
				super.pushToRemote(overridePreference, overwrite);
				if (pushToRemoteCalled <= 1) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
					}
				}
			}
		};
		setTaskBag(bag);

		TodoApplication app = getApplication();
		app.sendBroadcast(new Intent(Constants.INTENT_START_SYNC_TO_REMOTE));

		new Waiter() {
			public boolean test() {
				return bag.pushToRemoteCalled == 1;
			};
		}.doWait();

		app.sendBroadcast(new Intent(Constants.INTENT_START_SYNC_TO_REMOTE));
		app.sendBroadcast(new Intent(Constants.INTENT_START_SYNC_TO_REMOTE));

		// This pull should not be called
		app.sendBroadcast(new Intent(Constants.INTENT_START_SYNC_FROM_REMOTE));

		app.sendBroadcast(new Intent(Constants.INTENT_START_SYNC_TO_REMOTE));
		app.sendBroadcast(new Intent(Constants.INTENT_START_SYNC_TO_REMOTE));
		app.sendBroadcast(new Intent(Constants.INTENT_START_SYNC_TO_REMOTE));

		new Waiter() {
			public boolean test() {
				return bag.pullFromRemoteCalled == 1;
			};
		}.doWait();

		assertEquals("Should have called pushToRemote twice", 2,
				bag.pushToRemoteCalled);
		// Remember that pull is called automatically after a successful push
		assertEquals("Should have called pullToRemote once", 1,
				bag.pullFromRemoteCalled);
	}

	public void testPushToRemoteMultipleDelayedThenPull() {
		final TaskBagStub bag = new TaskBagStub() {
			@Override
			public void pushToRemote(boolean overridePreference,
					boolean overwrite) {
				super.pushToRemote(overridePreference, overwrite);
				if (pushToRemoteCalled <= 1) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
					}
				}
			}
		};
		setTaskBag(bag);

		TodoApplication app = getApplication();
		app.sendBroadcast(new Intent(Constants.INTENT_START_SYNC_TO_REMOTE));

		new Waiter() {
			public boolean test() {
				return bag.pushToRemoteCalled == 1;
			};
		}.doWait();

		app.sendBroadcast(new Intent(Constants.INTENT_START_SYNC_TO_REMOTE));
		app.sendBroadcast(new Intent(Constants.INTENT_START_SYNC_TO_REMOTE));
		app.sendBroadcast(new Intent(Constants.INTENT_START_SYNC_TO_REMOTE));
		app.sendBroadcast(new Intent(Constants.INTENT_START_SYNC_TO_REMOTE));
		app.sendBroadcast(new Intent(Constants.INTENT_START_SYNC_TO_REMOTE));

		new Waiter() {
			public boolean test() {
				return bag.pullFromRemoteCalled == 1;
			};
		}.doWait();

		// This pull should be called
		app.sendBroadcast(new Intent(Constants.INTENT_START_SYNC_FROM_REMOTE));

		new Waiter() {
			public boolean test() {
				return bag.pullFromRemoteCalled == 2;
			};
		}.doWait();

		assertEquals("Should have called pushToRemote twice", 2,
				bag.pushToRemoteCalled);
		// Remember that pull is called automatically after a successful push
		assertEquals("Should have called pullToRemote twice", 2,
				bag.pullFromRemoteCalled);
	}

}
