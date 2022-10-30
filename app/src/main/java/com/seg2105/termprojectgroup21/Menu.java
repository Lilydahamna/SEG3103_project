package com.seg2105.termprojectgroup21;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class Menu extends AppCompatActivity {
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        sharedPref = getSharedPreferences("user", Context.MODE_PRIVATE);

        TextView textview = findViewById(R.id.welcomeText);
        String welcomeString = "Welcome "+sharedPref.getString("username", "")+"! You are logged in as "+sharedPref.getString("role", "");
        textview.setText(welcomeString);
    }

    public void logoutUser(View view) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(getApplicationContext(), Login.class);
        startActivity(intent);
    }
}