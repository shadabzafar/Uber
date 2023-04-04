package com.example.uber;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.ArrayList;
import java.util.List;

public class DriverRequestListActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private MenuItem item;
    private Button btnSearchPassengers;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LatLng driverLocation;
    private ListView listView;
    private ArrayList<String>  nearByDriveRequests;
    private ArrayAdapter arrayAdapter;

    private ArrayList<Double> passengerLatitude;
    private ArrayList<Double> passengerLongitude;
    private ArrayList<String> requestCarUsername;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_request_list);

        btnSearchPassengers = findViewById(R.id.btnSearchPassengers);
        btnSearchPassengers.setOnClickListener(DriverRequestListActivity.this);

        prepareLocationServices();
        showMeTheDriverLocation();

        listView = findViewById(R.id.requestListView);
        nearByDriveRequests = new ArrayList<>();
        passengerLatitude = new ArrayList<>();
        passengerLongitude = new ArrayList<>();
        requestCarUsername = new ArrayList<>();

        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, nearByDriveRequests);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(DriverRequestListActivity.this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu2, menu);
        item = menu.findItem(R.id.menuLogOut);
        SpannableString s = new SpannableString("LOGOUT");
        s.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s.length(), 0);
        item.setTitle(s);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                finish();
            }
        });
        Intent intent = new Intent(DriverRequestListActivity.this, SignUp.class);
        startActivity(intent);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        showMeTheDriverLocation();
        updateRequestListView(driverLocation);
    }

    private void prepareLocationServices(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void updateRequestListView(LatLng driverLocation) {
        if(driverLocation != null){
            saveDriverLocationToParse(driverLocation);

        ParseGeoPoint currentDriverLocation = new ParseGeoPoint(driverLocation.latitude, driverLocation.longitude);
        ParseQuery<ParseObject> requestCarQuery = ParseQuery.getQuery("RequestCar");
        requestCarQuery.whereNear("passengerLocation", currentDriverLocation);
        requestCarQuery.whereDoesNotExist("driverOfMe");
        requestCarQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (objects.size() > 0) {
                        for (ParseObject nearRequest : objects) {
                            ParseGeoPoint pLocation = (ParseGeoPoint) nearRequest.get("passengerLocation");
                            Double kiloMeterDistanceToPassenger = currentDriverLocation.distanceInKilometersTo(pLocation);
                            float roundDistanceValue = Math.round(kiloMeterDistanceToPassenger * 10) / 10;
                            nearByDriveRequests.add("You are " + roundDistanceValue + "km away from " + nearRequest.get("username"));
                            passengerLatitude.add(pLocation.getLatitude());
                            passengerLongitude.add(pLocation.getLongitude());
                            requestCarUsername.add(nearRequest.get("username")+"");
                        }
                    } else {
                        FancyToast.makeText(DriverRequestListActivity.this, "No Nearby Passengers",
                                FancyToast.LENGTH_LONG, FancyToast.INFO, true).show();
                    }
                    arrayAdapter.notifyDataSetChanged();
                }
            }
        });
    }
    }

        private void showMeTheDriverLocation(){
        if (Build.VERSION.SDK_INT >= 23) {
        if (ActivityCompat.checkSelfPermission(DriverRequestListActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DriverRequestListActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2000);
        }
        else {

//            mMap.setMyLocationEnabled(true);

            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Location location = task.getResult();

                    if(location != null) {

                        driverLocation = new LatLng(location.getLatitude(), location.getLongitude());
//                                mMap.addMarker(new MarkerOptions().position(latLng).title("You are here"));
//                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(driverLocation, 15));
                    }
                    else {
                        FancyToast.makeText(DriverRequestListActivity.this,
                                "Turn on your GPS", FancyToast.LENGTH_LONG,
                                FancyToast.INFO, true).show();
                    }
                }
            });
        }
    }
}

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 2000 && grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            showMeTheDriverLocation();
        }
        else {
            FancyToast.makeText(DriverRequestListActivity.this,
                    "You have denied to access your location.", FancyToast.LENGTH_LONG,
                    FancyToast.ERROR, true).show();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Intent intent = new Intent(DriverRequestListActivity.this, ViewLocationMapActivity.class);
        intent.putExtra("dLatitude", driverLocation.latitude);
        intent.putExtra("dLongitude", driverLocation.longitude);
        intent.putExtra("pLatitude", passengerLatitude.get(position));
        intent.putExtra("pLongitude", passengerLongitude.get(position));

        intent.putExtra("rUsername", requestCarUsername.get(position));
        startActivity(intent);

    }

    private void saveDriverLocationToParse(LatLng location){
        ParseUser driver = ParseUser.getCurrentUser();
        ParseGeoPoint driverLocation = new ParseGeoPoint(location.latitude, location.longitude);
        driver.put("driverLocation", driverLocation);

        driver.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
//                    Toast.makeText(DriverRequestListActivity.this, "Location saved", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}