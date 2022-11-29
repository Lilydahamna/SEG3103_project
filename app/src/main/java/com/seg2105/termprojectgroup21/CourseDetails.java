package com.seg2105.termprojectgroup21;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.seg2105.termprojectgroup21.Adapters.ScheduleItemAdapter;
import com.seg2105.termprojectgroup21.Objects.Course;
import com.seg2105.termprojectgroup21.Objects.ScheduleItem;
import com.seg2105.termprojectgroup21.Objects.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CourseDetails extends AppCompatActivity implements ScheduleItemAdapter.onItemClickListener {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference enrollmentRef = db.collection("enrollment");
    CollectionReference scheduleRef = db.collection("course_days");
    CollectionReference courseRef = db.collection("courses");
    DocumentReference enrollmentDoc;
    SharedPreferences sharedPref;
    Intent intent;
    Button enrollToggle;
    Boolean isEnrolled = false;
    Course course;
    TextView courseName;
    TextView couseDescription;
    TextView courseCapacity;
    TextView courseInstructor;
    RecyclerView recyclerView;
    ScheduleItemAdapter itemAdapter;
    ArrayList<ScheduleItem> schedule = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_details);

        sharedPref = getSharedPreferences("user",Context.MODE_PRIVATE);
        intent = getIntent();

        courseName = findViewById(R.id.courseName);
        couseDescription = findViewById(R.id.courseDescription);
        courseCapacity = findViewById(R.id.courseCapacity);
        courseInstructor = findViewById(R.id.courseInstructor);
        recyclerView = findViewById(R.id.schedule);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        itemAdapter = new ScheduleItemAdapter(this, schedule, this, false);
        recyclerView.setAdapter(itemAdapter);


        enrollToggle = findViewById(R.id.enrollToggle);
        checkEnrollment();
        getCourseData();

        if (!sharedPref.getString("role", "").equals("Student")) enrollToggle.setVisibility(View.INVISIBLE);
        enrollToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isEnrolled) {
                    new AlertDialog.Builder(getApplicationContext())
                            .setTitle("Un-enrollment")
                            .setMessage("Are you sure you want to un-enroll from this course?")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton("Un-enroll", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    unenroll();
                            }})
                            .setNegativeButton("Cancel", null).show();
                }
                //TODO: Add verification for course capacity and time conflict (both of which should be stored somehow as they are displayed)
                else if (true /* verification condition */) enroll(sharedPref.getString("username", ""));
            }
        });
    }

    private void getCourseData() {
        Task<?> task = courseRef.document(intent.getStringExtra("course_id")).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot doc = task.getResult();
                course = new Course(doc.getId(), doc.getString("name"), doc.getString("code"), doc.getString("instructor_username"), ((Number)doc.getLong("capacity")).intValue(), doc.getString("description"));
                courseName.setText(course.getName());
                couseDescription.setText(course.getDescription());
                courseCapacity.setText(Integer.toString(course.getCapacity()));
                courseInstructor.setText(course.getInstructor());

                fetchSchedule();
            }
        });

        while(!task.isComplete());
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

    public void enroll(String username) {
        Map<String, Object> data = new HashMap<>();
        data.put("student_username", username);
        data.put("course_id", intent.getStringExtra("course_id"));

        db.collection("enrollment").add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        isEnrolled = true;
                        enrollToggle.setText(R.string.unenroll);
                        enrollmentDoc = documentReference;
                        course.enrollSomeStudent();
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

    public void unenroll() {
        enrollmentDoc.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                isEnrolled = false;
                enrollToggle.setText(R.string.enroll);
                enrollmentDoc = null;
                course.unenrollSomeStudent();
                Toast.makeText(getApplicationContext(), "Un-enrolled successfully.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void fetchSchedule() {
        schedule.clear();
        Task<?> task = scheduleRef.whereEqualTo("course_id", course.getId()).orderBy("day", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        schedule.add(new ScheduleItem(doc.getId(), ((Number)doc.getLong("day")).intValue(), doc.getString("start"), doc.getString("end")));
                    }
                    itemAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getApplicationContext(), "An error has occurred trying to fetch the course schedule.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        while(!task.isComplete());
    }

    @Override
    public void onItemClick(ScheduleItem item) {}
}