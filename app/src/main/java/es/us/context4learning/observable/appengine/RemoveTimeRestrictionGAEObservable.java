package es.us.context4learning.observable.appengine;

import java.io.IOException;

import es.us.context4learning.backend.timeRestrictionApi.TimeRestrictionApi;
import es.us.context4learning.exception.TimeRestrictionNotFoundGAEException;
import rx.Observable;
import rx.Subscriber;

public class RemoveTimeRestrictionGAEObservable implements Observable.OnSubscribe<Void> {

    private static final String TAG = RemoveTimeRestrictionGAEObservable.class.getCanonicalName();

    private TimeRestrictionApi mGaeTimeRestrictionApi;
    private Long mTimeRestrictionId;

    public RemoveTimeRestrictionGAEObservable(TimeRestrictionApi timeRestrictionApi, Long timeRestrictionId) {
        this.mGaeTimeRestrictionApi = timeRestrictionApi;
        this.mTimeRestrictionId = timeRestrictionId;
    }

    @Override
    public void call(Subscriber<? super Void> subscriber) {
        try {
            mGaeTimeRestrictionApi.remove(mTimeRestrictionId).execute();
            subscriber.onNext(null);
            subscriber.onCompleted();
        } catch (IOException e) {
            subscriber.onError(new TimeRestrictionNotFoundGAEException());
        }
    }
}