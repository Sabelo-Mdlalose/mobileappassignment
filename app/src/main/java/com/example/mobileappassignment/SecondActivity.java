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

import java.util.ArrayList;

// activity for managing pantry ingredients (insert, view, update, delete)
public class SecondActivity extends AppCompatActivity {
    // database helper instance
    DBHelper dbHelper;
    // input fields for ingredient name, quantity, and unit
    EditText ingredientInput, quantityInput, unitInput;
    // action buttons for database operations
    Button insertBtn, viewBtn, updateBtn, deleteBtn;
    // container for dynamic views during updates
    LinearLayout resultContainer;
    // list view to display retrieved ingredients
    ListView ingredientsListView;

    // array list holding ingredient display items
    ArrayList<String> ingredientsList = new ArrayList<>();
    // array adapter connecting list data to list view
    ArrayAdapter<String> adapter;

    // called when activity is created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // enable edge to edge display handling
        EdgeToEdge.enable(this);
        // set layout for second activity
        setContentView(R.layout.activity_second);

        // instantiate database helper
        dbHelper = new DBHelper(this);

        // bind ui elements to corresponding xml view ids
        ingredientInput = findViewById(R.id.ingredientInput);
        quantityInput = findViewById(R.id.quantityInput);
        unitInput = findViewById(R.id.unitInput);
        insertBtn = findViewById(R.id.insertBtn);
        viewBtn = findViewById(R.id.viewBtn);
        updateBtn = findViewById(R.id.updateBtn);
        deleteBtn = findViewById(R.id.deleteBtn);
        resultContainer = findViewById(R.id.resultsContainer);
        ingredientsListView = findViewById(R.id.ingredientsListView);
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);

        // initialize array adapter with simple list item layout and ingredients data list
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,ingredientsList);
        ingredientsListView.setAdapter(adapter);

        // set click listener for insert button to add ingredient to database
        insertBtn.setOnClickListener(v -> {
            boolean success = dbHelper.insertingredient(
                    ingredientInput.getText().toString(),
                    Double.parseDouble(quantityInput.getText().toString().trim()),
                    unitInput.getText().toString().trim()
            );
            // show toast message indicating success or failure of insertion
            Toast.makeText(this, success ? "Inserted!" : "Failed", Toast.LENGTH_SHORT).show();
        });

        // set click listener for view button to retrieve and display all pantry ingredients
        viewBtn.setOnClickListener(v -> {
            ingredientsList.clear();
            Cursor cursor = dbHelper.getingredient();
            // iterate through cursor to extract ingredient attributes
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String ingredient = cursor.getString(cursor.getColumnIndexOrThrow("ingredient"));
                double qty = cursor.getDouble(cursor.getColumnIndexOrThrow("quantity"));
                String unit = cursor.getString(cursor.getColumnIndexOrThrow("unit"));
                // format and add ingredient entry to list
                ingredientsList.add(id + ": " + ingredient + "-" + qty + " " + unit);
            }
            cursor.close();
            // notify adapter that dataset has changed
            adapter.notifyDataSetChanged();
        });

        // set item click listener on list view to select an ingredient for update or deletion
        ingredientsListView.setOnItemClickListener((parent, view, position, id) -> {
            String selected = ingredientsList.get(position);

            // parse selected item string to extract name, quantity, and unit
            String[] parts = selected.split(":");
            String[] ingParts = parts[1].split("-");
            String ingredient = ingParts[0].trim();
            String[] qtyUnit = ingParts[1].trim().split(" ");
            String qty = qtyUnit[0];
            String unit = qtyUnit.length > 1 ? qtyUnit[1] : "";

            // populate input fields with selected ingredient values
            ingredientInput.setText(ingredient);
            quantityInput.setText(qty);
            unitInput.setText(unit);
            // make update and delete buttons visible
            updateBtn.setVisibility(view.VISIBLE);
            deleteBtn.setVisibility(view.VISIBLE);
        });

        // set click listener for update button to prompt for new ingredient details
        updateBtn.setOnClickListener(v->{
            double oldQty = Double.parseDouble(quantityInput.getText().toString().trim());
            String oldUnit = unitInput.getText().toString().trim();

            resultContainer.removeAllViews();
            // create dynamic edit text for new ingredient name
            EditText newingredientInput = new EditText(this);
            newingredientInput.setHint("enter new ingredient");

            // create dynamic edit text for new quantity
            EditText newquantityInput = new EditText(this);
            newquantityInput.setHint("enter new ingredient");

            // create dynamic edit text for new unit
            EditText newunitInput = new EditText(this);
            newunitInput.setHint("Enter new unit");

            // add new input fields to result container layout
            resultContainer.addView(newingredientInput);
            resultContainer.addView(newquantityInput);
            resultContainer.addView(newunitInput);

            // create confirm update button dynamically
            Button confirmupdatebtn = new Button(this);
            confirmupdatebtn.setText("confirm update");
            resultContainer.addView(confirmupdatebtn);

            // set click listener on confirmation button to execute database update
            confirmupdatebtn.setOnClickListener(c->{
                double newQty = Double.parseDouble(newquantityInput.getText().toString().trim());
                String newUnit = newunitInput.getText().toString().trim();
                boolean success = dbHelper.updateingredient(
                        ingredientInput.getText().toString().trim(),
                        oldQty,
                        oldUnit,
                        newingredientInput.getText().toString().trim(),
                        newQty,
                        newUnit
                );

                // show toast message reporting update result
                Toast.makeText(this, success ? "updated" : "failed" , Toast.LENGTH_SHORT).show();
            });
        });

        // set click listener for delete button to remove selected ingredient from database
        deleteBtn.setOnClickListener(v -> {
            double qty = Double.parseDouble(quantityInput.getText().toString().trim());
            String unit = unitInput.getText().toString().trim();
            boolean success = dbHelper.deleteingredient(
                    ingredientInput.getText().toString().trim(),
                    qty,
                    unit);
            // show toast message indicating deletion success or failure
            Toast.makeText(this, success ? "Deleted!" : "Failed", Toast.LENGTH_SHORT).show();
        });

        // set bottom navigation item selection listener for screen transitions
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_ingredients) {
                return true;
            } else if (id == R.id.nav_home) {
                // navigate back to main activity
                startActivity(new Intent(SecondActivity.this, MainActivity.class));
                return true;
            }
            else if (id == R.id.nav_recipes) {
                // navigate to available recipes display activity
                startActivity(new Intent(SecondActivity.this, availableRecipesDisplay.class));
                return true;
            }
            return false;
        });
        // set default selected item in bottom navigation to ingredients
        bottomNavigation.setSelectedItemId(R.id.nav_ingredients);
    }
}
