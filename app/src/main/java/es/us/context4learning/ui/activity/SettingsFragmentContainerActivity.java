package es.us.context4learning.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import javax.inject.Inject;

import es.us.context4learning.R;
import es.us.context4learning.di.HasComponent;
import es.us.context4learning.di.components.DaggerSettingsFragmentContainerComponent;
import es.us.context4learning.di.components.SettingsFragmentContainerComponent;
import es.us.context4learning.ui.fragment.LimitNotificationsFragment;
import es.us.context4learning.ui.fragment.LocationRestrictionFragment;
import es.us.context4learning.ui.fragment.TimeRestrictionFragment;

public class SettingsFragmentContainerActivity extends BaseActivity implements HasComponent<SettingsFragmentContainerComponent> {

    private static final String TAG = SettingsFragmentContainerActivity.class.getCanonicalName();
    private static final String EXTRA_FRAGMENT_ID = "extra_fragment_id";
    public static final int FRAGMENT_TIME_RESTRICTIONS = 1;
    public static final int FRAGMENT_LOCATION_RESTRICTIONS = 2;
    public static final int FRAGMENT_LIMIT_NOTIFICATIONS_RESTRICTIONS = 3;

    @Inject
    Tracker mTracker;

    private SettingsFragmentContainerComponent mSettingsFragmentContainerComponent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.initializeInjector();
        setContentView(R.layout.activity_fragment_container);
        buildActionBarToolbar("",true);
        matchStatusBarHeight();
        if(getIntent().hasExtra(EXTRA_FRAGMENT_ID)){
            Fragment fragment = null;
            switch (getIntent().getExtras().getInt(EXTRA_FRAGMENT_ID)){
                case FRAGMENT_TIME_RESTRICTIONS:
                    mTracker.setScreenName("Time Restrictions Fragment");
                    fragment = TimeRestrictionFragment.newInstance();
                    break;
                case FRAGMENT_LOCATION_RESTRICTIONS:
                    mTracker.setScreenName("Location Restrictions Fragment");
                    fragment = LocationRestrictionFragment.newInstance();
                    break;
                case FRAGMENT_LIMIT_NOTIFICATIONS_RESTRICTIONS:
                    mTracker.setScreenName("Limit Notifications Fragment");
                    fragment = LimitNotificationsFragment.newInstance();
                    break;
            }
            if(fragment != null){
                mTracker.send(new HitBuilders.ScreenViewBuilder().build());
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.content_frame, fragment)
                        .commit();
            }
        }
    }

    @Override
    public SettingsFragmentContainerComponent getComponent() {
        if(this.mSettingsFragmentContainerComponent == null){
            this.initializeInjector();
        }
        return this.mSettingsFragmentContainerComponent;
    }

    private void initializeInjector() {
        mSettingsFragmentContainerComponent = DaggerSettingsFragmentContainerComponent.builder()
                .applicationComponent(getApplicationComponent())
                .activityModule(getActivityModule())
                .build();
        mSettingsFragmentContainerComponent.inject(this);
    }

    public static void launch(Activity activity, int fragmentId) {
        Intent intent = new Intent(activity, SettingsFragmentContainerActivity.class);
        intent.putExtra(EXTRA_FRAGMENT_ID, fragmentId);
        ActivityCompat.startActivity(activity, intent, null);
    }
}
