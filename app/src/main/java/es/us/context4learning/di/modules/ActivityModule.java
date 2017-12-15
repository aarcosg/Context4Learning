package es.us.context4learning.di.modules;

import android.app.Activity;

import dagger.Module;
import dagger.Provides;
import es.us.context4learning.di.scopes.PerActivity;

@Module
public class ActivityModule {

    private final Activity mActivity;

    public ActivityModule(Activity activity) {
        this.mActivity = activity;
    }

    @Provides
    @PerActivity
    Activity provideActivity() {
        return this.mActivity;
    }
}