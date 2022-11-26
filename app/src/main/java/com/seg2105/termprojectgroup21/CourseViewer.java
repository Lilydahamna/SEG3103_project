package com.seg2105.termprojectgroup21;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.seg2105.termprojectgroup21.Adapters.CourseAdapter;
import com.seg2105.termprojectgroup21.Objects.Course;

import java.util.ArrayList;

public class CourseViewer extends AppCompatActivity implements CourseAdapter.onItemClickListener {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference coursesRef = db.collection("courses");
    Spinner day_picker;
    Button search;
    RecyclerView recyclerView;
    CourseAdapter courseAdapter;
    EditText inputName, inputCode;
    ToggleButton viewToggle;

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
    }

    @Override
    protected void onStart() {
        super.onStart();
        fetchCourses();
    }

    private void searchCourse(String name, String code, int day, Boolean enrolledOnly){
        ArrayList<Course> result = courses;
        //TODO: Add search. All Courses/My Courses and Day have to be queried, both of which will give a list of course docIds that satisfy the condition which can be used to filter like the search in CourseManager.
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