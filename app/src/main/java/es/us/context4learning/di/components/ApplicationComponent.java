package es.us.context4learning.di.components;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;

import dagger.Component;
import es.us.context4learning.backend.auditEventApi.AuditEventApi;
import es.us.context4learning.backend.contextApi.ContextApi;
import es.us.context4learning.backend.deviceApi.DeviceApi;
import es.us.context4learning.backend.limitNotificationApi.LimitNotificationApi;
import es.us.context4learning.backend.locationRestrictionApi.LocationRestrictionApi;
import es.us.context4learning.backend.notificationApi.NotificationApi;
import es.us.context4learning.backend.timeRestrictionApi.TimeRestrictionApi;
import es.us.context4learning.backend.userApi.UserApi;
import es.us.context4learning.data.api.google.firebase.FirebaseApi;
import es.us.context4learning.data.api.moodle.MoodleApi;
import es.us.context4learning.di.modules.ApplicationModule;
import es.us.context4learning.di.modules.GAEModule;
import es.us.context4learning.di.modules.NetworkModule;
import es.us.context4learning.di.scopes.PerApp;
import es.us.context4learning.ui.activity.BaseActivity;
import es.us.context4learning.utils.RxNetwork;
import okhttp3.OkHttpClient;
import rx.Observable;

@PerApp
@Component(
        modules = {
                ApplicationModule.class,
                NetworkModule.class,
                GAEModule.class
        }
)
public interface ApplicationComponent {

    void inject(BaseActivity baseActivity);

    Context getContext();
    SharedPreferences getSharedPreferences();

    RxNetwork getRxNetwork();
    OkHttpClient getOkHttpClient();
    Gson getGson();
    MoodleApi getMoodleApi();
    FirebaseRemoteConfig getFirebaseRemoteConfig();
    FirebaseApi getFirebaseApi();

    UserApi getGAEUserApi();
    DeviceApi getGAEDeviceApi();
    LocationRestrictionApi getGAELocationRestrictionApi();
    TimeRestrictionApi getGAETimeRestrictionApi();
    LimitNotificationApi getGAELimitNotificationApi();
    ContextApi getGAEContextApi();
    NotificationApi getGAENotificationApi();
    AuditEventApi getGAEAuditEventApi();

    GoogleAnalytics getGoogleAnalytics();
    Tracker getGoogleAnalyticsTracker();
    GoogleCloudMessaging getGoogleCloudMessaging();

    Observable<GoogleApiClient> getObservableGoogleApiClientAwareness();

}