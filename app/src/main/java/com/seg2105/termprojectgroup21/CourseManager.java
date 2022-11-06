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
    private SharedPreferences sharedPref;
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
        sharedPref = getSharedPreferences("user", Context.MODE_PRIVATE);
        recyclerView = findViewById(R.id.courses_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        courseAdapter = new CourseAdapter(this, courses, this);
        recyclerView.setAdapter(courseAdapter);

        inputName = findViewById(R.id.course_name);
        inputCode = findViewById(R.id.course_code);

        add = findViewById(R.id.addBtn);
        search = findViewById(R.id.findBtn);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!areFieldsValid(getApplicationContext(), inputName.getText().toString(), inputCode.getText().toString())) return;
                addCourse(inputName.getText().toString(), inputCode.getText().toString());
            }
        });
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

    private void fetchCourses() {
        courses.clear();
        coursesRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        courses.add(new Course(doc.getId(), doc.getString("name"), doc.getString("code"), doc.getString("instructor_username")));
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
            //TODO: attach all the instructor fields using putExtra so they can be used in the instructor course editor.
            Intent intent = new Intent(getApplicationContext(), CourseEditorInstructor.class);
            intent.putExtra("doc_id", course.getId());
            startActivity(intent);
            Toast.makeText(getApplicationContext(), "Enter Course Editor for Instructors.", Toast.LENGTH_SHORT).show();
        } else if (course.getInstructor() == null) {
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
            new AlertDialog.Builder(this)
                    .setTitle("Instructor Assignment")
                    .setMessage("There is already an instructor assigned to this course.")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Ok", null).show();
        }
    }

    private void assignInstructor(String doc_id) {
        coursesRef.document(doc_id).update("instructor_username", sharedPref.getString("username", "")).addOnSuccessListener(new OnSuccessListener<Void>() {
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
    }

    private void clearFields() {
        inputCode.setText("");
        inputName.setText("");
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
                                clearFields();
                                fetchCourses();
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