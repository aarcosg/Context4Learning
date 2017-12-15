package es.us.context4learning.ui.fragment;


import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TimePicker;

import com.google.api.client.util.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import es.us.context4learning.Constants;
import es.us.context4learning.R;
import es.us.context4learning.backend.timeRestrictionApi.TimeRestrictionApi;
import es.us.context4learning.backend.timeRestrictionApi.model.TimeRestriction;
import es.us.context4learning.backend.timeRestrictionApi.model.User;
import es.us.context4learning.data.api.google.appengine.GAEHelper;
import es.us.context4learning.data.api.google.awareness.AwarenessApiHelper;
import es.us.context4learning.di.components.SettingsFragmentContainerComponent;
import es.us.context4learning.exception.InternetNotAvailableException;
import es.us.context4learning.exception.TimeRestrictionNotFoundGAEException;
import es.us.context4learning.exception.TimeRestrictionNotInsertedGAEException;
import es.us.context4learning.observable.appengine.GetTimeRestrictionListGAEObservable;
import es.us.context4learning.observable.appengine.InsertTimeRestrictionGAEObservable;
import es.us.context4learning.observable.appengine.RemoveTimeRestrictionGAEObservable;
import es.us.context4learning.ui.activity.SettingsFragmentContainerActivity;
import es.us.context4learning.ui.adapter.TimeRestrictionsAdapter;
import es.us.context4learning.ui.decorator.SimpleDividerItemDecoration;
import es.us.context4learning.utils.RxNetwork;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

public class TimeRestrictionFragment extends BaseFragment {

    private static final String TAG = TimeRestrictionFragment.class.getCanonicalName();

    private static final int DIALOG_START_TIME = 1;
    private static final int DIALOG_END_TIME = 2;

    @Inject
    Context mContext;
    @Inject
    SharedPreferences mPrefs;
    @Inject
    TimeRestrictionApi mGAETimeRestrictionApi;
    @Inject
    RxNetwork mRxNetwork;

    @Bind(R.id.progress_bar)
    ProgressBar mProgressBar;
    @Bind(R.id.time_restriction_empty)
    View mRestrictionsEmpty;
    @Bind(R.id.recycler_view_time_restrictions)
    RecyclerView mRecyclerView;
    @Bind(R.id.fab)
    FloatingActionButton mFab;
    @Bind(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipeRefreshLayout;

    private TimeRestrictionsAdapter mAdapter;
    private TimePickerDialog mTimePickerDialogStart;
    private TimePickerDialog mTimePickerDialogEnd;
    private Date mStartTime;
    private Subscription mSubscription = Subscriptions.empty();
    private Calendar mCalendar;

    public static TimeRestrictionFragment newInstance() {
        TimeRestrictionFragment fragment = new TimeRestrictionFragment();
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
            actionBar.setTitle(getString(R.string.title_time_restriction_activity));
            actionBar.setSubtitle(getString(R.string.dont_receive_notifications));
        }
        setHasOptionsMenu(true);
        setupCalendar();
        setupTimePickerDialogs();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_time_restriction, container, false);
        ButterKnife.bind(this, rootView);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));

        List<TimeRestriction> restrictions = new ArrayList<TimeRestriction>();
        mAdapter = new TimeRestrictionsAdapter(restrictions){
            @Override
            public boolean onLongClick(View view) {
                return onLongClickListener(view);
            }
        };
        mRecyclerView.setAdapter(mAdapter);

        mFab.setOnClickListener(v -> mTimePickerDialogStart.show());

        if(mSwipeRefreshLayout != null){
            mSwipeRefreshLayout.setColorSchemeResources(R.color.primary_light,
                    R.color.primary,
                    R.color.primary_dark);
            mSwipeRefreshLayout.setOnRefreshListener(this::refreshRestrictions);
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshRestrictions();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
        if(mTimePickerDialogStart.isShowing()){
            mTimePickerDialogStart.dismiss();
        }
        if(mTimePickerDialogEnd.isShowing()){
            mTimePickerDialogEnd.dismiss();
        }
        if(mSubscription.isUnsubscribed()){
            mSubscription.unsubscribe();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_empty, menu);
    }

    private void refreshRestrictions(){
        Long userId = mPrefs.getLong(Constants.PROPERTY_USER_ID,0L);
        if(userId > 0L){
            if (mProgressBar != null) mProgressBar.setVisibility(View.VISIBLE);
            mSubscription = mRxNetwork.checkInternetConnection()
                    .andThen(Observable.create(new GetTimeRestrictionListGAEObservable(mGAETimeRestrictionApi, userId))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()))
                    .subscribe(
                            this::bindTimeRestrictions
                            ,this::showErrorMessage
                    );
        }
    }

    private void bindTimeRestrictions(List<TimeRestriction> timeRestrictions) {
        if(isAdded()){
            if (mProgressBar != null) mProgressBar.setVisibility(View.GONE);
            if(timeRestrictions != null && !timeRestrictions.isEmpty()){
                mRestrictionsEmpty.setVisibility(View.GONE);
                mAdapter.clear();
                mAdapter.addAll(timeRestrictions);
            } else {
                mRestrictionsEmpty.setVisibility(View.VISIBLE);
            }
            if(mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()){
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    private boolean onLongClickListener(View view){
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(view.getContext());
        dialogBuilder.setTitle(mContext.getString(R.string.delete));
        dialogBuilder.setMessage(mContext.getString(R.string.ask_delete_restriction));
        dialogBuilder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            final TimeRestriction timeRestriction = (TimeRestriction) view.getTag();
            if (mProgressBar != null) mProgressBar.setVisibility(View.VISIBLE);
            mSubscription = mRxNetwork.checkInternetConnection()
                    .andThen(Observable.create(new RemoveTimeRestrictionGAEObservable(mGAETimeRestrictionApi, timeRestriction.getId()))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                    ).subscribe(
                            aVoid -> onTimeRestrictionRemoved(timeRestriction)
                            , this::showErrorMessage
                    );
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss());
        dialogBuilder.show();
        return true;
    }

    private void setupCalendar() {
        mCalendar = Calendar.getInstance();
        mCalendar.set(Calendar.SECOND,0);
        mCalendar.set(Calendar.DAY_OF_MONTH,1);
        mCalendar.set(Calendar.MONTH,0);
        mCalendar.set(Calendar.YEAR,1111);
    }

    private void setupTimePickerDialogs() {
        mTimePickerDialogStart = new TimePickerDialog(
                getContext()
                , (view, hourOfDay, minute) -> onTimeSet(DIALOG_START_TIME, view, hourOfDay, minute)
                , mCalendar.get(Calendar.HOUR_OF_DAY)
                , mCalendar.get(Calendar.MINUTE)
                , true);
        mTimePickerDialogStart.setTitle(getString(R.string.from));

        mTimePickerDialogEnd = new TimePickerDialog(
                getContext()
                , (view, hourOfDay, minute) -> onTimeSet(DIALOG_END_TIME, view, hourOfDay, minute)
                , mCalendar.get(Calendar.HOUR_OF_DAY)
                , mCalendar.get(Calendar.MINUTE)
                , true);
        mTimePickerDialogEnd.setTitle(getString(R.string.to));

    }

    private void onTimeSet(int key, TimePicker view, int hourOfDay, int minute) {
        if(view.isShown()){
            if(key == DIALOG_START_TIME){
                mCalendar.set(Calendar.HOUR_OF_DAY,hourOfDay);
                mCalendar.set(Calendar.MINUTE,minute);
                mStartTime = mCalendar.getTime();
                mTimePickerDialogEnd.show();
            } else {
                mCalendar.set(Calendar.HOUR_OF_DAY,hourOfDay);
                mCalendar.set(Calendar.MINUTE,minute);
                Date endTime = mCalendar.getTime();
                if(mStartTime.after(endTime)) {
                    Snackbar.make(mFab,getString(R.string.error_starttime_gt_endtime),Snackbar.LENGTH_LONG).show();
                }else{
                    Long userId = mPrefs.getLong(Constants.PROPERTY_USER_ID, 0L);
                    if (userId > 0L) {
                        TimeRestriction timeRestriction = new TimeRestriction();
                        timeRestriction.setStartTime(new DateTime(mStartTime));
                        timeRestriction.setEndTime(new DateTime(endTime));
                        User user = new User();
                        user.setId(userId);
                        timeRestriction.setUser(user);
                        mSubscription = mRxNetwork.checkInternetConnection()
                                .andThen(Observable.create(new InsertTimeRestrictionGAEObservable(mGAETimeRestrictionApi, timeRestriction))
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                ).subscribe(
                                        this::onTimeRestrictionInserted
                                        , this::showErrorMessage
                                );
                    }
                }
            }
        }
    }

    private void onTimeRestrictionInserted(TimeRestriction timeRestriction){
        if(isAdded()){
            if (mProgressBar != null) mProgressBar.setVisibility(View.GONE);
            if (timeRestriction != null) {
                Log.d(TAG, "New time restriction added: " + timeRestriction.getStartTime().toString() + " - " + timeRestriction.getEndTime().toString());
                if(mRecyclerView.getVisibility() == View.GONE){
                    mRecyclerView.setVisibility(View.VISIBLE);
                }
                if(mRestrictionsEmpty.getVisibility() == View.VISIBLE){
                    mRestrictionsEmpty.setVisibility(View.GONE);
                }
                mAdapter.add(timeRestriction);
                GAEHelper.sendAuditEventToServer(
                        mContext,
                        Constants.AUDIT_CATEGORY_TIME_RESTRICTION,
                        Constants.AUDIT_ACTION_ADDED,
                        mPrefs.getString(Constants.PROPERTY_USER_NAME, "") + " - [" + timeRestriction.getStartTime().toString() + " - " + timeRestriction.getEndTime().toString() + "]");
                // Update awareness fences
                AwarenessApiHelper.updateMainAwarenessFence(mContext, null);
            }
        }
    }

    private void onTimeRestrictionRemoved(TimeRestriction timeRestriction){
        if(isAdded()){
            // Update awareness fences
            AwarenessApiHelper.updateMainAwarenessFence(mContext,null);
            if (mProgressBar != null) mProgressBar.setVisibility(View.GONE);
            mAdapter.remove(timeRestriction);
            if(mAdapter.getItemCount() == 0
                    && mRecyclerView.getVisibility() == View.VISIBLE
                    && mRestrictionsEmpty.getVisibility() == View.GONE){
                mRecyclerView.setVisibility(View.GONE);
                mRestrictionsEmpty.setVisibility(View.VISIBLE);
            }
            Snackbar.make(mFab,
                    mContext.getString(R.string.restriction_deleted),
                    Snackbar.LENGTH_LONG)
                    .show();
            GAEHelper.sendAuditEventToServer(
                    mContext,
                    Constants.AUDIT_CATEGORY_TIME_RESTRICTION,
                    Constants.AUDIT_ACTION_REMOVED,
                    mPrefs.getString(Constants.PROPERTY_USER_NAME, "") + " - ["
                            + timeRestriction.getStartTime().toString() + " - "
                            + timeRestriction.getEndTime().toString() + "]");
        }
    }

    private void showErrorMessage(Throwable throwable) {
        if(isAdded()){
            if (mProgressBar != null) mProgressBar.setVisibility(View.GONE);
            if(mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()){
                mSwipeRefreshLayout.setRefreshing(false);
            }
            String message = getString(R.string.error_restrictions_not_loaded);
            if(throwable instanceof InternetNotAvailableException){
                message = getString(R.string.exception_message_no_connection);
            }else if(throwable instanceof TimeRestrictionNotFoundGAEException){
                message = getString(R.string.exception_message_restriction_not_found);
            }else if(throwable instanceof TimeRestrictionNotInsertedGAEException){
                message = getString(R.string.error_restriction_not_added);
            }
            Snackbar.make(mFab,
                    message,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(getString(R.string.retry),
                            v -> refreshRestrictions())
                    .show();
        }
    }

}
