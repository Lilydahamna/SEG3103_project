package com.seg2105.termprojectgroup21;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class CourseEditor extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference coursesRef = db.collection("courses");

    Intent intent;
    String course_id;
    EditText new_name, new_code;
    Button delete, edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_editor);

        intent = getIntent();
        course_id = intent.getStringExtra("doc_id");

        new_name = findViewById(R.id.CourseName);
        new_code = findViewById(R.id.CourseCode);
        new_name.setText(intent.getStringExtra("name"));
        new_code.setText(intent.getStringExtra("code"));

        delete = findViewById(R.id.DeleteCourse);
        edit = findViewById(R.id.EditCourse);

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteConfirm();
            }
        });
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!CourseManager.areFieldsValid(getApplicationContext(), new_name.getText().toString(), new_code.getText().toString())) return;
                updateCourse(course_id, new_name.getText().toString(), new_code.getText().toString());
            }
        });
    }

    private void deleteConfirm() {
        new AlertDialog.Builder(this)
                .setTitle("Course Deletion")
                .setMessage("Are you sure you want to delete this course?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        removeCourse(course_id);
                    }})
                .setNegativeButton("Cancel", null).show();
    }

    public void updateCourse(String doc_id, String name, String code) {
        Task<QuerySnapshot> task = coursesRef.whereEqualTo("code", code).get();
        while(!task.isComplete());
        if(task.isSuccessful()){
            Map<String, Object> course = new HashMap<>();
            course.put("name", name);
            course.put("code", code);
            if(task.getResult().isEmpty() || (task.getResult().size() == 1 && task.getResult().getDocuments().get(0).getId().equals(doc_id))) { // no course found or code unchanged, can add a new one
                Task<Void> newTask = coursesRef.document(doc_id).update(course);
                while(!newTask.isComplete());
                if(newTask.isSuccessful()){
                    Toast.makeText(getApplicationContext(), "Course updated.", Toast.LENGTH_SHORT).show();

                }else{
                    Toast.makeText(getApplicationContext(), "Error updating course.", Toast.LENGTH_SHORT).show();
                }
                // otherwise, inform user that the same course already exists
            } else {
                Toast.makeText(getApplicationContext(), "A course with that code already exists.", Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(getApplicationContext(), "An error has occurred.", Toast.LENGTH_SHORT).show();
        }
    }

    public void removeCourse(String doc_id) {
        // query database for course
        Task<Void> task = coursesRef.document(doc_id).delete();
        while(!task.isComplete());
        if(task.isSuccessful()){
            Toast.makeText(getApplicationContext(), "Course successfully deleted!", Toast.LENGTH_SHORT).show();
            finish();
        }else{
            Toast.makeText(getApplicationContext(), "Error deleting course!", Toast.LENGTH_SHORT).show();
        }
    }
}