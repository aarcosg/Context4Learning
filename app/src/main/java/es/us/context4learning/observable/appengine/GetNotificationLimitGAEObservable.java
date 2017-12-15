package es.us.context4learning.observable.appengine;

import android.util.Log;

import java.io.IOException;

import es.us.context4learning.backend.limitNotificationApi.LimitNotificationApi;
import es.us.context4learning.backend.limitNotificationApi.model.LimitNotification;
import es.us.context4learning.exception.UserNotFoundGAEException;
import rx.Observable;
import rx.Subscriber;

public class GetNotificationLimitGAEObservable implements Observable.OnSubscribe<LimitNotification> {

    private static final String TAG = GetNotificationLimitGAEObservable.class.getCanonicalName();

    private LimitNotificationApi mGaeLimitNotificationApi;
    private Long mUserId;

    public GetNotificationLimitGAEObservable(LimitNotificationApi limitNotificationApi, Long userId) {
        this.mGaeLimitNotificationApi = limitNotificationApi;
        this.mUserId = userId;
    }

    @Override
    public void call(Subscriber<? super LimitNotification> subscriber) {
        try {
            LimitNotification limitNotification = mGaeLimitNotificationApi.user(mUserId).execute();
            subscriber.onNext(limitNotification);
            subscriber.onCompleted();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            subscriber.onError(new UserNotFoundGAEException());
        }
    }
}