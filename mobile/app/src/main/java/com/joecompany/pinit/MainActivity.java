package com.joecompany.pinit;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.internal.LinkedTreeMap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.joecompany.pinit.constants.StorageKeys;
import com.joecompany.pinit.data.PinData;
import com.joecompany.pinit.utils.DialogUtil;
import com.joecompany.pinit.utils.IntentUtil;
import com.joecompany.pinit.utils.MapUtil;
import com.joecompany.pinit.utils.StorageUtil;

import java.util.ArrayList;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, MenuItem.OnMenuItemClickListener, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener {

    public static final int PERMISSION_REQUEST_CODE = 101;

    public static final int GOOGLE_MAP_PADDING_LEFT = 0;
    public static final int GOOGLE_MAP_PADDING_TOP = 0;
    public static final int GOOGLE_MAP_PADDING_RIGHT = 0;
    public static final int GOOGLE_MAP_PADDING_BOTTOM = 200;

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static Activity activityContext;
    private String fbId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);

        // Need to check if user has allowed us to use their current location. If they don't except can't use app.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION },
                    PERMISSION_REQUEST_CODE);

            return;
        }

        startupLogic();
    }

    // This function is called on start up and after they have granted us access to their location
    private void startupLogic(){

        // We need to hold on to this becaues we do some inline handling and will be in a different scope
        activityContext = this;

        // This is set when they login with facebook on the Main Activity
        fbId = (String)StorageUtil.get(this, StorageKeys.FB_ID, String.class);

        FragmentManager fm = getSupportFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.map, mapFragment).commit();
        }

        // Setting up google map
        mapFragment.getMapAsync(this);

        // Setting up the bottom navigation bar
        BottomNavigationView navView = findViewById(R.id.nav_view);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_pins, R.id.navigation_account)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        Menu menu = navView.getMenu();
        for (int i = 0; i < navView.getMenu().size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            menuItem.setOnMenuItemClickListener(this);
        }

        // Want to show a small tutorial to help the user get started, this is only shown once!
        if(StorageUtil.get(this, StorageKeys.TUTORIAL, Boolean.class) == null){
            DialogUtil.show(this, StorageKeys.TUTORIAL, "Tap on the map to pin a location, then tap the pin to remove it. Or if you hold down the pin you can drag it to a new position on the map.\n\nTap the screen to close window.");
            StorageUtil.set(this, StorageKeys.TUTORIAL, true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startupLogic();
                } else {
                    DialogUtil.show(this, DialogUtil.DIALOG_ERROR_TITLE, "The app will not work correctly unless you give us access to your location. Please restart the app.");
                }
                return;
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.googleMap = googleMap;

        ArrayList<LinkedTreeMap> myStoredData = (ArrayList<LinkedTreeMap>) StorageUtil.get(activityContext, fbId, ArrayList.class);

        // If stored pins are found need to populate the map with those pins
        if(myStoredData != null){
            for(int i = 0; i < myStoredData.size(); i++){
                LinkedTreeMap storedPinData = myStoredData.get(i);
                double longitude = (double)storedPinData.get(PinData.PIN_FIELD_LONGITUDE);
                double latitude = (double)storedPinData.get(PinData.PIN_FIELD_LATITUDE);
                String pinName = (String)storedPinData.get(PinData.PIN_FIELD_NAME);
                String pinId = (String)storedPinData.get(PinData.PIN_FIELD_ID);
                this.googleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title((pinName)).draggable(true).snippet(pinId));
            }
        }

        this.googleMap.setMyLocationEnabled(true);
        this.googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        this.googleMap.getUiSettings().setZoomControlsEnabled(true);
        this.googleMap.getUiSettings().setZoomGesturesEnabled(true);

        // Padding needed to be adjust because of the bottom nav bar so we can more clearly see the ui controls.
        this.googleMap.setPadding(GOOGLE_MAP_PADDING_LEFT, GOOGLE_MAP_PADDING_TOP, GOOGLE_MAP_PADDING_RIGHT, GOOGLE_MAP_PADDING_BOTTOM);

        this.googleMap.setOnMapClickListener(this);
        this.googleMap.setOnMarkerClickListener(this);
        this.googleMap.setOnMarkerDragListener(this);

        // Want to get the users current location, and then focus the map on that location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation()
            .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        // Move the camera instantly to Sydney with a zoom of 15.
                        MainActivity.this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
                    }
                }
            });
    }

    public void onLogoutButtonClick(View view) {
        DialogUtil.show(this, "Logging out", "This will log you out of the app and you will need to log into Facebook again, are you sure?\n\nNote: You should also logout of the Facebook website to make sure you are 100% logged out.", "YES", "NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
            StorageUtil.set(activityContext, StorageKeys.FB_ID, null);
            IntentUtil.start(activityContext, LoginActivity.class);
                    }
        },
        null);
    }

    // This is when we are selecting a button on the bottom nav bar
    public boolean onMenuItemClick(MenuItem item){

        MenuItem menuItem = item;

        final RelativeLayout layout = findViewById(R.id.map_container);

        //TODO: probably don't want to hardcode in name of button, should use a defined String in "values"
        if(menuItem.getTitle().equals("Home")) {

            // Want to hide the map, since it's in a container just want to make that invisible
            layout.setVisibility(View.INVISIBLE);

            googleMap.getUiSettings().setScrollGesturesEnabled(true);
            googleMap.getUiSettings().setAllGesturesEnabled(true);

            // Every time they head back to home screen want it to focus back on their current location
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Move the camera instantly to Sydney with a zoom of 15.
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
                        }
                    }
                });
        }else{
            // If not on home screen want to make map invisible and disable all map functionality
            layout.setVisibility(View.VISIBLE);
            googleMap.getUiSettings().setScrollGesturesEnabled(false);
            googleMap.getUiSettings().setAllGesturesEnabled(false);
        }

        return false;
    }

    // Once you click on a map want to get that locations current address, so we can store it for viewing in pins screen
    public void onMapClick(LatLng mapClickLatLng){

        String mapClickAddress = MapUtil.getAddress(getApplicationContext(), mapClickLatLng);

        PinData pinData = new PinData(mapClickAddress, mapClickLatLng.latitude, mapClickLatLng.longitude);

        if(!mapClickAddress.equals("")){
            googleMap.addMarker(new MarkerOptions().position(mapClickLatLng).draggable((true)).snippet(pinData.id));
        }else{
            DialogUtil.show(this, DialogUtil.DIALOG_ERROR_TITLE, "Address not found, can not pin... Did you select the ocean? Please select a country\n\nTap the screen to close window.");
            return;
        }

        ArrayList<Object> myStoredData = (ArrayList<Object>) StorageUtil.get(activityContext, fbId, ArrayList.class);

        // If we have existing stored pins want to update that list with this new pin, if not add a new one
        if(myStoredData != null){
            myStoredData.add(pinData);
            StorageUtil.set(this, fbId, myStoredData);
        }else{
            StorageUtil.set(this, fbId, new ArrayList<>(Arrays.asList(pinData)));
        }
    }

    public void onMarkerDragEnd(Marker pin){

        ArrayList<LinkedTreeMap> myStoredData = (ArrayList<LinkedTreeMap>) StorageUtil.get(activityContext, fbId, ArrayList.class);

        for(int i = 0; i < myStoredData.size(); i++){

            LinkedTreeMap storedPinData = myStoredData.get(i);

            if(pin.getSnippet().equals(storedPinData.get(PinData.PIN_FIELD_ID))) {

                storedPinData.put(PinData.PIN_FIELD_LATITUDE, pin.getPosition().latitude);
                storedPinData.put(PinData.PIN_FIELD_LONGITUDE, pin.getPosition().longitude);
                storedPinData.put(PinData.PIN_FIELD_NAME, MapUtil.getAddress(getApplicationContext(), pin.getPosition()));

                myStoredData.set(i, storedPinData);

                StorageUtil.set(activityContext, fbId, myStoredData);

                break;
            }
        }
    }

    public boolean onMarkerClick(Marker pin){

        final Marker clickedPin = pin;

        DialogUtil.show(this, "Pin Options", "Would you like to delete this Pin?", "YES", "NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            // This will remove the app from the map
            clickedPin.remove();

            ArrayList<LinkedTreeMap> myStoredData = (ArrayList<LinkedTreeMap>) StorageUtil.get(activityContext, fbId, ArrayList.class);

            // If decide to remove pin need to find pin with that location data in stored data and remove it, then save it
            for(int i = 0; i < myStoredData.size(); i++){
                LinkedTreeMap storedPinData = myStoredData.get(i);
                double longitude = (double)storedPinData.get("longitude");
                double latitude = (double)storedPinData.get("latitude");

                if(clickedPin.getPosition().longitude == longitude && clickedPin.getPosition().latitude == latitude) {
                    myStoredData.remove(myStoredData.get(i));
                    break;
                }
            }

            StorageUtil.set(activityContext, fbId, myStoredData);
            }
            }, null);

        return false;
    }

    public void onMarkerDragStart(Marker var1){

    }

    public void onMarkerDrag(Marker var1){

    }
}