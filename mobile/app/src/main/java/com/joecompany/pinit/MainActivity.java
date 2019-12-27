package com.joecompany.pinit;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.transition.Visibility;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.internal.LinkedTreeMap;
import com.joecompany.pinit.data.PinData;
import com.joecompany.pinit.ui.FullscreenActivity;
import com.joecompany.pinit.utils.DialogUtil;
import com.joecompany.pinit.utils.IntentUtil;
import com.joecompany.pinit.utils.StorageUtil;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, MenuItem.OnMenuItemClickListener, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static Activity activityContext;
    private String fbId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted

            ActivityCompat.requestPermissions(this, new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION },
                    101);

            return;
        }

        startupLogic();
    }

    private void startupLogic(){

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        FragmentManager fm = getSupportFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.map, mapFragment).commit();
        }

        mapFragment.getMapAsync(this);


        BottomNavigationView navView = findViewById(R.id.nav_view);


        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        Menu menu = navView.getMenu();


        for (int i = 0; i < navView.getMenu().size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            menuItem.setOnMenuItemClickListener(this);
        }

        activityContext = this;
        fbId = (String)StorageUtil.get(this, "fbid", String.class);
        if(StorageUtil.get(this, "tutorial", Boolean.class) == null){
            DialogUtil.show(this, "Tutorial", "Tap on the map to pin a location, then tap the pin to remove it.\n\nTap the screen to close window.");
            StorageUtil.set(this, "tutorial", true);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 101: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startupLogic();
                } else {
                    DialogUtil.show(this, "Error", "The app will not work correctly unless you give us access to your location. Please restart the app.");
                }
                return;
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // populating screen with markers
        ArrayList<LinkedTreeMap> myStoredData = (ArrayList<LinkedTreeMap>) StorageUtil.get(activityContext, fbId, ArrayList.class);
        if(myStoredData != null){
            for(int i = 0; i < myStoredData.size(); i++){
                LinkedTreeMap tree = myStoredData.get(i);
                double longitude = (double)tree.get("longitude");
                double latitude = (double)tree.get("latitude");
                String pinName = (String)tree.get("name");
                String pinId = (String)tree.get("id");
                mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title((pinName)).draggable(true).snippet(pinId));
            }
        }

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);

        mMap.setPadding(0, 0, 0, 200); // for zooming control, pushed it up



        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMarkerDragListener(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Move the camera instantly to Sydney with a zoom of 15.
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
                        }
                    }
                });
    }

    public void onLogoutButtonClick(View view)
    {
        DialogUtil.show(this, "Logging out", "This will log you out of the app and you will need to log into Facebook again, are you sure?", "YES", "NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    StorageUtil.set(activityContext, "fbid", null);
                    IntentUtil.start(activityContext, FullscreenActivity.class);
                }
                },
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });


    }

    public boolean onMenuItemClick(MenuItem item){
        MenuItem menuItem = item;
        final RelativeLayout layout = (RelativeLayout)findViewById(R.id.map_container);


        if(menuItem.getTitle().equals("Home")) {
            layout.setVisibility(View.INVISIBLE);
            mMap.getUiSettings().setScrollGesturesEnabled(true);
            mMap.getUiSettings().setAllGesturesEnabled(true);

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Move the camera instantly to Sydney with a zoom of 15.
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
                            }
                        }
                    });
        }else{
            layout.setVisibility(View.VISIBLE);
            mMap.getUiSettings().setScrollGesturesEnabled(false);
            mMap.getUiSettings().setAllGesturesEnabled(false);
        }

        return false;
    }

    public void onMapClick(LatLng var1){
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        String result = "";

        try {
            List<Address> addressList = geocoder.getFromLocation(
                    var1.latitude, var1.longitude, 1);
            if (addressList != null && addressList.size() > 0) {
                Address address = addressList.get(0);
                StringBuilder sb = new StringBuilder();

                if(address.getSubThoroughfare() != null && address.getThoroughfare() != null)
                    sb.append(address.getSubThoroughfare() + " " + address.getThoroughfare()).append(", ");

                if(address.getLocality() != null)
                    sb.append(address.getLocality()).append(", ");

                if(address.getPostalCode() != null)
                    sb.append(address.getPostalCode()).append(", ");

                if(address.getCountryName() != null)
                    sb.append(address.getCountryName());

                result = sb.toString();
            }
        } catch (IOException e) {
            Log.e("JOE: ", "Unable connect to Geocoder", e);
        }
        PinData pinData = new PinData(result, var1.latitude, var1.longitude);

        if(!result.equals("")){
            mMap.addMarker(new MarkerOptions().position(var1).draggable((true)).snippet(pinData.id));
        }else{
            DialogUtil.show(this, "Error", "Address not found, can not pin... Did you select the ocean? Please select a country\n\nTap the screen to close window.");
            return;
        }

        ArrayList<PinData> locations = new ArrayList<PinData>();
        locations.add(pinData);

        ArrayList<Object> myStoredData = (ArrayList<Object>) StorageUtil.get(activityContext, fbId, ArrayList.class);

        if(myStoredData != null){
            myStoredData.add(pinData);
            StorageUtil.set(this, fbId, myStoredData);
        }else{
            StorageUtil.set(this, fbId, locations);
        }
    }

    public void onMarkerDragStart(Marker var1){

    }

    public void onMarkerDrag(Marker var1){

    }

    public void onMarkerDragEnd(Marker var1){
        ArrayList<LinkedTreeMap> myStoredData = (ArrayList<LinkedTreeMap>) StorageUtil.get(activityContext, fbId, ArrayList.class);
        for(int i = 0; i < myStoredData.size(); i++){
            LinkedTreeMap tree = myStoredData.get(i);
            if(var1.getSnippet().equals(tree.get("id"))) {
                tree.put("latitude", var1.getPosition().latitude);
                tree.put("longitude", var1.getPosition().longitude);

                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                String result = "";

                try {
                    List<Address> addressList = geocoder.getFromLocation(
                            var1.getPosition().latitude, var1.getPosition().longitude, 1);
                    if (addressList != null && addressList.size() > 0) {
                        Address address = addressList.get(0);
                        StringBuilder sb = new StringBuilder();

                        if(address.getSubThoroughfare() != null && address.getThoroughfare() != null)
                            sb.append(address.getSubThoroughfare() + " " + address.getThoroughfare()).append(", ");

                        if(address.getLocality() != null)
                            sb.append(address.getLocality()).append(", ");

                        if(address.getPostalCode() != null)
                            sb.append(address.getPostalCode()).append(", ");

                        if(address.getCountryName() != null)
                            sb.append(address.getCountryName());

                        result = sb.toString();
                    }
                } catch (IOException e) {
                    Log.e("JOE: ", "Unable connect to Geocoder", e);
                }

                tree.put("name", result);
                myStoredData.set(i, tree);
                StorageUtil.set(activityContext, fbId, myStoredData);
                break;
            }
        }
    }

    public boolean onMarkerClick(Marker var1){

        final Marker clickedMarker = var1;

        DialogUtil.show(this, "Pin Options", "Would you like to delete this Pin?", "YES", "NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                clickedMarker.remove();

                ArrayList<LinkedTreeMap> myStoredData = (ArrayList<LinkedTreeMap>) StorageUtil.get(activityContext, fbId, ArrayList.class);
                for(int i = 0; i < myStoredData.size(); i++){
                    LinkedTreeMap tree = myStoredData.get(i);
                    double longitude = (double)tree.get("longitude");
                    double latitude = (double)tree.get("latitude");

                    if(clickedMarker.getPosition().longitude == longitude && clickedMarker.getPosition().latitude == latitude) {
                        myStoredData.remove(myStoredData.get(i));
                        break;
                    }
                }

                StorageUtil.set(activityContext, fbId, myStoredData);


                /*



                StorageUtil.set(this, "joetest", "JOE IT WORKS!!!");
                */
                }
            },
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

        return false;
    }
}