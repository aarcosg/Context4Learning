package es.us.context4learning.observable.google;

import com.google.android.gms.common.ConnectionResult;

public class GoogleAPIConnectionException extends RuntimeException {

    private final ConnectionResult connectionResult;

    GoogleAPIConnectionException(String detailMessage, ConnectionResult connectionResult) {
        super(detailMessage);
        this.connectionResult = connectionResult;
    }

    public ConnectionResult getConnectionResult() {
        return connectionResult;
    }
}