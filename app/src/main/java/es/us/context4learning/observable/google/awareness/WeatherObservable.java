package es.us.context4learning.observable.google.awareness;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.state.Weather;
import com.google.android.gms.common.api.GoogleApiClient;

import rx.Observable;
import rx.Subscriber;

public class WeatherObservable implements Observable.OnSubscribe<Weather> {

    private static final String TAG = WeatherObservable.class.getCanonicalName();

    private final Context mContext;
    private final GoogleApiClient mGoogleApiClient;

    public WeatherObservable(Context context, GoogleApiClient googleApiClient){
        this.mContext = context;
        this.mGoogleApiClient = googleApiClient;
    }

    @Override
    public void call(Subscriber<? super Weather> subscriber) {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Awareness.SnapshotApi.getWeather(mGoogleApiClient)
                    .setResultCallback(weatherResult -> {
                        if (weatherResult.getStatus().isSuccess()) {
                            Weather weather = weatherResult.getWeather();
                            subscriber.onNext(weather);
                            subscriber.onCompleted();
                        } else {
                            Log.e(TAG, "Could not get the current weather.");
                            subscriber.onCompleted();
                        }
                    });
        }else{
            subscriber.onCompleted();
        }
    }
}
