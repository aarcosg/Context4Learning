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
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.eowise.recyclerview.stickyheaders.StickyHeadersBuilder;
import com.eowise.recyclerview.stickyheaders.StickyHeadersItemDecoration;
import com.fernandocejas.frodo.annotation.RxLogObservable;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

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
import es.us.context4learning.ui.activity.MoodleTasksActivity;
import es.us.context4learning.ui.adapter.StickyHeaderTasksAdapter;
import es.us.context4learning.ui.adapter.TasksAdapter;
import es.us.context4learning.utils.RxNetwork;
import es.us.context4learning.utils.Utils;
import retrofit2.Response;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static es.us.context4learning.Constants.EMPTY_RESULTS_ERROR_MSG;


public class PendingTasksFragment extends BaseFragment {

    private static final String TAG = PendingTasksFragment.class.getCanonicalName();

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
    Observable<GoogleApiClient> mGoogleApiClientObservable;
    @Inject
    Tracker mTracker;

    private CompositeSubscription mSubscriptions = new CompositeSubscription();
    private String mLastActivity;
    private TasksAdapter mAdapter;
    private Course mCourse;
    private Toolbar mToolbar;
    private boolean mShowSingleCourse;
    private boolean mShowCurrentContextTasks = true;
    private CustomTabActivityHelper mCustomTabActivityHelper;

    public static PendingTasksFragment newInstance(@Nullable Course course) {
        PendingTasksFragment fragment = new PendingTasksFragment();
        Bundle args = new Bundle();
        if(course != null){
            args.putParcelable(Constants.EXTRA_COURSE, course);
        }
        fragment.setArguments(args);
        return fragment;
    }

    public PendingTasksFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null && getArguments().containsKey(Constants.EXTRA_COURSE)){
            this.getComponent(MoodleTasksComponent.class).inject(this);
            mCourse = getArguments().getParcelable(Constants.EXTRA_COURSE);
            setHasOptionsMenu(true);
        }else{
            this.getComponent(MainComponent.class).inject(this);
        }
        mShowSingleCourse = mCourse != null;
        mCustomTabActivityHelper = new CustomTabActivityHelper();
        getCurrentActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = null;
        if(mShowSingleCourse){
            rootView = inflater.inflate(es.us.context4learning.R.layout.fragment_pending_tasks, container, false);
        }else{
            rootView = inflater.inflate(es.us.context4learning.R.layout.fragment_pending_tasks_swipe_refresh, container, false);
        }
        ButterKnife.bind(this, rootView);

        if(mShowSingleCourse){
            mToolbar = ((MoodleTasksActivity)getActivity()).getToolbar();
            mToolbar.findViewById(es.us.context4learning.R.id.toolbar_title_wrapper).setVisibility(View.VISIBLE);
            ((MoodleTasksActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
            ((TextView)mToolbar.findViewById(es.us.context4learning.R.id.toolbar_title)).setText(mCourse.getName());
        }

        mLastActivity = mPrefs.getString(Constants.PROPERTY_LAST_ACTIVITY, null);

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
    public void onResume() {
        super.onResume();
        if(mSubscriptions.isUnsubscribed()){
            mSubscriptions = new CompositeSubscription();
        }
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
        if(mSubscriptions.isUnsubscribed()){
            mSubscriptions.unsubscribe();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if(mShowSingleCourse){
            inflater.inflate(es.us.context4learning.R.menu.menu_pending_tasks,menu);
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
                if(mCourse != null){
                    screenName = "Pending Tasks " + Constants.SCREEN_NAME_STUDENT_PROGRESS_REPORT;
                    MoodleHelper.openMoodleMarksWeb(getActivity(), mCustomTabActivityHelper, mCourse.getId());
                }
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
            mSubscriptions.add(getPendingTasksObservable(user,pass)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            this::bindTasksAndCourses
                            , this::showMoodleErrorMessage
                    ));
        }
    }

    private void bindTasksAndCourses(Pair<List<Task>,Map<Long,Course>> data){
        if(isAdded()){
            if (mProgressBar != null) mProgressBar.setVisibility(View.GONE);
            List<Task> tasks = data.first;
            Map<Long,Course> courses = data.second;
            if(tasks != null && !tasks.isEmpty() && courses != null && !courses.isEmpty()){
                if(mShowSingleCourse){
                    ((TextView)mToolbar.findViewById(es.us.context4learning.R.id.toolbar_subtitle)).setText(tasks.get(0).getObjective());
                }

                // Show videos only on WiFi
                boolean showVideosOnlyWifi = mPrefs.getBoolean(Constants.PROPERTY_SHOW_VIDEO_WIFI,false);
                if(showVideosOnlyWifi){
                    tasks = filterVideosOnlyWifi(tasks);
                }

                mAdapter.clear();
                mAdapter.addAll(tasks,courses);

                if(mRecyclerView.getVisibility() == View.GONE){
                    mRecyclerView.setVisibility(View.VISIBLE);
                }
                if(mTasksEmpty.getVisibility() == View.VISIBLE){
                    mTasksEmpty.setVisibility(View.GONE);
                }
                if(mLastActivity != null){
                    String snackbarText = getString(R.string.pending_tasks_other_context);
                    if(mShowCurrentContextTasks){
                        snackbarText = getString(R.string.pending_tasks_current_context, mLastActivity.toLowerCase());
                    }
                    Snackbar.make(mRecyclerView, snackbarText, Snackbar.LENGTH_LONG).show();
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

    private List<Task> filterVideosOnlyWifi(List<Task> tasks) {
        Map<Long,List<Task>> allTasks = new LinkedHashMap<>();
        Map<Long,List<Task>> videoTasks = new LinkedHashMap<>();
        List<Task> aux;
        for(Task task :  tasks){
            if(allTasks.containsKey(task.courseId)){
                aux = allTasks.get(task.courseId);
                aux.add(task);
                allTasks.put(task.courseId,aux);
            }else{
                aux = new ArrayList<Task>();
                aux.add(task);
                allTasks.put(task.courseId,aux);
            }

            if(Utils.isVideoTask(task)){
                if(videoTasks.containsKey(task.courseId)){
                    aux = videoTasks.get(task.courseId);
                    aux.add(task);
                    videoTasks.put(task.courseId,aux);
                }else{
                    aux = new ArrayList<Task>();
                    aux.add(task);
                    videoTasks.put(task.courseId,aux);
                }
            }
        }

        for(Long courseId : videoTasks.keySet()){
            if(allTasks.get(courseId).size() > videoTasks.get(courseId).size()){
                for(Task task : videoTasks.get(courseId)){
                    tasks.remove(task);
                }
            }
        }
        return tasks;
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
    private Observable<Pair<List<Task>,Map<Long,Course>>> getPendingTasksObservable(String user, String pass){
        Observable<Response<List<Task>>> observableTasks = Observable.empty();
        Observable<Response<List<Task>>> observableTasksByActivity = Observable.empty();
        Observable<Response<List<Course>>> observableCourses = mMoodleApi.getCourses(user, pass);

        if (mCourse != null && mCourse.getId() >= 0) {
            observableTasks = mMoodleApi.getTasksByCourse(user, pass, mCourse.getId(), null, null, null);
            observableTasksByActivity = mMoodleApi.getTasksByCourse(user, pass, mCourse.getId(), mLastActivity, null, null);
        } else {
            observableTasks = mMoodleApi.getTasks(user, pass, null, null, null);
            observableTasksByActivity = mMoodleApi.getTasks(user, pass, mLastActivity, null, null);
        }

        return mRxNetwork.checkInternetConnection()
                .andThen(Observable.zip(
                        observableTasks,
                        observableTasksByActivity,
                        observableCourses,
                        (tasks, tasksByActivity, courses) -> {
                            Map<Long, Course> coursesMap = Utils.getCoursesMapFromList(courses.body());
                            List<Task> tasksList = new ArrayList<>();
                            if (!tasksByActivity.body().isEmpty()) {
                                tasksList = tasksByActivity.body();
                                mShowCurrentContextTasks = true;
                            } else {
                                tasksList = tasks.body();
                                mShowCurrentContextTasks = false;
                            }
                            return new Pair<>(tasksList, coursesMap);
                        })
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                );
    }

    private void getCurrentActivity(){
        mSubscriptions.add(mGoogleApiClientObservable
                .subscribeOn(Schedulers.immediate())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        googleApiClient -> {
                            Awareness.SnapshotApi.getDetectedActivity(googleApiClient)
                                    .setResultCallback(detectedActivityResult -> {
                                        if (!detectedActivityResult.getStatus().isSuccess()) {
                                            Log.e(TAG, "Snapshot API could not get the current activity.");
                                            return;
                                        }
                                        ActivityRecognitionResult ar = detectedActivityResult.getActivityRecognitionResult();
                                        DetectedActivity probableActivity = ar.getMostProbableActivity();
                                        if(probableActivity.getConfidence() >= Constants.MIN_DETECTED_CONFIDENCE){
                                            String activityDetected = Constants.getActivityString(mContext,probableActivity.getType());
                                            mLastActivity = activityDetected;
                                        }
                                    });
                        }
                        , throwable ->  Log.e(TAG,"Google Api Client connection suspended. Snapshot API error.")
                ));
    }
}
