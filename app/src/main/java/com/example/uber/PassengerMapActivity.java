package com.example.uber;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.parse.DeleteCallback;
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
import java.util.Timer;
import java.util.TimerTask;

public class PassengerMapActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Button btnRequestCar, btnLogoutPassenger, btnDriverUpdate;
    private LatLng passengerLocation;
    private Boolean isUberCancelled = true;
    private Timer t;
    boolean flag = true, flag2 = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        prepareLocationServices();

        btnRequestCar = findViewById(R.id.btnRequestCar);
        btnRequestCar.setOnClickListener(PassengerMapActivity.this);

        ParseQuery<ParseObject> carRequestQuery = ParseQuery.getQuery("RequestCar");
        carRequestQuery.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        carRequestQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(objects.size() > 0 && e == null){
                    isUberCancelled = false;
                    btnRequestCar.setText("CANCEL");

                    getDriverUpdates();
                }
            }
        });

        btnLogoutPassenger = findViewById(R.id.btnLogoutPassenger);
        btnLogoutPassenger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseUser.logOutInBackground(new LogOutCallback() {
                    @Override
                    public void done(ParseException e) {
                        finish();
                    }
                });
                Intent intent = new Intent(PassengerMapActivity.this, SignUp.class);
                startActivity(intent);
            }
        });

        btnDriverUpdate = findViewById(R.id.btnDriverUpdate);
        btnDriverUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flag = true;
                flag2 = true;
                getDriverUpdates();
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        showMeTheUserCurrentLocation();
    }

    private void showMeTheUserCurrentLocation(){

        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(PassengerMapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(PassengerMapActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
            }
            else {

                mMap.setMyLocationEnabled(true);

                fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();

                        if(location != null){
                            passengerLocation = new LatLng(location.getLatitude(), location.getLongitude());
//                                mMap.addMarker(new MarkerOptions().position(latLng).title("You are here"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(passengerLocation, 15));
                        }
                        else {
                            FancyToast.makeText(PassengerMapActivity.this,
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

        if(requestCode == 1000 && grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            showMeTheUserCurrentLocation();
        }
        else {
            FancyToast.makeText(PassengerMapActivity.this,
                    "You have denied to access your location.", FancyToast.LENGTH_LONG,
                    FancyToast.ERROR, true).show();
        }

    }

    private void prepareLocationServices(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public void onClick(View view) {
        showMeTheUserCurrentLocation();

        try {
            if (isUberCancelled) {
                ParseObject requestCar = new ParseObject("RequestCar");
                requestCar.put("username", ParseUser.getCurrentUser().getUsername());
                ParseGeoPoint userLocation = new ParseGeoPoint(passengerLocation.latitude, passengerLocation.longitude);
                requestCar.put("passengerLocation", userLocation);

                requestCar.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            FancyToast.makeText(PassengerMapActivity.this, "You have requested for a cab",
                                    FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true).show();
                            btnRequestCar.setText("CANCEL");
                            isUberCancelled = false;
                        }
                    }
                });
            }
            else {
                ParseQuery<ParseObject> carRequestQuery = ParseQuery.getQuery("RequestCar");
                carRequestQuery.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
                carRequestQuery.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> requestList, ParseException e) {
                        if(requestList.size() > 0 && e == null){
                            isUberCancelled = true;
                            btnRequestCar.setText("BOOK YOUR CAB");

                            for(ParseObject uberRequest: requestList){
                                uberRequest.deleteInBackground(new DeleteCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if(e == null){
                                            FancyToast.makeText(PassengerMapActivity.this,
                                                    "CAB CANCELLED",
                                                    FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true).show();
//                                            mMap.clear();
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
            }
        }
      catch (Exception e){
            e.printStackTrace();
      }
    }

    private void getDriverUpdates() {
        t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                try {
                    ParseQuery<ParseObject> uberRequestQuery = ParseQuery.getQuery("RequestCar");
                    uberRequestQuery.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
                    uberRequestQuery.whereEqualTo("requestAccepted", true);
                    uberRequestQuery.whereExists("driverOfMe");

                    uberRequestQuery.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> objects, ParseException e) {
                            if (objects.size() > 0 && e == null) {

                                for (ParseObject requestObject : objects) {
                                    ParseQuery<ParseUser> driverQuery = ParseUser.getQuery();
                                    driverQuery.whereEqualTo("username", requestObject.getString("driverOfMe"));
                                    driverQuery.findInBackground(new FindCallback<ParseUser>() {
                                        @Override
                                        public void done(List<ParseUser> drivers, ParseException e) {
                                            if (drivers.size() > 0 && e == null) {
                                                for (ParseUser driverOfRequest : drivers) {

                                                    ParseGeoPoint driverOfRequestLocation = driverOfRequest
                                                            .getParseGeoPoint("driverLocation");
                                                    ParseGeoPoint pLocationAsParseGeoPoint = new ParseGeoPoint(passengerLocation.latitude
                                                            , passengerLocation.longitude);

                                                    double kmDistance = driverOfRequestLocation.distanceInKilometersTo(pLocationAsParseGeoPoint);
                                                    float roundDistance = Math.round(kmDistance * 10) / 10;

                                                    if (kmDistance < 0.1) {
                                                        requestObject.deleteInBackground(new DeleteCallback() {
                                                            @Override
                                                            public void done(ParseException e) {
                                                                FancyToast.makeText(PassengerMapActivity.this,
                                                                        requestObject.get("driverOfMe") + " has reached to your location"
                                                                        , FancyToast.LENGTH_LONG, FancyToast.SUCCESS, true).show();
                                                                isUberCancelled = true;
                                                                btnRequestCar.setText("BOOK YOUR CAB");
                                                            }
                                                        });
                                                        return;
                                                    }

                                                    if (flag) {
                                                        FancyToast.makeText(PassengerMapActivity.this,
                                                                requestObject.get("driverOfMe") + " has accepted your ride and he is "
                                                                        + roundDistance + "km away from you", FancyToast.LENGTH_LONG,
                                                                FancyToast.SUCCESS, true).show();
                                                        flag = false;
                                                    }

                                                    //        *******************DRIVER LOCATION******************************

                                                    LatLng dLocation = new LatLng(driverOfRequestLocation.getLatitude(), driverOfRequestLocation.getLongitude());

                                                    //        *******************PASSENGER LOCATION******************************

                                                    LatLng pLocation = new LatLng(pLocationAsParseGeoPoint.getLatitude(), pLocationAsParseGeoPoint.getLongitude());

                                                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                                    Marker driverMarker = mMap.addMarker(new MarkerOptions().position(dLocation).title("Driver Location"));
                                                    Marker passengerMarker = mMap.addMarker(new MarkerOptions().position(pLocation).title("Passenger Location")
                                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                                                    ArrayList<Marker> myMarker = new ArrayList<>();
                                                    myMarker.add(driverMarker);
                                                    myMarker.add(passengerMarker);

                                                    for (Marker marker : myMarker) {
                                                        builder.include(marker.getPosition());
                                                    }

                                                    LatLngBounds bounds = builder.build();

                                                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 80);
                                                    mMap.animateCamera(cameraUpdate);
                                                }
                                            }
                                        }
                                    });

                                }
                            } else {
                                if (flag2) {
                                    FancyToast.makeText(PassengerMapActivity.this, "Your ride is not accepted yet",
                                            FancyToast.LENGTH_LONG, FancyToast.INFO, true).show();
                                    flag2 = false;
                                }
                            }
                        }
                    });
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

        }, 0, 3000);
    }

}