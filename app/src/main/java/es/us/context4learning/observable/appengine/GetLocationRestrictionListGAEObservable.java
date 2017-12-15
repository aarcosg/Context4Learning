package es.us.context4learning.observable.appengine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import es.us.context4learning.backend.locationRestrictionApi.LocationRestrictionApi;
import es.us.context4learning.backend.locationRestrictionApi.model.LocationRestriction;
import es.us.context4learning.exception.UserNotFoundGAEException;
import rx.Observable;
import rx.Subscriber;

public class GetLocationRestrictionListGAEObservable implements Observable.OnSubscribe<List<LocationRestriction>> {

        private static final String TAG = GetLocationRestrictionListGAEObservable.class.getCanonicalName();

        private LocationRestrictionApi gaeLocationRestrictionApi;
        private Long userId;

        public GetLocationRestrictionListGAEObservable(LocationRestrictionApi gaeLocationRestrictionApi, Long userId) {
            this.gaeLocationRestrictionApi = gaeLocationRestrictionApi;
            this.userId = userId;
        }

        @Override
        public void call(Subscriber<? super List<LocationRestriction>> subscriber) {
            try {
                List<LocationRestriction> locationRestrictionList = gaeLocationRestrictionApi.user(userId).execute().getItems();
                if(locationRestrictionList == null){
                    locationRestrictionList = new ArrayList<>();
                }
                subscriber.onNext(locationRestrictionList);
                subscriber.onCompleted();
            } catch (IOException e) {
                subscriber.onError(new UserNotFoundGAEException());
            }
        }
    }