package es.us.context4learning.di.modules;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;

import dagger.Module;
import dagger.Provides;
import es.us.context4learning.BuildConfig;
import es.us.context4learning.MoodleContextApplication;
import es.us.context4learning.di.scopes.PerApp;
import io.fabric.sdk.android.Fabric;

@Module
public class ApplicationModule {

    private final MoodleContextApplication mApplication;

    public ApplicationModule(MoodleContextApplication application) {
        this.mApplication = application;

        if(BuildConfig.DEBUG){
            //LeakCanary.install(this.mApplication);
        }else{
            Fabric.with(this.mApplication, new Crashlytics());
            Fabric.with(this.mApplication, new Answers());
        }
    }

    @Provides
    @PerApp
    public Context provideApplicationContext() {
        return this.mApplication;
    }

    @Provides
    @PerApp
    public SharedPreferences provideDefaultSharedPreferences() {
        return PreferenceManager
                .getDefaultSharedPreferences(this.mApplication);
    }
}
