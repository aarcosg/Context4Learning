package es.us.context4learning.observable.appengine;

import java.io.IOException;

import es.us.context4learning.backend.deviceApi.DeviceApi;
import es.us.context4learning.backend.deviceApi.model.Device;
import es.us.context4learning.exception.DeviceNotFoundGAEException;
import rx.Observable;
import rx.Subscriber;

public class GetDeviceGAEObservable implements Observable.OnSubscribe<Device> {

    private static final String TAG = GetDeviceGAEObservable.class.getCanonicalName();

    private DeviceApi mGaeDeviceApi;
    private Long mDeviceId;

    public GetDeviceGAEObservable(DeviceApi deviceApi, Long deviceId) {
        this.mGaeDeviceApi = deviceApi;
        this.mDeviceId = deviceId;
    }

    @Override
    public void call(Subscriber<? super Device> subscriber) {
        try {
            Device device = mGaeDeviceApi.get(mDeviceId).execute();
            subscriber.onNext(device);
            subscriber.onCompleted();
        } catch (IOException e) {
            subscriber.onError(new DeviceNotFoundGAEException());
        }
    }
}