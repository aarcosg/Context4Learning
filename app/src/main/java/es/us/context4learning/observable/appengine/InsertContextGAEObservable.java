package es.us.context4learning.observable.appengine;

import com.google.android.gms.awareness.state.HeadphoneState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import es.us.context4learning.Constants;
import es.us.context4learning.R;
import es.us.context4learning.backend.contextApi.ContextApi;
import es.us.context4learning.backend.contextApi.model.Context;
import es.us.context4learning.backend.contextApi.model.Device;
import es.us.context4learning.backend.contextApi.model.GeoPt;
import es.us.context4learning.data.AwarenessContext;
import es.us.context4learning.exception.DeviceNotFoundGAEException;
import rx.Observable;
import rx.Subscriber;

public class InsertContextGAEObservable implements Observable.OnSubscribe<Context> {

    private static final String TAG = InsertContextGAEObservable.class.getCanonicalName();

    private android.content.Context mContextAndroid;
    private ContextApi mContextApi;
    private Long mDeviceId;
    private AwarenessContext mAwarenessContext;

    public InsertContextGAEObservable(android.content.Context context, ContextApi contextApi,
                                      Long deviceId, AwarenessContext awarenessContext) {
        this.mContextAndroid = context;
        this.mContextApi = contextApi;
        this.mDeviceId = deviceId;
        this.mAwarenessContext = awarenessContext;
    }

    @Override
    public void call(Subscriber<? super Context> subscriber) {
        try {
            Context context = new Context();

            // Set device
            Device device = new Device();
            device.setId(mDeviceId);
            context.setDevice(device);

            // Set activity
            if(mAwarenessContext.getDetectedActivity() != null){
                context.setActivity(Constants.getActivityString(mContextAndroid, mAwarenessContext.getDetectedActivity().getType()));
            }
            // Set location
            if(mAwarenessContext.getLocation() != null){
                GeoPt geoPoint = new GeoPt();
                geoPoint.setLatitude((float)mAwarenessContext.getLocation().getLatitude());
                geoPoint.setLongitude((float)mAwarenessContext.getLocation().getLongitude());
                context.setLocation(geoPoint);
            }

            // Set battery
            if(mAwarenessContext.getBatteryStatus() != null){
                context.setBattery(mAwarenessContext.getBatteryStatus().getBatteryPct());
            }

            // Set headphone
            if(mAwarenessContext.getHeadphoneState() != null){
                context.setHeadphone(mAwarenessContext.getHeadphoneState().getState() == HeadphoneState.PLUGGED_IN);
            }

            // Set weather conditions
            if(mAwarenessContext.getWeather() != null && mAwarenessContext.getWeather().getConditions().length > 0){
                List<String> weatherConditions = new ArrayList<>();
                for(int i = 0; i < mAwarenessContext.getWeather().getConditions().length; i++){
                    weatherConditions.add(mContextAndroid.getResources().getStringArray(R.array.weather_conditions)[i]);
                }
                context.setWeatherConditions(weatherConditions);
            }

            Context newContext = mContextApi.insert(context).execute();
            subscriber.onNext(newContext);
            subscriber.onCompleted();
        } catch (IOException e) {
            e.printStackTrace();
            subscriber.onError(new DeviceNotFoundGAEException());
        }
    }
}