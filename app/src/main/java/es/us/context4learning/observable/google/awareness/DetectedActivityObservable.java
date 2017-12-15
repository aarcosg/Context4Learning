package es.us.context4learning.observable.google.awareness;

import android.util.Log;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import rx.Observable;
import rx.Subscriber;

public class DetectedActivityObservable implements Observable.OnSubscribe<DetectedActivity> {

    private static final String TAG = DetectedActivityObservable.class.getCanonicalName();

    private final GoogleApiClient mGoogleApiClient;

    public DetectedActivityObservable(GoogleApiClient googleApiClient){
        this.mGoogleApiClient = googleApiClient;
    }

    @Override
    public void call(Subscriber<? super DetectedActivity> subscriber) {
        Awareness.SnapshotApi.getDetectedActivity(mGoogleApiClient)
                .setResultCallback(detectedActivityResult -> {
                    if (detectedActivityResult.getStatus().isSuccess()) {
                        ActivityRecognitionResult ar = detectedActivityResult.getActivityRecognitionResult();
                        DetectedActivity detectedActivity = ar.getMostProbableActivity();
                        subscriber.onNext(detectedActivity);
                        subscriber.onCompleted();
                    }else{
                        Log.e(TAG, "Could not get the current activity.");
                        subscriber.onCompleted();
                    }
                });
    }
}
