package es.us.context4learning.observable.appengine;

import java.io.IOException;

import es.us.context4learning.backend.notificationApi.NotificationApi;
import es.us.context4learning.backend.notificationApi.model.Notification;
import es.us.context4learning.exception.NotificationNotFoundGAEException;
import rx.Observable;
import rx.Subscriber;

public class UpdateNotificationGAEObservable implements Observable.OnSubscribe<Notification> {

    private static final String TAG = UpdateNotificationGAEObservable.class.getCanonicalName();

    private NotificationApi mGaeNotificationApi;
    private Long mNotificationId;
    private Notification mGaeNotification;

    public UpdateNotificationGAEObservable(NotificationApi notificationApi, Long notificationId, Notification notification) {
        this.mGaeNotificationApi = notificationApi;
        this.mNotificationId = notificationId;
        this.mGaeNotification = notification;
    }

    @Override
    public void call(Subscriber<? super Notification> subscriber) {
        try {
            Notification updatedNotification = mGaeNotificationApi.update(mNotificationId, mGaeNotification).execute();
            subscriber.onNext(updatedNotification);
            subscriber.onCompleted();
        } catch (IOException e) {
            subscriber.onError(new NotificationNotFoundGAEException());
        }
    }
}