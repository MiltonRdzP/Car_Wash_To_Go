package com.example.carwashtogo;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.example.carwashtogo.models.ModelPost;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class Map extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    private SupportMapFragment mapFragment;
    private FusedLocationProviderClient mFusedLocationClient;
    Button add;

    private LatLng pickupLocation;
    private Boolean requestBo1 = false;
    private Marker pickupMarker;


    private LinearLayout mdriverInfo;
    private ImageView mdriverProfileImage;
    private TextView mdriverName, mdriverPhone;
    private String name1;
    private Button msg, pagar, history, paypal;
    private EditText carro;
    ProgressDialog pd;
    private RatingBar mRatingBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map2);
        add = findViewById(R.id.btn_add);
        add.setVisibility(View.INVISIBLE);
        msg = findViewById(R.id.btn_msg);
        history = findViewById(R.id.history);
        mRatingBar = (RatingBar) findViewById(R.id.ratingBar);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        pd = new ProgressDialog(getApplicationContext());
        mdriverInfo = findViewById(R.id.driverinfo);
        mdriverProfileImage = findViewById(R.id.driverProfileImage);
        mdriverName = findViewById(R.id.drivername);
        mdriverPhone =findViewById(R.id.driverphone);
        paypal = findViewById(R.id.paypal);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String userId2 = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference("carwashmanAvailable");

                GeoFire geoFire2 = new GeoFire(ref2);
                geoFire2.removeLocation(userId2);
                if (requestBo1){
                    requestBo1 = false;
                    geoQuery.removeAllListeners();
                    try{
                        driverLocationRef.removeEventListener(driverLocationRefListener);
                    }catch (Exception e){
                        Toast.makeText(getApplicationContext(), R.string.nofound, Toast.LENGTH_SHORT).show();

                    }
                    if (driverFoundID != null){
                        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("CarWashMan").child(driverFoundID).child("customerRequest");
                        driverRef.removeValue();
                        driverFoundID = null;

                    }
                    driverFound = false;
                    radius = 1;
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.removeLocation(userId);

                    if(pickupMarker != null) {
                        pickupMarker.remove();
                    }
                    if (mDriverMarker != null){
                        mDriverMarker.remove();
                    }
                    add.setText(R.string.buscarcwman);
                    mdriverInfo.setVisibility(View.GONE);
                    mdriverName.setText("");
                    mdriverPhone.setText("");
                    mdriverProfileImage.setImageResource(R.mipmap.ic_lavador);

                }else{

                    requestBo1 = true;

                        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
                            GeoFire geoFire = new GeoFire(ref);
                            geoFire.setLocation(userId, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));


                        pickupLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                        pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLocation).title((String) getText(R.string.lavaraqui)).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_lavador2)));

                        add.setText(R.string.obteniedocarwashman);


                    getCloserLavador();

                }

            }


        });
        paypal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Map.this, HistoryActivity.class);
                intent.putExtra("customerOrDriver", "Customers");
                startActivity(intent);
                return;
            }
        });
        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Map.this, HistoryRegistro.class);
                intent.putExtra("customerOrDriver", "Customers");
                startActivity(intent);
                return;
            }
        });
    }

    private int radius = 1;
    private Boolean driverFound = false;
    private String driverFoundID;

    GeoQuery geoQuery;
    private void getCloserLavador() {
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("carwashmanAvailable");
        GeoFire geoFire = new GeoFire(driverLocation);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(pickupLocation.latitude, pickupLocation.longitude), radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if ((!driverFound && requestBo1)){
                    driverFound = true;
                    driverFoundID = key;

                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("CarWashMan").child(driverFoundID).child("customerRequest");
                    String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    HashMap map = new HashMap();
                    map.put("customerRideId", customerId);
                    driverRef.updateChildren(map);

                    getDriverLocalitation();
                    getAssignedDriverInfo();
                    add.setText(R.string.obteniedocarwashman);

                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!driverFound)
                {
                    radius++;
                    getCloserLavador();
                }
            }
            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private Marker mDriverMarker;
    private DatabaseReference driverLocationRef;
    private ValueEventListener driverLocationRefListener;
    private void getDriverLocalitation(){
        driverLocationRef = FirebaseDatabase.getInstance().getReference().child("carwashmanWorking").child(driverFoundID).child("l");
        driverLocationRefListener=  driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && requestBo1){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if(map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) != null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng driverLatLng = new LatLng(locationLat,locationLng);
                    if(mDriverMarker != null){
                        mDriverMarker.remove();
                    }
                    Location loc1 = new Location("");
                    loc1.setLatitude(pickupLocation.latitude);
                    loc1.setLongitude(pickupLocation.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(driverLatLng.latitude);
                    loc2.setLongitude(driverLatLng.longitude);

                    float distance = loc1.distanceTo(loc2);

                    if (distance<100){
                        add.setText(R.string.llego);
                        lavadorllego();
                    }else{
                        add.setText(R.string.carwman +" "+ String.valueOf(distance));
                    }

                    mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title((String) getText(R.string.hcm)).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_lavador)));
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void lavadorllego(){
       // pagar.setVisibility(View.VISIBLE);
    }

    private void getAssignedDriverInfo(){
        mdriverInfo.setVisibility(View.VISIBLE);
        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("CarWashMan").child(driverFoundID);
        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    java.util.Map<String, Object> map = (java.util.Map<String, Object>)dataSnapshot.getValue();
                    if (map.get("name")!=null){
                        mdriverName.setText(map.get("name").toString());
                        name1 = mdriverName.getText().toString();
                    }
                    if (map.get("telefono")!=null){
                        final String phone = "" +getText(R.string.telefono) +": "+ dataSnapshot.child("telefono").getValue();
                        SpannableString mix = new SpannableString(phone);
                        mix.setSpan(new UnderlineSpan(), 0, mix.length(), 0);
                        mdriverPhone.setText(mix);
                        final String ph = "tel: " + dataSnapshot.child("telefono").getValue();
                        mdriverPhone.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                AlertDialog.Builder alertdial = new AlertDialog.Builder(view.getContext());
                                alertdial.setTitle(getText(R.string.llamara ) +" "  +name1);
                                alertdial.setMessage(R.string.llamara2);
                                alertdial.setPositiveButton(R.string.si, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent callIntent = new Intent(Intent.ACTION_DIAL);
                                        callIntent.setData(Uri.parse(ph));
                                        startActivity(callIntent);
                                    }
                                });
                                alertdial.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                alertdial.create().show();
                            }
                        });
                    }
                    if (map.get("image")!=null){
                        Glide.with(getApplication()).load(map.get("image").toString()).into(mdriverProfileImage);
                    }
                    int ratingSum = 0;
                    float ratingsTotal = 0;
                    float ratingsAvg = 0;
                    for (DataSnapshot child : dataSnapshot.child("rating").getChildren()){
                        ratingSum = ratingSum + Integer.valueOf(child.getValue().toString());
                        ratingsTotal++;
                    }
                    if(ratingsTotal!= 0){
                        ratingsAvg = ratingSum/ratingsTotal;
                        mRatingBar.setRating(ratingsAvg);
                    }
                }
                msg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                        intent.putExtra("hisUid", driverFoundID);
                        startActivity(intent);
                            }
                        });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            }
            else{
                checkLocationPermission();
            }
        }

        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        if (mapFragment!=null){
            mMap.setMyLocationEnabled(true);
        }

    }
    LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for(Location location : locationResult.getLocations()){
                if(getApplicationContext()!=null){
                    mLastLocation = location;

                    LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(14));
                    add.setVisibility(View.VISIBLE);
                }
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:{
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }
                }else{
                    Toast.makeText(getApplicationContext(), R.string.permisogps, Toast.LENGTH_LONG).show();

                }break;
            }
        }
    }

    private void checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                new android.app.AlertDialog.Builder(this)
                        .setTitle(R.string.permisoubicacion)
                        .setMessage(R.string.permisoubicacion)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(Map.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            }
            else{
                ActivityCompat.requestPermissions(Map.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

}
