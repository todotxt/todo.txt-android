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

package com.todotxt.todotxttouch.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.todotxt.todotxttouch.AddTask;
import com.todotxt.todotxttouch.Constants;
import com.todotxt.todotxttouch.LoginScreen;
import com.todotxt.todotxttouch.R;
import com.todotxt.todotxttouch.TodoApplication;
import com.todotxt.todotxttouch.TodoTxtTouch;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class ListWidgetProvider extends AppWidgetProvider {
    private static final String TAG = ListWidgetProvider.class.getName();
    public static String ITEM_ACTION = "com.todotxt.todotxttouch.widget.ITEM";
    public static String REFRESH_ACTION = "com.todotxt.todotxttouch.widget.REFRESH";

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (action.equals(REFRESH_ACTION)) {
            Log.d(TAG, "Widget Refresh button pressed");

            Intent i = new Intent(Constants.INTENT_START_SYNC_WITH_REMOTE);
            context.sendBroadcast(i);
            Bundle extras = intent.getExtras();

            if (extras != null) {
                int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                RemoteViews rv = buildLayout(context, appWidgetId, true);
                appWidgetManager.partiallyUpdateAppWidget(appWidgetId, rv);
            }
        } else if (action.equals(Constants.INTENT_WIDGET_UPDATE)) {
            Log.d(TAG, "Update widget intent received ");

            int[] appWidgetIds = null;
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            Bundle extras = intent.getExtras();

            if (extras != null) {
                appWidgetIds = extras.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS);
            }

            if (appWidgetIds == null) {
                appWidgetIds = appWidgetManager
                        .getAppWidgetIds(new ComponentName(context,
                                ListWidgetProvider.class.getName()));
            }

            if (appWidgetIds != null && appWidgetIds.length > 0) {
                this.onUpdate(context, appWidgetManager, appWidgetIds);
            }
        }

        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        for (int i = 0; i < appWidgetIds.length; ++i) {
            RemoteViews layout = buildLayout(context, appWidgetIds[i], false);
            appWidgetManager.updateAppWidget(appWidgetIds[i], layout);
        }

        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    private RemoteViews buildLayout(Context context, int appWidgetId, boolean showProgress) {
        RemoteViews rv;
        // Specify the service to provide data for the collection widget. Note
        // that we need to
        // embed the appWidgetId via the data otherwise it will be ignored.
        final Intent intent = new Intent(context, ListWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        rv = new RemoteViews(context.getPackageName(), R.layout.listwidget);
        rv.setRemoteAdapter(R.id.widget_list, intent);

        // Set the empty view to be displayed if the collection is empty. It
        // must be a sibling
        // view of the collection view.
        rv.setEmptyView(R.id.widget_list, R.id.empty_view);

        // Set click listener for the logo
        Intent clickIntent = new Intent(context, LoginScreen.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, clickIntent, 0);
        rv.setOnClickPendingIntent(R.id.listwidget_header, pendingIntent);

        // Set click listener for the 'add' button
        if (isAuthenticated(context)) {
            PendingIntent taskStackBuilderPendingIntent = TaskStackBuilder
                    .create(context)
                    .addNextIntent(new Intent(context, TodoTxtTouch.class))
                    .addNextIntent(new Intent(context, AddTask.class))
                    .getPendingIntent(0, 0);
            rv.setOnClickPendingIntent(R.id.listwidget_additem,
                    taskStackBuilderPendingIntent);
        } else {
            // if not logged in, just go to login screen
            rv.setOnClickPendingIntent(R.id.listwidget_additem, pendingIntent);
        }

        // Bind a click listener template for the contents of the list.
        // Note that we
        // need to update the intent's data if we set an extra, since the extras
        // will be
        // ignored otherwise.
        final Intent onClickIntent = new Intent(context, TodoTxtTouch.class);
        onClickIntent.setAction(ListWidgetProvider.ITEM_ACTION);
        onClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        onClickIntent.setData(Uri.parse(onClickIntent.toUri(Intent.URI_INTENT_SCHEME)));
        final PendingIntent onClickPendingIntent = PendingIntent.getActivity(
                context, 0, onClickIntent, 0);
        rv.setPendingIntentTemplate(R.id.widget_list, onClickPendingIntent);

        // Bind the click intent for the refresh button on the widget
        final Intent refreshIntent = new Intent(context, ListWidgetProvider.class);
        refreshIntent.setAction(ListWidgetProvider.REFRESH_ACTION);
        refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        final PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(
                context, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.listwidget_refresh, refreshPendingIntent);

        if (showProgress) {
            rv.setViewVisibility(R.id.listwidget_progress, View.VISIBLE);
            rv.setViewVisibility(R.id.listwidget_refresh, View.INVISIBLE);
        } else {
            rv.setViewVisibility(R.id.listwidget_progress, View.INVISIBLE);
            rv.setViewVisibility(R.id.listwidget_refresh, View.VISIBLE);
        }

        return rv;
    }

    private boolean isAuthenticated(Context context) {
        TodoApplication app = (TodoApplication) context.getApplicationContext();

        return app.getRemoteClientManager().getRemoteClient().isAuthenticated();
    }
}
