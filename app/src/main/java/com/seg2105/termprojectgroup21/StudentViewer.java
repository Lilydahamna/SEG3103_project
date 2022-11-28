package com.seg2105.termprojectgroup21;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.seg2105.termprojectgroup21.Adapters.StudentAdapter;

import java.util.ArrayList;

public class StudentViewer extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference enrollmentRef = db.collection("enrollment");
    Intent intent;

    RecyclerView recyclerView;
    TextView textView;
    StudentAdapter studentAdapter;

    ArrayList<String> students = new ArrayList<>();
    String course_capacity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_viewer);
        intent = getIntent();

        recyclerView = findViewById(R.id.students_view);
        textView = findViewById(R.id.class_capacity);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        studentAdapter = new StudentAdapter(this, students);
        recyclerView.setAdapter(studentAdapter);
        course_capacity = "Capacity: 0/" + intent.getIntExtra("course_capacity", 0);
        textView.setText(course_capacity);
        getStudents();
    }

    protected void getStudents() {
        students.clear();
        enrollmentRef.whereEqualTo("course_id", intent.getStringExtra("course_id")).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                course_capacity = "Capacity: " + task.getResult().size() + "/" + intent.getIntExtra("course_capacity", 0);
                textView.setText(course_capacity);
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        students.add(doc.getString("student_username"));
                        studentAdapter.notifyDataSetChanged();
                        Toast.makeText(getApplicationContext(), "Students updated successfully.", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "An error has occurred.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}