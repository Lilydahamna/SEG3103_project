package com.seg2105.termprojectgroup21;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.seg2105.termprojectgroup21.Adapters.ScheduleItemAdapter;
import com.seg2105.termprojectgroup21.Objects.ScheduleItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CourseEditorInstructor extends AppCompatActivity implements ScheduleItemAdapter.onItemClickListener {

    Intent intent;
    String course_id, course_description;
    EditText description, capacity, start_time, end_time;
    int course_capacity;
    Button unassign, edit, view_students;
    Spinner day_picker;

    //private SharedPreferences sharedPref;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference scheduleRef = db.collection("course_days");
    CollectionReference coursesRef = db.collection("courses");

    RecyclerView recyclerView;
    ScheduleItemAdapter itemAdapter;
    ArrayList<ScheduleItem> schedule = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_editor_instructor);

        recyclerView = findViewById(R.id.schedule);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        itemAdapter = new ScheduleItemAdapter(this, schedule, this);
        recyclerView.setAdapter(itemAdapter);

        intent = getIntent();
        course_id = intent.getStringExtra("doc_id");
        course_capacity = intent.getIntExtra("capacity", 0);
        course_description = intent.getStringExtra("description");

        description = findViewById(R.id.CourseDescription);
        capacity = findViewById(R.id.CourseCapacity);
        description.setText(course_description);
        capacity.setText(String.valueOf(course_capacity));

        start_time = findViewById(R.id.StartTime);
        end_time = findViewById(R.id.EndTime);

        day_picker = findViewById(R.id.Day);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.days, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        day_picker.setAdapter(adapter);

        unassign = findViewById(R.id.Unassign);
        edit = findViewById(R.id.EditCourse);
        view_students = findViewById(R.id.view_students);

        unassign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unassignConfirm();
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(CourseEditorInstructor.areCourseFieldsValid(getApplicationContext(), Integer.parseInt(capacity.getText().toString()))) updateCourse(course_id, description.getText().toString(), Integer.parseInt(capacity.getText().toString()));
                int day = 0; // default value
                switch (day_picker.getSelectedItem().toString()) {
                    case "Tuesday":
                        day = 1;
                        break;
                    case "Wednesday":
                        day = 2;
                        break;
                    case "Thursday":
                        day = 3;
                        break;
                    case "Friday":
                        day = 4;
                        break;
                }
                if (areEventFieldsValid(getApplicationContext(), start_time.getText().toString(), end_time.getText().toString())) addEvent(course_id, day, start_time.getText().toString(), end_time.getText().toString());
            }
        });

        view_students.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), StudentViewer.class);
                intent.putExtra("course_id", course_id);
                intent.putExtra("course_capacity", course_capacity);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        fetchSchedule();
    }

    @Override
    public void onItemClick(ScheduleItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Schedule Deletion")
                .setMessage("Are you sure you want to delete the schedule event on " + item.getDay() + ", from " + item.getStartTime() + " to " + item.getEndTime() + "?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        deleteItem(item.getId());
                    }})
                .setNegativeButton("Cancel", null).show();
    }

    public static boolean areCourseFieldsValid(Context context, int capacity) {
        if(capacity < 0) {
            Toast.makeText(context, "Invalid course capacity.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // expand later IMPLEMENT REGEX
    public static boolean areEventFieldsValid(Context context, String start_time, String end_time) {
        if(start_time.isEmpty() || end_time.isEmpty() ) return false;
        if(!start_time.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$") || !end_time.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$")) {
            Toast.makeText(context, "Invalid start/end times.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void addEvent(String doc_id, int day, String start_time, String end_time) {
        // query for event, in case it already exists (in terms of day)
        scheduleRef.whereEqualTo("course_id", doc_id).whereEqualTo("day", day).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Map<String, Object> event = new HashMap<>();
                    event.put("course_id", doc_id);
                    event.put("day", day);
                    event.put("start", start_time);
                    event.put("end", end_time);
                    if(task.getResult().isEmpty()) { // no event found, can add a new one
                        scheduleRef.add(event).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Toast.makeText(getApplicationContext(), "Event addition successful!", Toast.LENGTH_SHORT).show();
                                clearFields();
                                fetchSchedule();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "An error has occurred.", Toast.LENGTH_SHORT).show();
                            }
                        });
                        // otherwise, inform user that the same event already exists
                    } else {
                        Toast.makeText(getApplicationContext(), "An event on the same day already exists.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "An error has occurred.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void clearFields() {
        start_time.setText("");
        end_time.setText("");
    }

    private void fetchSchedule() {
        schedule.clear();
        scheduleRef.whereEqualTo("course_id", course_id).orderBy("day", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
    }

    private void deleteItem(String doc_id) {
        scheduleRef.document(doc_id).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(), "Event successfully deleted!", Toast.LENGTH_SHORT).show();
                fetchSchedule();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Error deleting event.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateCourse(String doc_id, String description, int capacity) { // benjamin's
        Map<String, Object> course = new HashMap<>();
        course.put("description", description);
        course.put("capacity", capacity);
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

    public void unassignConfirm() {
        new AlertDialog.Builder(this)
                .setTitle("Instructor Un-assignment")
                .setMessage("Are you sure you want to un-assign yourself from this course?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        unassign(course_id);
                    }})
                .setNegativeButton("No", null).show();
    }

    public void unassign(String doc_id) {
        Map<String, Object> course = new HashMap<>();
        course.put("description", "");
        course.put("instructor_username", "");
        course.put("capacity", 0);
        coursesRef.document(doc_id).update(course).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                scheduleRef.whereEqualTo("course_id", course_id).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                doc.getReference().delete();
                            }
                            Toast.makeText(getApplicationContext(), "Unassigned successfully.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), CourseDetails.class);
                            intent.putExtra("course_id", course_id);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "Error unassigning.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Error unassigning.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}