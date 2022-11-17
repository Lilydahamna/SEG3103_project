package com.seg2105.termprojectgroup21;

import static org.junit.Assert.assertEquals;

import androidx.test.core.app.ActivityScenario;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.After;
import org.junit.Test;

public class TestRegisterUser {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference usersRef = db.collection("users");
    int startSize, endSize;

    @After
    public void cleanUsers(){
        Task<QuerySnapshot> getJohn = usersRef.whereEqualTo("username", "John Doe").get();
        while(!getJohn.isComplete());
        String johnID = getJohn.getResult().getDocuments().get(0).getId();

        Task<QuerySnapshot> getJane = usersRef.whereEqualTo("username", "Jane Doe").get();
        while(!getJane.isComplete());
        String janeID = getJane.getResult().getDocuments().get(0).getId();

        Task<Void> deleteJohn = usersRef.document(johnID).delete();
        while(!deleteJohn.isComplete());

        Task<Void> deleteJane = usersRef.document(janeID).delete();
        while(!deleteJane.isComplete());

    }
    @Test
    public void testRegisterUser() {
        Task<QuerySnapshot> task = usersRef.get();
        while(!task.isComplete());
        startSize = task.getResult().size();
        ActivityScenario<Login> loginScenario = ActivityScenario.launch(Login.class);
        loginScenario.onActivity(activity -> {
            activity.registerUser("John Doe", "password1", "Student");
            //duplicate names not added
            activity.registerUser("John Doe", "password123", "Student");
            activity.registerUser("Jane Doe", "password2", "Instructor");

            Task<QuerySnapshot> newTask = usersRef.get();
            while(!newTask.isComplete());
            if (newTask.isSuccessful()) {
                endSize = newTask.getResult().size();
                System.out.println(startSize);
                System.out.println(endSize);
                assertEquals("registerUser test failed!",startSize + 2, endSize);
            }

        });
        loginScenario.close();


    }
}
