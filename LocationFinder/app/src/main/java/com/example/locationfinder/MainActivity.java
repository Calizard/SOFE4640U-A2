package com.example.locationfinder;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

/**
 * MainActivity provides the main interface for displaying, filtering, and managing location data.
 * It loads locations from the database and displays them in a scrollable list, allowing users to search,
 * view, delete, and open locations in Google Maps.
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Location class representing a single location record with an ID, address, latitude, and longitude.
     */
    public static class Location {
        private int id;
        private String address;
        private String latitude;
        private String longitude;

        public Location(int id, String address, String latitude, String longitude) {
            this.id = id;
            this.address = address;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public int getId() {
            return id;
        }
        public String getAddress() {
            return address;
        }
        public String getLatitude() {
            return latitude;
        }
        public String getLongitude() {
            return longitude;
        }
    }

    private DatabaseHelper dbHelper; // Database helper for managing locations
    private LinearLayout locationsContainer; // Layout container to dynamically display location cards

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize the database helper
        dbHelper = new DatabaseHelper(this);

        // Find the container where locations will be displayed
        locationsContainer = findViewById(R.id.locationsContainer);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Find the SearchView and set up the query listener
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterLocations(query); // Filter locations on query submission
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterLocations(newText); // Filter locations as the query text changes
                return true;
            }
        });

        // Load all locations on activity start
        loadLocations();
    }

    /**
     * Loads all locations from the database and displays them in the locations container.
     */
    private void loadLocations() {
        locationsContainer.removeAllViews(); // Clear the container first
        List<Location> locations = dbHelper.getAllLocations(); // Get all locations from the database

        // Inflate a view for each location and add it to the container
        for (Location location : locations) {
            locationInflator(location);
        }
    }

    /**
     * Filters locations based on the search query and updates the display.
     *
     * @param query the search query string used to filter locations
     */
    private void filterLocations(String query) {
        locationsContainer.removeAllViews(); // Clear the container
        List<Location> allLocations = dbHelper.getAllLocations(); // Get all notes from the database

        // Filter the locations based on the query
        for (Location location : allLocations) {
            if (location.getAddress().toLowerCase().contains(query.toLowerCase())) {
                locationInflator(location);
            }
        }
    }

    /**
     * Inflates a view for a given location and adds it to the locations container.
     * Sets up onClick listeners for each location view, allowing the user to edit, view on map, or delete the location.
     *
     * @param location the Location object to be displayed
     */
    @SuppressLint("SetTextI18n")
    public void locationInflator(Location location) {
        // Create a View/TextView for each matching location
        View locationView = getLayoutInflater().inflate(R.layout.location_item, locationsContainer, false);
        TextView locationAddress = locationView.findViewById(R.id.locationAddress);
        TextView locationLatitude = locationView.findViewById(R.id.locationLatitude);
        TextView locationLongitude = locationView.findViewById(R.id.locationLongitude);

        // Set the location's address, latitude and longitude
        locationAddress.setText(location.getAddress());
        locationLatitude.setText("Latitude: " + location.getLatitude());
        locationLongitude.setText("Longitude: " + location.getLongitude());

        // Set an onClickListener for the location view to edit the location
        locationView.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LocationView.class);
            intent.putExtra("ID", location.getId()); // Pass location ID to the edit activity
            intent.putExtra("ADDRESS", location.getAddress());
            intent.putExtra("LATITUDE", location.getLatitude());
            intent.putExtra("LONGITUDE", location.getLongitude());
            startActivityForResult(intent, 2); // Start LocationView activity for editing
        });

        // Button to open the location in Google Maps
        Button openMapButton = locationView.findViewById(R.id.buttonOpenMap);
        openMapButton.setOnClickListener(v -> {
            String uri = "geo:0,0?q=" + location.getAddress();
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(uri));
            mapIntent.setPackage("com.google.android.apps.maps");
            try {
                startActivity(mapIntent);
            } catch (Exception e) {
                Toast.makeText(this, "Unable to open Google Maps", Toast.LENGTH_SHORT).show();
            }
        });

        // Button to delete the location from the database
        Button deleteButton = locationView.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(v -> {
            DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this);
            dbHelper.deleteLocationById(location.getId()); // Delete location by ID
            loadLocations(); // Refresh the locations
        });

        // Add the matching locations view to the container
        locationsContainer.addView(locationView);
    }

    /**
     * Launches the LocationView activity for adding a new location when the "Add" button is clicked.
     *
     * @param v the view that triggered this method
     */
    public void launchLocationView(View v) {
        Intent intent = new Intent(this, LocationView.class);
        startActivityForResult(intent, 1);
    }

    // Handle the result from LocationView activity and reload the locations
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            loadLocations();
        }
    }
}