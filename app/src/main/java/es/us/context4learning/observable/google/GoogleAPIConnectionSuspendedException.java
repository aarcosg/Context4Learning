package es.us.context4learning.observable.google;

public class GoogleAPIConnectionSuspendedException extends RuntimeException {

    private final int cause;

    GoogleAPIConnectionSuspendedException(int cause) {
        this.cause = cause;
    }

    public int getErrorCause() {
        return cause;
    }
}