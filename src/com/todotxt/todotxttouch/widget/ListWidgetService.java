/**
 * This file is part of Todo.txt Touch, an Android app for managing your todo.txt file (http://todotxt.com).
 *
 * Copyright (c) 2009-2013 Todo.txt contributors (http://todotxt.com)
 *
 * LICENSE:
 *
 * Todo.txt Touch is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any
 * later version.
 *
 * Todo.txt Touch is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with Todo.txt Touch.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * @author Todo.txt contributors <todotxt@yahoogroups.com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2013 Todo.txt contributors (http://todotxt.com)
 */
package com.todotxt.todotxttouch.widget;

import java.util.List;

import com.todotxt.todotxttouch.Constants;
import com.todotxt.todotxttouch.R;
import com.todotxt.todotxttouch.TodoApplication;
import com.todotxt.todotxttouch.task.Task;
import com.todotxt.todotxttouch.task.TaskBag;
import com.todotxt.todotxttouch.util.Strings;
import com.todotxt.todotxttouch.util.Util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ListWidgetService extends RemoteViewsService {
	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		return new ListRemoteViewsFactory(this.getApplicationContext(), intent);
	}
}

class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
	private static final String TAG = ListRemoteViewsFactory.class.getName();

	TodoApplication m_app;
	TaskBag taskBag;
	List<Task> tasks;

	public ListRemoteViewsFactory(Context applicationContext, Intent intent) {
		Log.d(TAG, "ListRemoteViewsFactory instantiated");
		m_app = (TodoApplication) applicationContext;
	}

	@Override
	public void onDataSetChanged() {
		taskBag = m_app.getTaskBag();
		tasks = taskBag.getTasks();
	}

	@Override
	public int getCount() {
		return tasks.size();
	}

	@Override
	public long getItemId(int position) {
		return tasks.get(position).getId();
	}

	@Override
	public RemoteViews getLoadingView() {
		return null;
	}

	@Override
	public RemoteViews getViewAt(int position) {
		Task task = tasks.get(position);
		RemoteViews rv = new RemoteViews(m_app.getPackageName(),
				R.layout.listwidget_item);

		SpannableString ss = new SpannableString(task.inScreenFormat());
		Util.setGray(ss, task.getProjects());
		Util.setGray(ss, task.getContexts());
		if (task.isCompleted()) {
			ss.setSpan(new StrikethroughSpan(), 0, ss.length(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		rv.setTextViewText(R.id.listwidget_tasktext, ss);

		rv.setTextViewText(R.id.listwidget_taskprio, task.getPriority()
				.inListFormat());
		int color = R.color.black;
		switch (task.getPriority()) {
		case A:
			color = R.color.green;
			break;
		case B:
			color = R.color.blue;
			break;
		case C:
			color = R.color.orange;
			break;
		case D:
			color = R.color.gold;
			break;
		default:
			color = R.color.black;
		}
		Resources resources = m_app.getResources();
		rv.setTextColor(R.id.listwidget_taskprio, resources.getColor(color));

		if (m_app.m_prefs.getBoolean("todotxtprependdate", false)
				&& !task.isCompleted()
				&& !Strings.isEmptyOrNull(task.getRelativeAge())) {
			rv.setTextViewText(R.id.listwidget_taskage, task.getRelativeAge());
			rv.setViewVisibility(R.id.listwidget_taskage, View.VISIBLE);
		} else {
			rv.setViewVisibility(R.id.listwidget_taskage, View.GONE);
		}

		// Set the click intent so that we can handle it
		final Intent fillInIntent = new Intent();
		fillInIntent.putExtra(Constants.EXTRA_TASK, getItemId(position));
		rv.setOnClickFillInIntent(R.id.listwidget_item, fillInIntent);

		return rv;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub

	}

}