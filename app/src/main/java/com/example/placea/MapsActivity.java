package com.example.placea;
import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import java.util.Arrays;
import java.util.List;
import com.google.android.libraries.places.compat.Place;
import com.google.android.libraries.places.compat.ui.PlaceAutocompleteFragment;
import com.google.android.libraries.places.compat.ui.PlaceSelectionListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {
    private FusedLocationProviderClient fusedLocationProviderClient;
    Location currentLocation;
    private static final int LOCATION_REQUEST_CODE = 101;
    private int ProximityRadius = 500;
    FloatingActionButton fab;
    private GoogleMap mMap;
    String url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
       fab= findViewById(R.id.location);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchLastLocation(1);
            }
        });
        setupAutoCompleteFragment();
        fetchLastLocation(0);

    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Dash myDASH = new Dash(50);
        Gap myGAP = new Gap(15);
        Dot dot = new Dot();
        List<PatternItem> PATTERN_DASHED = Arrays.asList(myDASH,myGAP,dot,myGAP);
        mMap = googleMap;
        LatLng mansoura = new LatLng(31.037933, 31.381523);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mansoura,10));
    }
    private void showGPSDisabledAlertToUser(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Goto Settings Page To Enable GPS",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
    private void fetchLastLocation(final int x) {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "GPS is Enabled in your device", Toast.LENGTH_SHORT).show();
        } else {
            showGPSDisabledAlertToUser();
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    mMap.clear();
                    currentLocation = location;
                    LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                    if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED&&x==2)
                    {
                        //علشان الدايره الزرقا ال بتكون علي موقعك
                        mMap.setMyLocationEnabled(true);
                    }

                    if(x==1){
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
                    Toast.makeText(MapsActivity.this, currentLocation.getLatitude() + " " + currentLocation.getLongitude(), Toast.LENGTH_SHORT).show();}
                } else {
                    Toast.makeText(MapsActivity.this, "No Location recorded", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void setupAutoCompleteFragment(){
            PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                    getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(Place place) {
                   LatLng latLng= place.getLatLng();
                    MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("You are Here")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin1));
                    mMap.addMarker(markerOptions);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
                }
                @Override
                public void onError(Status status) {
                    Toast.makeText(MapsActivity.this, "Failed search", Toast.LENGTH_SHORT).show();
                }
            });
     }
     public void onClick_Imagebtn(View v){
         fetchLastLocation(2);
         String hospital = "hospital", school = "school", restaurant = "restaurant";
         Object transferData[] = new Object[3];
         GetNearbyPlaces getNearbyPlaces = new GetNearbyPlaces();
        if(v.getId()==R.id.btn_hospital){
            mMap.clear();
             url = getUrl(currentLocation.getLatitude(), currentLocation.getLongitude(), hospital);
            transferData[0] = mMap;
            transferData[1] = url;
            transferData[2] = "hospital";
            getNearbyPlaces.execute(transferData);
            Toast.makeText(this, "Searching for Nearby Hospitals...", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "Showing Nearby Hospitals...", Toast.LENGTH_SHORT).show();
        }else if(v.getId()==R.id.btn_school){
            mMap.clear();
             url = getUrl(currentLocation.getLatitude(), currentLocation.getLongitude(), school);
            transferData[0] = mMap;
            transferData[1] = url;
            transferData[2] = "school";
            getNearbyPlaces.execute(transferData);
            Toast.makeText(this, "Searching for Nearby Schools...", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "Showing Nearby Schools...", Toast.LENGTH_SHORT).show();
        }else{
            mMap.clear();
             url = getUrl(currentLocation.getLatitude(), currentLocation.getLongitude(), restaurant);
            transferData[0] = mMap;
            transferData[1] = url;
            transferData[2] = "restaurant";
            getNearbyPlaces.execute(transferData);
            Toast.makeText(this, "Searching for Nearby Restaurants...", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "Showing Nearby Restaurants...", Toast.LENGTH_SHORT).show();
        }

     }
    private String getUrl(double latitide, double longitude, String nearbyPlace)
    {
        StringBuilder googleURL = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googleURL.append("location=" + latitide + "," + longitude);
        googleURL.append("&radius=" + ProximityRadius);
        googleURL.append("&type=" + nearbyPlace);
        googleURL.append("&sensor=true");
        googleURL.append("&key=" + "KEY");
        Log.d("GoogleMapsActivity", "url = " + googleURL.toString());
        return googleURL.toString();
    }
    @Override
    public void onLocationChanged(Location location)
    {
        currentLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("You are Here")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin1));
        mMap.addMarker(markerOptions);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));

    }

}
