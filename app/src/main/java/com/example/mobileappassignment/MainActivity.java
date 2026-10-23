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


public class MainActivity extends AppCompatActivity {




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_ingredients) {
                startActivity(new Intent(MainActivity.this, SecondActivity.class));
                return true;
            } else if(id == R.id.nav_recipes) {
                startActivity(new Intent(MainActivity.this, availableRecipesDisplay.class));
                return true;
            }else if (id == R.id.nav_home ){
                return true;
            }
            return false;

        });
        bottomNavigation.setSelectedItemId(R.id.nav_home);
    }
}

