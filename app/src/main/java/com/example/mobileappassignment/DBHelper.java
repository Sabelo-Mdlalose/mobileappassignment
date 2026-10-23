package com.example.mobileappassignment;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "ingredients db";
    private static final int DB_VERSION = 6;

    private Context context;
    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE ingredients (id INTEGER PRIMARY KEY AUTOINCREMENT, ingredient TEXT, quantity REAL,unit TEXT)");
        db.execSQL("CREATE TABLE recipes (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT,instructions TEXT)");
        db.execSQL("CREATE TABLE recipe_ingredients(id INTEGER PRIMARY KEY AUTOINCREMENT, recipe_id INTEGER, ingredient TEXT, quantity REAL,unit TEXT, FOREIGN KEY(recipe_id) REFERENCES recipes(id))");
        preloadrecipesfromjson(db);
        preloadIngredients(db);
        Log.d("DBHelper", "Database and table created successfully!");
    }

    private void preloadrecipesfromjson(SQLiteDatabase db) {
        try {
            InputStream is = context.getResources().openRawResource(R.raw.recipes);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            reader.close();

            JSONArray recipesArray = new JSONArray(builder.toString());
            for (int i = 0; i < recipesArray.length(); i++) {
                JSONObject recipeobj = recipesArray.getJSONObject(i);
                String name = recipeobj.getString("name");
                String instructions = recipeobj.getString("instructions");

                ContentValues recipevalues = new ContentValues();
                recipevalues.put("name", name);
                recipevalues.put("instructions", instructions);
                long recipeId = db.insert("recipes", null, recipevalues);

                JSONArray ingredients = recipeobj.getJSONArray("ingredients");
                JSONArray quantities = recipeobj.getJSONArray("quantities");
                JSONArray units = recipeobj.getJSONArray("units");

                for (int j = 0; j < ingredients.length(); j++) {
                    String ingName = normalizeName(ingredients.getString(j));
                    double qty = quantities.getDouble(j);
                    String unit = units.getString(j);

                    ContentValues ingValues = new ContentValues();
                    ingValues.put("recipe_id", recipeId);
                    ingValues.put("ingredient", ingName);
                    ingValues.put("quantity", normalizeQuantity(qty, unit));
                    ingValues.put("unit", normalizeUnit(unit));
                    db.insert("recipe_ingredients", null, ingValues);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void preloadIngredients(SQLiteDatabase db) {
        try {
            // Example: preload a subset of ingredients that match recipes
            ContentValues ing1 = new ContentValues();
            ing1.put("ingredient", normalizeName("Spaghetti"));
            ing1.put("quantity", normalizeQuantity(200,"g"));
            ing1.put("unit", normalizeUnit("g"));
            db.insert("ingredients", null, ing1);

            ContentValues ing2 = new ContentValues();
            ing2.put("ingredient", normalizeName("Ground Beef"));
            ing2.put("quantity", normalizeQuantity(250,"g"));
            ing2.put("unit", normalizeUnit("g"));
            db.insert("ingredients", null, ing2);

            ContentValues ing3 = new ContentValues();
            ing3.put("ingredient", normalizeName("Onion"));
            ing3.put("quantity", normalizeQuantity(1,"medium"));
            ing3.put("unit", normalizeUnit("medium"));
            db.insert("ingredients", null, ing3);

            ContentValues ing4 = new ContentValues();
            ing4.put("ingredient", normalizeName("Garlic"));
            ing4.put("quantity", normalizeQuantity(2,"cloves"));
            ing4.put("unit", normalizeUnit("cloves"));
            db.insert("ingredients", null, ing4);

            ContentValues ing5 = new ContentValues();
            ing5.put("ingredient", normalizeName("Tomato Sauce"));
            ing5.put("quantity", normalizeQuantity(1,"cup"));
            ing5.put("unit", normalizeUnit("cup"));
            db.insert("ingredients", null, ing5);

            // Add more ingredients that overlap with your recipes.json
            // e.g. Chicken Breast, Curry Powder, Coconut Milk, etc.
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String normalizeName(String name) {
        name = name.trim().toLowerCase();
        if (name.endsWith("es")) {
            name = name.substring(0, name.length() - 2);
        } else if (name.endsWith("s")) {
            name = name.substring(0, name.length() - 1);
        }
        return name;
    }

    private String normalizeUnit(String unit) {
        unit = unit.trim().toLowerCase();

        switch (unit) {
            case "kg":
                return "g"; // kilograms → grams
            case "g":
                return "g";
            case "mg":
                return "mg";

            case "l":
                return "ml"; // liters → milliliters
            case "ml":
                return "ml";
            case "cup":
                return "ml"; // assume 1 cup = 250 ml
            case "tablespoon":
            case "tbsp":
                return "ml"; // assume 1 tbsp = 15 ml
            case "teaspoon":
            case "tsp":
                return "ml"; // assume 1 tsp = 5 ml

            default:
                return unit; // fallback: keep original
        }
    }

    // Convert quantity to normalized base unit
    private double normalizeQuantity(double quantity, String unit) {
        unit = unit.trim().toLowerCase();

        switch (unit) {
            case "kg":
                return quantity * 1000; // kg → g
            case "l":
                return quantity * 1000; // L → ml
            case "cup":
                return quantity * 250;  // cup → ml
            case "tablespoon":
            case "tbsp":
                return quantity * 15;   // tbsp → ml
            case "teaspoon":
            case "tsp":
                return quantity * 5;    // tsp → ml
            default:
                return quantity; // already normalized
        }
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS ingredients");
        db.execSQL("DROP TABLE IF EXISTS recipes");
        db.execSQL("DROP TABLE IF EXISTS recipe_ingredients");
        onCreate(db);
    }

    // Insert
    public boolean insertingredient(String ingredient, double quantity, String unit) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("ingredient", normalizeName(ingredient));
        values.put("quantity", normalizeQuantity(quantity,unit));
        values.put("unit", normalizeUnit(unit));
        long result = db.insert("ingredients", null, values);
        return result != -1;
    }

    // Read
    public Cursor getingredient() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM ingredients", null);
    }

    // Update
    // Update
    public boolean updateingredient(String oldIngredient, double oldQuantity, String oldUnit,
                                    String newIngredient, double newQuantity, String newUnit) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Normalize new values
        ContentValues values = new ContentValues();
        values.put("ingredient", normalizeName(newIngredient));
        values.put("quantity", normalizeQuantity(newQuantity, newUnit));
        values.put("unit", normalizeUnit(newUnit));

        // Normalize old values for WHERE clause
        String normalizedOldIngredient = normalizeName(oldIngredient);
        String normalizedOldUnit = normalizeUnit(oldUnit);
        String normalizedOldQuantity = String.valueOf(normalizeQuantity(oldQuantity, oldUnit));

        int result = db.update(
                "ingredients",
                values,
                "ingredient=? AND quantity=? AND unit=?",
                new String[]{normalizedOldIngredient, normalizedOldQuantity, normalizedOldUnit}
        );

        return result > 0;
    }


    // Delete
    public boolean deleteingredient(String ingredient, double quantity, String unit) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete("ingredients", "ingredient=? AND quantity=? AND unit=?", new String[]{normalizeName(ingredient), String.valueOf(normalizeQuantity(quantity, unit)), normalizeUnit(unit)});
        return result > 0;
    }

    public Cursor getMatchingRecipes() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query =
                "SELECT r.name " +
                        "FROM recipes r " +
                        "JOIN recipe_ingredients ri ON r.id = ri.recipe_id " +
                        "WHERE EXISTS ( " +
                        "   SELECT 1 FROM ingredients ing " +
                        "   WHERE LOWER(TRIM(ing.ingredient)) = LOWER(TRIM(ri.ingredient)) " +
                        "     AND ing.unit = ri.unit " +
                        "     AND ing.quantity >= ri.quantity " +
                        ") " +
                        "GROUP BY r.id " +
                        "HAVING COUNT(*) = ( " +
                        "   SELECT COUNT(*) FROM recipe_ingredients ri2 WHERE ri2.recipe_id = r.id " +
                        ");";
        return db.rawQuery(query, null);
    }
}

