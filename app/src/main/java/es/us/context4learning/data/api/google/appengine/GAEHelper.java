package es.us.context4learning.data.api.google.appengine;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.fernandocejas.frodo.annotation.RxLogObservable;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.api.client.util.DateTime;

import java.util.Date;

import es.us.context4learning.Constants;
import es.us.context4learning.MoodleContextApplication;
import es.us.context4learning.backend.auditEventApi.AuditEventApi;
import es.us.context4learning.backend.auditEventApi.model.AuditEvent;
import es.us.context4learning.backend.auditEventApi.model.GeoPt;
import es.us.context4learning.backend.auditEventApi.model.User;
import es.us.context4learning.backend.notificationApi.NotificationApi;
import es.us.context4learning.di.components.ApplicationComponent;
import es.us.context4learning.observable.appengine.GetNotificationGAEObservable;
import es.us.context4learning.observable.appengine.InsertAuditGAEObservable;
import es.us.context4learning.observable.appengine.UpdateNotificationGAEObservable;
import es.us.context4learning.utils.RxNetwork;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class GAEHelper {

    private static final String TAG = GAEHelper.class.getCanonicalName();

    @RxLogObservable
    public static void saveNotificationToServer(Context context, Long notificationId, String action) {
        ApplicationComponent applicationComponent = MoodleContextApplication.get(context).getApplicationComponent();
        RxNetwork rxNetwork = applicationComponent.getRxNetwork();
        NotificationApi notificationApi = applicationComponent.getGAENotificationApi();
        rxNetwork.checkInternetConnection()
                .andThen(Observable.create(new GetNotificationGAEObservable(notificationApi,notificationId))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                )
                .flatMap(notification -> {
                    notification.setAction(action);
                    notification.setActionTime(new DateTime(new Date()));
                    return Observable.create(new UpdateNotificationGAEObservable(notificationApi,notificationId,notification))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread());
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        notification -> Log.d(TAG,"Notification updated -> "
                        + "time: " + notification.getActionTime()
                        + " action: " + notification.getAction())
                        , throwable -> Log.e(TAG, "Notification could not be updated"));
    }

    public static void sendAuditEventToServer(Context context, String category, String action, String label){
        ApplicationComponent applicationComponent = MoodleContextApplication.get(context).getApplicationComponent();
        SharedPreferences preferences = applicationComponent.getSharedPreferences();
        RxNetwork rxNetwork = applicationComponent.getRxNetwork();
        AuditEventApi auditEventApi = applicationComponent.getGAEAuditEventApi();
        Tracker tracker = applicationComponent.getGoogleAnalyticsTracker();

        AuditEvent auditEvent = new AuditEvent();

        // Set user
        Long userId = preferences.getLong(Constants.PROPERTY_USER_ID, 0L);
        if(userId > 0L){
            User user = new User();
            user.setId(userId);
            auditEvent.setUser(user);
        }

        // Set activity
        String activity = preferences.getString(Constants.PROPERTY_LAST_ACTIVITY,"");
        if(TextUtils.isEmpty(activity)){
            activity = preferences.getString(Constants.PROPERTY_LAST_CANDIDATE_ACTIVITY, "");
        }
        auditEvent.setActivity(activity);

        // Set location
        Float latitude = preferences.getFloat(Constants.PROPERTY_LAST_LATITUDE, 0L);
        Float longitude = preferences.getFloat(Constants.PROPERTY_LAST_LONGITUDE, 0L);
        GeoPt geoPoint = new GeoPt();
        geoPoint.setLatitude(latitude);
        geoPoint.setLongitude(longitude);
        auditEvent.setLocation(geoPoint);

        // Set category
        auditEvent.setCategory(category);

        // Set action
        auditEvent.setAction(action);

        // Set label
        auditEvent.setLabel(label);

        rxNetwork.checkInternetConnection()
                .andThen(Observable.create(new InsertAuditGAEObservable(auditEventApi, auditEvent))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()))
                .subscribe(
                    audit -> Log.d(TAG, "Audit event " + action + " sent to server."),
                    throwable -> Log.e(TAG, "Audit event " + action + " could not be send to server.")
        );

        // Send Google Analytics Event
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .build());
    }

}
