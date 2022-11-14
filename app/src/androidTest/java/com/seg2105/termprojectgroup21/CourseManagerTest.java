package com.seg2105.termprojectgroup21;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import android.content.Context;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class CourseManagerTest {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference coursesRef = db.collection("courses");

    @Before
    public void setUp() throws Exception {
        coursesRef.whereEqualTo("code", "TST1234").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(queryDocumentSnapshots.size() > 0) coursesRef.document(queryDocumentSnapshots.getDocuments().get(0).getId()).delete();
            }
        });
    }

    @After
    public void tearDown() throws Exception {
        coursesRef.whereEqualTo("code", "TST1234").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(queryDocumentSnapshots.size() > 0) coursesRef.document(queryDocumentSnapshots.getDocuments().get(0).getId()).delete();
            }
        });
    }

    @Rule
    public ActivityScenarioRule<CourseManager> courseManagerRule = new ActivityScenarioRule<>(
            CourseManager.class);
    int startSize, endSize;

    @Test
    public void testFetchCourses() {
        startSize = 0;
        courseManagerRule.getScenario().onActivity(activity -> {
            coursesRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()){
                        startSize = task.getResult().size();
                    }
                    endSize = activity.courses.size();
                    System.out.println(startSize);
                    System.out.println(endSize);
                    assertEquals(startSize, endSize);

                }
            });

        });

    }

    @Test
    public void testAddCourse() {
        startSize = 0;
        courseManagerRule.getScenario().onActivity(activity -> {
            coursesRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        startSize = task.getResult().size();
                        activity.addCourse("test_course", "TST1234");
                        coursesRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    endSize = task.getResult().size();
                                    assertEquals(startSize + 1, endSize);
                                } else throw new RuntimeException("second fetch failed");
                            }
                        });
                    } else throw new RuntimeException("first fetch failed");
                }
            });

        });
    }

    @Test
    public void testAddDuplicateCourse() {
        startSize = 0;
        courseManagerRule.getScenario().onActivity(activity -> {
            coursesRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        startSize = task.getResult().size();
                        activity.addCourse("test_course", "TST1234");
                        activity.addCourse("test_course", "TST1234");
                        coursesRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    endSize = task.getResult().size();
                                    assertEquals(startSize + 1, endSize);
                                } else throw new RuntimeException("second fetch failed");
                            }
                        });
                    } else throw new RuntimeException("first fetch failed");
                }
            });

        });
    }

    @Test
    public void testAreFieldsValid() {

        courseManagerRule.getScenario().onActivity(activity -> {
            Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
            boolean[] expectedArray = {false, false, false, false, true, false, false, false};
            boolean[] actualArray = {CourseManager.areFieldsValid(appContext,"", ""),
                    CourseManager.areFieldsValid(appContext,"name", ""),
                    CourseManager.areFieldsValid(appContext,"name", "code"),
                    CourseManager.areFieldsValid(appContext,"name", "tst1234"),
                    CourseManager.areFieldsValid(appContext,"name", "TST1234"),
                    CourseManager.areFieldsValid(appContext,"", "TST1234"),
                    CourseManager.areFieldsValid(appContext,"", "tst1234"),
                    CourseManager.areFieldsValid(appContext,"", "code")};
            assertArrayEquals(expectedArray, actualArray);
        });
    }
}
