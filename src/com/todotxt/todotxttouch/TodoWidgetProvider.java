/**
 *
 * Todo.txt Touch/src/com/todotxt/todotxttouch/TodoWidgetProvider.java
 *
 * Copyright (c) 2011 Scott Anderson
 *
 * LICENSE:
 *
 * This file is part of Todo.txt Touch, an Android app for managing your todo.txt file (http://todotxt.com).
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
 * @author Scott Anderson <scotta[at]gmail[dot]com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2011 Scott Anderson
 */
package com.todotxt.todotxttouch;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Resources;
import android.text.SpannableString;
import android.view.View;
import android.widget.RemoteViews;

import com.todotxt.todotxttouch.task.Task;
import com.todotxt.todotxttouch.task.TaskBag;
import com.todotxt.todotxttouch.util.Strings;
import com.todotxt.todotxttouch.util.Util;

public class TodoWidgetProvider extends AppWidgetProvider {

	@Override
	public void onEnabled(Context context) {
		RemoteViews remoteViews = new RemoteViews( context.getPackageName(), R.layout.widget );

		Intent intent = new Intent(context, LoginScreen.class);
		PendingIntent loginScreen = PendingIntent.getActivity(context, 0, intent, 0);
		remoteViews.setOnClickPendingIntent( R.id.widget_launchbutton, loginScreen);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		TodoApplication app = null;
		try {
			// context is a ContextWrapper wrapping TodoApplication
			// This probably isn't guaranteed
			app = (TodoApplication)((ContextWrapper)context).getBaseContext();
		} catch(Exception e) {
			e.printStackTrace();
		}

		if(app == null) {
			DateFormat format = SimpleDateFormat.getTimeInstance( SimpleDateFormat.MEDIUM, Locale.getDefault() );
			for (int i = 0; i < appWidgetIds.length; ++i) {
				RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
				String string = "couldnt get app\nTime = " + format.format( new Date());
				remoteViews.setTextViewText(android.R.id.empty, string);
				remoteViews.setViewVisibility(android.R.id.empty, View.VISIBLE);
				appWidgetManager.updateAppWidget(appWidgetIds[i], remoteViews);
			}
		} else {
			TaskBag taskBag = app.getTaskBag();
			List<Task> tasks = taskBag.getTasks();
			int taskCount = tasks.size();

			for (int i = 0; i < appWidgetIds.length; ++i) {
				RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);

				if(taskCount > 0)
					update(app, remoteViews, R.id.taskid1, R.id.taskprio1, R.id.tasktext1, R.id.taskage1, tasks.get(0));
				if(taskCount > 1)
					update(app, remoteViews, R.id.taskid2, R.id.taskprio2, R.id.tasktext2, R.id.taskage2, tasks.get(1));
				if(taskCount > 2)
					update(app, remoteViews, R.id.taskid3, R.id.taskprio3, R.id.tasktext3, R.id.taskage3, tasks.get(2));
				if(taskCount > 3)
					update(app, remoteViews, R.id.taskid4, R.id.taskprio4, R.id.tasktext4, R.id.taskage4, tasks.get(3));

				remoteViews.setViewVisibility(android.R.id.empty, taskCount == 0 ? View.VISIBLE : View.GONE);

				appWidgetManager.updateAppWidget(appWidgetIds[i], remoteViews);
			}
		}
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	private void update(TodoApplication app, RemoteViews remoteViews, int taskid, int taskprio, int tasktext, int taskage, Task task) {
		// Copied and modified from TodoTxtTouch.TaskAdapter.getView()
		remoteViews.setTextViewText(taskid, String.format("%02d", task.getId() + 1));
		remoteViews.setTextViewText(taskprio, task.getPriority().inListFormat());
		SpannableString ss = new SpannableString(task.inScreenFormat());
		Util.setGray(ss, task.getProjects());
		Util.setGray(ss, task.getContexts());
		remoteViews.setTextViewText(tasktext, ss);

		Resources res = app.getResources();
		remoteViews.setTextColor(tasktext, res.getColor(R.color.black));

		switch (task.getPriority()) {
		case A:
			remoteViews.setTextColor(taskprio, res.getColor(R.color.green));
			break;
		case B:
			remoteViews.setTextColor(taskprio, res.getColor(R.color.blue));
			break;
		case C:
			remoteViews.setTextColor(taskprio, res.getColor(R.color.orange));
			break;
		case D:
			remoteViews.setTextColor(taskprio, res.getColor(R.color.gold));
			break;
		default:
			remoteViews.setTextColor(taskprio, res.getColor(R.color.black));
		}
		/*if (task.isCompleted()) {
			Log.v(TAG, "Striking through " + task.getText());
			holder.tasktext.setPaintFlags(holder.tasktext
					.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
		} else {
			holder.tasktext.setPaintFlags(holder.tasktext
					.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
		}*/

		// hide line numbers unless show preference is checked
		if (!app.m_prefs.getBoolean("showlinenumberspref", false)) {
			remoteViews.setViewVisibility(taskid, View.GONE);
		} else {
			remoteViews.setViewVisibility(taskid, View.VISIBLE);
		}

		if (app.m_prefs.getBoolean("show_task_age_pref", false)) {
			if (!task.isCompleted()
					&& !Strings.isEmptyOrNull(task.getRelativeAge())) {
				remoteViews.setTextViewText(taskage, task.getRelativeAge());
				remoteViews.setViewVisibility(taskage, View.VISIBLE);
			} else {
				remoteViews.setTextViewText(taskage, "");
				remoteViews.setViewVisibility(taskage, View.GONE);
				/*holder.tasktext.setPadding(
						holder.tasktext.getPaddingLeft(),
						holder.tasktext.getPaddingTop(),
						holder.tasktext.getPaddingRight(), 4);*/
			}
		} else {
			/*holder.tasktext.setPadding(
					holder.tasktext.getPaddingLeft(),
					holder.tasktext.getPaddingTop(),
					holder.tasktext.getPaddingRight(), 4);*/
		}
	}
}
