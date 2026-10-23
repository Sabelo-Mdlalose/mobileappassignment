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


public class SecondActivity extends AppCompatActivity {
    DBHelper dbHelper;
    EditText ingredientInput, quantityInput, unitInput;
    Button insertBtn, viewBtn, updateBtn, deleteBtn;
    LinearLayout resultContainer;
    ListView ingredientsListView;

    ArrayList<String> ingredientsList = new ArrayList<>();
    ArrayAdapter<String> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_second);


        dbHelper = new DBHelper(this);

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


        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,ingredientsList);
        ingredientsListView.setAdapter(adapter);

        insertBtn.setOnClickListener(v -> {

            boolean success = dbHelper.insertingredient(ingredientInput.getText().toString(),Double.parseDouble(quantityInput.getText().toString().trim()) ,unitInput.getText().toString().trim());
            Toast.makeText(this, success ? "Inserted!" : "Failed", Toast.LENGTH_SHORT).show();
        });

        viewBtn.setOnClickListener(v -> {
            ingredientsList.clear();
            Cursor cursor = dbHelper.getingredient();
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String ingredient = cursor.getString(cursor.getColumnIndexOrThrow("ingredient"));
                double qty = cursor.getDouble(cursor.getColumnIndexOrThrow("quantity"));
                String unit = cursor.getString(cursor.getColumnIndexOrThrow("unit"));
                ingredientsList.add(id + ": " + ingredient + "-" + qty + " " + unit);
            }
            cursor.close();
            adapter.notifyDataSetChanged();
        });

        ingredientsListView.setOnItemClickListener((parent, view, position, id) -> {
            String selected = ingredientsList.get(position);

            String[] parts = selected.split(":");
            String[] ingParts = parts[1].split("-");
            String ingredient = ingParts[0].trim();
            String[] qtyUnit = ingParts[1].trim().split(" ");
            String qty = qtyUnit[0];
            String unit = qtyUnit.length > 1 ? qtyUnit[1] : "";

            ingredientInput.setText(ingredient);
            quantityInput.setText(qty);
            unitInput.setText(unit);
            updateBtn.setVisibility(view.VISIBLE);
            deleteBtn.setVisibility(view.VISIBLE);
        });

        updateBtn.setOnClickListener(v->{
            double oldQty = Double.parseDouble(quantityInput.getText().toString().trim());
            String oldUnit = unitInput.getText().toString().trim();

            resultContainer.removeAllViews();
            EditText newingredientInput = new EditText(this);
            newingredientInput.setHint("enter new ingredient");

            EditText newquantityInput = new EditText(this);
            newquantityInput.setHint("enter new ingredient");

            EditText newunitInput = new EditText(this);
            newunitInput.setHint("Enter new unit");

            resultContainer.addView(newingredientInput);
            resultContainer.addView(newquantityInput);
            resultContainer.addView(newunitInput);


            Button confirmupdatebtn = new Button(this);
            confirmupdatebtn.setText("confirm update");
            resultContainer.addView(confirmupdatebtn);

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

                Toast.makeText(this, success ? "updated" : "failed" , Toast.LENGTH_SHORT).show();
            });


        });

        deleteBtn.setOnClickListener(v -> {
            double qty = Double.parseDouble(quantityInput.getText().toString().trim());
            String unit = unitInput.getText().toString().trim();
            boolean success = dbHelper.deleteingredient(
                    ingredientInput.getText().toString().trim(),
                    qty,
                    unit);
            Toast.makeText(this, success ? "Deleted!" : "Failed", Toast.LENGTH_SHORT).show();
        });

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_ingredients) {
                return true;
            } else if (id == R.id.nav_home) {
                startActivity(new Intent(SecondActivity.this, MainActivity.class));
                return true;
            }
            else if (id == R.id.nav_recipes) {
                startActivity(new Intent(SecondActivity.this, availableRecipesDisplay.class));
                return true;
            }
            return false;
        });
        bottomNavigation.setSelectedItemId(R.id.nav_ingredients);


    }
}


