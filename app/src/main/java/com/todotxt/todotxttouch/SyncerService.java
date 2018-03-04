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
package com.todotxt.todotxttouch;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

/**
 * Hoster service for the periodic sync service. We need this to spin up the
 * TodoApplication object.
 */
public class SyncerService extends Service {
	private static final long KEEP_ALIVE = 30 * 1000L;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Intent i = new Intent(Constants.INTENT_START_SYNC_WITH_REMOTE);
		i.putExtra(Constants.EXTRA_SUPPRESS_TOAST, true);

		sendBroadcast(i);

		// Shut down after a short delay. This is slightly strange, but we have
		// no way of knowing when
		// the sync has finished so in most cases this will be long enough.
		new Handler(new Handler.Callback() {
			@Override
			public boolean handleMessage(Message msg) {
				stopSelf();
				return true;
			}
		}).sendEmptyMessageDelayed(0, KEEP_ALIVE);

		return Service.START_NOT_STICKY; // If it crashes, don't restart
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
