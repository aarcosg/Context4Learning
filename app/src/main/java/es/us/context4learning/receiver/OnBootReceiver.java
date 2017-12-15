package es.us.context4learning.receiver;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import es.us.context4learning.data.api.google.awareness.AwarenessApiHelper;

public class OnBootReceiver extends WakefulBroadcastReceiver {

    private static final String TAG = OnBootReceiver.class.getCanonicalName();
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive");
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            //Start activity recognition
            /*ActivityRecognitionService.actionStartActivityRecognition(context.getApplicationContext());

            //Fire the alarm in one hour and every hour after that
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent alarmIntent = new Intent(context, ActivityRecognitionService.class);
            intent.setAction(Constants.ACTION_START_ACTIVITY_RECOGNITION);
            PendingIntent alarmPendingIntent = PendingIntent.getService(context, 0, alarmIntent, 0);

            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    AlarmManager.INTERVAL_HOUR,
                    AlarmManager.INTERVAL_HOUR, alarmPendingIntent);*/

            if(ContextCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                AwarenessApiHelper.updateMainAwarenessFence(context, null);
            }else{
                Log.e(TAG, "Access Fine Location permission not granted. Fences not activated");
            }

        }
    }
}
