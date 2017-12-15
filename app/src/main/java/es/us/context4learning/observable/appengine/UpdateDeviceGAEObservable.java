package es.us.context4learning.observable.appengine;

import java.io.IOException;

import es.us.context4learning.exception.DeviceNotFoundGAEException;
import rx.Observable;
import rx.Subscriber;
import es.us.context4learning.backend.deviceApi.DeviceApi;
import es.us.context4learning.backend.deviceApi.model.Device;

public class UpdateDeviceGAEObservable implements Observable.OnSubscribe<Device> {

    private static final String TAG = UpdateDeviceGAEObservable.class.getCanonicalName();

    private DeviceApi mGaeDeviceApi;
    private Long mDeviceId;
    private Device mGaeDevice;

    public UpdateDeviceGAEObservable(DeviceApi deviceApi, Long deviceId, Device device) {
        this.mGaeDeviceApi = deviceApi;
        this.mDeviceId = deviceId;
        this.mGaeDevice = device;
    }

    @Override
    public void call(Subscriber<? super Device> subscriber) {
        try {
            Device updatedDevice = mGaeDeviceApi.update(mDeviceId,mGaeDevice).execute();
            subscriber.onNext(updatedDevice);
            subscriber.onCompleted();
        } catch (IOException e) {
            subscriber.onError(new DeviceNotFoundGAEException());
        }
    }
}