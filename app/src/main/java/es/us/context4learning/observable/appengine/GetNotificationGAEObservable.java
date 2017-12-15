package es.us.context4learning.observable.appengine;

import java.io.IOException;

import es.us.context4learning.backend.notificationApi.NotificationApi;
import es.us.context4learning.backend.notificationApi.model.Notification;
import es.us.context4learning.exception.NotificationNotFoundGAEException;
import rx.Observable;
import rx.Subscriber;

public class GetNotificationGAEObservable implements Observable.OnSubscribe<Notification> {

    private static final String TAG = GetNotificationGAEObservable.class.getCanonicalName();

    private NotificationApi mGaeNotificationApi;
    private Long mNotificationId;

    public GetNotificationGAEObservable(NotificationApi notificationApi, Long notificationId) {
        this.mGaeNotificationApi = notificationApi;
        this.mNotificationId = notificationId;
    }

    @Override
    public void call(Subscriber<? super Notification> subscriber) {
        try {
            Notification notification = mGaeNotificationApi.get(mNotificationId).execute();
            subscriber.onNext(notification);
            subscriber.onCompleted();
        } catch (IOException e) {
            subscriber.onError(new NotificationNotFoundGAEException());
        }
    }
}