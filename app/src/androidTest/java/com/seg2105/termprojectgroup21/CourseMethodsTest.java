package com.seg2105.termprojectgroup21;

import static androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.*;

import android.content.Context;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4.class)
public class CourseMethodsTest {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference coursesRef = db.collection("courses");

    @Rule
    public ActivityScenarioRule<CourseManager> courseManagerRule = new ActivityScenarioRule<>(
            CourseManager.class);
    @Rule
    public ActivityScenarioRule<CourseEditor> courseEditorRule = new ActivityScenarioRule<>(
            CourseEditor.class);
    int startSize, endSize;

    @Test
    public void A_testAddCourse() {
        Task<QuerySnapshot> task = coursesRef.get();
        while(!task.isComplete());
        startSize = task.getResult().size();
        courseManagerRule.getScenario().onActivity(activity -> {

                    activity.addCourse("Physics", "PHY1001");

                    activity.addCourse("Physics", "PHY1002");

                    activity.addCourse("Physics", "PHY1003");
                    //does not add duplicates
                    activity.addCourse("Physics", "PHY1003");

                    activity.addCourse("Physics", "PHY1004");

                    Task<QuerySnapshot> newTask = coursesRef.get();
                    while(!newTask.isComplete());
                    if (newTask.isSuccessful()) {
                            endSize = newTask.getResult().size();
                            System.out.println(startSize);
                            System.out.println(endSize);
                            assertEquals("addCourse test failed!",startSize + 4, endSize);
                    }

            });


    }

    @Test
    public void B_testUpdateCourse(){
        courseEditorRule.getScenario().onActivity(activity -> {
            Task<QuerySnapshot> task = coursesRef.whereEqualTo("name", "Physics").whereEqualTo("code","PHY1004").get();
            while(!task.isComplete());
            if(!task.getResult().isEmpty()){
                String courseID = task.getResult().getDocuments().get(0).getId();
                activity.updateCourse(courseID,"Physics","2224");
                Task<DocumentSnapshot> newTask = coursesRef.document(courseID).get();
                while(!newTask.isComplete());
                String courseCode = newTask.getResult().getString("code");
                System.out.println(courseCode);
                assertEquals("Course code was not successfully updated!","2224", courseCode);

            }else{
                System.out.println("Course not found");
            }

        });

    }

   @Test
    public void C_testRemoveCourse(){
       Task<QuerySnapshot> getTask = coursesRef.get();
       while(!getTask.isComplete());
       startSize = getTask.getResult().size();
        courseEditorRule.getScenario().onActivity(activity -> {
            Task<QuerySnapshot> task = coursesRef.whereEqualTo("name", "Physics").get();
            while(!task.isComplete());
            int physicsCourses = task.getResult().size();
            ArrayList<String> docID = new ArrayList<>();
            if(task.isSuccessful()){
                for(QueryDocumentSnapshot doc: task.getResult()){
                    docID.add(doc.getId());
                }
            }

            for(String courseID: docID){
                activity.removeCourse(courseID);
            }

            Task<QuerySnapshot> newTask = coursesRef.get();
            while(!newTask.isComplete());
            if (newTask.isSuccessful()) {
                endSize = newTask.getResult().size();
                assertEquals("removeCourse test failed!",startSize - physicsCourses, endSize);
            }

        });

    }

    @Test
    public void D_testAreFieldsValid() {

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
            assertArrayEquals("areFieldsValid test failed!",expectedArray, actualArray);
        });
    }
}
