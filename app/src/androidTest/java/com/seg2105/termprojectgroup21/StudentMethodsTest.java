package com.seg2105.termprojectgroup21;

import static org.junit.Assert.assertEquals;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.seg2105.termprojectgroup21.Objects.Course;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

public class StudentMethodsTest {
    Task<DocumentReference> newUserTask;
    DocumentReference studentReference;

    @Before
    public void setUp() {
        ActivityScenario<CourseManager> courseManagerScenario = ActivityScenario.launch(CourseManager.class);
        courseManagerScenario.onActivity(activity -> {
            activity.addCourse("Student Test Course", "TST7883");
        });
        courseManagerScenario.close();

        ActivityScenario<Login> LoginScenario = ActivityScenario.launch((Login.class));
        LoginScenario.onActivity(new ActivityScenario.ActivityAction<Login>() {
            @Override
            public void perform(Login activity) {
                newUserTask = activity.registerUser("TestStudent", "password", "Student");
                while(!newUserTask.isComplete());
                studentReference = newUserTask.getResult();
            }
        });

        LoginScenario.close();

    }

    @After
    public void tearDown() {
        ActivityScenario<CourseEditor> courseEditorScenario = ActivityScenario.launch(CourseEditor.class);
        courseEditorScenario.onActivity(activity -> {
            activity.removeCourse("TST7883");
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
        Course course = null; //TODO: get course object of TST7883
        intent.putExtra("course_id", course.getId());
        intent.putExtra("name", course.getName());
        intent.putExtra("code", course.getCode());
        ActivityScenario<CourseDetails> courseDetailsScenario = ActivityScenario.launch(intent);

        int start = course.getEnrolledStudents();
        courseDetailsScenario.onActivity( activity -> {
            activity.enroll("TestStudent");
            assertEquals(start+1, course.getEnrolledStudents());
        });

    }

    @Test
    public void B_test_unenroll() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), CourseDetails.class);
        Course course = null; //TODO: get course object of TST7883
        intent.putExtra("course_id", course.getId());
        intent.putExtra("name", course.getName());
        intent.putExtra("code", course.getCode());
        ActivityScenario<CourseDetails> courseDetailsScenario = ActivityScenario.launch(intent);

        int start = course.getEnrolledStudents();
        courseDetailsScenario.onActivity( activity -> {
            activity.enroll("TestStudent");
            activity.unenroll();
            assertEquals(start, course.getEnrolledStudents());
        });
    }

}
