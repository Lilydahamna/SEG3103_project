package com.seg2105.termprojectgroup21;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import com.seg2105.termprojectgroup21.Objects.User;

import java.util.HashMap;
import java.util.Map;

public class CourseDetails extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference enrollmentRef = db.collection("enrollment");
    DocumentReference enrollmentDoc;
    SharedPreferences sharedPref;
    Intent intent;
    Button enrollToggle;
    Boolean isEnrolled = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_details);
        sharedPref = getSharedPreferences("user",Context.MODE_PRIVATE);
        intent = getIntent();
        enrollToggle = findViewById(R.id.enrollToggle);
        checkEnrollment();

        //TODO: Finish UI and populate all course details (Can pass a good amount of them through intent) but have to fetch course days/hours (see CourseEditorInstructor) and enrollment total

        enrollToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isEnrolled) unenroll();
                //TODO: Add verification for course capacity and time conflict (both of which should be stored somehow as they are displayed)
                else if (true /* verification condition */) enroll();
            }
        });
    }

    private void checkEnrollment() {
        enrollmentRef.whereEqualTo("student_username", sharedPref.getString("username", "")).whereEqualTo("course_id", intent.getStringExtra("course_id")).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if(!task.getResult().isEmpty()) {
                        isEnrolled = true;
                        enrollToggle.setText(R.string.unenroll);
                        enrollmentDoc = task.getResult().getDocuments().get(0).getReference();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "An error has occurred.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void enroll() {
        Map<String, Object> data = new HashMap<>();
        data.put("student_username", sharedPref.getString("username", ""));
        data.put("course_id", intent.getStringExtra("course_id"));

        db.collection("enrollment").add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        isEnrolled = true;
                        enrollToggle.setText(R.string.unenroll);
                        enrollmentDoc = documentReference;
                        Toast.makeText(getApplicationContext(), "Enrolled successfully.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "An error has occurred.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void unenroll() {
        new AlertDialog.Builder(this)
                .setTitle("Un-enrollment")
                .setMessage("Are you sure you want to un-enroll from this course?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Un-enroll", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        enrollmentDoc.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                isEnrolled = false;
                                enrollToggle.setText(R.string.enroll);
                                enrollmentDoc = null;
                                Toast.makeText(getApplicationContext(), "Un-enrolled successfully.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }})
                .setNegativeButton("Cancel", null).show();
    }
}