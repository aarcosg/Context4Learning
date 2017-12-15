package es.us.context4learning.observable.appengine;

import java.io.IOException;

import es.us.context4learning.exception.DeviceNotFoundGAEException;
import rx.Observable;
import rx.Subscriber;
import es.us.context4learning.backend.deviceApi.DeviceApi;
import es.us.context4learning.backend.deviceApi.model.Device;

public class InsertDeviceGAEObservable implements Observable.OnSubscribe<Device> {

    private static final String TAG = InsertDeviceGAEObservable.class.getCanonicalName();

    private DeviceApi mGaeDeviceApi;
    private Device mGaeDevice;

    public InsertDeviceGAEObservable(DeviceApi deviceApi, Device device) {
        this.mGaeDeviceApi = deviceApi;
        this.mGaeDevice = device;
    }

    @Override
    public void call(Subscriber<? super Device> subscriber) {
        try {
            Device newDevice = mGaeDeviceApi.insert(mGaeDevice).execute();
            subscriber.onNext(newDevice);
            subscriber.onCompleted();
        } catch (IOException e) {
            subscriber.onError(new DeviceNotFoundGAEException());
        }
    }
}