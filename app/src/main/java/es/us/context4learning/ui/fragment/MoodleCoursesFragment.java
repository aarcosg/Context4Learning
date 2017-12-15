package es.us.context4learning.ui.fragment;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.fernandocejas.frodo.annotation.RxLogObservable;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import es.us.context4learning.Constants;
import es.us.context4learning.data.api.moodle.MoodleApi;
import es.us.context4learning.data.api.moodle.entity.Course;
import es.us.context4learning.exception.InternetNotAvailableException;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;
import es.us.context4learning.R;
import es.us.context4learning.di.components.MainComponent;
import es.us.context4learning.ui.adapter.CoursesAdapter;
import es.us.context4learning.utils.RxNetwork;

public class MoodleCoursesFragment extends BaseFragment {

    private static final String TAG = MoodleCoursesFragment.class.getCanonicalName();

    @Bind(R.id.recycler_view_courses)
    RecyclerView mRecyclerView;
    @Bind(R.id.progress_bar)
    ProgressBar mProgressBar;
    @Bind(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Inject
    RxNetwork mRxNetwork;
    @Inject
    MoodleApi mMoodleApi;
    @Inject
    SharedPreferences mPrefs;

    private Subscription mSubscription = Subscriptions.empty();
    private CoursesAdapter mAdapter;

    public static MoodleCoursesFragment newInstance() {
        MoodleCoursesFragment fragment = new MoodleCoursesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getComponent(MainComponent.class).inject(this);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_moodle_courses, container, false);
        ButterKnife.bind(this, rootView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        List<Course> courses = new ArrayList<>();
        mAdapter = new CoursesAdapter(courses);
        mRecyclerView.setAdapter(mAdapter);

        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary_light,
                R.color.primary,
                R.color.primary_dark);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshCourses();
            }
        });

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        refreshCourses();
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
        inflater.inflate(R.menu.menu_empty,menu);
    }

    private void refreshCourses(){
        String user = mPrefs.getString(Constants.PROPERTY_USER_NAME, "");
        String pass = mPrefs.getString(Constants.PROPERTY_USER_PASS, "");
        if(!TextUtils.isEmpty(user) && !TextUtils.isEmpty(pass)) {
            if (mProgressBar != null) mProgressBar.setVisibility(View.VISIBLE);
            mSubscription = getCoursesObservable(user,pass)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            this::bindCourses
                            , this::showMoodleErrorMessage
                    );
        }
    }

    private void bindCourses(List<Course> courses) {
        if(isAdded()){
            if (mProgressBar != null) mProgressBar.setVisibility(View.GONE);
            if(courses != null && !courses.isEmpty()){
                mAdapter.clear();
                mAdapter.addAll(courses);
            }
            if(mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()){
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    private void showMoodleErrorMessage(Throwable throwable) {
        if(isAdded()){
            if (mProgressBar != null) mProgressBar.setVisibility(View.GONE);
            if(mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()){
                mSwipeRefreshLayout.setRefreshing(false);
            }
            String message = getString(R.string.error_moodle_courses_not_loaded);
            if(throwable instanceof InternetNotAvailableException){
                message = getString(R.string.exception_message_no_connection);
            }
            Snackbar.make(mRecyclerView,
                    message,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(getString(R.string.retry),
                            v -> refreshCourses())
                    .show();
        }
    }

    @RxLogObservable
    private Observable<List<Course>> getCoursesObservable(String user, String pass){
        return mRxNetwork.checkInternetConnection()
                .andThen(mMoodleApi.getStudentProgress(user, pass)
                .flatMap(response -> Observable.just(response.body()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                );
    }
}
