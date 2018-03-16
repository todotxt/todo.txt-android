/**
 * This file is part of Todo.txt for Android, an app for managing your todo.txt file (http://todotxt.com).
 * <p>
 * Copyright (c) 2009-2013 Todo.txt for Android contributors (http://todotxt.com)
 * <p>
 * LICENSE:
 * <p>
 * Todo.txt for Android is free software: you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any
 * later version.
 * <p>
 * Todo.txt for Android is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with Todo.txt for Android. If not, see
 * <http://www.gnu.org/licenses/>.
 * <p>
 * Todo.txt for Android's source code is available at https://github.com/ginatrapani/todo.txt-android
 *
 * @author Todo.txt for Android contributors <todotxt@yahoogroups.com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2013 Todo.txt for Android contributors (http://todotxt.com)
 */

package com.todotxt.todotxttouch;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Resources;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.todotxt.todotxttouch.task.Task;
import com.todotxt.todotxttouch.task.TaskBag;

import java.util.List;

public class TodoWidgetProvider extends AppWidgetProvider {
    private static final String TAG = TodoWidgetProvider.class.getName();
    private static final int TASKS_TO_DISPLAY = 3;

    private static final int TASK_ID = 0;
    private static final int TASK_PRIO = 1;
    private static final int TASK_TEXT = 2;

    private final int[][] id = {
            {
                    R.id.todoWidget_IdTask1, R.id.todoWidget_PrioTask1,
                    R.id.todoWidget_TextTask1
            },
            {
                    R.id.todoWidget_IdTask2, R.id.todoWidget_PrioTask2,
                    R.id.todoWidget_TextTask2
            },
            {
                    R.id.todoWidget_IdTask3, R.id.todoWidget_PrioTask3,
                    R.id.todoWidget_TextTask3
            }
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        // receive intent and update widget content
        if (Constants.INTENT_WIDGET_UPDATE.equals(intent.getAction())) {
            Log.d(TAG, "Update widget intent received ");

            updateWidgetContent(context, AppWidgetManager.getInstance(context), null, null);
        }
    }

    private void updateWidgetContent(Context context,
                                     AppWidgetManager appWidgetManager, int[] widgetIds,
                                     RemoteViews remoteViews) {

        Log.d(TAG, "Updating TodoWidgetProvider content.");

        // get widget ID's if not provided
        if (widgetIds == null) {
            widgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(
                    context, TodoWidgetProvider.class.getName()));
        }

        // get remoteViews if not provided
        if (remoteViews == null) {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
        }

        // get taskBag from application
        TaskBag taskBag = ((TodoApplication) ((ContextWrapper) context).getBaseContext())
                .getTaskBag();

        List<Task> tasks = taskBag.getTasks();
        int taskCount = tasks.size();
        Resources resources = context.getResources();

        for (int i = 0; i < TASKS_TO_DISPLAY; i++) {
            // get task to display
            if (i >= tasks.size()) {
                // no more tasks to display
                remoteViews.setViewVisibility(id[i][TASK_ID], View.GONE);
                remoteViews.setViewVisibility(id[i][TASK_PRIO], View.GONE);
                remoteViews.setViewVisibility(id[i][TASK_TEXT], View.GONE);

                continue;
            }

            Task task = tasks.get(i);

            if (!task.isCompleted()) { // don't show completed tasks
                // text
                String taskText;

                if (task.inScreenFormat().length() > 33) {
                    taskText = task.inScreenFormat().substring(0, 33) + "...";
                } else {
                    taskText = task.inScreenFormat();
                }

                SpannableString ss = new SpannableString(taskText);
                remoteViews.setTextViewText(id[i][TASK_TEXT], ss);
                remoteViews.setViewVisibility(id[i][TASK_TEXT], View.VISIBLE);

                // priority
                int color = R.color.white;

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
                    default:
                        break;
                }

                remoteViews.setTextViewText(id[i][TASK_PRIO], task.getPriority().inListFormat());
                remoteViews.setTextColor(id[i][TASK_PRIO], resources.getColor(color));
                remoteViews.setViewVisibility(id[i][TASK_PRIO], View.VISIBLE);
            }
        }

        remoteViews.setViewVisibility(R.id.empty, taskCount == 0 ? View.VISIBLE : View.GONE);

        appWidgetManager.updateAppWidget(widgetIds, remoteViews);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);

        Intent intent = new Intent(context, LoginScreen.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        remoteViews.setOnClickPendingIntent(R.id.widget_launchbutton, pendingIntent);
        intent = new Intent(context, AddTask.class);
        pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        remoteViews.setOnClickPendingIntent(R.id.widget_addbutton, pendingIntent);

        updateWidgetContent(context, appWidgetManager, appWidgetIds, remoteViews);
    }
}
