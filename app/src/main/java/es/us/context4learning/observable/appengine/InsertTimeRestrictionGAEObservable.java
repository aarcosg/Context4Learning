package es.us.context4learning.observable.appengine;

import android.util.Log;

import java.io.IOException;

import es.us.context4learning.backend.timeRestrictionApi.TimeRestrictionApi;
import es.us.context4learning.backend.timeRestrictionApi.model.TimeRestriction;
import es.us.context4learning.exception.TimeRestrictionNotInsertedGAEException;
import rx.Observable;
import rx.Subscriber;

public class InsertTimeRestrictionGAEObservable implements Observable.OnSubscribe<TimeRestriction> {

    private static final String TAG = InsertTimeRestrictionGAEObservable.class.getCanonicalName();

    private TimeRestrictionApi mGaeTimeRestrictionApi;
    private TimeRestriction mGaeTimeRestriction;

    public InsertTimeRestrictionGAEObservable(TimeRestrictionApi timeRestrictionApi, TimeRestriction timeRestriction) {
        this.mGaeTimeRestrictionApi = timeRestrictionApi;
        this.mGaeTimeRestriction = timeRestriction;
    }

    @Override
    public void call(Subscriber<? super TimeRestriction> subscriber) {
        try {
            TimeRestriction newRestriction = mGaeTimeRestrictionApi.insert(mGaeTimeRestriction).execute();
            subscriber.onNext(newRestriction);
            subscriber.onCompleted();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            subscriber.onError(new TimeRestrictionNotInsertedGAEException());
        }
    }
}