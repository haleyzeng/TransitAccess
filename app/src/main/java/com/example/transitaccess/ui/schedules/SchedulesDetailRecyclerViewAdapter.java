package com.example.transitaccess.ui.schedules;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.transitaccess.R;
import com.example.transitaccess.ui.ScheduleTimes;

import java.util.List;

public class SchedulesDetailRecyclerViewAdapter extends RecyclerView.Adapter<SchedulesDetailRecyclerViewAdapter.ViewHolder> {
    private List<ScheduleTimes> mData;
    private LayoutInflater mInflater;

    // data is passed into the constructor
    SchedulesDetailRecyclerViewAdapter(Context context, List<ScheduleTimes> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.schedules_recyclerview_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.departTimeView.setText("Depart " + mData.get(position).getDepartTime());
        holder.arriveTimeView.setText("Arrive " + mData.get(position).getArriveTime());
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView departTimeView;
        TextView arriveTimeView;

        ViewHolder(View itemView) {
            super(itemView);
            departTimeView = itemView.findViewById(R.id.depart_time);
            arriveTimeView = itemView.findViewById(R.id.arrive_time);
        }

    }

}
