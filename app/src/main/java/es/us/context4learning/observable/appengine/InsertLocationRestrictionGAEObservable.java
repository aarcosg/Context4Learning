package es.us.context4learning.observable.appengine;

import android.util.Log;

import java.io.IOException;

import es.us.context4learning.backend.locationRestrictionApi.LocationRestrictionApi;
import es.us.context4learning.backend.locationRestrictionApi.model.LocationRestriction;
import es.us.context4learning.exception.LocationRestrictionNotInsertedGAEException;
import rx.Observable;
import rx.Subscriber;

public class InsertLocationRestrictionGAEObservable implements Observable.OnSubscribe<LocationRestriction> {

    private static final String TAG = InsertLocationRestrictionGAEObservable.class.getCanonicalName();

    private LocationRestrictionApi mGaeLocationRestrictionApi;
    private LocationRestriction mGaeLocationRestriction;

    public InsertLocationRestrictionGAEObservable(LocationRestrictionApi timeRestrictionApi, LocationRestriction timeRestriction) {
        this.mGaeLocationRestrictionApi = timeRestrictionApi;
        this.mGaeLocationRestriction = timeRestriction;
    }

    @Override
    public void call(Subscriber<? super LocationRestriction> subscriber) {
        try {
            LocationRestriction newRestriction = mGaeLocationRestrictionApi.insert(mGaeLocationRestriction).execute();
            subscriber.onNext(newRestriction);
            subscriber.onCompleted();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            subscriber.onError(new LocationRestrictionNotInsertedGAEException());
        }
    }
}