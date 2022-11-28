package com.seg2105.termprojectgroup21.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.seg2105.termprojectgroup21.R;
import com.seg2105.termprojectgroup21.Objects.ScheduleItem;

import java.util.ArrayList;

public class ScheduleItemAdapter extends RecyclerView.Adapter<ScheduleItemAdapter.ItemViewHolder> {
    // assign day to int field of ScheduleItem:
    //TODO: get string array straight from strings file
    final String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};

    boolean showDeleteIcon;

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView day;
        TextView start_time;
        TextView end_time;
        ImageView deleteIcon;

        public ItemViewHolder(View view) {
            super(view);

            day = view.findViewById(R.id.item_day);
            start_time = view.findViewById(R.id.start_time);
            end_time = view.findViewById(R.id.end_time);
            deleteIcon = view.findViewById(R.id.deleteIcon);
        }
    }

    Context context;
    ArrayList<ScheduleItem> schedule;
    ScheduleItemAdapter.onItemClickListener listener;

    public ScheduleItemAdapter(Context context, ArrayList<ScheduleItem> schedule, ScheduleItemAdapter.onItemClickListener onItemClickListener) {
        this.context = context;
        this.schedule = schedule;
        this.listener = onItemClickListener;
        this.showDeleteIcon = true;
    }

    public ScheduleItemAdapter(Context context, ArrayList<ScheduleItem> schedule, ScheduleItemAdapter.onItemClickListener onItemClickListener, boolean showDeleteIcon) {
        this.context = context;
        this.schedule = schedule;
        this.listener = onItemClickListener;
        this.showDeleteIcon = showDeleteIcon;
    }

    @NonNull
    @Override
    public ScheduleItemAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.schedule_item, parent, false);
        return new ScheduleItemAdapter.ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleItemAdapter.ItemViewHolder holder, int position) {
        ScheduleItem item = schedule.get(position);
        holder.day.setText(days[item.getDay()]);
        holder.start_time.setText(item.getStartTime());
        holder.end_time.setText(item.getEndTime());
        holder.deleteIcon.setVisibility(showDeleteIcon ? View.VISIBLE : View.INVISIBLE);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return schedule.size();
    }

    public interface onItemClickListener {
        void onItemClick(ScheduleItem item);
    }
}
