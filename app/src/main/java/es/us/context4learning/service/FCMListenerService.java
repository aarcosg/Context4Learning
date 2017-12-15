package es.us.context4learning.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Date;
import java.util.Random;

import es.us.context4learning.Constants;
import es.us.context4learning.MoodleContextApplication;
import es.us.context4learning.R;
import es.us.context4learning.data.api.google.appengine.GAEHelper;
import es.us.context4learning.observable.google.awareness.LocationObservable;
import es.us.context4learning.ui.activity.MainActivity;
import es.us.context4learning.utils.Utils;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class FCMListenerService extends FirebaseMessagingService{

    private static final String TAG = FCMListenerService.class.getCanonicalName();
    private static final String MSG_CONTEXT = "context";
    private static final String MSG_TEXT = "message";
    private static final String MSG_NOTIFICATION_ID = "notification_id";
    private static final int NOTIFICATION_ID = 1;

    private Location mLocation;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "onMessageReceived");
        super.onMessageReceived(remoteMessage);
        if(remoteMessage.getNotification() == null){
            if(remoteMessage.getData().containsKey(MSG_NOTIFICATION_ID)
                    && remoteMessage.getData().containsKey(MSG_TEXT)){
                // Message from GAE with notification id
                // Fetch current location
                fetchLocation();
                Long gaeNotificationId = Long.valueOf(remoteMessage.getData().get(MSG_NOTIFICATION_ID));
                String message = remoteMessage.getData().get(MSG_TEXT);
                showNotification(gaeNotificationId, message);
            }else if(remoteMessage.getData().containsKey(MSG_TEXT)){
                // Message from GAE without notification id
                String message = remoteMessage.getData().get(MSG_TEXT);
                showNotification(message);
            }
        }else{
            //Message from Firebase dashboard
            showNotification(remoteMessage.getNotification().getBody());
        }
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void showNotification(String msg) {
        Intent tasksIntent = new Intent(this, MainActivity.class);
        tasksIntent.putExtra(Constants.EXTRA_NOTIFICATION_ID, NOTIFICATION_ID);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, tasksIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(es.us.context4learning.R.drawable.ic_school)
                        .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher))
                        .setContentTitle(getResources().getString(R.string.moodle_notification_title))
                        .setAutoCancel(true)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        //.setVibrate(new long[]{200, 500})
                        .setLights(0x009900, 300, 1000)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                        .setContentIntent(resultPendingIntent);

        //Track notification received
        GAEHelper.sendAuditEventToServer(
                this,
                Constants.AUDIT_CATEGORY_NOTIFICATION,
                Constants.AUDIT_ACTION_RECEIVED,
                MoodleContextApplication.get(this).getApplicationComponent()
                        .getSharedPreferences().getString(Constants.PROPERTY_USER_NAME, "") + " - " + "FCM message text");
        NotificationManager notificationManager = (NotificationManager)
                this.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    private void fetchLocation(){
        MoodleContextApplication.get(this)
                .getApplicationComponent()
                .getObservableGoogleApiClientAwareness()
                .subscribeOn(Schedulers.immediate())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(googleApiClient ->
                        Observable.create(new LocationObservable(this, googleApiClient))
                                .subscribeOn(Schedulers.immediate())
                                .observeOn(AndroidSchedulers.mainThread())
                ).subscribe(location -> {
                    mLocation = location;
                    Utils.saveLastLocationToPreferences(this, location);
                });
    }

    private void showNotification(Long gaeNotificationId, String message) {
        int notificationId = new Random().nextInt();
        Intent tasksIntent = new Intent(this, MainActivity.class);
        tasksIntent.putExtra(Constants.EXTRA_NOTIFICATION_ID, notificationId);
        tasksIntent.putExtra(Constants.EXTRA_GAE_NOTIFICATION_ID, gaeNotificationId);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, tasksIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_school)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentTitle(getResources().getString(R.string.pending_tasks))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setLights(0x009900, 300, 1000)
                .setAutoCancel(true)
                .setContentIntent(resultPendingIntent);

        Intent hourIntent = new Intent(Constants.ACTION_MUTE_NOTIFICATION_1_HOUR);
        hourIntent.putExtra(Constants.EXTRA_NOTIFICATION_ID, notificationId);
        hourIntent.putExtra(Constants.EXTRA_GAE_NOTIFICATION_ID, gaeNotificationId);
        PendingIntent piHour = PendingIntent.getBroadcast(this,1001,hourIntent,PendingIntent.FLAG_CANCEL_CURRENT);
        notificationBuilder.addAction(R.drawable.ic_snooze_white_24dp, getResources().getString(R.string.hour_1), piHour);

        Intent dayIntent = new Intent(Constants.ACTION_MUTE_NOTIFICATION_1_DAY);
        dayIntent.putExtra(Constants.EXTRA_NOTIFICATION_ID, notificationId);
        dayIntent.putExtra(Constants.EXTRA_GAE_NOTIFICATION_ID, gaeNotificationId);
        if(mLocation != null){
            dayIntent.putExtra(Constants.EXTRA_LOCATION, mLocation);
        }
        PendingIntent piDay = PendingIntent.getBroadcast(this,1002,dayIntent,PendingIntent.FLAG_CANCEL_CURRENT);

        if(mLocation != null){
            notificationBuilder.addAction(R.drawable.ic_place_white_24dp,getString(R.string.day_1_here), piDay);
        } else {
            notificationBuilder.addAction(R.drawable.ic_snooze_white_24dp,getString(R.string.day_1), piDay);
        }

        //Track notification received
        GAEHelper.sendAuditEventToServer(
                this,
                Constants.AUDIT_CATEGORY_NOTIFICATION,
                Constants.AUDIT_ACTION_RECEIVED,
                MoodleContextApplication.get(this).getApplicationComponent()
                        .getSharedPreferences().getString(Constants.PROPERTY_USER_NAME, "") + " - " + "Context message text");

        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, notificationBuilder.build());

        SharedPreferences preferences = Utils.getSharedPreferences(this.getApplicationContext());
        preferences.edit().putLong(Constants.PROPERTY_LAST_NOTIFICATION_TIME, new Date().getTime()).commit();
    }

}
