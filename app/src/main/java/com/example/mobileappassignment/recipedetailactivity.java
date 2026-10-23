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

public class recipedetailactivity extends AppCompatActivity {
    DBHelper dbHelper;
    TextView recipeNameView, instructionsView;
    ListView ingredientsListView;

    ArrayList<String> ingredientsList = new ArrayList<>();
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recipedetailactivity);

        dbHelper = new DBHelper(this);

        recipeNameView = findViewById(R.id.recipeName);
        instructionsView = findViewById(R.id.instructions);
        ingredientsListView = findViewById(R.id.ingredientsList);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, ingredientsList);
        ingredientsListView.setAdapter(adapter);

        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);


        // Get recipe name from intent
        String recipeName = getIntent().getStringExtra("recipeName");

        if (recipeName != null){
            recipeNameView.setText(recipeName);
            loadRecipeDetails(recipeName);
        }else {
            recipeNameView.setText("error: no recipe selected");
        }
        loadRecipeDetails(recipeName);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_ingredients) {
                startActivity(new Intent(recipedetailactivity.this, SecondActivity.class));
                return true;
            } else if (id == R.id.nav_home) {
                startActivity(new Intent(recipedetailactivity.this, MainActivity.class));
                return true;
            }
            else if (id == R.id.nav_recipes) {
                startActivity(new Intent(recipedetailactivity.this, availableRecipesDisplay.class));
                return true;
            }
            return false;

        });


    }
    private void loadRecipeDetails(String recipeName) {
        if (recipeName == null || recipeName.isEmpty()) return;

        // Get instructions
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT instructions FROM recipes WHERE name = ?",
                new String[]{recipeName});
        if (cursor.moveToFirst()) {
            instructionsView.setText(cursor.getString(0));
        }
        cursor.close();

        // Get ingredients
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
                ingredientsList.add(ingredient + " - " + qty + " " + unit);
            } while (ingCursor.moveToNext());
        }
        ingCursor.close();
        adapter.notifyDataSetChanged();
    }

}