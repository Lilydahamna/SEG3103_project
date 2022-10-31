package com.seg2105.termprojectgroup21;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.hash.Hashing;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference usersRef = db.collection("users");

    EditText username_field;
    EditText password_field;
    Spinner role_field;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        role_field = findViewById(R.id.role_select);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.roles, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        role_field.setAdapter(adapter);

        username_field = findViewById(R.id.username_field);
        password_field = findViewById(R.id.password_field);
        username_field.setText(null);
        password_field.setText(null);
    }

    public void registerUser(View view) {
        String username = username_field.getText().toString();
        String password = password_field.getText().toString();
        String role = role_field.getSelectedItem().toString();

        //Validate fields (can be expanded later)
        if(username.equals("") || password.equals("")) {
            Toast.makeText(getApplicationContext(), "Please enter a valid username/password.", Toast.LENGTH_SHORT).show();
            return;
        }

        //Query for username
        usersRef.whereEqualTo("username", username).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    //If no entries exist, register. Otherwise do not.
                    if(task.getResult().isEmpty()) {
                        Map<String, Object> user = new HashMap<>();
                        user.put("username", username);
                        user.put("password", Hashing.sha256().hashString(password, StandardCharsets.UTF_8).toString());
                        user.put("role", role);
                        usersRef.add(user).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Toast.makeText(getApplicationContext(), "Registration successful!", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "An error has occurred.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(getApplicationContext(), "Username already exists.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "An error has occurred.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @SuppressLint("NotConstructor")
    public void loginUser(View view) {
        String username = username_field.getText().toString();
        String password = password_field.getText().toString();
        String role = role_field.getSelectedItem().toString();

        //Validate fields (can be expanded later)
        if(username.equals("") || password.equals("")) {
            Toast.makeText(getApplicationContext(), "Please enter a valid username/password.", Toast.LENGTH_SHORT).show();
            return;
        }

        //Queries for username/password and logs in user
        usersRef.whereEqualTo("username", username).whereEqualTo("password", Hashing.sha256().hashString(password, StandardCharsets.UTF_8).toString()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if(task.getResult().isEmpty()) {
                        Toast.makeText(getApplicationContext(), "Invalid credentials.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Login successful!", Toast.LENGTH_SHORT).show();
                        DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                        saveUser(doc.getId(), doc.getString("username"), doc.getString("role"));

                        switch(doc.getString("role")) {
                            case "Admin":
                                startActivity(new Intent(getApplicationContext(), AdminMenu.class));
                                break;
                            default:
                                // TODO: Clear stored credentials when unknown user type has logged in

                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "An error has occurred.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveUser(String id, String username, String role) {
        SharedPreferences sharedPref = getSharedPreferences("user", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("id", id);
        editor.putString("username", username);
        editor.putString("role", role);
        editor.commit();
    }

}