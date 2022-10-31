package com.seg2105.termprojectgroup21;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class Menu extends AppCompatActivity {

    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPref = getSharedPreferences("user", Context.MODE_PRIVATE);
    }

    public void logoutUser() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.apply();

        startActivity(new Intent(getApplicationContext(), Login.class));
    }
}