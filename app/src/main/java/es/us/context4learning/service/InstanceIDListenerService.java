package es.us.context4learning.service;

import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.fernandocejas.frodo.annotation.RxLogObservable;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import es.us.context4learning.Constants;
import es.us.context4learning.MoodleContextApplication;
import es.us.context4learning.R;
import es.us.context4learning.backend.deviceApi.DeviceApi;
import es.us.context4learning.di.components.ApplicationComponent;
import es.us.context4learning.observable.appengine.GetDeviceGAEObservable;
import es.us.context4learning.observable.appengine.UpdateDeviceGAEObservable;
import es.us.context4learning.utils.RxNetwork;
import es.us.context4learning.utils.Utils;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class InstanceIDListenerService extends FirebaseInstanceIdService {

    private static final String TAG = InstanceIDListenerService.class.getCanonicalName();

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is also called
     * when the InstanceID token is initially generated, so this is where
     * you retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        SharedPreferences preferences = Utils.getSharedPreferences(this);
        Long userId = preferences.getLong(Constants.PROPERTY_USER_ID, 0L);
        Long deviceId = preferences.getLong(Constants.PROPERTY_DEVICE_ID, 0L);
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        if(userId > 0 && deviceId > 0){
            saveTokenToServer(deviceId, refreshedToken);
        }else{
            saveTokenToPrefs(refreshedToken);
        }
    }

    private void saveTokenToPrefs(String token) {
        SharedPreferences preferences = Utils.getSharedPreferences(this.getApplicationContext());
        preferences.edit()
                .putString(Constants.PROPERTY_INSTANCE_ID, token)
                .commit();
    }

    @RxLogObservable
    private void saveTokenToServer(Long deviceId, String token) {
        ApplicationComponent applicationComponent = MoodleContextApplication.get(this).getApplicationComponent();
        RxNetwork rxNetwork = applicationComponent.getRxNetwork();
        DeviceApi deviceApi = applicationComponent.getGAEDeviceApi();
        rxNetwork.checkInternetConnection()
                .andThen(
                        Observable.create(new GetDeviceGAEObservable(deviceApi,deviceId))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                ).doOnError(throwable ->
                    Toast.makeText(this,getString(R.string.exception_message_device_not_found_gae), Toast.LENGTH_LONG).show()
                )
                .flatMap(device -> {
                    device.setInstanceId(token);
                    return Observable.create(new UpdateDeviceGAEObservable(deviceApi,deviceId,device));
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(device -> saveTokenToPrefs(device.getInstanceId()));
    }

}
