package com.todotxt.todotxttouch;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PeriodicSyncStarter extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            setupPeriodicSyncer(context);
        }
    }

    public static void setupPeriodicSyncer(Context context) {

        TodoApplication a = (TodoApplication) context.getApplicationContext();
        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, SyncerService.class);
        PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        alarms.cancel(pi); // Cancel any previously started
        long syncPeriod = a.getSyncPeriod();
        if (syncPeriod > 0) {
            // Wake up and synchronise after after inexact fixed delay
            alarms.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 0, syncPeriod, pi);
//            alarms.setRepeating(AlarmManager.ELAPSED_REALTIME, 0, 60 * 1000, pi); // for testing
        }
    }

}
