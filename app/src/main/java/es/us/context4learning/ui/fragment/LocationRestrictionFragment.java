package es.us.context4learning.ui.fragment;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import es.us.context4learning.Constants;
import es.us.context4learning.R;
import es.us.context4learning.backend.locationRestrictionApi.LocationRestrictionApi;
import es.us.context4learning.backend.locationRestrictionApi.model.GeoPt;
import es.us.context4learning.backend.locationRestrictionApi.model.LocationRestriction;
import es.us.context4learning.data.api.google.appengine.GAEHelper;
import es.us.context4learning.data.api.google.awareness.AwarenessApiHelper;
import es.us.context4learning.di.components.SettingsFragmentContainerComponent;
import es.us.context4learning.exception.InternetNotAvailableException;
import es.us.context4learning.exception.LocationRestrictionNotFoundGAEException;
import es.us.context4learning.observable.appengine.GetLocationRestrictionListGAEObservable;
import es.us.context4learning.observable.appengine.RemoveLocationRestrictionGAEObservable;
import es.us.context4learning.ui.activity.MapActivity;
import es.us.context4learning.ui.activity.SettingsFragmentContainerActivity;
import es.us.context4learning.ui.adapter.LocationRestrictionsAdapter;
import es.us.context4learning.ui.decorator.SimpleDividerItemDecoration;
import es.us.context4learning.utils.RxNetwork;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

public class LocationRestrictionFragment extends BaseFragment {

    private static final String TAG = LocationRestrictionFragment.class.getCanonicalName();
    private static final int REQUEST_CODE_MAP = 1;

    @Inject
    Context mContext;
    @Inject
    SharedPreferences mPrefs;
    @Inject
    LocationRestrictionApi mGAELocationRestrictionApi;
    @Inject
    RxNetwork mRxNetwork;

    @Bind(R.id.progress_bar)
    ProgressBar mProgressBar;
    @Bind(R.id.location_restriction_empty)
    View mRestrictionsEmpty;
    @Bind(R.id.recycler_view_location_restrictions)
    RecyclerView mRecyclerView;
    @Bind(R.id.fab)
    FloatingActionButton mFab;
    @Bind(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipeRefreshLayout;

    private LocationRestrictionsAdapter mAdapter;
    private Subscription mSubscription = Subscriptions.empty();

    public static LocationRestrictionFragment newInstance() {
        LocationRestrictionFragment fragment = new LocationRestrictionFragment();
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
            actionBar.setTitle(getString(R.string.title_location_restriction_activity));
            actionBar.setSubtitle(getString(R.string.dont_receive_notifications));
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_location_restriction, container, false);
        ButterKnife.bind(this, rootView);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        List<LocationRestriction> restrictions = new ArrayList<LocationRestriction>();
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        mAdapter = new LocationRestrictionsAdapter(restrictions){
            @Override
            public boolean onLongClick(View view) {
                return onLongClickListener(view);
            }
        };
        mRecyclerView.setAdapter(mAdapter);

        mFab.setOnClickListener(v ->
                startActivityForResult(new Intent(getActivity(), MapActivity.class), REQUEST_CODE_MAP));

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
        if(mSubscription.isUnsubscribed()){
            mSubscription.unsubscribe();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_MAP) {
            if(resultCode == Activity.RESULT_OK){
                LocationRestriction  locationRestriction = new LocationRestriction();
                locationRestriction.setId(data.getLongExtra(Constants.EXTRA_ID,0L));
                GeoPt geoPt = new GeoPt();
                geoPt.setLatitude(data.getFloatExtra(Constants.EXTRA_LATITUDE,0));
                geoPt.setLongitude(data.getFloatExtra(Constants.EXTRA_LONGITUDE,0));
                locationRestriction.setLocation(geoPt);
                locationRestriction.setAddress(data.getStringExtra(Constants.EXTRA_ADDRESS));
                if(mRecyclerView.getVisibility() == View.GONE){
                    mRecyclerView.setVisibility(View.VISIBLE);
                }
                if(mRestrictionsEmpty.getVisibility() == View.VISIBLE){
                    mRestrictionsEmpty.setVisibility(View.GONE);
                }
                mAdapter.add(locationRestriction);
                GAEHelper.sendAuditEventToServer(
                        mContext,
                        Constants.AUDIT_CATEGORY_LOCATION_RESTRICTION,
                        Constants.AUDIT_ACTION_ADDED,
                        mPrefs.getString(Constants.PROPERTY_USER_NAME, "") + " - " + locationRestriction.getLocation().toString());
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //There's no result
            }
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
                    .andThen(Observable.create(new GetLocationRestrictionListGAEObservable(mGAELocationRestrictionApi, userId))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread()))
                    .subscribe(
                            this::bindLocationRestrictions
                            ,this::showErrorMessage
                    );
        }
    }

    private boolean onLongClickListener(View view){
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(view.getContext());
        dialogBuilder.setTitle(mContext.getString(R.string.delete));
        dialogBuilder.setMessage(mContext.getString(R.string.ask_delete_restriction));
        dialogBuilder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            final LocationRestriction locationRestriction = (LocationRestriction) view.getTag();
            if (mProgressBar != null) mProgressBar.setVisibility(View.VISIBLE);
            mSubscription = mRxNetwork.checkInternetConnection()
                    .andThen(Observable.create(new RemoveLocationRestrictionGAEObservable(mGAELocationRestrictionApi, locationRestriction.getId()))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                    ).subscribe(
                            aVoid -> onLocationRestrictionRemoved(locationRestriction)
                            , this::showErrorMessage
                    );
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss());
        dialogBuilder.show();
        return true;
    }


    private void bindLocationRestrictions(List<LocationRestriction> locationRestrictions) {
        if(isAdded()){
            if (mProgressBar != null) mProgressBar.setVisibility(View.GONE);
            if(locationRestrictions != null && !locationRestrictions.isEmpty()){
                mRestrictionsEmpty.setVisibility(View.GONE);
                mAdapter.clear();
                mAdapter.addAll(locationRestrictions);
            } else {
                mRestrictionsEmpty.setVisibility(View.VISIBLE);
            }
            if(mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()){
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    private void onLocationRestrictionRemoved(LocationRestriction locationRestriction) {
        if(isAdded()){
            // Update awareness fences
            AwarenessApiHelper.updateMainAwarenessFence(mContext,null);
            if (mProgressBar != null) mProgressBar.setVisibility(View.GONE);
            mAdapter.remove(locationRestriction);
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
                    Constants.AUDIT_CATEGORY_LOCATION_RESTRICTION,
                    Constants.AUDIT_ACTION_REMOVED,
                    mPrefs.getString(Constants.PROPERTY_USER_NAME, "") + " - " + locationRestriction.getLocation().toString());
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
            }else if(throwable instanceof LocationRestrictionNotFoundGAEException){
                message = getString(R.string.exception_message_restriction_not_found);
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
