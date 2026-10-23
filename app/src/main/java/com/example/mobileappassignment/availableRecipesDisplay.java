package com.example.mobileappassignment;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class availableRecipesDisplay extends AppCompatActivity {

    DBHelper dbHelper;
    ListView recipesListView;

    ArrayList<String> recipenames = new ArrayList<>();
    ArrayAdapter<String> adapter;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.available_recipes_display);

        dbHelper = new DBHelper(this);
        recipesListView = findViewById(R.id.recipesListview);
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);


        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,recipenames);
        recipesListView.setAdapter(adapter);

        loadrecipes();

        recipesListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedrecipe = recipenames.get(position);
            Intent intent = new Intent(availableRecipesDisplay.this,recipedetailactivity.class);
            intent.putExtra("recipeName",selectedrecipe);
            startActivity(intent);
        });

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_ingredients) {
                startActivity(new Intent(availableRecipesDisplay.this, SecondActivity.class));
                return true;
            } else if (id == R.id.nav_home) {
                startActivity(new Intent(availableRecipesDisplay.this, MainActivity.class));
                return true;
            }
            else if (id == R.id.nav_recipes) {
                return true;
            }
            return false;

        });
        bottomNavigation.setSelectedItemId(R.id.nav_recipes);



    }

    private void loadrecipes(){
        recipenames.clear();
        Cursor cursor = dbHelper.getMatchingRecipes();
        if (cursor != null) {
            int nameIndex = cursor.getColumnIndex("name");
            if (nameIndex != -1 && cursor.moveToFirst()) {
                do {
                    recipenames.add(cursor.getString(nameIndex));
                } while (cursor.moveToNext());
            }else {
                recipenames.add("No recipes match your current pantry. Try adding more ingredients!");
            }
            cursor.close();
        }else {
            recipenames.add("Error loading recipes.");
        }
        adapter.notifyDataSetChanged();
    }

}
