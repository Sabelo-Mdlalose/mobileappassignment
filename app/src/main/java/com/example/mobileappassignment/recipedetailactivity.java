package com.example.mobileappassignment;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

// activity to display detailed information and ingredients for a selected recipe
public class recipedetailactivity extends AppCompatActivity {
    // database helper instance
    DBHelper dbHelper;
    // text views for recipe name and instructions
    TextView recipeNameView, instructionsView;
    // list view for recipe ingredients
    ListView ingredientsListView;

    // list holding ingredient strings required for the recipe
    ArrayList<String> ingredientsList = new ArrayList<>();
    // array adapter connecting ingredients list to list view
    ArrayAdapter<String> adapter;

    // called when activity is created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // enable edge to edge display
        EdgeToEdge.enable(this);
        // set layout for recipe detail activity
        setContentView(R.layout.activity_recipedetailactivity);

        // initialize database helper
        dbHelper = new DBHelper(this);

        // bind text view and list view references by id
        recipeNameView = findViewById(R.id.recipeName);
        instructionsView = findViewById(R.id.instructions);
        ingredientsListView = findViewById(R.id.ingredientsList);

        // setup array adapter with simple list item layout and ingredients list
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, ingredientsList);
        ingredientsListView.setAdapter(adapter);

        // bind bottom navigation view reference
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);

        // get recipe name passed from intent extras
        String recipeName = getIntent().getStringExtra("recipeName");

        // check if recipe name is valid and load details
        if (recipeName != null){
            recipeNameView.setText(recipeName);
            loadRecipeDetails(recipeName);
        }else {
            recipeNameView.setText("error: no recipe selected");
        }
        loadRecipeDetails(recipeName);

        // set bottom navigation item selection listener for screen transitions
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_ingredients) {
                // navigate to ingredients activity
                startActivity(new Intent(recipedetailactivity.this, SecondActivity.class));
                return true;
            } else if (id == R.id.nav_home) {
                // navigate to main activity home screen
                startActivity(new Intent(recipedetailactivity.this, MainActivity.class));
                return true;
            }
            else if (id == R.id.nav_recipes) {
                // navigate back to available recipes display
                startActivity(new Intent(recipedetailactivity.this, availableRecipesDisplay.class));
                return true;
            }
            return false;
        });
    }

    // method to query and load recipe instructions and required ingredients from database
    private void loadRecipeDetails(String recipeName) {
        if (recipeName == null || recipeName.isEmpty()) return;

        // query database to retrieve instructions for the specified recipe name
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT instructions FROM recipes WHERE name = ?",
                new String[]{recipeName});
        if (cursor.moveToFirst()) {
            instructionsView.setText(cursor.getString(0));
        }
        cursor.close();

        // query database to retrieve required ingredients, quantities, and units for the recipe
        Cursor ingCursor = dbHelper.getReadableDatabase().rawQuery(
            "SELECT ri.ingredient, ri.quantity, ri.unit " +
                    "FROM recipe_ingredients ri " +
                    "JOIN recipes r ON ri.recipe_id = r.id " +
                    "WHERE r.name = ?",
                new String[]{recipeName});
        ingredientsList.clear();
        if (ingCursor.moveToFirst()) {
            do {
                String ingredient = ingCursor.getString(0);
                double qty = ingCursor.getDouble(1);
                String unit = ingCursor.getString(2);
                // format ingredient item string and add to list
                ingredientsList.add(ingredient + " - " + qty + " " + unit);
            } while (ingCursor.moveToNext());
        }
        ingCursor.close();
        // notify adapter that dataset changed
        adapter.notifyDataSetChanged();
    }
}
