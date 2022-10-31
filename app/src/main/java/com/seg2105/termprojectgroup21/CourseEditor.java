package com.seg2105.termprojectgroup21;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CourseEditor extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference coursesRef = db.collection("courses");

    Intent intent;
    String course_id;
    EditText new_name;
    EditText new_code;
    Button delete;
    Button edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_editor);

        intent = getIntent();
        course_id = intent.getStringExtra("doc_id");

        new_name = findViewById(R.id.CourseName);
        new_code = findViewById(R.id.CourseCode);

        delete = findViewById(R.id.DeleteCourse);
        edit = findViewById(R.id.EditCourse);

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeCourse(course_id);
            }
        });
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateCourse(course_id, new_name.getText().toString(), new_code.getText().toString());
            }
        });
    }



    public void updateCourse(String doc_id, String name, String code) {
        Map<String, Object> course = new HashMap<>();
        course.put("name", name);
        course.put("code", code);
        coursesRef.document(doc_id).update(course).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(), "Course updated.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Error updating course.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void removeCourse(String doc_id) {
        // query database for course
        coursesRef.document(doc_id).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(), "Course successfully deleted!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Error deleting course!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}