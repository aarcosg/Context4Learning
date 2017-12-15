package es.us.context4learning.ui.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.us.context4learning.Constants;
import es.us.context4learning.R;
import es.us.context4learning.backend.limitNotificationApi.LimitNotificationApi;
import es.us.context4learning.backend.limitNotificationApi.model.LimitNotification;
import es.us.context4learning.backend.limitNotificationApi.model.User;
import es.us.context4learning.data.api.google.appengine.GAEHelper;
import es.us.context4learning.di.components.SettingsFragmentContainerComponent;
import es.us.context4learning.exception.InternetNotAvailableException;
import es.us.context4learning.exception.LimitNotificationNotFoundGAEException;
import es.us.context4learning.observable.appengine.GetNotificationLimitGAEObservable;
import es.us.context4learning.observable.appengine.UpdateNotificationLimitGAEObservable;
import es.us.context4learning.ui.activity.SettingsFragmentContainerActivity;
import es.us.context4learning.utils.RxNetwork;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

public class LimitNotificationsFragment extends BaseFragment {

    private static final String TAG = LimitNotificationsFragment.class.getCanonicalName();

    @Inject
    Context mContext;
    @Inject
    SharedPreferences mPrefs;
    @Inject
    LimitNotificationApi mGAELimitNotificationApi;
    @Inject
    RxNetwork mRxNetwork;

    @Bind(R.id.progress_bar)
    ProgressBar mProgressBar;
    @Bind(R.id.morning_counter)
    TextView mCounterMorning;
    @Bind(R.id.evening_counter)
    TextView mCounterEvening;
    @Bind(R.id.night_counter)
    TextView mCounterNight;
    @Bind(R.id.morning_seekbar)
    SeekBar mSeekBarMorning;
    @Bind(R.id.evening_seekbar)
    SeekBar mSeekBarEvening;
    @Bind(R.id.night_seekbar)
    SeekBar mSeekBarNight;

    private Subscription mSubscription = Subscriptions.empty();

    public static LimitNotificationsFragment newInstance() {
        LimitNotificationsFragment fragment = new LimitNotificationsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getComponent(SettingsFragmentContainerComponent.class).inject(this);
        ActionBar actionBar = ((SettingsFragmentContainerActivity)getActivity()).getSupportActionBar();
        if(actionBar != null){
            actionBar.setTitle(getString(R.string.title_limit_notifications_activity));
        }
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_limit_notifications, container, false);
        ButterKnife.bind(this, rootView);

        mSeekBarMorning.setOnSeekBarChangeListener(new CustomOnSeekBarChangeListener(mCounterMorning));
        mSeekBarEvening.setOnSeekBarChangeListener(new CustomOnSeekBarChangeListener(mCounterEvening));
        mSeekBarNight.setOnSeekBarChangeListener(new CustomOnSeekBarChangeListener(mCounterNight));

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        refreshLimits();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
        if(mSubscription.isUnsubscribed()){
            mSubscription.unsubscribe();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_empty, menu);
    }

    private void refreshLimits(){
        Long userId = mPrefs.getLong(Constants.PROPERTY_USER_ID,0L);
        if(userId > 0L){
            if (mProgressBar != null) mProgressBar.setVisibility(View.VISIBLE);
            mSubscription = mRxNetwork.checkInternetConnection()
                    .andThen(Observable.create(new GetNotificationLimitGAEObservable(mGAELimitNotificationApi, userId))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread()))
                    .subscribe(
                            this::bindNotificationLimits
                            ,this::showErrorMessage
                    );
        }
    }

    private void bindNotificationLimits(LimitNotification limitNotification) {
        if(isAdded()){
            if (mProgressBar != null) mProgressBar.setVisibility(View.GONE);
            if(limitNotification != null){
                mCounterMorning.setText(limitNotification.getMorning().toString());
                mCounterEvening.setText(limitNotification.getEvening().toString());
                mCounterNight.setText(limitNotification.getNight().toString());

                mSeekBarMorning.setProgress(limitNotification.getMorning());
                mSeekBarEvening.setProgress(limitNotification.getEvening());
                mSeekBarNight.setProgress(limitNotification.getNight());
            }else{
                Log.e(TAG,"limitNotification == null");
            }
        }
    }

    private void showErrorMessage(Throwable throwable) {
        if(isAdded()){
            if (mProgressBar != null) mProgressBar.setVisibility(View.GONE);
            String message = getString(R.string.error_limits_not_loaded);
            if(throwable instanceof InternetNotAvailableException){
                message = getString(R.string.exception_message_no_connection);
            }else if(throwable instanceof LimitNotificationNotFoundGAEException){
                message = getString(R.string.exception_message_notification_limits_not_found);
            }
            Snackbar.make(mProgressBar,
                    message,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(getString(R.string.retry),
                            v -> refreshLimits())
                    .show();
        }
    }

    @OnClick(R.id.save_limits_btn)
    public void saveNotificationLimits(){
        Long userId = mPrefs.getLong(Constants.PROPERTY_USER_ID,0L);
        if(userId > 0L){
            if (mProgressBar != null) mProgressBar.setVisibility(View.VISIBLE);
            LimitNotification limitNotification = new LimitNotification();
            User user = new User();
            user.setId(userId);
            limitNotification.setUser(user);
            limitNotification.setMorning(Integer.valueOf(mCounterMorning.getText().toString()));
            limitNotification.setEvening(Integer.valueOf(mCounterEvening.getText().toString()));
            limitNotification.setNight(Integer.valueOf(mCounterNight.getText().toString()));
            mSubscription = mRxNetwork.checkInternetConnection()
                    .andThen(Observable.create(new UpdateNotificationLimitGAEObservable(mGAELimitNotificationApi, limitNotification))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread()))
                    .subscribe(
                            this::onNotificationLimitsUpdated
                            ,this::showErrorMessage
                    );
        }
    }

    private void onNotificationLimitsUpdated(LimitNotification limitNotification) {
        if(isAdded()){
            if (mProgressBar != null) mProgressBar.setVisibility(View.GONE);
            if (limitNotification != null) {
                Log.d(TAG, "Edited limit notifications: [" + limitNotification.getMorning()
                        + "," + limitNotification.getEvening()
                        + "," + limitNotification.getNight() + "]");
                Snackbar.make(
                        mProgressBar,
                        getString(R.string.limits_updated),
                        Snackbar.LENGTH_LONG)
                        .show();
                GAEHelper.sendAuditEventToServer(
                        mContext,
                        Constants.AUDIT_CATEGORY_LIMIT_NOTIFICATION,
                        Constants.AUDIT_ACTION_EDITED,
                        mPrefs.getString(Constants.PROPERTY_USER_NAME, "") + " - [" + limitNotification.getMorning() + "," + limitNotification.getEvening() + "," + limitNotification.getNight() + "]");
            }
        }
    }

    private class CustomOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        private TextView mCounter;

        CustomOnSeekBarChangeListener(TextView counter){
            this.mCounter = counter;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            //progress = progress + 1;
            mCounter.setText(Integer.valueOf(progress).toString());
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    }
}
