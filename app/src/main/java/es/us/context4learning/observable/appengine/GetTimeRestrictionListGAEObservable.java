package es.us.context4learning.observable.appengine;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import es.us.context4learning.backend.timeRestrictionApi.TimeRestrictionApi;
import es.us.context4learning.backend.timeRestrictionApi.model.TimeRestriction;
import rx.Observable;
import rx.Subscriber;

public class GetTimeRestrictionListGAEObservable implements Observable.OnSubscribe<List<TimeRestriction>> {

    private static final String TAG = GetTimeRestrictionListGAEObservable.class.getCanonicalName();

    private TimeRestrictionApi gaeTimeRestrictionApi;
    private Long userId;

    public GetTimeRestrictionListGAEObservable(TimeRestrictionApi gaeTimeRestrictionApi, Long userId) {
        this.gaeTimeRestrictionApi = gaeTimeRestrictionApi;
        this.userId = userId;
    }

    @Override
    public void call(Subscriber<? super List<TimeRestriction>> subscriber) {
        try {
            List<TimeRestriction> timeRestrictionList = gaeTimeRestrictionApi.user(userId).execute().getItems();
            if(timeRestrictionList == null){
                timeRestrictionList = new ArrayList<>();
            }
            subscriber.onNext(timeRestrictionList);
            subscriber.onCompleted();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }
}