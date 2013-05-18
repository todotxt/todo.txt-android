package com.todotxt.todotxttouch;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;


/**
 * Hoster service for the periodic sync service. We need this to spin up the TodoApplication object.
 */
public class SyncerService extends Service {
    
    private static final long KEEP_ALIVE = 30 * 1000L;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Intent i = new Intent(Constants.INTENT_START_SYNC_WITH_REMOTE);
        i.putExtra(Constants.EXTRA_SUPPRESS_TOAST, true);
        sendBroadcast(i);
        // Shut down after a short delay. This is slightly strange, but we have no way of knowing when
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
