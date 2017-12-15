package es.us.context4learning.ui.activity;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import butterknife.Bind;
import butterknife.ButterKnife;
import es.us.context4learning.Constants;
import es.us.context4learning.data.api.moodle.entity.Course;
import es.us.context4learning.R;
import es.us.context4learning.di.HasComponent;
import es.us.context4learning.di.components.DaggerMoodleTasksComponent;
import es.us.context4learning.di.components.MoodleTasksComponent;
import es.us.context4learning.ui.fragment.CompletedTasksFragment;
import es.us.context4learning.ui.fragment.PendingTasksFragment;

public class MoodleTasksActivity extends BaseActivity implements HasComponent<MoodleTasksComponent> {

    @Bind(R.id.viewpager)
    ViewPager mViewPager;
    @Bind(R.id.tabs)
    TabLayout mTabLayout;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    private PagerAdapter mViewPagerAdapter;
    private Course mCourse;
    private MoodleTasksComponent mMoodleTasksComponent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moodle_tasks);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        final ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mViewPagerAdapter = new PagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mViewPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mCourse = getIntent().getParcelableExtra(Constants.EXTRA_COURSE);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    @Override
    public MoodleTasksComponent getComponent() {
        if(this.mMoodleTasksComponent == null){
            this.initializeInjector();
        }
        return this.mMoodleTasksComponent;
    }

    private void initializeInjector() {
        mMoodleTasksComponent = DaggerMoodleTasksComponent.builder()
                .applicationComponent(getApplicationComponent())
                .activityModule(getActivityModule())
                .build();
        mMoodleTasksComponent.inject(this);
    }

    private class PagerAdapter extends FragmentPagerAdapter {

        private final String[] tabTitles = getResources().getStringArray(R.array.tasks_spinner_nav);

        PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }

        @Override
        public int getCount() {
            return tabTitles.length;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch (position) {
                case 0:
                    fragment = PendingTasksFragment.newInstance(mCourse);
                    break;
                case 1:
                    fragment = CompletedTasksFragment.newInstance(mCourse);
                    break;
            }

            return fragment;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*switch (item.getItemId()){
            case android.R.id.home:
                if(getIntent().hasExtra(Constants.EXTRA_COURSE)){
                    Intent coursesIntent = new Intent(MoodleTasksActivity.this, MoodleCoursesActivity.class);
                    coursesIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    ActivityCompat.startActivity(MoodleTasksActivity.this, coursesIntent, null);
                }else{
                    return super.onOptionsItemSelected(item);
                }
                return true;
        }*/
        return super.onOptionsItemSelected(item);
    }

    public static void launch(Activity activity) {
        Intent intent = new Intent(activity, MoodleTasksActivity.class);
        ActivityCompat.startActivity(activity, intent, null);
    }
    public static void launch(Activity activity,Course course) {
        Intent intent = new Intent(activity, MoodleTasksActivity.class);
        intent.putExtra(Constants.EXTRA_COURSE, course);
        ActivityCompat.startActivity(activity, intent, null);
    }

    public Toolbar getToolbar(){
        return mToolbar;
    }

}
