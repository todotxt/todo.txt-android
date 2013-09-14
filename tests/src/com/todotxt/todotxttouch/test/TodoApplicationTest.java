/**
 * This file is part of Todo.txt for Android, an app for managing your todo.txt file (http://todotxt.com).
 *
 * Copyright (c) 2009-2013 Todo.txt for Android contributors (http://todotxt.com)
 *
 * LICENSE:
 *
 * Todo.txt for Android is free software: you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any
 * later version.
 *
 * Todo.txt for Android is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the 
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with Todo.txt for Android. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * Todo.txt for Android's source code is available at https://github.com/ginatrapani/todo.txt-android
 *
 * @author Todo.txt for Android contributors <todotxt@yahoogroups.com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2013 Todo.txt for Android contributors (http://todotxt.com)
 */

package com.todotxt.todotxttouch.test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.os.Handler;
import android.os.Looper;
import android.test.ApplicationTestCase;
import android.util.Log;

import com.todotxt.todotxttouch.TodoApplication;
import com.todotxt.todotxttouch.remote.RemoteClientManager;
import com.todotxt.todotxttouch.task.TaskBag;

public class TodoApplicationTest extends ApplicationTestCase<TodoApplication> {
    LooperThread looper;

    class LooperThread extends Thread {
        public Handler mHandler;

        public void run() {
            Looper.prepare();
            mHandler = new Handler();
            Looper.loop();
        }
    }

    public TodoApplicationTest() {
        super(TodoApplication.class);
        looper = new LooperThread();
        looper.start();
    }

    @Override
    protected void finalize() throws Throwable {
        looper.join(100);
        Handler h = looper.mHandler;
        Looper l = h.getLooper();
        l.quit();
        looper.join();
        super.finalize();
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

    private void setTaskBag(TodoApplication app, TaskBag bag) {
        try {
            Field bagField = TodoApplication.class.getDeclaredField("taskBag");
            bagField.setAccessible(true);
            bagField.set(app, bag);
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    private void startPush(final TodoApplication app) {
        looper.mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Class<?> parameterTypes[] = new Class[3];
                    parameterTypes[0] = boolean.class;
                    parameterTypes[1] = boolean.class;
                    parameterTypes[2] = boolean.class;
                    // pushToRemote(force_sync, overwrite, suppressToast)
                    Method method = TodoApplication.class.getDeclaredMethod(
                            "pushToRemote", parameterTypes);
                    method.setAccessible(true);
                    method.invoke(app, false, false, false);
                } catch (Exception e) {
                    Log.e("TEST", "Failed to run pushToRemote", e);
                    fail(e.toString());
                }
            }
        });
    }

    private void startPull(final TodoApplication app) {
        looper.mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Class<?> parameterTypes[] = new Class[2];
                    parameterTypes[0] = boolean.class;
                    parameterTypes[1] = boolean.class;
                    Method method = TodoApplication.class.getDeclaredMethod(
                            "pullFromRemote", parameterTypes);
                    method.setAccessible(true);
                    method.invoke(app, false, false);
                } catch (Exception e) {
                    Log.e("TEST", "Failed to run pullFromRemote", e);
                    fail(e.toString());
                }
            }
        });
    }

    public class Waiter {
        private static final long sleepInterval = 60;
        private static final long maxWait = 15000;

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
            if (elapsed >= maxWait) {
                Log.e("WAITER", "WAITER ELAPSED!!!");
            }
            return res;
        }

        public boolean test() {
            return false;
        }
    }

    public void testPushToRemote() {
        TodoApplication app = getApplication();
        final TaskBagStub bag = new TaskBagStub();
        setTaskBag(app, bag);

        // app.sendBroadcast(new Intent(Constants.INTENT_START_SYNC_TO_REMOTE));
        startPush(app);

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
        TodoApplication app = getApplication();
        final TaskBagStub bag = new TaskBagStub();
        setTaskBag(app, bag);

        // app.sendBroadcast(new Intent(Constants.INTENT_START_SYNC_TO_REMOTE));
        // app.sendBroadcast(new Intent(Constants.INTENT_START_SYNC_TO_REMOTE));
        startPush(app);
        startPush(app);

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
        TodoApplication app = getApplication();
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
        setTaskBag(app, bag);

        // app.sendBroadcast(new Intent(Constants.INTENT_START_SYNC_TO_REMOTE));
        startPush(app);

        new Waiter() {
            public boolean test() {
                return bag.pushToRemoteCalled == 1;
            };
        }.doWait();

        // app.sendBroadcast(new Intent(Constants.INTENT_START_SYNC_TO_REMOTE));
        // app.sendBroadcast(new Intent(Constants.INTENT_START_SYNC_TO_REMOTE));
        // app.sendBroadcast(new Intent(Constants.INTENT_START_SYNC_TO_REMOTE));
        // app.sendBroadcast(new Intent(Constants.INTENT_START_SYNC_TO_REMOTE));
        // app.sendBroadcast(new Intent(Constants.INTENT_START_SYNC_TO_REMOTE));
        startPush(app);
        startPush(app);
        startPush(app);
        startPush(app);
        startPush(app);

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
        TodoApplication app = getApplication();
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
        setTaskBag(app, bag);

        // app.sendBroadcast(new Intent(Constants.INTENT_START_SYNC_TO_REMOTE));
        startPush(app);

        new Waiter() {
            public boolean test() {
                return bag.pushToRemoteCalled == 1;
            };
        }.doWait();

        // app.sendBroadcast(new Intent(Constants.INTENT_START_SYNC_TO_REMOTE));
        // app.sendBroadcast(new Intent(Constants.INTENT_START_SYNC_TO_REMOTE));
        startPush(app);
        startPush(app);

        // This pull should not be called
        // app.sendBroadcast(new
        // Intent(Constants.INTENT_START_SYNC_FROM_REMOTE));
        startPull(app);

        // app.sendBroadcast(new Intent(Constants.INTENT_START_SYNC_TO_REMOTE));
        // app.sendBroadcast(new Intent(Constants.INTENT_START_SYNC_TO_REMOTE));
        // app.sendBroadcast(new Intent(Constants.INTENT_START_SYNC_TO_REMOTE));
        startPush(app);
        startPush(app);
        startPush(app);

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
        TodoApplication app = getApplication();
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
        setTaskBag(app, bag);

        // app.sendBroadcast(new Intent(Constants.INTENT_START_SYNC_TO_REMOTE));
        startPush(app);

        new Waiter() {
            public boolean test() {
                return bag.pushToRemoteCalled == 1;
            };
        }.doWait();

        // app.sendBroadcast(new Intent(Constants.INTENT_START_SYNC_TO_REMOTE));
        // app.sendBroadcast(new Intent(Constants.INTENT_START_SYNC_TO_REMOTE));
        // app.sendBroadcast(new Intent(Constants.INTENT_START_SYNC_TO_REMOTE));
        // app.sendBroadcast(new Intent(Constants.INTENT_START_SYNC_TO_REMOTE));
        // app.sendBroadcast(new Intent(Constants.INTENT_START_SYNC_TO_REMOTE));
        startPush(app);
        startPush(app);
        startPush(app);
        startPush(app);
        startPush(app);

        new Waiter() {
            public boolean test() {
                return bag.pullFromRemoteCalled == 1;
            };
        }.doWait();

        // This pull should be called
        // app.sendBroadcast(new
        // Intent(Constants.INTENT_START_SYNC_FROM_REMOTE));
        startPull(app);

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
