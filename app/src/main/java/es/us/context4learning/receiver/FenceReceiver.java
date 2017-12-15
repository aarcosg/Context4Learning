package es.us.context4learning.receiver;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.text.TextUtils;
import android.util.Log;

import com.cantrowitz.rxbroadcast.RxBroadcast;
import com.google.android.gms.awareness.fence.FenceState;
import com.google.android.gms.awareness.fence.TimeFence;
import com.google.android.gms.awareness.state.HeadphoneState;
import com.google.android.gms.awareness.state.Weather;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.DetectedActivity;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.Date;

import es.us.context4learning.Constants;
import es.us.context4learning.MoodleContextApplication;
import es.us.context4learning.R;
import es.us.context4learning.data.AwarenessContext;
import es.us.context4learning.data.BatteryStatus;
import es.us.context4learning.data.api.google.appengine.GAEHelper;
import es.us.context4learning.data.api.google.awareness.AwarenessApiHelper;
import es.us.context4learning.observable.appengine.InsertContextGAEObservable;
import es.us.context4learning.observable.google.awareness.DetectedActivityObservable;
import es.us.context4learning.observable.google.awareness.HeadphoneStateObservable;
import es.us.context4learning.observable.google.awareness.LocationObservable;
import es.us.context4learning.observable.google.awareness.WeatherObservable;
import es.us.context4learning.utils.Utils;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class FenceReceiver extends BroadcastReceiver {

    private static final String TAG = FenceReceiver.class.getCanonicalName();

    private Context mContext;
    private SharedPreferences mPrefs;
    private GoogleApiClient mGoogleApiClient;
    private DetectedActivity mCurrentActivity;
    private String mCurrentActivityName;
    private Long mDeviceId;
    private CompositeSubscription mSubscriptions = new CompositeSubscription();
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "@onReceive");
        mContext = context.getApplicationContext();
        mPrefs = Utils.getSharedPreferences(mContext);
        mDeviceId = mPrefs.getLong(Constants.PROPERTY_DEVICE_ID,0L);
        mFirebaseRemoteConfig = MoodleContextApplication.get(context).getApplicationComponent().getFirebaseRemoteConfig();
        Observable<GoogleApiClient> googleApiClientObservable =
                MoodleContextApplication.get(mContext).getApplicationComponent().getObservableGoogleApiClientAwareness();
        FenceState fenceState = FenceState.extract(intent);
        if (TextUtils.equals(fenceState.getFenceKey(), AwarenessApiHelper.MAIN_FENCE_KEY)) {
            switch (fenceState.getCurrentState()) {
                case FenceState.TRUE:
                    Log.e(TAG, "Fence triggered");
                    googleApiClientObservable.subscribe(googleApiClient -> {
                        mGoogleApiClient = googleApiClient;
                        handleDetectedActivity();
                        long nowMillis = System.currentTimeMillis();
                        AwarenessApiHelper.updateMainAwarenessFence(mContext,
                                TimeFence.inInterval(
                                        nowMillis + mFirebaseRemoteConfig.getLong(Constants.REMOTE_CONFIG_DETECTION_INTERVAL_IN_MILLIS),
                                        Long.MAX_VALUE));
                    });
                    break;
                case FenceState.FALSE:
                    Log.e(TAG, "Fence not triggered");
                    break;
                case FenceState.UNKNOWN:
                    Log.e(TAG, "UNKNOWN");
                    break;
            }
        }
    }

    private void handleDetectedActivity() {
        mSubscriptions.add(Observable.create(new DetectedActivityObservable(mGoogleApiClient))
                .subscribeOn(Schedulers.immediate())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(detectedActivity -> {
                    Log.d(TAG, detectedActivity.toString());
                    if (detectedActivity.getConfidence() >= Constants.MIN_DETECTED_CONFIDENCE) {
                        mCurrentActivity = detectedActivity;
                        mCurrentActivityName = Constants.getActivityString(mContext, detectedActivity.getType());
                        GAEHelper.sendAuditEventToServer(
                                mContext,
                                Constants.AUDIT_CATEGORY_ACTIVITY,
                                Constants.AUDIT_ACTION_DETECTED,
                                mPrefs.getString(Constants.PROPERTY_USER_NAME, "") + " - " + mCurrentActivityName);
                        String lastActivity = mPrefs.getString(Constants.PROPERTY_LAST_ACTIVITY, "");
                        // Reset properties saved in preferences if the user has been still for a long time
                        long lastNotificationTime = mPrefs.getLong(Constants.PROPERTY_LAST_NOTIFICATION_TIME, 0L);
                        if(mFirebaseRemoteConfig.getBoolean(Constants.REMOTE_CONFIG_STILL_CONTEXT_RESET)
                                && TextUtils.equals(mCurrentActivityName, mContext.getString(R.string.still))
                                && TextUtils.equals(mCurrentActivityName, lastActivity)
                                && (new Date().getTime() - lastNotificationTime)
                                    > mFirebaseRemoteConfig.getLong(Constants.REMOTE_CONFIG_STILL_RESET_INTERVAL_IN_MILLIS)){
                            mPrefs.edit()
                                    .remove(Constants.PROPERTY_LAST_ACTIVITY)
                                    .remove(Constants.PROPERTY_LAST_CANDIDATE_ACTIVITY)
                                    .remove(Constants.PROPERTY_LAST_NOTIFICATION_TIME)
                                    .commit();
                            lastActivity = "";
                        }
                        if (TextUtils.isEmpty(lastActivity) || !TextUtils.equals(mCurrentActivityName, lastActivity)) {
                            String candidateActivity = mPrefs.getString(Constants.PROPERTY_LAST_CANDIDATE_ACTIVITY, "");
                            if (TextUtils.isEmpty(candidateActivity) || !TextUtils.equals(mCurrentActivityName, candidateActivity)) {
                                mPrefs.edit().putString(Constants.PROPERTY_LAST_CANDIDATE_ACTIVITY, mCurrentActivityName).commit();
                            } else if (TextUtils.equals(mCurrentActivityName, candidateActivity)) {
                                fetchCurrentContext();
                            }
                        }
                    }
                })
        );
    }

    private void fetchCurrentContext(){
        Log.d(TAG, "@fetchCurrentContext");
        Observable<Location> locationObservable = Observable.create(new LocationObservable(mContext, mGoogleApiClient));
        Observable<HeadphoneState> headphoneStateObservable = Observable.create(new HeadphoneStateObservable(mGoogleApiClient));
        Observable<Weather> weatherObservable = Observable.create(new WeatherObservable(mContext, mGoogleApiClient));
        Observable<Intent> batteryStatusObservable = RxBroadcast.fromBroadcast(mContext, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        mSubscriptions.add(Observable.zip(
                locationObservable,
                headphoneStateObservable,
                weatherObservable,
                batteryStatusObservable,
                (location, headphoneState, weather, batteryStatusIntent) -> {
                    Utils.saveLastLocationToPreferences(mContext, location);
                    return new AwarenessContext(mCurrentActivity, location, headphoneState, weather, new BatteryStatus(batteryStatusIntent));
                })
                .subscribeOn(Schedulers.immediate())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::sendContextToBackend));
    }

    private void sendContextToBackend(AwarenessContext awarenessContext){
        Log.d(TAG, "@sendContextToBackend");
        if(mDeviceId > 0L && !Utils.isNotificationDisabledOnPrefs(mContext, awarenessContext.getLocation())){
            mSubscriptions.add(Observable.create(
                    new InsertContextGAEObservable(
                            mContext,
                            MoodleContextApplication.get(mContext).getApplicationComponent().getGAEContextApi(),
                            mDeviceId,
                            awarenessContext))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(context -> {
                        if(context != null){
                            mPrefs.edit()
                                    .putString(Constants.PROPERTY_LAST_ACTIVITY, mCurrentActivityName)
                                    .remove(Constants.PROPERTY_LAST_CANDIDATE_ACTIVITY)
                                    .commit();
                            Log.d(TAG,"New context sent to backend: Activity = " + mCurrentActivityName);
                            GAEHelper.sendAuditEventToServer(
                                    mContext,
                                    Constants.AUDIT_CATEGORY_CONTEXT,
                                    Constants.AUDIT_ACTION_DETECTED,
                                    mPrefs.getString(Constants.PROPERTY_USER_NAME, "") + " - " + awarenessContext.toString());
                        }
                    }, throwable -> Log.e(TAG, "Error @InsertContextGAEObservable"))
            );
        }
    }
}
