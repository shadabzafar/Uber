package com.example.uber;

import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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
import com.example.uber.databinding.ActivityViewLocationMapBinding;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class ViewLocationMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityViewLocationMapBinding binding;

    private Button btnRidePassenger;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityViewLocationMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnRidePassenger = findViewById(R.id.btnRidePassenger);

        btnRidePassenger.setText("GIVE RIDE TO " + getIntent().getStringExtra("rUsername"));
        btnRidePassenger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(ViewLocationMapActivity.this, getIntent().getStringExtra("rUsername")+"'s ride accepted",
//                        Toast.LENGTH_SHORT).show();

                ParseQuery<ParseObject> carRequestQuery = ParseQuery.getQuery("RequestCar");
                carRequestQuery.whereEqualTo("username", getIntent().getStringExtra("rUsername"));
                carRequestQuery.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if(objects.size() > 0 && e == null){

                            for(ParseObject uberRequest : objects){
                                uberRequest.put("requestAccepted", true);
                                uberRequest.put("driverOfMe", ParseUser.getCurrentUser().getUsername());

                                uberRequest.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if(e == null){
                                            Intent googleIntent = new Intent(Intent.ACTION_VIEW
                                                    , Uri.parse("http://maps.google.com/maps?saddr="
                                            + getIntent().getDoubleExtra("dLatitude", 0) + ","
                                            + getIntent().getDoubleExtra("dLongitude", 0) + "&"
                                            + "daddr=" + getIntent().getDoubleExtra("passengerLatitude", 0)
                                            + getIntent().getDoubleExtra("passengerLongitude", 0)));

                                            startActivity(googleIntent);
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
//        Toast.makeText(this, getIntent().getDoubleExtra("passengerLatitude", 0) + "", Toast.LENGTH_SHORT).show();

        // **************************DRIVER LOCATION***********************

        LatLng dLocation = new LatLng(getIntent().getDoubleExtra("dLatitude", 0),
                getIntent().getDoubleExtra("dLongitude", 0));
//        mMap.addMarker(new MarkerOptions().position(dLocation).title("Driver Location"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dLocation, 15));
//
//        *******************PASSENGER LOCATION******************************

        LatLng pLocation = new LatLng(getIntent().getDoubleExtra("pLatitude", 0),
                getIntent().getDoubleExtra("pLongitude", 0));
//        mMap.addMarker(new MarkerOptions().position(dLocation).title("Passenger"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dLocation, 15));


        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        Marker driverMarker = mMap.addMarker(new MarkerOptions().position(dLocation).title("Driver Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        Marker passengerMarker = mMap.addMarker(new MarkerOptions().position(pLocation).title("Passenger Location"));

        ArrayList<Marker> myMarker = new ArrayList<>();
        myMarker.add(driverMarker);
        myMarker.add(passengerMarker);

        for (Marker marker : myMarker){
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 0);
        mMap.animateCamera(cameraUpdate);

    }

}