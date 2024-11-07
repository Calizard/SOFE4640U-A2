package com.example.locationfinder;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * LocationView allows the user to add or edit a location by entering an address.
 * It uses Geocoder to convert the address into latitude and longitude for storage in the database.
 */
public class LocationView extends AppCompatActivity {
    private EditText addressEditText; // Input field for entering the address
    private String latitude, longitude; // Strings to store latitude and longitude

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_view);

        // Initialize address input field
        addressEditText = findViewById(R.id.addressEditText);

        // Initialize buttons
        Button buttonSave = findViewById(R.id.buttonSave);
        Button buttonClose = findViewById(R.id.buttonClose);

        // Set on-click listeners
        buttonSave.setOnClickListener(v -> saveLocation());
        buttonClose.setOnClickListener(v -> finish());

        // If editing an existing location, populate the fields with current data
        if (getIntent().hasExtra("ID")) {
            String address = getIntent().getStringExtra("ADDRESS");
            latitude = getIntent().getStringExtra("LATITUDE");
            longitude = getIntent().getStringExtra("LONGITUDE");

            addressEditText.setText(address);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Saves the entered location by converting the address to latitude and longitude.
     * Updates an existing entry if editing, otherwise inserts a new entry in the database.
     */
    private void saveLocation() {
        String address = addressEditText.getText().toString();

        if (address.isEmpty()) {
            Toast.makeText(this, "Address can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use Geocoder to get the latitude and longitude from the address
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if (addresses == null || addresses.isEmpty()) {
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
                return;
            }
            Address location = addresses.get(0);
            latitude = String.valueOf(location.getLatitude());
            longitude = String.valueOf(location.getLongitude());

            DatabaseHelper dbHelper = new DatabaseHelper(this);

            // Check if we are updating an existing location or adding a new one
            if (getIntent().hasExtra("ID")) {
                int id = getIntent().getIntExtra("ID", -1);
                boolean isUpdated = dbHelper.updateData(id, address, latitude, longitude);
                if (isUpdated) {
                    Toast.makeText(this, "Location updated", Toast.LENGTH_SHORT).show();
                    launchHome();
                } else {
                    Toast.makeText(this, "Failed to update location", Toast.LENGTH_SHORT).show();
                }
            } else {
                boolean isInserted = dbHelper.insertData(address, latitude, longitude);
                if (isInserted) {
                    Toast.makeText(this, "Location saved", Toast.LENGTH_SHORT).show();
                    launchHome();
                } else {
                    Toast.makeText(this, "Failed to save location", Toast.LENGTH_SHORT).show();
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Launches the main activity after saving or updating the location.
     * This also finishes the current activity, so the user cannot navigate back to it.
     */
    private void launchHome() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}