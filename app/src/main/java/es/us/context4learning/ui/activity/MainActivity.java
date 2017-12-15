package es.us.context4learning.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.crashlytics.android.Crashlytics;
import com.fernandocejas.frodo.annotation.RxLogObservable;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import es.us.context4learning.BuildConfig;
import es.us.context4learning.Constants;
import es.us.context4learning.R;
import es.us.context4learning.backend.deviceApi.DeviceApi;
import es.us.context4learning.backend.deviceApi.model.Device;
import es.us.context4learning.backend.deviceApi.model.User;
import es.us.context4learning.backend.locationRestrictionApi.LocationRestrictionApi;
import es.us.context4learning.backend.timeRestrictionApi.TimeRestrictionApi;
import es.us.context4learning.chrometabs.CustomTabActivityHelper;
import es.us.context4learning.data.api.google.appengine.GAEHelper;
import es.us.context4learning.data.api.google.awareness.AwarenessApiHelper;
import es.us.context4learning.data.api.moodle.MoodleApi;
import es.us.context4learning.data.api.moodle.MoodleHelper;
import es.us.context4learning.data.api.moodle.entity.response.MoodleCourse;
import es.us.context4learning.di.HasComponent;
import es.us.context4learning.di.components.DaggerMainComponent;
import es.us.context4learning.di.components.MainComponent;
import es.us.context4learning.observable.appengine.InsertDeviceGAEObservable;
import es.us.context4learning.ui.fragment.MoodleCoursesFragment;
import es.us.context4learning.ui.fragment.MoodleTasksFragment;
import es.us.context4learning.utils.RxNetwork;
import es.us.context4learning.utils.Utils;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

@RuntimePermissions
public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, HasComponent<MainComponent> {

    private static final String TAG = MainActivity.class.getCanonicalName();
    private static final long DRAWER_DELAY_MS = 250;
    private static final String NAV_ITEM_ID = "nav_item_id";

    @Inject
    Context mContext;
    @Inject
    SharedPreferences mPrefs;
    @Inject
    DeviceApi mGAEDeviceApi;
    @Inject
    LocationRestrictionApi mGAELocationRestrictionApi;
    @Inject
    TimeRestrictionApi mGAETimeRestrictionApi;
    @Inject
    Observable<GoogleApiClient> mGoogleApiClientObservable;
    @Inject
    RxNetwork mRxNetwork;
    @Inject
    MoodleApi mMoodleApi;
    @Inject
    FirebaseRemoteConfig mFirebaseRemoteConfig;
    @Inject
    Tracker mTracker;

    @Bind(R.id.toolbar_actionbar)
    Toolbar mToolbar;
    @Bind(R.id.drawer_nav_view)
    NavigationView mNavigationView;
    @Bind(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    private MainComponent mMainComponent;
    private Long mUserId;
    private Long mDeviceId;
    private TextView mUser;
    private TextView mUserName;
    private ImageView mUserImageView;
    private final Handler mDrawerActionHandler = new Handler();
    private CompositeSubscription mSubscriptions = new CompositeSubscription();
    private ActionBarDrawerToggle mDrawerToggle;
    private int mNavItemId;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private boolean mUserLearnedDrawer;
    private Snackbar mPermissionsSnackbar;
    private CustomTabActivityHelper mCustomTabActivityHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.initializeInjector();
        checkPlayServices();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        mUserId = mPrefs.getLong(Constants.PROPERTY_USER_ID, 0L);
        mDeviceId = mPrefs.getLong(Constants.PROPERTY_DEVICE_ID, 0L);
        setupFirebase();

        if (mUserId == 0L) {
            LoginActivity.launch(MainActivity.this);
            finish();
            return;
        }

        checkMoodleUser();

        Bundle extras = getIntent().getExtras();
        if(extras != null && extras.containsKey(Constants.EXTRA_NOTIFICATION_ID)) {
            if(extras.containsKey(Constants.EXTRA_GAE_NOTIFICATION_ID)){
                Long gaeNotificationId = extras.getLong(Constants.EXTRA_GAE_NOTIFICATION_ID);
                if(gaeNotificationId > 0L){
                    GAEHelper.saveNotificationToServer(mContext, gaeNotificationId, Constants.NOTIFICATION_ACTION_OPENED);
                    GAEHelper.sendAuditEventToServer(
                            mContext,
                            Constants.AUDIT_CATEGORY_NOTIFICATION,
                            Constants.AUDIT_ACTION_OPENED,
                            mPrefs.getString(Constants.PROPERTY_USER_NAME, ""));
                }
            }

        }

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // Register device on GAE
        if(mUserId > 0L && mDeviceId == 0L){
            sendDeviceToServer();
        }

        mToolbar = buildActionBarToolbar(getString(R.string.app_name), false);

        mCustomTabActivityHelper = new CustomTabActivityHelper();

        mSubscriptions.add(mGoogleApiClientObservable.subscribe(
                googleApiClient -> {
                    MainActivityPermissionsDispatcher.setupAwarenessFencesWithCheck(this);
                }
                , throwable ->  Log.e(TAG,"Google Api Client connection suspended. Awareness fence unregistered.")
        ));

        // load saved navigation state if present
        if (null == savedInstanceState) {
            mNavItemId = R.id.navigation_tasks;
        } else {
            mNavItemId = savedInstanceState.getInt(NAV_ITEM_ID);
        }

        setupNavigationDrawer();
        setupNavigationDrawerHeader();
        if(!BuildConfig.DEBUG){
            logCrashlyticsUser();
        }

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mCustomTabActivityHelper.bindCustomTabsService(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCustomTabActivityHelper.unbindCustomTabsService(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
        if(mSubscriptions.isUnsubscribed()){
            mSubscriptions = new CompositeSubscription();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
        if(mDrawerLayout != null && mDrawerToggle != null){
            mDrawerLayout.removeDrawerListener(mDrawerToggle);
        }
        if(mSubscriptions.isUnsubscribed()){
            mSubscriptions.unsubscribe();
        }
    }

    @Override
    public MainComponent getComponent() {
        if(this.mMainComponent == null){
            this.initializeInjector();
        }
        return this.mMainComponent;
    }

    private void initializeInjector() {
        mMainComponent = DaggerMainComponent.builder()
                .applicationComponent(getApplicationComponent())
                .activityModule(getActivityModule())
                .build();
        mMainComponent.inject(this);
    }


    private void setupFirebase() {
        FirebaseApp.initializeApp(this);
        //Fetch remote config
        mFirebaseRemoteConfig.fetch(0)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Fetch Succeeded");
                    // Once the config is successfully fetched it must be activated before newly fetched
                    // values are returned.
                    mFirebaseRemoteConfig.activateFetched();
                });
    }

    @RxLogObservable
    private void sendDeviceToServer() {
        String UDID = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        String instanceId = mPrefs.getString(Constants.PROPERTY_INSTANCE_ID, "");
        if (!TextUtils.isEmpty(instanceId)) {
            // Register device in GAE
            Device device = new Device();
            device.setDeviceId(UDID);
            device.setInstanceId(instanceId);
            User user = new User();
            user.setId(mUserId);
            device.setUser(user);
            mSubscriptions.add(Observable.create(
                    new InsertDeviceGAEObservable(mGAEDeviceApi, device))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            this::saveDeviceIdToPrefs
                            ,this::handleGAEError)
            );
        }
    }

    private void setupNavigationDrawer() {
        mTitle = mDrawerTitle = getTitle();

        // Read in the flag indicating whether or not the user has demonstrated awareness of the drawer.
        mUserLearnedDrawer = mPrefs.getBoolean(Constants.PROPERTY_DRAWER_LEARNED, false);

        // listen for navigation events
        mNavigationView.setNavigationItemSelectedListener(this);

        // select the correct nav menu item
        mNavigationView.getMenu().findItem(mNavItemId).setChecked(true);

        // set up the hamburger icon to open and close the drawer
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open,
                R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                mToolbar.setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                mToolbar.setTitle(mDrawerTitle);
                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    mPrefs.edit().putBoolean(Constants.PROPERTY_DRAWER_LEARNED, true).apply();
                }
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);

        selectDrawerItem(mNavItemId);

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer) {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
    }

    private void setupNavigationDrawerHeader(){
        View header = mNavigationView.getHeaderView(0);
        mUser = (TextView) header.findViewById(R.id.user_nav_header);
        mUserName = (TextView) header.findViewById(R.id.username_nav_header);
        mUserImageView = (ImageView) header.findViewById(R.id.profile_image_nav_header);

        if(mUser != null){
            mUser.setText(mPrefs.getString(Constants.PROPERTY_USER_NAME, ""));
        }

        String token = mPrefs.getString(Constants.PROPERTY_MOODLE_TOKEN,"");
        if(!TextUtils.isEmpty(token)){
            mSubscriptions.add(mRxNetwork.checkInternetConnection()
                    .andThen(mMoodleApi.getSiteInfo(token)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread()))
                    .subscribe(
                            response -> {
                                mPrefs.edit().putInt(Constants.PROPERTY_MOODLE_USER_ID, response.body().getUserid()).commit();
                                mUserName.setText(response.body().getFirstname());
                                Glide.with(this).load(response.body().getUserpictureurl()).into(mUserImageView);
                                // Subscribe to FCM topic channels
                                subscribeTopics();
                            },
                            throwable -> {
                                Log.e(TAG,"Error getting site info");
                            }
                    )
            );
        }
    }

    /**
     * Performs the actual navigation logic, updating the main content fragment.
     */
    private void selectDrawerItem(final int itemId) {
        Fragment fragment = null;
        String screenName = "";
        switch (itemId) {
            case R.id.navigation_tasks:
                fragment = MoodleTasksFragment.newInstance(null);
                mTitle = getString(R.string.title_moodle_tasks_activity);
                screenName = "Tasks Fragment";
                break;
            case R.id.navigation_courses:
                fragment = MoodleCoursesFragment.newInstance();
                mTitle = getString(R.string.title_moodle_courses_activity);
                screenName = "Courses Fragment";
                break;
            case R.id.navigation_student_progress:
                screenName = "Main " + Constants.SCREEN_NAME_STUDENT_PROGRESS_REPORT;
                mNavigationView.getMenu().getItem(2).setChecked(false);
                MoodleHelper.openMoodleMarksWeb(this, mCustomTabActivityHelper, Constants.PILOT_COURSE_ID);
                break;
            case R.id.navigation_messages:
                screenName = Constants.SCREEN_NAME_MOODLE_MESSAGES_WEB;
                mNavigationView.getMenu().getItem(3).setChecked(false);
                MoodleHelper.openMoodleMessagesWeb(this, mCustomTabActivityHelper, Constants.PILOT_COURSE_ID);
                break;
            case R.id.navigation_course_info:
                screenName = "General Course Info";
                mNavigationView.getMenu().getItem(4).setChecked(false);
                String user = mPrefs.getString(Constants.PROPERTY_USER_NAME, "");
                String pass = mPrefs.getString(Constants.PROPERTY_USER_PASS, "");
                if(!TextUtils.isEmpty(user) && !TextUtils.isEmpty(pass)){
                    String url = MoodleApi.MOODLE_TASK_ACCESS_URL + "?user=" + user + "&pass=" + pass + "&url_acceso=" + Constants.PILOT_GENERAL_COURSE_INFO_URL;
                    Utils.openChromeTab(this, mCustomTabActivityHelper, url);
                }else{
                    Snackbar.make(mToolbar.getRootView(),
                            getString(R.string.exception_message_generic),
                            Snackbar.LENGTH_LONG)
                            .show();
                }
                break;
            case R.id.navigation_settings:
                screenName = "Settings Activity";
                mNavigationView.getMenu().getItem(5).setChecked(false);
                SettingsActivity.launch(this);
                break;
            default:
                // ignore
                break;
        }

        if (fragment != null) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(mTitle);
                getSupportActionBar().setSubtitle(null);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (itemId == R.id.navigation_tasks) {
                    getToolbar().setElevation(0);
                } else {
                    getToolbar().setElevation(getResources().getDimension(R.dimen.headerbar_elevation));
                }
            }

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        }

        mTracker.setScreenName(screenName);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        GAEHelper.sendAuditEventToServer(
                mContext,
                Constants.AUDIT_CATEGORY_SCREEN_VIEW,
                Constants.AUDIT_ACTION_HIT,
                mPrefs.getString(Constants.PROPERTY_USER_NAME, "") + " - " + screenName);
    }

    @Override
    public boolean onNavigationItemSelected(final MenuItem menuItem) {
        // update highlighted item in the navigation menu
        menuItem.setChecked(true);
        mNavItemId = menuItem.getItemId();

        // allow some time after closing the drawer before performing real navigation
        // so the user can see what is happening
        mDrawerLayout.closeDrawer(GravityCompat.START);
        mDrawerActionHandler.postDelayed(() ->
                selectDrawerItem(menuItem.getItemId()),DRAWER_DELAY_MS);
        return true;
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.support.v7.appcompat.R.id.home) {
            return mDrawerToggle.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(NAV_ITEM_ID, mNavItemId);
    }

    private void saveDeviceIdToPrefs(Device device){
        if (device != null) {
            mPrefs.edit().putLong(Constants.PROPERTY_DEVICE_ID, device.getId()).apply();
        }
    }

    private void subscribeTopics() {
        FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance();
        for(String topic : Constants.FCM_TOPICS){
            firebaseMessaging.subscribeToTopic(topic);
        }

        mSubscriptions.add(mRxNetwork.checkInternetConnection()
                .andThen(mMoodleApi.getCourses(
                        mPrefs.getString(Constants.PROPERTY_MOODLE_TOKEN, ""),
                        mPrefs.getInt(Constants.PROPERTY_MOODLE_USER_ID, 0))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()))
                .subscribe(
                        response -> {
                            List<MoodleCourse> courses = response.body();
                            for(MoodleCourse course : courses){
                                firebaseMessaging.subscribeToTopic("course_" + course.getId().toString());
                            }
                        },
                        throwable -> {
                            Log.e(TAG,"Error getting courses");
                        }
                )
        );
    }

    private void handleGAEError(Throwable throwable) {
        Log.e(TAG,"Error @handleGAEError");
        Snackbar.make(mToolbar.getRootView(),
            getString(R.string.exception_message_generic),
            Snackbar.LENGTH_LONG)
            .show();
    }

    private void logCrashlyticsUser(){
        Long userId = mPrefs.getLong(Constants.PROPERTY_USER_ID, 0L);
        String userName = mPrefs.getString(Constants.PROPERTY_USER_NAME, "");
        if(userId > 0L && !TextUtils.isEmpty(userName)){
            Crashlytics.setUserIdentifier(userId.toString());
            Crashlytics.setUserName(userName);
        }
    }

    private void checkMoodleUser() {
        String user = mPrefs.getString(Constants.PROPERTY_USER_NAME, "");
        String pass = mPrefs.getString(Constants.PROPERTY_USER_PASS, "");
        mRxNetwork.checkInternetConnection()
                .andThen(mMoodleApi.existsUser(user,pass)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()))
                .subscribe(booleanResponse -> {
                            if(!booleanResponse.body()){
                                ResetPassActivity.launch(MainActivity.this);
                                finish();
                            }
                        },
                        throwable ->
                                Toast.makeText(this, R.string.exception_message_generic,Toast.LENGTH_LONG).show()
                );
    }

    public static void launch(Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        ActivityCompat.startActivity(activity, intent, null);
    }

    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION})
    public void setupAwarenessFences() {
        Log.i(TAG,"@setupAwarenessFences");
        AwarenessApiHelper.updateMainAwarenessFence(mContext,null);
    }

    @OnShowRationale({Manifest.permission.ACCESS_FINE_LOCATION})
    protected void showRationaleForAppPermissions(PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.app_permissions_required)
                .setMessage(R.string.app_permissions_rationale)
                .setPositiveButton(android.R.string.ok, (dialog, button) -> request.proceed())
                .setNegativeButton(android.R.string.cancel, (dialog, button) -> request.cancel())
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnPermissionDenied({Manifest.permission.ACCESS_FINE_LOCATION})
    public void onAppPermissionsDenied() {
        mPermissionsSnackbar = Snackbar.make(mNavigationView.getRootView(),
                getString(R.string.exception_message_permissions_denied),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.retry),
                        v -> MainActivityPermissionsDispatcher.setupAwarenessFencesWithCheck(this));
        mPermissionsSnackbar.show();
    }

    @OnNeverAskAgain({Manifest.permission.ACCESS_FINE_LOCATION})
    protected void onNeverAskAppPermissions() {
        mPermissionsSnackbar = Snackbar.make(mNavigationView.getRootView(),
                getString(R.string.exception_message_permissions_denied),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.settings), v -> {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                    intent.setData(uri);
                    this.startActivity(intent);
                });
        mPermissionsSnackbar.show();
    }
}
