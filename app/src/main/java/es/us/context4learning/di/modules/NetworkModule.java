package es.us.context4learning.di.modules;

import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import es.us.context4learning.BuildConfig;
import es.us.context4learning.R;
import es.us.context4learning.data.api.google.firebase.FirebaseApi;
import es.us.context4learning.data.api.moodle.MoodleApi;
import es.us.context4learning.di.scopes.PerApp;
import es.us.context4learning.observable.google.GoogleAPIClientObservable;
import es.us.context4learning.utils.RxNetwork;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;

@Module
public class NetworkModule {

    private static final String ANALYTICS_PROPERTY_ID = "UA-60728639-3";
    private static final String ANALYTICS_PROPERTY_ID_TEST = "UA-69203026-1";
    private static final String NAME_RETROFIT_MOODLE = "NAME_RETROFIT_MOODLE";
    private static final String NAME_RETROFIT_FCM = "NAME_RETROFIT_FCM";
    private final static long SECONDS_TIMEOUT = 20;

    @Provides
    @PerApp
    GoogleAnalytics provideGoogleAnalytics(Context context){
        GoogleAnalytics googleAnalytics = GoogleAnalytics.getInstance(context);
        googleAnalytics.setLocalDispatchPeriod(1800);
        googleAnalytics.setDryRun(BuildConfig.DEBUG);
        return googleAnalytics;
    }

    @Provides
    @PerApp
    Tracker provideGoogleAnalyticsTracker(GoogleAnalytics googleAnalytics){
        Tracker tracker = googleAnalytics.newTracker(ANALYTICS_PROPERTY_ID);
        tracker.enableExceptionReporting(true);
        tracker.enableAdvertisingIdCollection(true);
        tracker.enableAutoActivityTracking(true);
        return tracker;
    }

    @Provides
    @PerApp
    GoogleCloudMessaging provideGoogleCloudMessaging(Context context){
        return GoogleCloudMessaging.getInstance(context);
    }

    @Provides
    @PerApp
    Observable<GoogleApiClient> provideObservableGoogleApiClientAwareness(Context context){
        return GoogleAPIClientObservable.create(context, Awareness.API);
    }

    @Provides
    @PerApp
    RxNetwork provideRxNetwork(Context context){
        return new RxNetwork(context);
    }

    @Provides
    @PerApp
    Cache provideOkHttpCache(Context context) {
        int cacheSize = 10 * 1024 * 1024; // 10 MiB
        return new Cache(context.getCacheDir(), cacheSize);
    }

    @Provides
    @PerApp
    OkHttpClient provideOkHttpClient(Cache cache) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(SECONDS_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(SECONDS_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(SECONDS_TIMEOUT, TimeUnit.SECONDS)
                .cache(cache);
        if(BuildConfig.DEBUG){
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(logging);
        }
        return builder.build();
    }

    @Provides
    @PerApp
    Gson provideGson() {
        return new Gson();
    }

    @Provides
    @PerApp
    Retrofit.Builder provideRetrofit(Gson gson, OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient);
    }

    @Named(NAME_RETROFIT_MOODLE)
    @Provides
    @PerApp
    Retrofit provideMoodleRetrofit(Retrofit.Builder builder) {
        return builder
                .baseUrl(MoodleApi.SERVICE_ENDPOINT)
                .build();
    }

    @Provides
    @PerApp
    MoodleApi provideMoodleApi(
            @Named(NAME_RETROFIT_MOODLE) Retrofit retrofit) {
        return retrofit.create(MoodleApi.class);
    }

    @Provides
    @PerApp
    FirebaseRemoteConfig provideFirebaseRemoteConfig(){
        FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        firebaseRemoteConfig.setConfigSettings(configSettings);
        firebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
        return firebaseRemoteConfig;
    }

    @Named(NAME_RETROFIT_FCM)
    @Provides
    @PerApp
    Retrofit provideFCMRetrofit(Retrofit.Builder builder) {
        return builder
                .baseUrl(FirebaseApi.SERVICE_ENDPOINT)
                .build();
    }

    @Provides
    @PerApp
    FirebaseApi provideFCMApi(
            @Named(NAME_RETROFIT_FCM) Retrofit retrofit) {
        return retrofit.create(FirebaseApi.class);
    }

}
