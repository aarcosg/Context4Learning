package es.us.context4learning.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import es.us.context4learning.R;
import es.us.context4learning.backend.locationRestrictionApi.model.LocationRestriction;

public class LocationRestrictionsAdapter extends RecyclerView.Adapter<LocationRestrictionsAdapter.ViewHolder> implements View.OnLongClickListener{

    private static final String TAG = LocationRestrictionsAdapter.class.getCanonicalName();
    private List<LocationRestriction> restrictions;

    public LocationRestrictionsAdapter(List<LocationRestriction> restrictions) {
        this.restrictions=restrictions;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_location_restriction_item, parent, false);
        // Set the view to the ViewHolder
        ViewHolder holder = new ViewHolder(v);
        holder.itemView.setOnLongClickListener(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        LocationRestriction restriction = restrictions.get(position);
        holder.mLocation.setText(
                restriction.getAddress() != null ?
                        restriction.getAddress() :
                        restriction.getLocation().getLatitude() + ", " + restriction.getLocation().getLongitude());
        holder.itemView.setTag(restriction);
    }

    @Override
    public int getItemCount() {
        return restrictions.size();
    }

    public void add(LocationRestriction restriction, int position) {
        restrictions.add(position, restriction);
        notifyItemInserted(position);
    }
    public void add(LocationRestriction restriction) {
        restrictions.add(restriction);
        notifyItemInserted(restrictions.size()-1);
    }

    public void addAll(List<LocationRestriction> restrictions){
        for(LocationRestriction lr : restrictions){
            add(lr);
        }
    }

    public void remove(LocationRestriction restriction){
        int position = restrictions.indexOf(restriction);
        restrictions.remove(position);
        notifyItemRemoved(position);
    }

    public void clear(){
        restrictions.clear();
        notifyDataSetChanged();
    }

    @Override
    public boolean onLongClick(final View view) {
        return false;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder  {
        private TextView mLocation;

        public ViewHolder(View itemView) {
            super(itemView);
            mLocation = (TextView) itemView.findViewById(R.id.location);
        }

    }


}
