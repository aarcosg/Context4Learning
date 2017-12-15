package es.us.context4learning.ui.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import es.us.context4learning.R;
import es.us.context4learning.data.api.moodle.entity.Course;
import es.us.context4learning.ui.activity.MoodleTasksActivity;

public class CoursesAdapter extends RecyclerView.Adapter<CoursesAdapter.ViewHolder> implements View.OnClickListener{

    private static final String TAG = CoursesAdapter.class.getCanonicalName();
    private List<Course> courses;

    public CoursesAdapter(List<Course> courses) {
        this.courses=courses;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_course_item, parent, false);
        ViewHolder holder = new ViewHolder(v);
        holder.itemView.setOnClickListener(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Course course = courses.get(position);
        holder.mName.setText(course.getName());
        holder.mProgress.setProgress(course.getProgress().intValue());
        holder.mProgressValue.setText(String.format("%.2f", course.getProgress()) + "%");
        holder.itemView.setTag(course);
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    @Override
    public long getItemId(int position) {
        return courses.get(position).getId();
    }

    public void add(Course course, int position) {
        courses.add(position, course);
        notifyItemInserted(position);
    }
    public void add(Course course) {
        courses.add(course);
        notifyItemInserted(courses.size()-1);
    }

    public void addAll(List<Course> courses){
        for(Course c : courses){
            add(c);
        }
    }

    public void clear(){
        courses.clear();
        notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        Course course = (Course) view.getTag();
        MoodleTasksActivity.launch((Activity) view.getContext(), course);
    }


    public static class ViewHolder extends RecyclerView.ViewHolder  {
        private TextView mName;
        private ProgressBar mProgress;
        private TextView mProgressValue;

        public ViewHolder(View itemView) {
            super(itemView);
            mName = (TextView) itemView.findViewById(R.id.course_name);
            mProgress = (ProgressBar) itemView.findViewById(R.id.course_progress);
            mProgressValue = (TextView) itemView.findViewById(R.id.course_progress_val);
        }
    }

}
