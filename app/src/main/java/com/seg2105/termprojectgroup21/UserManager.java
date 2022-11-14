package com.seg2105.termprojectgroup21;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UserManager extends AppCompatActivity implements UserAdapter.onItemClickListener{
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference usersRef = db.collection("users");
    CollectionReference coursesRef = db.collection("courses");
    CollectionReference scheduleRef = db.collection("course_days");
    RecyclerView recyclerView;
    UserAdapter userAdapter;
    ArrayList<User> users = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_manager);


        recyclerView = findViewById(R.id.users_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(this, users, this);
        recyclerView.setAdapter(userAdapter);

        fetchUsers();
    }

    private void fetchUsers() {
        users.clear();
        usersRef.whereNotEqualTo("role", "Admin").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        users.add(new User(doc.getId(), doc.getString("username"), doc.getString("role")));
                    }
                    userAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getApplicationContext(), "An error has occurred.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public void onItemClick(User user) {
        new AlertDialog.Builder(this)
                .setTitle("User Deletion")
                .setMessage("Are you sure you want to delete user "+user.getUsername()+"?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        deleteUser(user);
                    }})
                .setNegativeButton("Cancel", null).show();
    }

    private void deleteUser(User user) {
        String doc_id = user.getId();
        usersRef.document(doc_id).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(), "User successfully deleted!", Toast.LENGTH_SHORT).show();
                fetchUsers();

                Map<String, Object> course = new HashMap<>();
                course.put("description", "");
                course.put("instructor_username", "");
                course.put("capacity", 0);

                coursesRef.whereEqualTo("instructor_username", user.getUsername()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot course_doc : task.getResult()) {
                                course_doc.getReference().update(course);
                                scheduleRef.whereEqualTo("course_id", course_doc.getId()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot event_doc : task.getResult()) {
                                                event_doc.getReference().delete();
                                            }
                                        } else {
                                            Toast.makeText(getApplicationContext(), "An error has occurred during cleanup.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "An error has occurred during cleanup.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Error deleting user.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}