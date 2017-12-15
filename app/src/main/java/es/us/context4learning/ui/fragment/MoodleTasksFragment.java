package es.us.context4learning.ui.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.Bind;
import butterknife.ButterKnife;
import es.us.context4learning.data.api.moodle.entity.Course;
import es.us.context4learning.Constants;
import es.us.context4learning.R;

public class MoodleTasksFragment extends Fragment {

    @Bind(R.id.viewpager)
    ViewPager mViewPager;
    @Bind(R.id.tabs)
    TabLayout mTabLayout;

    private Course mCourse;

    public static MoodleTasksFragment newInstance(@Nullable Course course) {
        MoodleTasksFragment fragment = new MoodleTasksFragment();
        Bundle args = new Bundle();
        if(course != null){
            args.putParcelable(Constants.EXTRA_COURSE, course);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments()!=null && getArguments().containsKey(Constants.EXTRA_COURSE)){
            mCourse = getArguments().getParcelable(Constants.EXTRA_COURSE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_moodle_tasks, container, false);
        ButterKnife.bind(this, rootView);
        PagerAdapter viewPagerAdapter = new PagerAdapter(getChildFragmentManager());
        mViewPager.setAdapter(viewPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
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



}
