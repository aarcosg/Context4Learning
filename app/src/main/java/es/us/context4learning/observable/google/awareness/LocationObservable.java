package es.us.context4learning.observable.google.awareness;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.common.api.GoogleApiClient;

import rx.Observable;
import rx.Subscriber;

public class LocationObservable implements Observable.OnSubscribe<Location> {

    private static final String TAG = LocationObservable.class.getCanonicalName();

    private final Context mContext;
    private final GoogleApiClient mGoogleApiClient;

    public LocationObservable(Context context, GoogleApiClient googleApiClient){
        this.mContext = context;
        this.mGoogleApiClient = googleApiClient;
    }

    @Override
    public void call(Subscriber<? super Location> subscriber) {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Awareness.SnapshotApi.getLocation(mGoogleApiClient)
                    .setResultCallback(locationResult -> {
                        if (locationResult.getStatus().isSuccess()) {
                            Location location = locationResult.getLocation();
                            Log.d(TAG, "Location !=null: Lat=" + location.getLatitude() + " Lon=" + location.getLongitude());
                            subscriber.onNext(location);
                            subscriber.onCompleted();
                        } else {
                            Log.e(TAG, "Could not get the current location.");
                            subscriber.onCompleted();
                        }
                    });
        }else{
            subscriber.onCompleted();
        }
    }
}
