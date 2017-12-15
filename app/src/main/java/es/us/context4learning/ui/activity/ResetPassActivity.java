package es.us.context4learning.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fernandocejas.frodo.annotation.RxLogObservable;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import es.us.context4learning.Constants;
import es.us.context4learning.R;
import es.us.context4learning.backend.userApi.UserApi;
import es.us.context4learning.chrometabs.CustomTabActivityHelper;
import es.us.context4learning.data.api.moodle.MoodleApi;
import es.us.context4learning.di.components.DaggerResetPassComponent;
import es.us.context4learning.exception.UserNotFoundGAEException;
import es.us.context4learning.observable.appengine.GetUserGAEObservable;
import es.us.context4learning.observable.appengine.UpdateUserGAEObservable;
import es.us.context4learning.utils.RxNetwork;
import es.us.context4learning.utils.Utils;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;


/**
 * A login screen that offers reset Moodle password.
 */
public class ResetPassActivity extends BaseActivity {

    private static final String TAG = ResetPassActivity.class.getCanonicalName();

    @Inject
    SharedPreferences mPrefs;
    @Inject
    RxNetwork mRxNetwork;
    @Inject
    MoodleApi mMoodleApi;
    @Inject
    UserApi mGAEUserApi;

    @Bind(R.id.progress_bar)
    ProgressBar mProgressBar;
    @Bind(R.id.password_et)
    EditText mPasswordET;
    @Bind(R.id.current_user_tv)
    TextView mCurrentUsernameET;
    @Bind(R.id.save_btn)
    Button mSaveBtn;
    @Bind(R.id.form_ll)
    LinearLayout mFormLL;
    @Bind(R.id.goto_moodle_btn)
    Button mGoToMoodleBtn;

    private Subscription mSubscription = Subscriptions.empty();
    private String mUsername;
    private CustomTabActivityHelper mCustomTabActivityHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.initializeInjector();
        setContentView(R.layout.activity_reset_pass);
        ButterKnife.bind(this);
        buildActionBarToolbar(getString(R.string.title_reset_pass_activity), false);
        matchStatusBarHeight();
        mCustomTabActivityHelper = new CustomTabActivityHelper();
        mUsername = mPrefs.getString(Constants.PROPERTY_USER_NAME, "");
        mCurrentUsernameET.setText(getString(R.string.current_user, mUsername));

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
    protected void onDestroy() {
        super.onDestroy();
        if (mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
        ButterKnife.unbind(this);
    }

    @OnEditorAction(R.id.password_et)
    boolean onPasswordEdited(int id){
        if (id == R.id.action_login || id == EditorInfo.IME_NULL) {
            onClickSaveBtn();
            return true;
        }
        return false;
    }

    @OnClick(R.id.save_btn)
    void onClickSaveBtn(){
        attemptPassReset();
    }

    @OnClick(R.id.goto_moodle_btn)
    void onClickGoToMoodleBtn(){
        Utils.openChromeTab(this, mCustomTabActivityHelper, MoodleApi.SERVICE_ENDPOINT);
    }

    private void initializeInjector() {
        DaggerResetPassComponent.builder()
            .applicationComponent(getApplicationComponent())
            .activityModule(getActivityModule())
            .build()
            .inject(this);
    }

    public void attemptPassReset() {
        // Reset errors.
        mPasswordET.setError(null);

        // Store values at the time of the saving attempt.
        String password = mPasswordET.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password
        if (TextUtils.isEmpty(password)) {
            mPasswordET.setError(getString(R.string.error_field_required));
            focusView = mPasswordET;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mRxNetwork.checkInternetConnection()
                .andThen(mMoodleApi.existsUser(mUsername, password)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()))
                .subscribe(
                    booleanResponse -> {
                        if(booleanResponse.body()){
                            updateUserInGAE(password);
                        }else{
                            showProgress(false);
                            mPasswordET.setError(getString(R.string.error_pass_doesnt_match_moodle));
                            mPasswordET.requestFocus();
                        }
                    },
                        this::handleGAEError
                );
        }
    }

    @RxLogObservable
    private void updateUserInGAE(String newPassword) {
        Long userId = mPrefs.getLong(Constants.PROPERTY_USER_ID, 0L);
        mRxNetwork.checkInternetConnection()
                .andThen(
                        Observable.create(new GetUserGAEObservable(mGAEUserApi,userId))
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                ).doOnError(this::handleGAEError)
                .flatMap(user -> {
                    user.setPassword(newPassword);
                    return Observable.create(new UpdateUserGAEObservable(mGAEUserApi,userId,user))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread());
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        user -> savePasswordToPrefs(user.getPassword())
                        ,this::handleGAEError
                );
    }

    private void savePasswordToPrefs(String password) {
        showProgress(false);
        mPrefs.edit()
                .putString(Constants.PROPERTY_USER_PASS, password)
                .commit();
       MainActivity.launch(ResetPassActivity.this);
       finish();
    }

    private void handleGAEError(Throwable throwable) {
        showProgress(false);
        String message = getString(R.string.exception_message_generic);
        if(throwable instanceof UserNotFoundGAEException){
            message = getString(R.string.exception_message_user_not_found_gae);
        }
        Snackbar.make(mPasswordET.getRootView(),
                message,
                Snackbar.LENGTH_LONG)
                .show();
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {
        mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        mFormLL.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    public static void launch(Activity activity) {
        Intent intent = new Intent(activity, ResetPassActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        ActivityCompat.startActivity(activity, intent, null);
    }
}



