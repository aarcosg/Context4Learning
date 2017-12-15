package es.us.context4learning.observable.google;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import es.us.context4learning.R;
import es.us.context4learning.utils.Utils;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

public abstract class BaseObservable<T> implements Observable.OnSubscribe<T> {

    private static final String TAG = BaseObservable.class.getCanonicalName();

    private final Context ctx;
    private final List<Api<? extends Api.ApiOptions.NotRequiredOptions>> services;
    private final Map<GoogleApiClient, Subscriber<? super T>> subscriptionInfoMap = new ConcurrentHashMap<>();
    protected static final Set<BaseObservable> observableSet = new HashSet<>();

    @SafeVarargs
    protected BaseObservable(Context ctx, Api<? extends Api.ApiOptions.NotRequiredOptions>... services) {
        this.ctx = ctx;
        this.services = Arrays.asList(services);
    }

    @Override
    public void call(Subscriber<? super T> subscriber) {
        final GoogleApiClient apiClient = createApiClient(subscriber);
        subscriptionInfoMap.put(apiClient, subscriber);

        try {
            apiClient.connect();
        } catch (Throwable ex) {
            subscriber.onError(ex);
        }

        subscriber.add(Subscriptions.create(new Action0() {
            @Override
            public void call() {
                // Avoid google api client disconnection
                /*if (apiClient.isConnected() || apiClient.isConnecting()) {
                    onUnsubscribed(apiClient);
                    apiClient.disconnect();
                }*/
                subscriptionInfoMap.remove(apiClient);
            }
        }));
    }


    protected GoogleApiClient createApiClient(Subscriber<? super T> subscriber) {

        ApiClientConnectionCallbacks apiClientConnectionCallbacks = new ApiClientConnectionCallbacks(subscriber);

        GoogleApiClient.Builder apiClientBuilder = new GoogleApiClient.Builder(ctx);


        for (Api<? extends Api.ApiOptions.NotRequiredOptions> service : services) {
            apiClientBuilder.addApi(service);
        }

        apiClientBuilder.addConnectionCallbacks(apiClientConnectionCallbacks);
        apiClientBuilder.addOnConnectionFailedListener(apiClientConnectionCallbacks);

        GoogleApiClient apiClient = apiClientBuilder.build();

        apiClientConnectionCallbacks.setClient(apiClient);

        return apiClient;

    }

    static final void onResolutionResult(int resultCode, ConnectionResult connectionResult) {
        for(BaseObservable observable : observableSet) { observable.handleResolutionResult(resultCode, connectionResult); }
        observableSet.clear();
    }

    protected final void handleResolutionResult(int resultCode, ConnectionResult connectionResult) {
        for (Map.Entry<GoogleApiClient, Subscriber<? super T>> entry : subscriptionInfoMap.entrySet()) {
            if (!entry.getValue().isUnsubscribed()) {
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        entry.getKey().connect();
                    } catch (Throwable ex) {
                        entry.getValue().onError(ex);
                    }
                } else {
                    entry.getValue().onError(new GoogleAPIConnectionException("Error connecting to GoogleApiClient, resolution was not successful.", connectionResult));
                }
            }
        }
    }

    protected void onUnsubscribed(GoogleApiClient locationClient) {}

    protected abstract void onGoogleApiClientReady(GoogleApiClient apiClient, Observer<? super T> observer);

    private class ApiClientConnectionCallbacks implements
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener {

        final private Subscriber<? super T> subscriber;

        private GoogleApiClient apiClient;

        private ApiClientConnectionCallbacks(Subscriber<? super T> subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public void onConnected(Bundle bundle) {
            try {
                onGoogleApiClientReady(apiClient, subscriber);
            } catch (Throwable ex) {
                subscriber.onError(ex);
            }
        }

        @Override
        public void onConnectionSuspended(int cause) {
            //subscriber.onError(new GoogleAPIConnectionSuspendedException(cause));
            String errorMessage = "";
            switch (cause){
                case CAUSE_NETWORK_LOST:
                    errorMessage = ctx.getString(R.string.error_google_api_client_network_lost);
                    break;
                case CAUSE_SERVICE_DISCONNECTED:
                    errorMessage = ctx.getString(R.string.error_google_api_client_service_disconnected);
                    break;
            }
            Log.e(TAG, "@onConnectionSuspended:" + errorMessage);
            Utils.showToast(ctx, errorMessage);
        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            //subscriber.onError(new GoogleAPIConnectionException("Error connecting to GoogleApiClient.", connectionResult));
            if(connectionResult.hasResolution()) {
                observableSet.add(BaseObservable.this);
                if(!ResolutionActivity.isResolutionShown()) {
                    Intent intent = new Intent(ctx, ResolutionActivity.class);
                    intent.putExtra(ResolutionActivity.ARG_CONNECTION_RESULT, connectionResult);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ctx.startActivity(intent);
                }
            } else {
                //subscriber.onError(new GoogleAPIConnectionException("Error connecting to GoogleApiClient.", connectionResult));
                Utils.showToast(ctx, ctx.getString(R.string.error_google_api_client_connection));
            }
        }

        public void setClient(GoogleApiClient client) {
            this.apiClient = client;
        }
    }

}