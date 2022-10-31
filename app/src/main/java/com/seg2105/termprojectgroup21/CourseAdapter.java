package com.seg2105.termprojectgroup21;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    public static class CourseViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        TextView code;

        public CourseViewHolder(View view) {
            super(view);

            name = view.findViewById(R.id.course_name);
            code = view.findViewById(R.id.course_code);
        }

    }

    Context context;
    ArrayList<Course> courses;
    onItemClickListener listener;

    public CourseAdapter(Context context, ArrayList<Course> courses, CourseAdapter.onItemClickListener onItemClickListener) {
        this.context = context;
        this.courses = courses;
        this.listener = onItemClickListener;
    }

    @NonNull
    @Override
    public CourseAdapter.CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.course_item, parent, false);
        return new CourseAdapter.CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseAdapter.CourseViewHolder holder, int position) {
        Course course = courses.get(position);
        holder.name.setText(course.getName());
        holder.code.setText(course.getCode());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(course);
            }
        });
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    public interface onItemClickListener {
        void onItemClick(Course course);
    }
}