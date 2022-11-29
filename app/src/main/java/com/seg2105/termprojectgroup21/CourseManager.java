package com.seg2105.termprojectgroup21;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.seg2105.termprojectgroup21.Adapters.CourseAdapter;
import com.seg2105.termprojectgroup21.Objects.Course;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CourseManager extends AppCompatActivity implements CourseAdapter.onItemClickListener {
    private SharedPreferences sharedPref;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference coursesRef = db.collection("courses");
    RecyclerView recyclerView;
    CourseAdapter courseAdapter;
    ArrayList<Course> courses = new ArrayList<>();
    Button add, search;
    EditText inputName, inputCode;
    LinearLayout btnLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_manager);
        sharedPref = getSharedPreferences("user", Context.MODE_PRIVATE);
        recyclerView = findViewById(R.id.schedule);
        //recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        courseAdapter = new CourseAdapter(this, courses, this);
        recyclerView.setAdapter(courseAdapter);

        inputName = findViewById(R.id.course_name);
        inputCode = findViewById(R.id.course_code);

        btnLayout = findViewById(R.id.btnLayout);
        search = createButton(R.string.search);
        //create add button only if logged in as admin
        if(sharedPref.getString("role", "").equals("Admin")){
            add = createButton(R.string.add);
        }

        inputCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.toString().length() == 0 && inputName.getText().toString().length() == 0){
                    courseAdapter.filterList(courses);
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
                if(editable.toString().length() == 0 && inputCode.getText().toString().length() == 0){
                    courseAdapter.filterList(courses);
                }
            }
        });

    }
    private Button createButton(int stringReference){
        Button temp = new Button(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                100);
        temp.setBackgroundColor(Color.parseColor("#FF6200EE"));
        temp.setTextColor(Color.parseColor("#FFFFFFFF"));
        temp.setText(stringReference);
        if(stringReference == R.string.add){
            params.setMargins(40,0,0,0);
            temp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!areFieldsValid(getApplicationContext(), inputName.getText().toString(), inputCode.getText().toString())) return;
                    addCourse(inputName.getText().toString(), inputCode.getText().toString());
                }
            });
        }else{
            params.setMargins(0,0,0,0);
            temp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    searchCourse(inputName.getText().toString(), inputCode.getText().toString());
                }
            });
        }
        temp.setLayoutParams(params);
        btnLayout.addView(temp);
        return temp;
    }

    private void searchCourse(String name, String code){

        ArrayList<Course> result = new ArrayList<>();
        if(name.length() == 0 && code.length() == 0){
            result = courses;
        }
        name = name.toLowerCase();
        code = code.toLowerCase();
        for(Course course: courses){
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

    public static boolean areFieldsValid(Context context, String name, String code) {
        if(name.isEmpty()) {
            Toast.makeText(context, "Invalid course name.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(!code.matches("^([A-Z]{3}[0-9]{4})$")) {
            Toast.makeText(context, "Invalid course code.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        fetchCourses();

    }

    private void fetchCourses()  {
        courses.clear();
        Task<QuerySnapshot> task = coursesRef.get();
        /*.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        courses.add(new Course(doc.getId(), doc.getString("name"), doc.getString("code"), doc.getString("instructor_username"), ((Number)doc.getLong("capacity")).intValue(), doc.getString("description")));
                    }
                    searchCourse(inputName.getText().toString(), inputCode.getText().toString());
                } else {
                    Toast.makeText(getApplicationContext(), "An error has occurred trying to fetch courses.", Toast.LENGTH_SHORT).show();
                }
            }
        });*/
        while(!task.isComplete());
        if (task.isSuccessful()) {
            for (QueryDocumentSnapshot doc : task.getResult()) {
                courses.add(new Course(doc.getId(), doc.getString("name"), doc.getString("code"), doc.getString("instructor_username"), ((Number)doc.getLong("capacity")).intValue(), doc.getString("description")));
            }
            searchCourse(inputName.getText().toString(), inputCode.getText().toString());
        } else {
            Toast.makeText(getApplicationContext(), "An error has occurred trying to fetch courses.", Toast.LENGTH_SHORT).show();
        }
        //while(!fetchTask.isComplete());
    }

    @Override
    public void onItemClick(Course course) {
        switch (sharedPref.getString("role", "")) {
            case "Admin":
                adminPress(course);
                break;
            case "Instructor":
                instructorPress(course);
                break;
        }
    }

    private void adminPress(Course course) {
        Intent intent = new Intent(getApplicationContext(), CourseEditor.class);
        intent.putExtra("doc_id", course.getId());
        intent.putExtra("name", course.getName());
        intent.putExtra("code", course.getCode());
        startActivity(intent);
    }

    private void instructorPress(Course course) {
        if(sharedPref.getString("username", "").equals(course.getInstructor())) {
            Intent intent = new Intent(getApplicationContext(), CourseEditorInstructor.class);
            intent.putExtra("doc_id", course.getId());
            intent.putExtra("capacity", course.getCapacity());
            intent.putExtra("description", course.getDescription());
            startActivity(intent);
        } else if (course.getInstructor().equals("")) {
            new AlertDialog.Builder(this)
                    .setTitle("Instructor Assignment")
                    .setMessage("Would you like to assign yourself as an instructor for this course?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            assignInstructor(course.getId());
                        }})
                    .setNegativeButton("No", null).show();
        } else {
            Intent intent = new Intent(getApplicationContext(), CourseDetails.class);
            intent.putExtra("course_id", course.getId());
            startActivity(intent);
        }
    }

    private void assignInstructor(String doc_id) {
        Task<?> assignTask = coursesRef.document(doc_id).update("instructor_username", sharedPref.getString("username", "")).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(), "Instructor assigned.", Toast.LENGTH_SHORT).show();
                fetchCourses();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Error assigning instructor.", Toast.LENGTH_SHORT).show();
            }
        });

        while(!assignTask.isComplete());
    }

    private void clearFields() {
        inputCode.setText("");
        inputName.setText("");
    }

    public DocumentReference addCourse(String name, String code) {
        boolean exists = false;
        for(Course course: courses){
            if(course.getCode().equals(code)){
                exists = true;
                break;
            }
        }
        if(!exists) {
            Map<String, Object> course = new HashMap<>();
            course.put("name", name);
            course.put("code", code);
            course.put("capacity", 0);
            course.put("instructor_username", "");
            course.put("description", "");
            Task<DocumentReference> addTask = coursesRef.add(course);

            while(!addTask.isComplete());

            if (addTask.isSuccessful()) {
                Toast.makeText(getApplicationContext(), "Course addition successful!", Toast.LENGTH_SHORT).show();
                clearFields();
                fetchCourses();

                return addTask.getResult();
            }
            else {
                Toast.makeText(getApplicationContext(), "An error has occurred.", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "A course with that code already exists.", Toast.LENGTH_SHORT).show();
            Task<QuerySnapshot> getTask = coursesRef.whereEqualTo("code", code).get();
            while(!getTask.isComplete());
            return getTask.getResult().getDocuments().get(0).getReference();
        }

        return null;
    }

    public DocumentReference addCourse(String name, String code, int capacity) {
        boolean exists = false;
        for(Course course: courses){
            if(course.getCode().equals(code)){
                exists = true;
                break;
            }
        }
        if(!exists) {
            Map<String, Object> course = new HashMap<>();
            course.put("name", name);
            course.put("code", code);
            course.put("capacity", capacity);
            course.put("instructor_username", "");
            course.put("description", "");
            Task<DocumentReference> addTask = coursesRef.add(course);

            while(!addTask.isComplete());

            if (addTask.isSuccessful()) {
                Toast.makeText(getApplicationContext(), "Course addition successful!", Toast.LENGTH_SHORT).show();
                clearFields();
                fetchCourses();

                return addTask.getResult();
            }
            else {
                Toast.makeText(getApplicationContext(), "An error has occurred.", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "A course with that code already exists.", Toast.LENGTH_SHORT).show();
            Task<QuerySnapshot> getTask = coursesRef.whereEqualTo("code", code).get();
            while(!getTask.isComplete());
            return getTask.getResult().getDocuments().get(0).getReference();
        }

        return null;
    }

}