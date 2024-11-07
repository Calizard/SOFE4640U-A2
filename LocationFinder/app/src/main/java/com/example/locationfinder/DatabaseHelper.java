package com.example.locationfinder;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;


/**
 * DatabaseHelper is a SQLiteOpenHelper subclass that manages the SQLite database for storing location data.
 * It provides methods to create, insert, update, delete, and retrieve location records.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Database name and table
    private static final String DATABASE_NAME = "LocationFinder";
    private static final String TABLE_NAME = "Locations";

    // Table column names
    private static final String COL_1 = "ID";
    private static final String COL_2 = "ADDRESS";
    private static final String COL_3 = "LATITUDE";
    private static final String COL_4 = "LONGITUDE";

    // Declare context
    private Context context;

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 2);
        this.context = context; // Store context for resource access
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the Locations table with columns for ID, address, latitude, and longitude
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                + COL_1 + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_2 + " TEXT, "
                + COL_3 + " TEXT, "
                + COL_4 + " TEXT)");

        // Insert the predefined locations only if the table is empty
        insertPredefinedLocationsIfEmpty(db);
    }

    private void insertPredefinedLocationsIfEmpty(SQLiteDatabase db) {
        // Check if the table is empty
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME, null);
        if (cursor != null && cursor.moveToFirst()) {
            int count = cursor.getInt(0);
            cursor.close();
            if (count > 0) {
                return; // Table is not empty, no need to insert predefined locations
            }
        }

        // Insert predefined locations if table is empty
        Resources res = context.getResources();
        for (int i = 1; i <= 101; i++) {
            // Get resource name dynamically
            String resourceName = "location_" + i;
            int resId = res.getIdentifier(resourceName, "string", context.getPackageName());

            if (resId != 0) { // Ensure the resource exists
                String[] locationData = res.getString(resId).split(",");
                if (locationData.length == 3) { // Ensure data is complete
                    String address = locationData[0];
                    String latitude = locationData[1];
                    String longitude = locationData[2];

                    // Insert location into the table
                    ContentValues values = new ContentValues();
                    values.put(COL_2, address);
                    values.put(COL_3, latitude);
                    values.put(COL_4, longitude);
                    db.insert(TABLE_NAME, null, values);
                }
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    /**
     * Inserts a new location into the Locations table.
     *
     * @param address   the address of the location
     * @param latitude  the latitude of the location
     * @param longitude the longitude of the location
     * @return true if the insertion was successful, false otherwise
     */
    public boolean insertData(String address, String latitude, String longitude) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_2, address);
        values.put(COL_3, latitude);
        values.put(COL_4, longitude);

        long result = db.insert(TABLE_NAME, null, values);

        db.close();
        return result > 0;
    }

    /**
     * Updates an existing location record in the Locations table.
     *
     * @param id        the ID of the location record to update
     * @param address   the new address for the location
     * @param latitude  the new latitude for the location
     * @param longitude the new longitude for the location
     * @return true if the update was successful, false otherwise
     */
    public boolean updateData(int id, String address, String latitude, String longitude) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_2, address);
        values.put(COL_3, latitude);
        values.put(COL_4, longitude);

        int result = db.update(TABLE_NAME, values, COL_1 + " = ?", new String[]{String.valueOf(id)});

        db.close();
        return result > 0;
    }

    /**
     * Deletes a location record from the Locations table based on its ID.
     *
     * @param id the ID of the location to delete
     * @return true if the deletion was successful, false otherwise
     */
    public boolean deleteLocationById(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_NAME, COL_1 + " = ?", new String[]{String.valueOf(id)});

        db.close();
        return result > 0;
    }

    /**
     * Retrieves all location records from the Locations table.
     *
     * @return a list of Location objects representing all locations in the database
     */
    @SuppressLint("Range")
    public List<MainActivity.Location> getAllLocations() {
        List<MainActivity.Location> locations = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex(COL_1));
                String address = cursor.getString(cursor.getColumnIndex(COL_2));
                String latitude = cursor.getString(cursor.getColumnIndex(COL_3));
                String longitude = cursor.getString(cursor.getColumnIndex(COL_4));
                locations.add(new MainActivity.Location(id, address, latitude, longitude));
            }
            cursor.close();
        }
        db.close();
        return locations;
    }
}