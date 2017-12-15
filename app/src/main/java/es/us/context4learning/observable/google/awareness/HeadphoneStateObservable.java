package es.us.context4learning.observable.google.awareness;

import android.util.Log;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.state.HeadphoneState;
import com.google.android.gms.common.api.GoogleApiClient;

import rx.Observable;
import rx.Subscriber;

public class HeadphoneStateObservable implements Observable.OnSubscribe<HeadphoneState> {

    private static final String TAG = HeadphoneStateObservable.class.getCanonicalName();

    private final GoogleApiClient mGoogleApiClient;

    public HeadphoneStateObservable(GoogleApiClient googleApiClient){
        this.mGoogleApiClient = googleApiClient;
    }

    @Override
    public void call(Subscriber<? super HeadphoneState> subscriber) {
        Awareness.SnapshotApi.getHeadphoneState(mGoogleApiClient)
                .setResultCallback(headphoneStateResult -> {
                    if (headphoneStateResult.getStatus().isSuccess()) {
                        HeadphoneState headphoneState = headphoneStateResult.getHeadphoneState();
                        subscriber.onNext(headphoneState);
                        subscriber.onCompleted();
                    } else {
                        Log.e(TAG, "Could not get the current headphone state.");
                        subscriber.onCompleted();
                    }
                });
    }
}
