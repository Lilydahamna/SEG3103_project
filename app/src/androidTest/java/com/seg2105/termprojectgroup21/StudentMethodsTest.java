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
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StudentMethodsTest {
    Task<DocumentSnapshot> newCourseTask;
    DocumentReference courseReference;
    DocumentReference studentReference, studentReference2;
    Course course, course2;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference enrollmentsRef = db.collection("enrollment");
    boolean flag = false;
    @Before
    public void setUp() {

            ActivityScenario<CourseManager> courseManagerScenario = ActivityScenario.launch(CourseManager.class);
            courseManagerScenario.onActivity(activity -> {
                courseReference = activity.addCourse("Student Test Course", "TST7883", 1);
                newCourseTask = courseReference.get();
                while(!newCourseTask.isComplete());
                DocumentSnapshot courseSnapshot = newCourseTask.getResult();
                course = new Course(courseSnapshot.getId(), courseSnapshot.getString("name"), courseSnapshot.getString("code"), courseSnapshot.getString("instructor_username"), ((Number)courseSnapshot.getLong("capacity")).intValue(), courseSnapshot.getString("description"));
                //course with conflicting schedule
                courseReference = activity.addCourse("Student Test Course 2", "TST7884", 1);
                newCourseTask = courseReference.get();
                while(!newCourseTask.isComplete());
                courseSnapshot = newCourseTask.getResult();
                course2 = new Course(courseSnapshot.getId(), courseSnapshot.getString("name"), courseSnapshot.getString("code"), courseSnapshot.getString("instructor_username"), ((Number)courseSnapshot.getLong("capacity")).intValue(), courseSnapshot.getString("description"));
            });
            courseManagerScenario.close();

            ActivityScenario<CourseEditorInstructor> courseEditorInstructorScenario = ActivityScenario.launch(CourseEditorInstructor.class);
            courseEditorInstructorScenario.onActivity(activity -> {
                activity.addEvent(course.getId(), 0, "13:00", "14:00");
                activity.addEvent(course2.getId(), 0, "13:30", "14:30");

            });
            courseEditorInstructorScenario.close();

            ActivityScenario<Login> LoginScenario = ActivityScenario.launch((Login.class));
            LoginScenario.onActivity(new ActivityScenario.ActivityAction<Login>() {
                @Override
                public void perform(Login activity) {
                    studentReference = activity.registerUser("TestStudent", "password", "Student");
                    studentReference2 = activity.registerUser("TestStudent2", "password", "Student");
                }
            });

            LoginScenario.close();


    }

    @After
    public void tearDown() {

            ActivityScenario<CourseEditor> courseEditorScenario = ActivityScenario.launch(CourseEditor.class);
            courseEditorScenario.onActivity(activity -> {
                activity.removeCourse(course.getId());
                activity.removeCourse(course2.getId());
            });
            courseEditorScenario.close();

            ActivityScenario<UserManager> UserManagerScenario = ActivityScenario.launch(UserManager.class);
            UserManagerScenario.onActivity(activity -> {
                studentReference.delete();
                studentReference2.delete();
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

        courseDetailsScenario.onActivity( activity -> {
            activity.enroll("TestStudent");
            //student 2 should not be added as capacity would be exceeded
            activity.enroll("TestStudent2");
            Task<QuerySnapshot> task2 = enrollmentsRef.whereEqualTo("course_id", course.getId()).get();
            while(!task2.isComplete());
            int end = task2.getResult().size();
            assertEquals(1, end);
        });

    }

    @Test
    public void B_test_enroll_conflicting_schedule() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), CourseDetails.class);
        Course course = this.course;
        intent.putExtra("course_id", course.getId());
        intent.putExtra("name", course.getName());
        intent.putExtra("code", course.getCode());
        ActivityScenario<CourseDetails> courseDetailsScenario = ActivityScenario.launch(intent);

        courseDetailsScenario.onActivity( activity -> {
            activity.enroll("TestStudent");
        });

        //should not be added since conflicting schedule
        course = this.course2;
        intent.putExtra("course_id", course.getId());
        intent.putExtra("name", course.getName());
        intent.putExtra("code", course.getCode());
        courseDetailsScenario = ActivityScenario.launch(intent);

        courseDetailsScenario.onActivity( activity -> {
            activity.enroll("TestStudent");
            Task<QuerySnapshot> task2 = enrollmentsRef.whereEqualTo("course_id", course2.getId()).get();
            while(!task2.isComplete());
            int end = task2.getResult().size();
            assertEquals(0, end);
        });

    }


    @Test
    public void C_test_unenroll() {
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
