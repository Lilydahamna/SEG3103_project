package com.seg2105.termprojectgroup21;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AdminMenu extends Menu {

    protected class MenuSwitcher implements View.OnClickListener {
        private Context applicationContext;

        public MenuSwitcher(Context applicationContext) {
            this.applicationContext = applicationContext;
        }

        @Override
        public void onClick(View view) {
            switch(view.getId()) {
                case R.id.courseManagerButton:
                    startActivity(new Intent(applicationContext, CourseManager.class));
                    break;
                case R.id.userManagerButton:
                    startActivity(new Intent(applicationContext, UserManager.class));
                    break;
                default:
                    startActivity(new Intent(applicationContext, AdminMenu.class));
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_menu);

        // Create Objects corresponding to UI elements
        TextView textView = findViewById(R.id.welcomeText);
        Button logoutButton = findViewById(R.id.adminLogout);
        Button courseManagerButton = findViewById(R.id.courseManagerButton);
        Button userManagerButton = findViewById(R.id.userManagerButton);

        // Set Text strings
        textView.setText("Welcome "+sharedPref.getString("username", "")+"! You are logged in as "+sharedPref.getString("role", ""));

        // Create onClickListeners
        logoutButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                logoutUser();
            }
        });

        MenuSwitcher menuSwitcher = new MenuSwitcher(getApplicationContext());
        courseManagerButton.setOnClickListener(menuSwitcher);
        userManagerButton.setOnClickListener(menuSwitcher);



    }
}