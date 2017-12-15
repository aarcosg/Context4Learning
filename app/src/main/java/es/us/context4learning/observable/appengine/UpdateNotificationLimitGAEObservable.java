package es.us.context4learning.observable.appengine;

import java.io.IOException;

import es.us.context4learning.backend.limitNotificationApi.LimitNotificationApi;
import es.us.context4learning.backend.limitNotificationApi.model.LimitNotification;
import es.us.context4learning.exception.LimitNotificationNotFoundGAEException;
import rx.Observable;
import rx.Subscriber;

public class UpdateNotificationLimitGAEObservable implements Observable.OnSubscribe<LimitNotification> {

    private static final String TAG = UpdateNotificationLimitGAEObservable.class.getCanonicalName();

    private LimitNotificationApi mGaeLimitNotificationApi;
    private LimitNotification mLimitNotification;

    public UpdateNotificationLimitGAEObservable(LimitNotificationApi limitNotificationApi, LimitNotification limitNotification) {
        this.mGaeLimitNotificationApi = limitNotificationApi;
        this.mLimitNotification = limitNotification;
    }

    @Override
    public void call(Subscriber<? super LimitNotification> subscriber) {
        try {
            LimitNotification updatedLimit = mGaeLimitNotificationApi.insert(mLimitNotification).execute();
            subscriber.onNext(updatedLimit);
            subscriber.onCompleted();
        } catch (IOException e) {
            subscriber.onError(new LimitNotificationNotFoundGAEException());
        }
    }
}