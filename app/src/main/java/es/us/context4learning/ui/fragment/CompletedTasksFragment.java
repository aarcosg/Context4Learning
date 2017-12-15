package es.us.context4learning.ui.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.eowise.recyclerview.stickyheaders.StickyHeadersBuilder;
import com.eowise.recyclerview.stickyheaders.StickyHeadersItemDecoration;
import com.fernandocejas.frodo.annotation.RxLogObservable;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import es.us.context4learning.Constants;
import es.us.context4learning.R;
import es.us.context4learning.chrometabs.CustomTabActivityHelper;
import es.us.context4learning.data.api.google.appengine.GAEHelper;
import es.us.context4learning.data.api.moodle.MoodleApi;
import es.us.context4learning.data.api.moodle.MoodleHelper;
import es.us.context4learning.data.api.moodle.entity.Course;
import es.us.context4learning.data.api.moodle.entity.Task;
import es.us.context4learning.di.components.MainComponent;
import es.us.context4learning.di.components.MoodleTasksComponent;
import es.us.context4learning.exception.InternetNotAvailableException;
import es.us.context4learning.ui.adapter.StickyHeaderTasksAdapter;
import es.us.context4learning.ui.adapter.TasksAdapter;
import es.us.context4learning.utils.RxNetwork;
import es.us.context4learning.utils.Utils;
import retrofit2.Response;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;


public class CompletedTasksFragment extends BaseFragment {

    private static final String TAG = CompletedTasksFragment.class.getCanonicalName();
    private static final String EMPTY_RESULTS_ERROR_MSG = "Expected BEGIN_ARRAY but was BEGIN_OBJECT at line 1 column 2 path $";

    @Bind(R.id.progress_bar)
    ProgressBar mProgressBar;
    @Bind(R.id.recycler_view_tasks)
    RecyclerView mRecyclerView;
    @Bind(R.id.tasks_done)
    View mTasksEmpty;
    @Nullable @Bind(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Inject
    Context mContext;
    @Inject
    RxNetwork mRxNetwork;
    @Inject
    MoodleApi mMoodleApi;
    @Inject
    SharedPreferences mPrefs;
    @Inject
    Tracker mTracker;

    private TasksAdapter mAdapter;
    private Course mCourse;
    private boolean mShowSingleCourse;
    private CustomTabActivityHelper mCustomTabActivityHelper;
    private Subscription mSubscription = Subscriptions.empty();

    public static CompletedTasksFragment newInstance(@Nullable Course course) {
        CompletedTasksFragment fragment = new CompletedTasksFragment();
        Bundle args = new Bundle();
        if(course != null){
            args.putParcelable(Constants.EXTRA_COURSE, course);
        }
        fragment.setArguments(args);
        return fragment;
    }

    public CompletedTasksFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null && getArguments().containsKey(Constants.EXTRA_COURSE)) {
            this.getComponent(MoodleTasksComponent.class).inject(this);
            mCourse = getArguments().getParcelable(Constants.EXTRA_COURSE);
            setHasOptionsMenu(true);
        }else{
            this.getComponent(MainComponent.class).inject(this);
        }
        mShowSingleCourse = mCourse != null;
        mCustomTabActivityHelper = new CustomTabActivityHelper();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = null;
        if(mShowSingleCourse){
            rootView = inflater.inflate(R.layout.fragment_completed_tasks, container, false);
        }else{
            rootView = inflater.inflate(R.layout.fragment_completed_tasks_swipe_refresh, container, false);
        }
        ButterKnife.bind(this,rootView);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        List<Task> tasks = new ArrayList<Task>();
        Map<Long, Course> courses = new LinkedHashMap<Long, Course>();

        mAdapter = new TasksAdapter(tasks, courses){
            @Override
            public void onClick(View view) {
                Task task = (Task) view.getTag();
                String user = mPrefs.getString(Constants.PROPERTY_USER_NAME, "");
                String pass = mPrefs.getString(Constants.PROPERTY_USER_PASS, "");
                if(!TextUtils.isEmpty(user) && !TextUtils.isEmpty(pass)){
                    String url = MoodleApi.MOODLE_TASK_ACCESS_URL + "?user=" + user + "&pass=" + pass + "&url_acceso=" + task.getUrl();
                    Utils.openChromeTab(getActivity(), mCustomTabActivityHelper, url);
                    GAEHelper.sendAuditEventToServer(
                            mContext,
                            Constants.AUDIT_CATEGORY_TASK,
                            Constants.AUDIT_ACTION_OPENED,
                            user + " - " + task.getName());
                }
            }
        };
        mRecyclerView.setAdapter(mAdapter);

        if (!mShowSingleCourse) {
            // Build item decoration and add it to the RecyclerView
            StickyHeadersItemDecoration decoration = new StickyHeadersBuilder()
                    .setAdapter(mAdapter)
                    .setRecyclerView(mRecyclerView)
                    .setStickyHeadersAdapter(
                            new StickyHeaderTasksAdapter(tasks, courses), // Class that implements StickyHeadersAdapter
                            false)     // Decoration position relative to a item
                    .build();
            mRecyclerView.addItemDecoration(decoration);

            mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh);
            mSwipeRefreshLayout.setColorSchemeResources(R.color.primary_light,
                    R.color.primary,
                    R.color.primary_dark);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    refreshTasks();
                }
            });
        }

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        refreshTasks();
    }

    @Override
    public void onStart() {
        super.onStart();
        mCustomTabActivityHelper.bindCustomTabsService(getActivity());
    }

    @Override
    public void onStop() {
        super.onStop();
        mCustomTabActivityHelper.unbindCustomTabsService(getActivity());
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
        if(mShowSingleCourse){
            inflater.inflate(R.menu.menu_completed_tasks,menu);
            menu.findItem(R.id.action_forum).setVisible(false);
            menu.findItem(R.id.action_marks).setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String screenName = "";
        switch (item.getItemId()){
            case R.id.action_refresh:
                refreshTasks();
                break;
            case R.id.action_forum:
                screenName = Constants.SCREEN_NAME_MOODLE_MESSAGES_WEB;
                if(mShowSingleCourse){
                    MoodleHelper.openMoodleMessagesWeb(getActivity(), mCustomTabActivityHelper, mCourse.getId());
                }else{
                    MoodleHelper.openMoodleMessagesWeb(getActivity(), mCustomTabActivityHelper, null);
                }
                break;
            case R.id.action_marks:
                if(mCourse != null)
                    screenName = "Completed Tasks " + Constants.SCREEN_NAME_STUDENT_PROGRESS_REPORT;
                    MoodleHelper.openMoodleMarksWeb(getActivity(), mCustomTabActivityHelper, mCourse.getId());
                break;
        }

        mTracker.setScreenName(screenName);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        GAEHelper.sendAuditEventToServer(
                mContext,
                Constants.AUDIT_CATEGORY_SCREEN_VIEW,
                Constants.AUDIT_ACTION_HIT,
                mPrefs.getString(Constants.PROPERTY_USER_NAME, "") + " - " + screenName);

        return super.onOptionsItemSelected(item);
    }

    private void refreshTasks(){
        String user = mPrefs.getString(Constants.PROPERTY_USER_NAME, "");
        String pass = mPrefs.getString(Constants.PROPERTY_USER_PASS, "");
        if(!TextUtils.isEmpty(user) && !TextUtils.isEmpty(pass)) {
            if (mProgressBar != null) mProgressBar.setVisibility(View.VISIBLE);
            mSubscription = getCompletedTasksObservable(user,pass)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            this::bindTasksAndCourses
                            , this::showMoodleErrorMessage
                    );
        }
    }

    private void bindTasksAndCourses(Pair<List<Task>,Map<Long,Course>> data){
        if(isAdded()){
            if (mProgressBar != null) mProgressBar.setVisibility(View.GONE);
            List<Task> tasks = data.first;
            Map<Long,Course> courses = data.second;
            if(tasks != null && !tasks.isEmpty() && courses != null && !courses.isEmpty()){
                mAdapter.clear();
                mAdapter.addAll(tasks,courses);
                if(mRecyclerView.getVisibility() == View.GONE){
                    mRecyclerView.setVisibility(View.VISIBLE);
                }
                if(mTasksEmpty.getVisibility() == View.VISIBLE){
                    mTasksEmpty.setVisibility(View.GONE);
                }
            } else if(tasks != null  && tasks.isEmpty()){
                mTasksEmpty.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
            }
            if(!mShowSingleCourse && mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()){
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
            if(throwable.getMessage() != null && throwable.getMessage().equalsIgnoreCase(EMPTY_RESULTS_ERROR_MSG)){
                mTasksEmpty.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
            }else{
                String message = getString(R.string.error_moodle_tasks_not_loaded);
                if(throwable instanceof InternetNotAvailableException){
                    message = getString(R.string.exception_message_no_connection);
                }else if(throwable instanceof SocketTimeoutException){
                    message = getString(R.string.exception_message_timeout);
                }
                Snackbar.make(mRecyclerView,
                        message,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(getString(R.string.retry),
                                v -> refreshTasks())
                        .show();

            }
        }
    }

    @RxLogObservable
    private Observable<Pair<List<Task>,Map<Long,Course>>> getCompletedTasksObservable(String user, String pass){
        Observable<Response<List<Task>>> observableTasks = Observable.empty();
        Observable<Response<List<Course>>> observableCourses = mMoodleApi.getCourses(user, pass);

        if (mCourse != null && mCourse.getId() >= 0) {
            observableTasks = mMoodleApi.getFinishedTasks(user, pass, mCourse.getId(), null, null, null);
        } else {
            observableTasks = mMoodleApi.getFinishedTasks(user, pass, null, null, null, null);
        }

        return mRxNetwork.checkInternetConnection()
                .andThen(Observable.zip(
                        observableTasks,
                        observableCourses,
                        (tasks, courses) ->
                                new Pair<>(tasks.body(), Utils.getCoursesMapFromList(courses.body())))
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                );
    }

}
