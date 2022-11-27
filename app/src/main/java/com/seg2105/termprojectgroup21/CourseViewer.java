package com.seg2105.termprojectgroup21;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.seg2105.termprojectgroup21.Adapters.CourseAdapter;
import com.seg2105.termprojectgroup21.Objects.Course;
import com.seg2105.termprojectgroup21.Objects.ScheduleItem;

import java.util.ArrayList;
import java.util.HashSet;

public class CourseViewer extends AppCompatActivity implements CourseAdapter.onItemClickListener {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference coursesRef = db.collection("courses");
    CollectionReference enrollmentRef = db.collection("enrollment");
    CollectionReference scheduleRef = db.collection("course_days");
    Spinner day_picker;
    Button search;
    RecyclerView recyclerView;
    CourseAdapter courseAdapter;
    EditText inputName, inputCode;
    ToggleButton viewToggle;
    SharedPreferences sharedPref;

    ArrayList<Course> courses = new ArrayList<>();




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_viewer);

        recyclerView = findViewById(R.id.schedule);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        courseAdapter = new CourseAdapter(this, courses, this);
        recyclerView.setAdapter(courseAdapter);

        day_picker = findViewById(R.id.Day);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.days_extended, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        day_picker.setAdapter(adapter);

        inputName = findViewById(R.id.course_name);
        inputCode = findViewById(R.id.course_code);
        viewToggle = findViewById(R.id.viewToggle);
        search = findViewById(R.id.search);

        sharedPref = getSharedPreferences("user", Context.MODE_PRIVATE);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchCourse(inputName.getText().toString(), inputCode.getText().toString(), getDayInt(day_picker.getSelectedItem().toString()), viewToggle.isChecked());
            }
        });

        viewToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                searchCourse(inputName.getText().toString(), inputCode.getText().toString(), getDayInt(day_picker.getSelectedItem().toString()), isChecked);
            }
        });


        inputCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.toString().length() == 0){
                    searchCourse(inputName.getText().toString(), inputCode.getText().toString(), getDayInt(day_picker.getSelectedItem().toString()), viewToggle.isChecked());
                }
            }
        });
        inputName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.toString().length() == 0){
                    searchCourse(inputName.getText().toString(), inputCode.getText().toString(), getDayInt(day_picker.getSelectedItem().toString()), viewToggle.isChecked());
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        fetchCourses();
    }

    private void searchCourse(String name, String code, int day, Boolean enrolledOnly){
        //contain course ID of courses that match day filter
        HashSet<String> searchDay = new HashSet<>();

        //contains courses that match day filter and enrolment status
        ArrayList<Course> filteredCourses = new ArrayList<>();

        //contains IDs of courses that match enrolment status
        ArrayList<String> coursesEnrolled = new ArrayList<>();

        name = name.toLowerCase();
        code = code.toLowerCase();

        Task<QuerySnapshot> task;
        if(day != -1){
            task = scheduleRef.whereEqualTo("day", day).get();
            while(!task.isComplete());
            if(task.isSuccessful()){
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    searchDay.add(doc.getString("course_id"));
                }
            }else{
                Toast.makeText(getApplicationContext(), "An error has occurred trying to fetch schedule.", Toast.LENGTH_SHORT).show();
            }
        }


        if(enrolledOnly){
            task = enrollmentRef.whereEqualTo("student_username", sharedPref.getString("username", "")).get();
            while(!task.isComplete());
            if(task.isSuccessful()){

                for (QueryDocumentSnapshot doc : task.getResult()) {
                    coursesEnrolled.add(doc.getString("course_id"));
                }

            }else{
                Toast.makeText(getApplicationContext(), "An error has occurred trying to fetch courses student enrolled in.", Toast.LENGTH_SHORT).show();
            }

            if(day != -1){
                for(String courseID: coursesEnrolled){
                    if(!searchDay.contains(courseID)){
                        coursesEnrolled.remove(courseID);
                    }
                }
                Toast.makeText(getApplicationContext(), "finished", Toast.LENGTH_SHORT).show();

            }

        }

        if(day == -1 && !enrolledOnly){
            filteredCourses = courses;
        }else if(day != -1 && !enrolledOnly){
            for(Course course: courses){
                if(searchDay.contains(course.getId())){
                    filteredCourses.add(course);
                }
            }
        }else{
            for(String courseID: coursesEnrolled){
                for(Course course: courses){
                    if (course.getId().equals(courseID)){
                        filteredCourses.add(course);

                    }
                }
            }
        }


        searchHelper(filteredCourses, name, code);
    }

    private void searchHelper(ArrayList<Course> filteredCourses, String name, String code){
        ArrayList<Course> result = new ArrayList<>();

        if(name.length() == 0 && code.length() == 0){
            result = filteredCourses;
        }

        for(Course course: filteredCourses){
            String courseCode = course.getCode().toLowerCase();
            String courseName = course.getName().toLowerCase();
            if(!code.equals("") && !name.equals("")){
                if(courseCode.contains(code) && courseName.contains(name)){
                    result.add(course);
                }
            }else if(!code.equals("")){
                if(courseCode.contains(code)){
                    result.add(course);
                }
            }else if(!name.equals("")){
                if(courseName.contains(name)){
                    result.add(course);
                }
            }
        }



        courseAdapter.filterList(result);

    }

    private void fetchCourses()  {
        courses.clear();
        coursesRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            courses.add(new Course(doc.getId(), doc.getString("name"), doc.getString("code"), doc.getString("instructor_username"), ((Number)doc.getLong("capacity")).intValue(), doc.getString("description")));
                            searchCourse(inputName.getText().toString(), inputCode.getText().toString(), getDayInt(day_picker.getSelectedItem().toString()), viewToggle.isChecked());
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "An error has occurred trying to fetch courses.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    public int getDayInt(String day) {
        switch (day) {
            case "Monday":
                return 0;
            case "Tuesday":
                return 1;
            case "Wednesday":
                return 2;
            case "Thursday":
                return 3;
            case "Friday":
                return 4;
            default:
                return -1;
        }
    }

    @Override
    public void onItemClick(Course course) {
        Intent intent = new Intent(getApplicationContext(), CourseDetails.class);
        intent.putExtra("course_id", course.getId());
        intent.putExtra("name", course.getName());
        intent.putExtra("code", course.getCode());
        startActivity(intent);
    }
}