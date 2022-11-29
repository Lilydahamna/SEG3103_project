package com.seg2105.termprojectgroup21;

import static org.junit.Assert.assertEquals;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.seg2105.termprojectgroup21.Objects.Course;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StudentMethodsTest {
    Task<DocumentSnapshot> newCourseTask;
    DocumentReference courseReference;
    DocumentReference studentReference;
    Course course;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference enrollmentsRef = db.collection("enrollment");

    @Before
    public void setUp() {
        ActivityScenario<CourseManager> courseManagerScenario = ActivityScenario.launch(CourseManager.class);
        courseManagerScenario.onActivity(activity -> {
            courseReference = activity.addCourse("Student Test Course", "TST7883", 15);
            newCourseTask = courseReference.get();
            while(!newCourseTask.isComplete());
            DocumentSnapshot courseSnapshot = newCourseTask.getResult();
            course = new Course(courseSnapshot.getId(), courseSnapshot.getString("name"), courseSnapshot.getString("code"), courseSnapshot.getString("instructor_username"), ((Number)courseSnapshot.getLong("capacity")).intValue(), courseSnapshot.getString("description"));
        });
        courseManagerScenario.close();

        ActivityScenario<CourseEditorInstructor> courseEditorInstructorScenario = ActivityScenario.launch(CourseEditorInstructor.class);
        courseEditorInstructorScenario.onActivity(activity -> {
            activity.addEvent(course.getId(), 0, "00:00", "01:00");
        });
        courseEditorInstructorScenario.close();

        ActivityScenario<Login> LoginScenario = ActivityScenario.launch((Login.class));
        LoginScenario.onActivity(new ActivityScenario.ActivityAction<Login>() {
            @Override
            public void perform(Login activity) {
                studentReference = activity.registerUser("TestStudent", "password", "Student");
            }
        });

        LoginScenario.close();

    }

    @After
    public void tearDown() {
        ActivityScenario<CourseEditor> courseEditorScenario = ActivityScenario.launch(CourseEditor.class);
        courseEditorScenario.onActivity(activity -> {
            activity.removeCourse(course.getId());
        });
        courseEditorScenario.close();

        ActivityScenario<UserManager> UserManagerScenario = ActivityScenario.launch(UserManager.class);
        UserManagerScenario.onActivity(activity -> {
            studentReference.delete();
        });
    }

    @Test
    public void A_test_enroll() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), CourseDetails.class);
        Course course = this.course;
        intent.putExtra("course_id", course.getId());
        intent.putExtra("name", course.getName());
        intent.putExtra("code", course.getCode());
        ActivityScenario<CourseDetails> courseDetailsScenario = ActivityScenario.launch(intent);

        Task<QuerySnapshot> task = enrollmentsRef.whereEqualTo("course_id", course.getId()).get();
        while(!task.isComplete());
        int start = task.getResult().size();
        courseDetailsScenario.onActivity( activity -> {
            activity.enroll("TestStudent");

            Task<QuerySnapshot> task2 = enrollmentsRef.whereEqualTo("course_id", course.getId()).get();
            while(!task2.isComplete());
            int end = task2.getResult().size();
            assertEquals(start+1, end);
        });

    }

    @Test
    public void B_test_unenroll() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), CourseDetails.class);
        Course course = this.course;
        intent.putExtra("course_id", course.getId());
        intent.putExtra("name", course.getName());
        intent.putExtra("code", course.getCode());
        ActivityScenario<CourseDetails> courseDetailsScenario = ActivityScenario.launch(intent);

        Task<QuerySnapshot> task = enrollmentsRef.whereEqualTo("course_id", course.getId()).get();
        while(!task.isComplete());
        int start = task.getResult().size();
        courseDetailsScenario.onActivity( activity -> {
            activity.enroll("TestStudent");
            activity.unenroll();

            Task<QuerySnapshot> task2 = enrollmentsRef.whereEqualTo("course_id", course.getId()).get();
            while(!task2.isComplete());
            int end = task2.getResult().size();
            assertEquals(start, end);
        });
    }

}
