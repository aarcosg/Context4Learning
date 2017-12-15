package es.us.context4learning.di.modules;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import dagger.Module;
import dagger.Provides;
import es.us.context4learning.backend.auditEventApi.AuditEventApi;
import es.us.context4learning.backend.contextApi.ContextApi;
import es.us.context4learning.backend.deviceApi.DeviceApi;
import es.us.context4learning.backend.limitNotificationApi.LimitNotificationApi;
import es.us.context4learning.backend.locationRestrictionApi.LocationRestrictionApi;
import es.us.context4learning.backend.notificationApi.NotificationApi;
import es.us.context4learning.backend.timeRestrictionApi.TimeRestrictionApi;
import es.us.context4learning.backend.userApi.UserApi;
import es.us.context4learning.di.scopes.PerApp;

import static es.us.context4learning.Constants.GOOGLE_APPENGINE_APP_NAME;
import static es.us.context4learning.Constants.GOOGLE_APPENGINE_URL;

@Module
public class GAEModule {

    @Provides
    @PerApp
    UserApi provideGAEUserApi(){
        return new UserApi.Builder(AndroidHttp.newCompatibleTransport(),new AndroidJsonFactory(), null)
                .setRootUrl(GOOGLE_APPENGINE_URL)
                .setApplicationName(GOOGLE_APPENGINE_APP_NAME)
                .build();
    }

    @Provides
    @PerApp
    DeviceApi provideGAEDeviceApi(){
        return new DeviceApi.Builder(AndroidHttp.newCompatibleTransport(),new AndroidJsonFactory(), null)
                .setRootUrl(GOOGLE_APPENGINE_URL)
                .setApplicationName(GOOGLE_APPENGINE_APP_NAME)
                .build();
    }

    @Provides
    @PerApp
    LocationRestrictionApi provideGAELocationRestrictionApi(){
        return new LocationRestrictionApi.Builder(AndroidHttp.newCompatibleTransport(),new AndroidJsonFactory(), null)
                .setRootUrl(GOOGLE_APPENGINE_URL)
                .setApplicationName(GOOGLE_APPENGINE_APP_NAME)
                .build();
    }


    @Provides
    @PerApp
    TimeRestrictionApi provideGAETimeRestrictionApi(){
        return new TimeRestrictionApi.Builder(AndroidHttp.newCompatibleTransport(),new AndroidJsonFactory(), null)
                .setRootUrl(GOOGLE_APPENGINE_URL)
                .setApplicationName(GOOGLE_APPENGINE_APP_NAME)
                .build();
    }

    @Provides
    @PerApp
    LimitNotificationApi provideGAELimitNotificationApi(){
        return new LimitNotificationApi.Builder(AndroidHttp.newCompatibleTransport(),new AndroidJsonFactory(), null)
                .setRootUrl(GOOGLE_APPENGINE_URL)
                .setApplicationName(GOOGLE_APPENGINE_APP_NAME)
                .build();
    }

    @Provides
    @PerApp
    ContextApi provideGAEContextApi(){
        return new ContextApi.Builder(AndroidHttp.newCompatibleTransport(),new AndroidJsonFactory(), null)
                .setRootUrl(GOOGLE_APPENGINE_URL)
                .setApplicationName(GOOGLE_APPENGINE_APP_NAME)
                .build();
    }

    @Provides
    @PerApp
    NotificationApi provideGAENotificationApi(){
        return new NotificationApi.Builder(AndroidHttp.newCompatibleTransport(),new AndroidJsonFactory(), null)
                .setRootUrl(GOOGLE_APPENGINE_URL)
                .setApplicationName(GOOGLE_APPENGINE_APP_NAME)
                .build();
    }


    @Provides
    @PerApp
    AuditEventApi provideGAEAuditEventApi(){
        return new AuditEventApi.Builder(AndroidHttp.newCompatibleTransport(),new AndroidJsonFactory(), null)
                .setRootUrl(GOOGLE_APPENGINE_URL)
                .setApplicationName(GOOGLE_APPENGINE_APP_NAME)
                .build();
    }
}
