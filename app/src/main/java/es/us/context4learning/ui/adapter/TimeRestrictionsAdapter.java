package es.us.context4learning.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import es.us.context4learning.R;
import es.us.context4learning.backend.timeRestrictionApi.model.TimeRestriction;

public class TimeRestrictionsAdapter extends RecyclerView.Adapter<TimeRestrictionsAdapter.ViewHolder> implements View.OnLongClickListener {

    private static final String TAG = TimeRestrictionsAdapter.class.getCanonicalName();
    private List<TimeRestriction> restrictions;
    private SimpleDateFormat sdf;

    public TimeRestrictionsAdapter(List<TimeRestriction> restrictions) {
        this.restrictions=restrictions;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(es.us.context4learning.R.layout.recyclerview_time_restriction_item, parent, false);
        // Set the view to the ViewHolder
        ViewHolder holder = new ViewHolder(v);
        holder.itemView.setOnLongClickListener(this);
        sdf = new SimpleDateFormat("HH:mm",new Locale("es","ES"));
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        TimeRestriction restriction = restrictions.get(position);
        holder.mStartTime.setText(sdf.format(new Date(restriction.getStartTime().getValue())));
        holder.mEndTime.setText(sdf.format(new Date(restriction.getEndTime().getValue())));
        holder.itemView.setTag(restriction);
    }

    @Override
    public int getItemCount() {
        return restrictions.size();
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    public void add(TimeRestriction restriction, int position) {
        restrictions.add(position, restriction);
        notifyItemInserted(position);
    }
    public void add(TimeRestriction restriction) {
        restrictions.add(restriction);
        notifyItemInserted(restrictions.size()-1);
    }

    public void addAll(List<TimeRestriction> restrictions){
        for(TimeRestriction tr : restrictions){
            add(tr);
        }
    }

    public void remove(TimeRestriction restriction){
        int position = restrictions.indexOf(restriction);
        restrictions.remove(position);
        notifyItemRemoved(position);
    }

    public void clear(){
        restrictions.clear();
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder  {
        private TextView mStartTime;
        private TextView mEndTime;

        private ViewHolder(View itemView) {
            super(itemView);
            mStartTime = (TextView) itemView.findViewById(R.id.start_time);
            mEndTime = (TextView) itemView.findViewById(R.id.end_time);
        }

    }

}
