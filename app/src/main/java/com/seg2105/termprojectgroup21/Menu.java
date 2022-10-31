package com.seg2105.termprojectgroup21;

import java.lang.Class;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class Menu extends AppCompatActivity {
    private LinearLayout menuSwitcher;
    private SharedPreferences sharedPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = getSharedPreferences("user", Context.MODE_PRIVATE);
        setContentView(R.layout.activity_menu);

        // Create Objects corresponding to UI elements
        TextView textView = findViewById(R.id.welcomeText);
        Button logoutButton = findViewById(R.id.logout);
        menuSwitcher = findViewById(R.id.menuSwitcher);


        // Set Text strings
        textView.setText("Welcome "+sharedPref.getString("username", "")+"! You are logged in as "+sharedPref.getString("role", ""));

        // Create onClickListeners
        logoutButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                logoutUser();
            }
        });

        // FIXME: Make it an iterable for loop over some array or sth
        switch(sharedPref.getString("role", "")) {
            case "Admin":
                menuSwitcher.addView(createMenuButton(R.string.course_manager_title, CourseManager.class));
                menuSwitcher.addView(createMenuButton(R.string.user_manager_title, UserManager.class));
                break;
        }
    }

    private Button createMenuButton(int stringReference, Class activity) {
        Button menuButton = new Button(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 0);
        menuButton.setLayoutParams(params);
        menuButton.back
        menuButton.setText(stringReference);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), activity));
            }
        });

        return menuButton;
    }


    public void logoutUser() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.apply();

        startActivity(new Intent(getApplicationContext(), Login.class));
    }
}