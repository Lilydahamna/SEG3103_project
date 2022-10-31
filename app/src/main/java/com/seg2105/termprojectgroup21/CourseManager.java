package com.seg2105.termprojectgroup21;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
//import com.google.common.hash.Hashing;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
//import com.google.firebase.firestore.GetOptions;
//import com.google.firebase.firestore.QueryDocumentSnapshot;
//import com.google.firebase.firestore.QuerySnapshot;

//import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CourseManager extends AppCompatActivity implements CourseAdapter.onItemClickListener {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference coursesRef = db.collection("courses");
    RecyclerView recyclerView;
    CourseAdapter courseAdapter;
    ArrayList<Course> courses = new ArrayList<>();
    Button add;
    Button search;
    EditText inputName;
    EditText inputCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_manager);

        recyclerView = findViewById(R.id.courses_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        courseAdapter = new CourseAdapter(this, courses, this);
        recyclerView.setAdapter(courseAdapter);

        fetchCourses();

        inputName = findViewById(R.id.course_name);
        inputCode = findViewById(R.id.course_code);

        add = findViewById(R.id.addBtn);
        search = findViewById(R.id.findBtn);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addCourse(inputName.getText().toString(), inputCode.getText().toString());
            }
        });
    }

    private void fetchCourses() {
        courses.clear();
        coursesRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        courses.add(new Course(doc.getId(), doc.getString("name"), doc.getString("code")));
                    }
                    courseAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getApplicationContext(), "An error has occurred trying to fetch courses.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onItemClick(Course course) {
        Toast.makeText(getApplicationContext(), "You clicked on: "+course.getName(), Toast.LENGTH_SHORT).show();

        //TODO: When CourseEditor activity is done, uncomment these. Then getIntent().getExtras().getString("doc_id") to get the passed parameter in that activity
        Intent intent = new Intent(getApplicationContext(), CourseEditor.class);
        intent.putExtra("doc_id", course.getId());
        startActivity(intent);
    }

    public void addCourse(String name, String code) {
        // query for course, in case it already exists
        coursesRef.whereEqualTo("code", code).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Map<String, Object> course = new HashMap<>();
                    course.put("name", name);
                    course.put("code", code);
                    if(task.getResult().isEmpty()) { // no course found, can add a new one
                        coursesRef.add(course).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Toast.makeText(getApplicationContext(), "Course addition successful!", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "An error has occurred.", Toast.LENGTH_SHORT).show();
                            }
                        });
                        // otherwise, inform user that the same course already exists
                    } else {
                        Toast.makeText(getApplicationContext(), "A course with that code already exists.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "An error has occurred.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}