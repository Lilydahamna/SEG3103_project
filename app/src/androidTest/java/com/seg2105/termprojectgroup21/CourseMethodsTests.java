package com.seg2105.termprojectgroup21;

import static androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class CourseMethodsTests {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference coursesRef = db.collection("courses");
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
    public void testAddCourse(){
        startSize = 0;
        courseManagerRule.getScenario().onActivity(activity -> {

            coursesRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    startSize = queryDocumentSnapshots.size();
                    new Thread(() -> {
                        activity.addCourse("Physics", "PHY2224");
                        try {
                            runOnUiThread(()->{
                                coursesRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        endSize = queryDocumentSnapshots.size();
                                        assertEquals(startSize+1, endSize);
                                    }
                                });
                            });
                        } catch (Throwable e) {
                            System.out.println("could not execute post");
                        }
                    }).start();
                }
            });



        });


    }
}
