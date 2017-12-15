package es.us.context4learning;

import android.content.Context;
import android.support.multidex.MultiDexApplication;

import es.us.context4learning.di.components.ApplicationComponent;
import es.us.context4learning.di.components.DaggerApplicationComponent;
import es.us.context4learning.di.modules.ApplicationModule;
import es.us.context4learning.di.modules.GAEModule;
import es.us.context4learning.di.modules.NetworkModule;

public class MoodleContextApplication extends MultiDexApplication {

    private static final String TAG = MoodleContextApplication.class.getCanonicalName();

    private ApplicationComponent mApplicationComponent;

    public MoodleContextApplication(){
        super();
    }

    public static MoodleContextApplication get(Context context){
        return (MoodleContextApplication) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.initializeInjector();
    }

    public ApplicationComponent getApplicationComponent() {
        return this.mApplicationComponent;
    }

    private void initializeInjector() {
        mApplicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .networkModule(new NetworkModule())
                .gAEModule(new GAEModule())
                .build();
    }

}
