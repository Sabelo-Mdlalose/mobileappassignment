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

// database helper class managing sqlite database creation, upgrades, and operations
public class DBHelper extends SQLiteOpenHelper {
    // define database name constant
    private static final String DB_NAME = "ingredients db";
    // define database version constant
    private static final int DB_VERSION = 6;

    // context reference for loading raw assets
    private Context context;

    // constructor initializing sqlite open helper with context, name, and version
    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    // called when database is created for the first time
    @Override
    public void onCreate(SQLiteDatabase db) {
        // create ingredients table to store pantry items
        db.execSQL("CREATE TABLE ingredients (id INTEGER PRIMARY KEY AUTOINCREMENT, ingredient TEXT, quantity REAL,unit TEXT)");
        // create recipes table to store recipe names and instructions
        db.execSQL("CREATE TABLE recipes (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT,instructions TEXT)");
        // create recipe_ingredients table linking recipes to required ingredients with foreign key constraint
        db.execSQL("CREATE TABLE recipe_ingredients(id INTEGER PRIMARY KEY AUTOINCREMENT, recipe_id INTEGER, ingredient TEXT, quantity REAL,unit TEXT, FOREIGN KEY(recipe_id) REFERENCES recipes(id))");
        // preload recipes data from json raw resource
        preloadrecipesfromjson(db);
        // preload initial pantry ingredients
        preloadIngredients(db);
        // log successful database creation
        Log.d("DBHelper", "Database and table created successfully!");
    }

    // method to read and parse recipes from raw json file into database
    private void preloadrecipesfromjson(SQLiteDatabase db) {
        try {
            // open raw resource input stream for recipes json
            InputStream is = context.getResources().openRawResource(R.raw.recipes);
            // setup buffered reader to read input stream lines
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder builder = new StringBuilder();
            String line;
            // read file line by line and append to string builder
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            // close reader stream
            reader.close();

            // parse json array from string builder content
            JSONArray recipesArray = new JSONArray(builder.toString());
            // loop through each recipe json object in array
            for (int i = 0; i < recipesArray.length(); i++) {
                JSONObject recipeobj = recipesArray.getJSONObject(i);
                String name = recipeobj.getString("name");
                String instructions = recipeobj.getString("instructions");

                // prepare content values for recipe insertion
                ContentValues recipevalues = new ContentValues();
                recipevalues.put("name", name);
                recipevalues.put("instructions", instructions);
                // insert recipe and retrieve generated row id
                long recipeId = db.insert("recipes", null, recipevalues);

                // extract arrays for ingredients, quantities, and units
                JSONArray ingredients = recipeobj.getJSONArray("ingredients");
                JSONArray quantities = recipeobj.getJSONArray("quantities");
                JSONArray units = recipeobj.getJSONArray("units");

                // loop through recipe ingredients and insert into relation table
                for (int j = 0; j < ingredients.length(); j++) {
                    String ingName = normalizeName(ingredients.getString(j));
                    double qty = quantities.getDouble(j);
                    String unit = units.getString(j);

                    // prepare content values for recipe ingredient mapping
                    ContentValues ingValues = new ContentValues();
                    ingValues.put("recipe_id", recipeId);
                    ingValues.put("ingredient", ingName);
                    ingValues.put("quantity", normalizeQuantity(qty, unit));
                    ingValues.put("unit", normalizeUnit(unit));
                    db.insert("recipe_ingredients", null, ingValues);
                }
            }
        } catch (Exception e) {
            // print stack trace on json parsing or database exception
            e.printStackTrace();
        }
    }


    // extract numeric part from measurement string like "200g", "1 cup"
    private double extractQuantity(String measurement) {
        try {
            // replace non-numeric characters except decimal point with empty string
            String numberPart = measurement.replaceAll("[^0-9.]", "");
            return Double.parseDouble(numberPart);
        } catch (Exception e) {
            return 0; // return fallback zero on failure
        }
    }

    // extract unit part from measurement string like "200g", "1 cup"
    private String extractUnit(String measurement) {
        // remove digits and decimal points and trim whitespace
        String unitPart = measurement.replaceAll("[0-9.]", "").trim();
        return unitPart.isEmpty() ? "unit" : unitPart;
    }



    // method to preload default ingredients into pantry database
    private void preloadIngredients(SQLiteDatabase db) {
        try {
            // insert spaghetti ingredient record
            ContentValues ing1 = new ContentValues();
            ing1.put("ingredient", normalizeName("Spaghetti"));
            ing1.put("quantity", normalizeQuantity(200,"g"));
            ing1.put("unit", normalizeUnit("g"));
            db.insert("ingredients", null, ing1);

            // insert ground beef ingredient record
            ContentValues ing2 = new ContentValues();
            ing2.put("ingredient", normalizeName("Ground Beef"));
            ing2.put("quantity", normalizeQuantity(250,"g"));
            ing2.put("unit", normalizeUnit("g"));
            db.insert("ingredients", null, ing2);

            // insert onion ingredient record
            ContentValues ing3 = new ContentValues();
            ing3.put("ingredient", normalizeName("Onion"));
            ing3.put("quantity", normalizeQuantity(1,"medium"));
            ing3.put("unit", normalizeUnit("medium"));
            db.insert("ingredients", null, ing3);

            // insert garlic ingredient record
            ContentValues ing4 = new ContentValues();
            ing4.put("ingredient", normalizeName("Garlic"));
            ing4.put("quantity", normalizeQuantity(2,"cloves"));
            ing4.put("unit", normalizeUnit("cloves"));
            db.insert("ingredients", null, ing4);

            // insert tomato sauce ingredient record
            ContentValues ing5 = new ContentValues();
            ing5.put("ingredient", normalizeName("Tomato Sauce"));
            ing5.put("quantity", normalizeQuantity(1,"cup"));
            ing5.put("unit", normalizeUnit("cup"));
            db.insert("ingredients", null, ing5);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // helper method to normalize ingredient names by trimming, lowercasing, and removing plural suffixes
    private String normalizeName(String name) {
        name = name.trim().toLowerCase();
        if (name.endsWith("es")) {
            name = name.substring(0, name.length() - 2);
        } else if (name.endsWith("s")) {
            name = name.substring(0, name.length() - 1);
        }
        return name;
    }

    // helper method to normalize units to standard base units (e.g. converting volume and weight units)
    private String normalizeUnit(String unit) {
        unit = unit.trim().toLowerCase();

        switch (unit) {
            case "kg":
                return "g"; // kilograms to grams conversion
            case "g":
                return "g";
            case "mg":
                return "mg";

            case "l":
                return "ml"; // liters to milliliters conversion
            case "ml":
                return "ml";
            case "cup":
                return "ml"; // assume 1 cup equals 250 ml
            case "tablespoon":
            case "tbsp":
                return "ml"; // assume 1 tbsp equals 15 ml
            case "teaspoon":
            case "tsp":
                return "ml"; // assume 1 tsp equals 5 ml

            default:
                return unit; // fallback to keep original unit if unknown
        }
    }

    // convert quantity values into normalized base unit quantities
    private double normalizeQuantity(double quantity, String unit) {
        unit = unit.trim().toLowerCase();

        switch (unit) {
            case "kg":
                return quantity * 1000; // convert kg to g
            case "l":
                return quantity * 1000; // convert liters to ml
            case "cup":
                return quantity * 250;  // convert cups to ml
            case "tablespoon":
            case "tbsp":
                return quantity * 15;   // convert tablespoons to ml
            case "teaspoon":
            case "tsp":
                return quantity * 5;    // convert teaspoons to ml
            default:
                return quantity; // return original quantity if no conversion needed
        }
    }


    // called when database version is updated, dropping old tables and recreating them
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS ingredients");
        db.execSQL("DROP TABLE IF EXISTS recipes");
        db.execSQL("DROP TABLE IF EXISTS recipe_ingredients");
        onCreate(db);
    }

    // insert new ingredient into pantry database table
    public boolean insertingredient(String ingredient, double quantity, String unit) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("ingredient", normalizeName(ingredient));
        values.put("quantity", normalizeQuantity(quantity,unit));
        values.put("unit", normalizeUnit(unit));
        long result = db.insert("ingredients", null, values);
        return result != -1; // return true if insertion succeeded
    }

    // retrieve all ingredients from pantry database table
    public Cursor getingredient() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM ingredients", null);
    }

    // update existing ingredient record in pantry database table
    public boolean updateingredient(String oldIngredient, double oldQuantity, String oldUnit,String newIngredient, double newQuantity, String newUnit) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("ingredient", normalizeName(newIngredient));
        values.put("quantity", normalizeQuantity(newQuantity,newUnit));
        values.put("unit", normalizeUnit(newUnit));
        int result = db.update("ingredients", values, "ingredient=? AND quantity=? AND unit=?" , new String[]{normalizeName(oldIngredient), String.valueOf(normalizeQuantity(oldQuantity, oldUnit)), normalizeUnit(oldUnit)});
        return result > 0; // return true if update affected rows
    }

    // delete ingredient record from pantry database table
    public boolean deleteingredient(String ingredient, double quantity, String unit) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete("ingredients", "ingredient=? AND quantity=? AND unit=?", new String[]{normalizeName(ingredient), String.valueOf(normalizeQuantity(quantity, unit)), normalizeUnit(unit)});
        return result > 0; // return true if deletion affected rows
    }

    // query matching recipes where pantry contains all required ingredients with sufficient quantities
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
