package es.us.context4learning.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eowise.recyclerview.stickyheaders.StickyHeadersAdapter;

import java.util.List;
import java.util.Map;

import es.us.context4learning.R;
import es.us.context4learning.data.api.moodle.entity.Course;
import es.us.context4learning.data.api.moodle.entity.Task;

public class StickyHeaderTasksAdapter implements StickyHeadersAdapter<StickyHeaderTasksAdapter.ViewHolder> {
    private List<Task> tasks;
    //private ArrayList<Card> cards;
    private Map<Long,Course> courses;

    public StickyHeaderTasksAdapter(List<Task> tasks, Map<Long,Course> courses) {
        this.tasks = tasks;
        this.courses = courses;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_task_item_header, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder headerViewHolder, int position) {
        headerViewHolder.mCourse.setText(courses.get(tasks.get(position).getCourseId()).getName());
    }

    @Override
    public long getHeaderId(int position) {
        return tasks.get(position).getCourseId();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView mCourse;

        public ViewHolder(View itemView) {
            super(itemView);
            mCourse = (TextView) itemView.findViewById(R.id.task_course);
        }
    }
}