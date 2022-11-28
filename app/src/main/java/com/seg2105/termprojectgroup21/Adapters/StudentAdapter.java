package com.seg2105.termprojectgroup21.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.seg2105.termprojectgroup21.R;

import java.util.List;
import java.util.ArrayList;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    public static class StudentViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        public StudentViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.student_name);
        }
    }

    ArrayList<String> students;
    Context context;

    public StudentAdapter(Context context, ArrayList<String> students) {
        this.context = context;
        this.students = students;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.student_item, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentAdapter.StudentViewHolder holder, int position) {
        String student = students.get(position);
        holder.name.setText(student);

    }

    @Override
    public int getItemCount() {
        return students.size();
    }
}
