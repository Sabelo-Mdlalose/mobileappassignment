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

// activity to display recipes that can be made with current pantry ingredients
public class availableRecipesDisplay extends AppCompatActivity {

    // database helper instance
    DBHelper dbHelper;
    // list view showing available recipes
    ListView recipesListView;

    // array list storing recipe names
    ArrayList<String> recipenames = new ArrayList<>();
    // array adapter connecting recipe names to list view
    ArrayAdapter<String> adapter;

    // called when activity is created
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // enable edge to edge display
        EdgeToEdge.enable(this);
        // set layout for available recipes display activity
        setContentView(R.layout.available_recipes_display);

        // initialize db helper and ui views
        dbHelper = new DBHelper(this);
        recipesListView = findViewById(R.id.recipesListview);
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);

        // setup array adapter with simple list item layout and recipe names list
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,recipenames);
        recipesListView.setAdapter(adapter);

        // load matching recipes from database
        loadrecipes();

        // set item click listener to open recipe detail activity when a recipe is selected
        recipesListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedrecipe = recipenames.get(position);
            Intent intent = new Intent(availableRecipesDisplay.this, recipedetailactivity.class);
            intent.putExtra("recipeName", selectedrecipe);
            startActivity(intent);
        });

        // set bottom navigation item selection listener for navigation between screens
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_ingredients) {
                // navigate to second activity for managing ingredients
                startActivity(new Intent(availableRecipesDisplay.this, SecondActivity.class));
                return true;
            } else if (id == R.id.nav_home) {
                // navigate to main activity home screen
                startActivity(new Intent(availableRecipesDisplay.this, MainActivity.class));
                return true;
            }
            else if (id == R.id.nav_recipes) {
                return true;
            }
            return false;
        });
        // set default selected item in bottom navigation to recipes
        bottomNavigation.setSelectedItemId(R.id.nav_recipes);
    }

    // method to load matching recipes from database and update list adapter
    private void loadrecipes(){
        recipenames.clear();
        // query database for recipes matching pantry ingredients
        Cursor cursor = dbHelper.getMatchingRecipes();
        if (cursor != null) {
            int nameIndex = cursor.getColumnIndex("name");
            // check if name column exists and cursor has rows
            if (nameIndex != -1 && cursor.moveToFirst()) {
                do {
                    recipenames.add(cursor.getString(nameIndex));
                } while (cursor.moveToNext());
            }else {
                // add fallback message if no matching recipes found
                recipenames.add("No recipes match your current pantry. Try adding more ingredients!");
            }
            cursor.close();
        }else {
            // add error message if cursor is null
            recipenames.add("Error loading recipes.");
        }
        // notify adapter that dataset changed
        adapter.notifyDataSetChanged();
    }
}
