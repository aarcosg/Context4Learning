package es.us.context4learning.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.iconics.IconicsDrawable;

import java.util.List;
import java.util.Map;

import es.us.context4learning.R;
import es.us.context4learning.data.api.moodle.entity.Course;
import es.us.context4learning.data.api.moodle.entity.Task;
import es.us.context4learning.utils.Utils;

public class TasksAdapter extends RecyclerView.Adapter<TasksAdapter.ViewHolder> implements View.OnClickListener{

    private static final String TAG = TasksAdapter.class.getCanonicalName();
    private List<Task> tasks;
    private Map<Long,Course> courses;

    public TasksAdapter(List<Task> tasks,Map<Long,Course> courses) {
        this.tasks=tasks;
        this.courses = courses;
        setHasStableIds(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_task_item, parent, false);
        // Set the view to the ViewHolder
        ViewHolder holder = new ViewHolder(v);
        holder.itemView.setOnClickListener(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Task task = tasks.get(position);

        holder.mName.setText(task.getName());
        holder.mDescription.setText(task.getDescription().replaceAll("\\<.*?>", ""));
        holder.mDuration.setText(Html.fromHtml(holder.mDuration.getContext().getResources().getString(R.string.x_minutes, task.getDuration())));
        holder.mType.setText(task.getType());
        String iconName = getIconicsIcon(task.getSourceType());
        if(iconName == null){
            holder.mIcon.setVisibility(View.GONE);
        }else{
            holder.mIcon.setVisibility(View.VISIBLE);
            if(task.getSourceType().equalsIgnoreCase("url")
                    && Utils.isVideoTask(task)){
                iconName = "cmd_youtube_play";
            }
            holder.mIcon.setImageDrawable(
                    new IconicsDrawable(holder.itemView.getContext())
                            .sizeDp(20)
                            .icon(iconName)
                            .colorRes(R.color.primary)
            );
        }

        holder.itemView.setTag(task);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    @Override
    public long getItemId(int position) {
        return tasks.get(position).getId();
    }

    public void add(Task task, int position) {
        tasks.add(position, task);
        notifyItemInserted(position);
    }
    public void add(Task task) {
        tasks.add(task);
        notifyItemInserted(tasks.size()-1);
    }

    public void addAll(List<Task> tasks, Map<Long,Course> courses){
        for(Task t : tasks){
            add(t);
        }
        for(Course course : courses.values()){
            this.courses.put(course.getId(),course);
        }
    }

    public void clear(){
        tasks.clear();
        courses.clear();
        notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {}


    public static class ViewHolder extends RecyclerView.ViewHolder  {
        private TextView mName;
        private TextView mDescription;
        private TextView mDuration;
        private TextView mType;
        private ImageView mIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            mName = (TextView) itemView.findViewById(R.id.task_name);
            mDescription = (TextView) itemView.findViewById(R.id.task_description);
            mDuration = (TextView) itemView.findViewById(R.id.task_duration);
            mType = (TextView) itemView.findViewById(R.id.task_type);
            mIcon = (ImageView) itemView.findViewById(R.id.task_icon);
        }

    }


    private String getIconicsIcon(String taskType){
        String icon = null;
        if(taskType.equalsIgnoreCase("data")){
            icon = "cmd_database";
        }else if(taskType.equalsIgnoreCase("chat")){
            icon = "cmd_wechat";
        }else if(taskType.equalsIgnoreCase("choice")){
            icon = "cmd_help";
        }else if(taskType.equalsIgnoreCase("quiz")){
            icon = "cmd_checkbox_multiple_marked";
        }else if(taskType.equalsIgnoreCase("survey")){
            icon = "faw_wpforms";
        }else if(taskType.equalsIgnoreCase("forum")){
            icon = "cmd_forum";
        }else if(taskType.equalsIgnoreCase("glossary")){
            icon = "faw_list_alt";
        }else if(taskType.equalsIgnoreCase("lti")){
            icon = "cmd_puzzle";
        }else if(taskType.equalsIgnoreCase("lesson")){
            icon = "cmd_school";
        }else if(taskType.equalsIgnoreCase("scorm")){
            icon = "cmd_package";
        }else if(taskType.equalsIgnoreCase("workshop")){
            icon = "cmd_flask_outline";
        }else if(taskType.equalsIgnoreCase("assign")){
            icon = "faw_files_o";
        }else if(taskType.equalsIgnoreCase("wiki")){
            icon = "cmd_wikipedia";
        }else if(taskType.equalsIgnoreCase("resource")){
            icon = "cmd_file";
        }else if(taskType.equalsIgnoreCase("folder")){
            icon = "cmd_folder_download";
        }else if(taskType.equalsIgnoreCase("label")){
            icon = "cmd_tag_multiple";
        }else if(taskType.equalsIgnoreCase("book")){
            icon = "cmd_book_open_page_variant";
        }else if(taskType.equalsIgnoreCase("page")){
            icon = "gmd_web";
        }else if(taskType.equalsIgnoreCase("imscp")){
            icon = "faw_cubes";
        }else if(taskType.equalsIgnoreCase("url")){
            icon = "cmd_web";
        }
        return icon;
    }

}
