package es.us.context4learning.observable;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import es.us.context4learning.R;
import es.us.context4learning.exception.FetchAddressException;
import rx.Observable;
import rx.Subscriber;

public class FetchAddressObservable implements Observable.OnSubscribe<String> {

    private static final String TAG = FetchAddressObservable.class.getCanonicalName();
    private static final int MAX_ADDRESSES = 1;

    private Context mContext;
    private LatLng mLocation;

    public FetchAddressObservable(Context context, LatLng location) {
        this.mContext = context;
        this.mLocation = location;
    }

    @Override
    public void call(Subscriber<? super String> subscriber) {

        String errorMessage = "";

        // Make sure that the location data was set. If it wasn't
        // send an error error message and return.
        if (mLocation == null) {
            errorMessage = mContext.getString(R.string.no_location_data_provided);
            Log.d(TAG, errorMessage);
            subscriber.onError(new FetchAddressException(errorMessage));
            return;
        }

        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(mLocation.latitude, mLocation.longitude, MAX_ADDRESSES);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            errorMessage = mContext.getString(R.string.service_not_available);
            Log.d(TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = mContext.getString(R.string.error_invalid_lat_long);
            Log.d(TAG, errorMessage + ". " +
                    "Latitude = " + mLocation.latitude +
                    ", Longitude = " +
                    mLocation.longitude, illegalArgumentException);
        } finally {
            if(!TextUtils.isEmpty(errorMessage)){
                subscriber.onError(new FetchAddressException(errorMessage));
            }
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size()  == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = mContext.getString(R.string.no_address_found);
                Log.e(TAG, errorMessage);
            }
            subscriber.onError(new FetchAddressException(errorMessage));
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
            for(int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }
            Log.i(TAG, mContext.getString(R.string.address_found));
            subscriber.onNext(TextUtils.join(System.getProperty("line.separator"),addressFragments));
            subscriber.onCompleted();
        }
    }

}