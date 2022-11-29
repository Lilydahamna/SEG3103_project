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
import java.util.HashSet;
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
                else enroll(sharedPref.getString("username", ""));
            }
        });
    }

    private boolean verifyEnroll(){
        String courseID = intent.getStringExtra("course_id");
        //get capacity
        Task<DocumentSnapshot> task = courseRef.document(courseID).get();
        int capacity = 0;
        while(!task.isComplete());
        if(task.isSuccessful()){
            capacity = ((Number)task.getResult().getLong("capacity")).intValue();
            if(capacity == 0){
                Toast.makeText(getApplicationContext(), "This course is not open for enrollment yet!", Toast.LENGTH_SHORT).show();
                return false;
            }
        }else{
            Toast.makeText(getApplicationContext(), "An error has occurred.", Toast.LENGTH_SHORT).show();
            return false;
        }

        //current students enrolled in course
        Task <QuerySnapshot> task2 = enrollmentRef.get();
        while(!task2.isComplete());
        HashSet<String> courseIDs = new HashSet<>();
        if(task2.isSuccessful()){
            //number of students registered in course
            int counter = 0;
            for(DocumentSnapshot doc : task2.getResult()){
                if(doc.getString("course_id").equals(courseID)){
                    counter++;
                }
                //store ids of courses where student already enrolled
                if(doc.getString("student_username").equals(sharedPref.getString("username", ""))){
                    courseIDs.add(doc.getString("course_id"));
                }
            }
            if(counter == capacity){
                Toast.makeText(getApplicationContext(), "This course is full!", Toast.LENGTH_SHORT).show();
                return false;
            }
        }else{
            Toast.makeText(getApplicationContext(), "An error has occurred.", Toast.LENGTH_SHORT).show();
            return false;
        }
        Task<QuerySnapshot> task3 = scheduleRef.get();
        while(!task3.isComplete());
        ArrayList<ScheduleItem> schedules = new ArrayList<>();
        ScheduleItem courseSchedule = null;
        if(task3.isSuccessful()){
            for(DocumentSnapshot doc: task3.getResult()){
                if(courseIDs.contains(doc.getString("course_id"))){
                    schedules.add(new ScheduleItem(doc.getId(), ((Number)doc.getLong("day")).intValue(), doc.getString("start"), doc.getString("end")));
                }
                if(courseID.equals(doc.getString("course_id"))){
                    courseSchedule = new ScheduleItem(doc.getId(), ((Number)doc.getLong("day")).intValue(), doc.getString("start"), doc.getString("end"));
                }
            }
        }else{
            Toast.makeText(getApplicationContext(), "An error has occurred.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(courseSchedule == null){
            Toast.makeText(getApplicationContext(), "This course is not open for enrollment yet!", Toast.LENGTH_SHORT).show();
            return false;
        }
        int courseScheduleStart = Integer.parseInt(courseSchedule.getStartTime().substring(0,2))*60 + Integer.parseInt(courseSchedule.getStartTime().substring(3));
        int courseScheduleEnd = Integer.parseInt(courseSchedule.getEndTime().substring(0,2))*60 + Integer.parseInt(courseSchedule.getEndTime().substring(3));

        for(ScheduleItem schedule : schedules){
            int scheduleStart = Integer.parseInt(schedule.getStartTime().substring(0,2))*60 + Integer.parseInt(schedule.getStartTime().substring(3));
            int scheduleEnd = Integer.parseInt(schedule.getEndTime().substring(0,2))*60 + Integer.parseInt(schedule.getEndTime().substring(3));

            if(courseSchedule.getDay() == schedule.getDay()){
                if(courseScheduleEnd >= scheduleStart && scheduleEnd >= courseScheduleEnd){
                    Toast.makeText(getApplicationContext(), "Schedule conflict", Toast.LENGTH_SHORT).show();
                    return false;
                }
                if(courseScheduleStart <= scheduleEnd && courseScheduleEnd>=scheduleEnd){
                    Toast.makeText(getApplicationContext(), "Schedule conflict", Toast.LENGTH_SHORT).show();
                    return false;
                }
                if(courseScheduleStart == scheduleStart || courseScheduleEnd == scheduleEnd ){
                    Toast.makeText(getApplicationContext(), "Schedule conflict", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }





        return true;
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
        if(verifyEnroll()){
            Map<String, Object> data = new HashMap<>();
            data.put("student_username", username);
            data.put("course_id", intent.getStringExtra("course_id"));

            Task<?> task = db.collection("enrollment").add(data)
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

            while(!task.isComplete());
        }
    }

    public void unenroll() {
        Task<?> task = enrollmentDoc.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                isEnrolled = false;
                enrollToggle.setText(R.string.enroll);
                enrollmentDoc = null;
                Toast.makeText(getApplicationContext(), "Un-enrolled successfully.", Toast.LENGTH_SHORT).show();
            }
        });

        while(!task.isComplete());
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