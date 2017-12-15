package es.us.context4learning.data.api.google.awareness;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.util.Log;

import com.fernandocejas.frodo.annotation.RxLogObservable;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.DetectedActivityFence;
import com.google.android.gms.awareness.fence.FenceQueryRequest;
import com.google.android.gms.awareness.fence.FenceState;
import com.google.android.gms.awareness.fence.FenceStateMap;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.awareness.fence.LocationFence;
import com.google.android.gms.awareness.fence.TimeFence;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.DetectedActivity;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import es.us.context4learning.Constants;
import es.us.context4learning.MoodleContextApplication;
import es.us.context4learning.R;
import es.us.context4learning.backend.locationRestrictionApi.LocationRestrictionApi;
import es.us.context4learning.backend.locationRestrictionApi.model.LocationRestriction;
import es.us.context4learning.backend.timeRestrictionApi.TimeRestrictionApi;
import es.us.context4learning.backend.timeRestrictionApi.model.TimeRestriction;
import es.us.context4learning.exception.InternetNotAvailableException;
import es.us.context4learning.observable.appengine.GetLocationRestrictionListGAEObservable;
import es.us.context4learning.observable.appengine.GetTimeRestrictionListGAEObservable;
import es.us.context4learning.receiver.FenceReceiver;
import es.us.context4learning.utils.RxNetwork;
import es.us.context4learning.utils.Utils;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AwarenessApiHelper {

    private static final String TAG = AwarenessApiHelper.class.getCanonicalName();
    private static final String ACTION_NEW_FENCE_STATE = "es.us.context4learning.ACTION_NEW_FENCE_STATE";
    private static final int REQUEST_CODE_FENCE_STATE = 1;
    public static final String MAIN_FENCE_KEY = "awarenessMainFenceKey";

    public static AwarenessFence getDetectedActivityFences(){
        //Prueba 2
        /*return DetectedActivityFence.starting(
                DetectedActivityFence.STILL
                ,DetectedActivityFence.IN_VEHICLE
                ,DetectedActivityFence.ON_FOOT
                ,DetectedActivityFence.WALKING
                ,DetectedActivityFence.RUNNING
        );*/
        //Prueba 1
        /*List<AwarenessFence> detectedActivityFences = Arrays.asList(
                DetectedActivityFence.starting(DetectedActivityFence.STILL)
                , DetectedActivityFence.starting(DetectedActivityFence.IN_VEHICLE)
                , DetectedActivityFence.starting(DetectedActivityFence.ON_FOOT)
                , DetectedActivityFence.starting(DetectedActivityFence.WALKING)
                , DetectedActivityFence.starting(DetectedActivityFence.RUNNING)
        );
        return AwarenessFence.or(detectedActivityFences);*/
        //Prueba 3
        /*List<AwarenessFence> detectedActivityFences = Arrays.asList(
                AwarenessFence.not(DetectedActivityFence.starting(DetectedActivityFence.ON_BICYCLE))
                , AwarenessFence.not(DetectedActivityFence.starting(DetectedActivityFence.TILTING))
                , AwarenessFence.not(DetectedActivityFence.starting(DetectedActivityFence.UNKNOWN))
        );
        return AwarenessFence.and(detectedActivityFences);*/

        //Prueba 4
        return DetectedActivityFence.during(
                DetectedActivity.STILL,
                DetectedActivity.IN_VEHICLE,
                DetectedActivityFence.ON_FOOT,
                DetectedActivityFence.WALKING,
                DetectedActivityFence.RUNNING);

    }

    public static void registerFence(final Context context, final GoogleApiClient googleApiClient,
                                     final String fenceKey, final AwarenessFence fence) {
        Log.i(TAG,"@registerFence");
        FirebaseRemoteConfig firebaseRemoteConfig
                = MoodleContextApplication.get(context).getApplicationComponent().getFirebaseRemoteConfig();
        if(firebaseRemoteConfig.getBoolean(Constants.REMOTE_CONFIG_CONTEXT_RECOGNITION)){
            Log.d(TAG, "Context recognition enabled in remote config");
            Awareness.FenceApi.updateFences(
                    googleApiClient,
                    new FenceUpdateRequest.Builder()
                            .addFence(fenceKey, fence, getPendingIntent(context))
                            .build())
                    .setResultCallback(status -> {
                        if(status.isSuccess()) {
                            Log.i(TAG, "Fence was successfully registered.");
                            queryFence(googleApiClient, fenceKey);
                        } else {
                            Log.e(TAG, "Fence could not be registered: " + status);
                        }
                    });
        }else{
            Log.d(TAG, "Context recognition disabled in remote config");
        }
    }

    public static void unregisterFence(final GoogleApiClient googleApiClient, final String fenceKey) {
        Awareness.FenceApi.updateFences(
                googleApiClient,
                new FenceUpdateRequest.Builder()
                        .removeFence(fenceKey)
                        .build()).setResultCallback(new ResultCallbacks<Status>() {
                            @Override
                            public void onSuccess(@NonNull Status status) {
                                Log.i(TAG, "Fence " + fenceKey + " successfully removed.");
                            }

                            @Override
                            public void onFailure(@NonNull Status status) {
                                Log.i(TAG, "Fence " + fenceKey + " could NOT be removed.");
                            }
                        });
    }

    private static void queryFence(final GoogleApiClient googleApiClient, final String fenceKey) {
        Awareness.FenceApi.queryFences(googleApiClient,
                FenceQueryRequest.forFences(fenceKey))
                .setResultCallback(fenceQueryResult -> {
                    if (!fenceQueryResult.getStatus().isSuccess()) {
                        Log.e(TAG, "Could not query fence: " + fenceKey);
                        return;
                    }
                    FenceStateMap map = fenceQueryResult.getFenceStateMap();
                    for (String fenceKey1 : map.getFenceKeys()) {
                        FenceState fenceState = map.getFenceState(fenceKey1);
                        Log.i(TAG, "Fence " + fenceKey1 + ": "
                                + fenceState.getCurrentState()
                                + ", was="
                                + fenceState.getPreviousState()
                                + ", lastUpdateTime="
                                + new Date(fenceState.getLastFenceUpdateTimeMillis()));
                    }
                });
    }

    private static PendingIntent getPendingIntent(Context context){
        Intent fenceIntent = new Intent(context.getApplicationContext(),FenceReceiver.class);
        fenceIntent.setAction(ACTION_NEW_FENCE_STATE);
        return PendingIntent.getBroadcast(context,REQUEST_CODE_FENCE_STATE,fenceIntent,0);
    }

    @RxLogObservable
    public static Observable<AwarenessFence> getMainAwarenessFenceObservable(Context context) {
        RxNetwork rxNetwork = MoodleContextApplication.get(context).getApplicationComponent().getRxNetwork();
        LocationRestrictionApi locationRestrictionApi = MoodleContextApplication.get(context)
                .getApplicationComponent().getGAELocationRestrictionApi();
        TimeRestrictionApi timeRestrictionApi = MoodleContextApplication.get(context)
                .getApplicationComponent().getGAETimeRestrictionApi();
        SharedPreferences prefs = MoodleContextApplication.get(context)
                .getApplicationComponent().getSharedPreferences();
        Long userId = prefs.getLong(Constants.PROPERTY_USER_ID,0L);
        if(userId > 0L){
            AwarenessFence activitiesFence = getDetectedActivityFences();
            return rxNetwork.checkInternetConnection()
                    .andThen(Observable.zip(
                    Observable.create(new GetLocationRestrictionListGAEObservable(locationRestrictionApi, userId))
                    , Observable.create(new GetTimeRestrictionListGAEObservable(timeRestrictionApi, userId))
                    , Pair::new)
                    .flatMap(restrictions -> {
                        List<AwarenessFence> locationFenceList = getLocationFencesFromRestrictions(restrictions.first);
                        List<AwarenessFence> timeFenceList = getTimeFencesFromRestrictions(restrictions.second);
                        AwarenessFence mainFence = activitiesFence;
                        if(!locationFenceList.isEmpty()){
                            AwarenessFence locationFence = AwarenessFence.and(locationFenceList);
                            mainFence = AwarenessFence.and(mainFence,locationFence);
                        }
                        if(!timeFenceList.isEmpty()){
                            AwarenessFence timeFence = AwarenessFence.and(timeFenceList);
                            mainFence = AwarenessFence.and(mainFence,timeFence);
                        }
                        return Observable.just(mainFence);
                    })
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    );
        }else{
            return Observable.empty();
        }
    }

    public static void updateMainAwarenessFence(Context context, @Nullable AwarenessFence timeFence){
        Observable.combineLatest(
                AwarenessApiHelper.getMainAwarenessFenceObservable(context)
                , MoodleContextApplication.get(context).getApplicationComponent().getObservableGoogleApiClientAwareness()
                , Pair::new)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        pair -> AwarenessApiHelper.registerFence(
                                context,
                                pair.second,
                                AwarenessApiHelper.MAIN_FENCE_KEY,
                                timeFence != null ? AwarenessFence.and(pair.first, timeFence) : pair.first
                        )
                        , throwable -> {
                                if(throwable instanceof InternetNotAvailableException){
                                    Utils.showToast(context, context.getString(R.string.exception_message_no_connection));
                                }else{
                                    Log.e(TAG, throwable.getClass() + " @updateMainAwarenessFence");
                                }
                        }
                );
    }

    private static List<AwarenessFence> getLocationFencesFromRestrictions(List<LocationRestriction> locationRestrictions){
        List<AwarenessFence> locationFences = new ArrayList<AwarenessFence>();
        try{
            for(LocationRestriction locationRestriction : locationRestrictions){
                locationFences.add(AwarenessFence.not(
                        LocationFence.in(
                                locationRestriction.getLocation().getLatitude()
                                ,locationRestriction.getLocation().getLongitude()
                                ,Constants.DEFAULT_RADIUS_RESTRICTION
                                ,0)));
            }
        }catch (SecurityException se){
            Log.e(TAG,"Access Fine Location permission needed");
        }
        return locationFences;
    }

    private static List<AwarenessFence> getTimeFencesFromRestrictions(List<TimeRestriction> timeRestrictions){
        List<AwarenessFence> timeFences = new ArrayList<AwarenessFence>();
        if(timeRestrictions != null && !timeRestrictions.isEmpty()){

            Calendar midnight = Calendar.getInstance(TimeZone.getDefault());
            midnight.set(Calendar.HOUR_OF_DAY, 0);
            midnight.set(Calendar.MINUTE, 0);
            midnight.set(Calendar.SECOND, 0);
            midnight.set(Calendar.MILLISECOND, 0);

            Calendar restrictionStart = Calendar.getInstance(TimeZone.getDefault());
            Calendar restrictionEnd = Calendar.getInstance(TimeZone.getDefault());

            for(TimeRestriction timeRestriction : timeRestrictions){

                restrictionStart.setTimeInMillis(timeRestriction.getStartTime().getValue());
                restrictionStart.set(Calendar.DAY_OF_MONTH, midnight.get(Calendar.DAY_OF_MONTH));
                restrictionStart.set(Calendar.MONTH, midnight.get(Calendar.MONTH));
                restrictionStart.set(Calendar.YEAR, midnight.get(Calendar.YEAR));
                long startTimeMillis = restrictionStart.getTimeInMillis() - midnight.getTimeInMillis();

                restrictionEnd.setTimeInMillis(timeRestriction.getEndTime().getValue());
                restrictionEnd.set(Calendar.DAY_OF_MONTH, midnight.get(Calendar.DAY_OF_MONTH));
                restrictionEnd.set(Calendar.MONTH, midnight.get(Calendar.MONTH));
                restrictionEnd.set(Calendar.YEAR, midnight.get(Calendar.YEAR));
                long endTimeMillis = restrictionEnd.getTimeInMillis() - midnight.getTimeInMillis();

                timeFences.add(AwarenessFence.not(
                        TimeFence.inDailyInterval(
                                TimeZone.getDefault()
                                ,startTimeMillis
                                ,endTimeMillis)));
            }
        }

        return timeFences;
    }
}
