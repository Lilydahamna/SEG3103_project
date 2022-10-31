package com.seg2105.termprojectgroup21;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    public static class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView username;
        TextView role;
        onItemClickListener listener;

        public UserViewHolder(View view, onItemClickListener listener) {
            super(view);

            username = view.findViewById(R.id.user_username);
            role = view.findViewById(R.id.user_role);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onItemClick(getAdapterPosition());
        }
    }

    Context context;
    ArrayList<User> users;
    onItemClickListener onItemClickListener;

    public UserAdapter(Context context, ArrayList<User> users, onItemClickListener onItemClickListener) {
        this.context = context;
        this.users = users;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.username.setText(user.getUsername());
        holder.role.setText(user.getRole());
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public interface onItemClickListener {
        void onItemClick(int position);
    }
}
