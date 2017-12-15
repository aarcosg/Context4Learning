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

import com.fernandocejas.frodo.annotation.RxLogObservable;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import es.us.context4learning.Constants;
import es.us.context4learning.R;
import es.us.context4learning.backend.userApi.UserApi;
import es.us.context4learning.backend.userApi.model.User;
import es.us.context4learning.data.api.moodle.MoodleApi;
import es.us.context4learning.data.api.moodle.entity.response.AuthTokenResponse;
import es.us.context4learning.di.components.DaggerLoginComponent;
import es.us.context4learning.observable.appengine.InsertUserGAEObservable;
import es.us.context4learning.utils.RxNetwork;
import retrofit2.Response;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends BaseActivity {

    private static final String TAG = LoginActivity.class.getCanonicalName();

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
    @Bind(R.id.username_et)
    EditText mUsernameET;
    @Bind(R.id.password_et)
    EditText mPasswordET;
    @Bind(R.id.signin_btn)
    Button mSignInBtn;
    @Bind(R.id.form_ll)
    LinearLayout mFormLL;

    private Subscription mSubscription = Subscriptions.empty();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.initializeInjector();
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        buildActionBarToolbar(getString(R.string.title_login_activity), false);
        matchStatusBarHeight();
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
            onClickSignInBtn();
            return true;
        }
        return false;
    }

    @OnClick(R.id.signin_btn)
    void onClickSignInBtn(){
        attemptLogin();
    }

    private void initializeInjector() {
        DaggerLoginComponent.builder()
            .applicationComponent(getApplicationComponent())
            .activityModule(getActivityModule())
            .build()
            .inject(this);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        // Reset errors.
        mUsernameET.setError(null);
        mPasswordET.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameET.getText().toString();
        String password = mPasswordET.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password
        if (TextUtils.isEmpty(password)) {
            mPasswordET.setError(getString(R.string.error_field_required));
            focusView = mPasswordET;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            mUsernameET.setError(getString(R.string.error_field_required));
            focusView = mUsernameET;
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
                .andThen(mMoodleApi.getAuthToken(username, password, MoodleApi.REST_SERVICE_MOBILE_APP)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()))
                .subscribe(
                    this::handleAuthResponse,
                    throwable -> {
                        handleAuthError(throwable);
                        showProgress(false);
                    }
                );
        }
    }

    private void handleAuthResponse(Response<AuthTokenResponse> response) {
        AuthTokenResponse authTokenResponse = response.body();
        if (!TextUtils.isEmpty(authTokenResponse.getToken())) {
            saveUserInGAE(
                mUsernameET.getText().toString(),
                mPasswordET.getText().toString(),
                authTokenResponse.getToken()
            );
        } else {
            showProgress(false);
            mUsernameET.setError(authTokenResponse.getError());
        }
    }

    @RxLogObservable
    private void saveUserInGAE(String username, String password, String token) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setMoodleToken(token);
        user.setMoodleServerName(MoodleApi.SERVER_NAME);
        mSubscription = Observable.create(new InsertUserGAEObservable(mGAEUserApi, user))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                this::handleGAEUser,
                throwable -> {
                    handleGAEError(throwable);
                    showProgress(false);
                }
            );
    }

    private void handleGAEUser(User user) {
        showProgress(false);
        if (user != null && user.getId() != null) {
            mPrefs.edit()
                    .putLong(Constants.PROPERTY_USER_ID, user.getId())
                    .putString(Constants.PROPERTY_USER_NAME, user.getUsername())
                    .putString(Constants.PROPERTY_USER_PASS, user.getPassword())
                    .putString(Constants.PROPERTY_MOODLE_TOKEN, user.getMoodleToken())
                    .commit();
           MainActivity.launch(LoginActivity.this);
           finish();
        } else if (user != null) {
            mUsernameET.setError(getString(R.string.error_user_taken));
            mUsernameET.requestFocus();
        } else {
            mPasswordET.setError(getString(R.string.error_incorrect_password));
            mPasswordET.requestFocus();
        }
    }

    private void handleAuthError(Throwable throwable) {
        Snackbar.make(mUsernameET.getRootView(),
                getString(R.string.exception_message_generic),
                Snackbar.LENGTH_LONG)
                .show();
    }

    private void handleGAEError(Throwable throwable) {
        Snackbar.make(mUsernameET.getRootView(),
                getString(R.string.exception_message_generic),
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
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        ActivityCompat.startActivity(activity, intent, null);
    }
}



