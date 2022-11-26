package com.seg2105.termprojectgroup21.Adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.seg2105.termprojectgroup21.R;
import com.seg2105.termprojectgroup21.Objects.User;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView username;
        TextView role;

        public UserViewHolder(View view) {
            super(view);

            username = view.findViewById(R.id.user_username);
            role = view.findViewById(R.id.user_role);
        }

    }

    Context context;
    ArrayList<User> users;
    onItemClickListener listener;


    public UserAdapter(Context context, ArrayList<User> users, onItemClickListener listener) {
        this.context = context;
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.username.setText(user.getUsername());
        holder.role.setText(user.getRole());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public interface onItemClickListener {
        void onItemClick(User user);
    }

}
