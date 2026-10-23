package com.example.mobileappassignment;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import com.google.android.material.bottomnavigation.BottomNavigationView;

// main activity representing the home screen of the application
public class MainActivity extends AppCompatActivity {

    // called when main activity is created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // enable edge to edge layout display
        EdgeToEdge.enable(this);
        // set activity layout from xml resource
        setContentView(R.layout.activity_main);
        // find bottom navigation view reference by id
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);

        // set item selection listener for bottom navigation bar
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            // check if ingredients navigation item is selected
            if (id == R.id.nav_ingredients) {
                // start second activity for managing ingredients
                startActivity(new Intent(MainActivity.this, SecondActivity.class));
                return true;
                // check if recipes navigation item is selected
            } else if(id == R.id.nav_recipes) {
                // start available recipes display activity
                startActivity(new Intent(MainActivity.this, availableRecipesDisplay.class));
                return true;
                // check if home navigation item is selected
            }else if (id == R.id.nav_home ){
                return true;
            }
            return false;

        });
        // set default selected bottom navigation item to home
        bottomNavigation.setSelectedItemId(R.id.nav_home);
    }
}
