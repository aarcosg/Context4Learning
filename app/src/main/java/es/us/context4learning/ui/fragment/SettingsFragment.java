package es.us.context4learning.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import butterknife.Bind;
import butterknife.ButterKnife;
import es.us.context4learning.Constants;
import es.us.context4learning.MoodleContextApplication;
import es.us.context4learning.data.api.google.appengine.GAEHelper;
import es.us.context4learning.ui.activity.SettingsFragmentContainerActivity;
import es.us.context4learning.ui.activity.SettingsActivity;
import es.us.context4learning.R;
import es.us.context4learning.utils.Utils;

public class SettingsFragment extends PreferenceFragment {

    private static final String TAG = SettingsFragment.class.getCanonicalName();
    private static final String PREF_TIME_RESTRICTIONS = "time_restrictions";
    private static final String PREF_LOCATION_RESTRICTIONS = "location_restrictions";
    private static final String PREF_LIMIT_NOTIFICATION_RESTRICTIONS = "limit_notifications_restrictions";

    @Bind(R.id.toolbar_actionbar)
    Toolbar mToolbar;

    private Context mContext;
    private SharedPreferences mPrefs;
    private Preference mTimeRestrictionsPreference;
    private Preference mLocationRestrictionsPreference;
    private Preference mLimitNotificationsRestrictionsPreference;

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity.getApplicationContext();
        mPrefs = MoodleContextApplication.get(mContext).getApplicationComponent().getSharedPreferences();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        mTimeRestrictionsPreference = findPreference(PREF_TIME_RESTRICTIONS);
        mTimeRestrictionsPreference.setOnPreferenceClickListener(preference -> {
            SettingsFragmentContainerActivity.launch(getActivity(), SettingsFragmentContainerActivity.FRAGMENT_TIME_RESTRICTIONS);
            return true;
        });

        mLocationRestrictionsPreference = findPreference(PREF_LOCATION_RESTRICTIONS);
        mLocationRestrictionsPreference.setOnPreferenceClickListener(preference -> {
            SettingsFragmentContainerActivity.launch(getActivity(), SettingsFragmentContainerActivity.FRAGMENT_LOCATION_RESTRICTIONS);
            return true;
        });

        mLimitNotificationsRestrictionsPreference = findPreference(PREF_LIMIT_NOTIFICATION_RESTRICTIONS);
        mLimitNotificationsRestrictionsPreference.setOnPreferenceClickListener(preference -> {
            SettingsFragmentContainerActivity.launch(getActivity(), SettingsFragmentContainerActivity.FRAGMENT_LIMIT_NOTIFICATIONS_RESTRICTIONS);
            return true;
        });

        SwitchPreference receiveNotificationsPreference = (SwitchPreference) findPreference(Constants.PROPERTY_RECEIVE_NOTIFICATIONS);
        receiveNotificationsPreference.setOnPreferenceChangeListener(
                (preference, areNotificationsEnabledObject) -> {
                    boolean areNotificationsEnabled = (boolean) areNotificationsEnabledObject;
                    enableNotificationPreferences(areNotificationsEnabled);
                    GAEHelper.sendAuditEventToServer(
                            mContext,
                            Constants.AUDIT_CATEGORY_NOTIFICATION,
                            areNotificationsEnabled ? Constants.AUDIT_ACTION_ENABLED : Constants.AUDIT_ACTION_DISABLED,
                            mPrefs.getString(Constants.PROPERTY_USER_NAME, ""));
                    return true;
                }
        );
        enableNotificationPreferences(receiveNotificationsPreference.isChecked());

        SwitchPreference videosOnlyWifiPreference = (SwitchPreference) findPreference(Constants.PROPERTY_SHOW_VIDEO_WIFI);
        videosOnlyWifiPreference.setOnPreferenceChangeListener(
                (preference, areVideosOnlyWifiObject) -> {
                    boolean areVideosOnlyWifiEnabled = (boolean) areVideosOnlyWifiObject;
                    GAEHelper.sendAuditEventToServer(
                            mContext,
                            Constants.AUDIT_CATEGORY_TASK,
                            areVideosOnlyWifiEnabled ? Constants.AUDIT_ACTION_ENABLED : Constants.AUDIT_ACTION_DISABLED,
                            mPrefs.getString(Constants.PROPERTY_USER_NAME, ""));
                    return true;
                });

        FirebaseRemoteConfig firebaseRemoteConfig = MoodleContextApplication.get(mContext)
                .getApplicationComponent().getFirebaseRemoteConfig();
        boolean isContextRecognitionEnabled = firebaseRemoteConfig.getBoolean(Constants.REMOTE_CONFIG_CONTEXT_RECOGNITION);
        findPreference(Constants.PROPERTY_APP_VERSION).setSummary(
                "v. " + Utils.getAppVersion(mContext) + " - "
                + (isContextRecognitionEnabled ?
                        getString(R.string.context_recognition_enabled) : getString(R.string.context_recognition_disabled))
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, fragmentView);
        setupToolbar();
        setHasOptionsMenu(true);
        return fragmentView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    private void setupToolbar(){
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mToolbar.setElevation(getResources().getDimension(R.dimen.headerbar_elevation));
        }
        SettingsActivity activity = (SettingsActivity) getActivity();
        activity.setSupportActionBar(mToolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mToolbar.setPadding(0, ((SettingsActivity)getActivity()).getStatusBarHeight(), 0, 0);
    }

    private void enableNotificationPreferences(boolean enable){
        mTimeRestrictionsPreference.setEnabled(enable);
        mLocationRestrictionsPreference.setEnabled(enable);
        mLimitNotificationsRestrictionsPreference.setEnabled(enable);
    }

}