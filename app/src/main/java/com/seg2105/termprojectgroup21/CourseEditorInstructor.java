package com.seg2105.termprojectgroup21;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class CourseEditorInstructor extends AppCompatActivity {

    Intent intent;
    String course_id;
    EditText description, capacity;
    Button unassign, edit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_editor_instructor);

        intent = getIntent();
        course_id = intent.getStringExtra("doc_id");

        description = findViewById(R.id.CourseDescription);
        capacity = findViewById(R.id.CourseCapacity);

        unassign = findViewById(R.id.Unassign);
        edit = findViewById(R.id.EditCourse);

        unassign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unassignConfirm();
            }
        });
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(false) return; //Validation
                updateCourse();
            }
        });
    }

    public void updateCourse() {

    }

    public void unassignConfirm() {
        new AlertDialog.Builder(this)
                .setTitle("Instructor Un-assignment")
                .setMessage("Are you sure you want to un-assign yourself from this course?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        unassign();
                    }})
                .setNegativeButton("No", null).show();
    }

    public void unassign() {

    }
}