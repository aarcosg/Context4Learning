package es.us.context4learning.observable.appengine;

import java.io.IOException;

import es.us.context4learning.backend.locationRestrictionApi.LocationRestrictionApi;
import es.us.context4learning.exception.LocationRestrictionNotFoundGAEException;
import rx.Observable;
import rx.Subscriber;

public class RemoveLocationRestrictionGAEObservable implements Observable.OnSubscribe<Void> {

    private static final String TAG = RemoveLocationRestrictionGAEObservable.class.getCanonicalName();

    private LocationRestrictionApi mGaeLocationRestrictionApi;
    private Long mLocationRestrictionId;

    public RemoveLocationRestrictionGAEObservable(LocationRestrictionApi locationRestrictionApi, Long locationRestrictionId) {
        this.mGaeLocationRestrictionApi = locationRestrictionApi;
        this.mLocationRestrictionId = locationRestrictionId;
    }

    @Override
    public void call(Subscriber<? super Void> subscriber) {
        try {
            mGaeLocationRestrictionApi.remove(mLocationRestrictionId).execute();
            subscriber.onNext(null);
            subscriber.onCompleted();
        } catch (IOException e) {
            subscriber.onError(new LocationRestrictionNotFoundGAEException());
        }
    }
}