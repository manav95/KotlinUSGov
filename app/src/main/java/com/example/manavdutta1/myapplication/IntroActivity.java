package com.example.manavdutta1.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class IntroActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener{
    private static final int MY_PERMISSIONS_FINE_LOCATION = 2;
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private NetworkAsyncWrapper networkAsyncWrapper;
    private LocationManager mLocationManager;
    private LocationRequest mLocationRequest;
    private LocationManager locationManager;
    private long UPDATE_INTERVAL = 2 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */
    private boolean setIntro = true;
    private TextView stateView;
    private TextView countyView;
    private TextView congressionalView;
    private TextView cityView;
    private TextView zipView;
    private EditText username;
    private EditText password;
    private RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        networkAsyncWrapper = new NetworkAsyncWrapper(this);
        cityView = (TextView) findViewById(R.id.textView);
        countyView = (TextView) findViewById(R.id.textView2);
        stateView = (TextView) findViewById(R.id.textView4);
        zipView = (TextView) findViewById(R.id.textView5);
        congressionalView = (TextView) findViewById(R.id.textView6);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        radioGroup = (RadioGroup) findViewById(R.id.queryGroup);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String type = "";
                String value = "";
                switch (radioGroup.getCheckedRadioButtonId()) {
                    case R.id.zipButton:
                        value = (String) zipView.getText();
                        type = "Zip";
                        break;
                    case R.id.cityButton:
                        value = (String) cityView.getText();
                        type = "City";
                        break;
                    case R.id.countyButton:
                        value = (String) countyView.getText();
                        type = "County";
                        break;
                    case R.id.stateButton:
                        value = (String) stateView.getText();
                        type=  "State";
                        break;
                    case R.id.congressionalButton:
                        value = (String) congressionalView.getText();
                        type = "Congressional";
                        break;
                }
                String[] tasks = {type, value, username.getText().toString(), password.getText().toString()};
                networkAsyncWrapper.getDatabaseTask().execute(tasks);
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSIONS_FINE_LOCATION);

    }

    //need to request permissions first
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mGoogleApiClient.connect();
                }
                else {
                    Log.e("Error: ", "Permission not granted to get location");
                    //finish activity due to lack of permissions
                    finish();
                }
            }

        }
    }
    @Override
    public void onConnected(Bundle bundle) {
        if ((ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            return;
        }

        //startLocationUpdates();

        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if(mLocation == null){
            startLocationUpdates();
        }
        if (mLocation != null) {
            LatLng latLng = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
            setIntro = false;
            networkAsyncWrapper.start(latLng, this);
        } else {
            Toast.makeText(this, "Location not Detected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("LatLong Connection: ", "Connection Suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i("LatLong Connection: ", "Connection failed. Error: " + connectionResult.getErrorCode());
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    protected void startLocationUpdates() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
        // Request location updates
        if ((ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
        Log.d("reque", "--->>>>");
    }

    public void switchToTabbed(ArrayList<Award> awards, String type) {
        Intent i = new Intent(this, ListSpendingActivity.class);
        i.putParcelableArrayListExtra("AWARDLIST", awards);
        i.putExtra("TYPE", type);
        startActivity(i);
    }

    @Override
    public void onLocationChanged(Location location) {

        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());

        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        setIntro = false;
        networkAsyncWrapper.start(latLng, this);
    }

    private boolean checkLocation() {
        if(!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                        "use this app")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                    }
                });
        dialog.show();
    }

    private boolean isLocationEnabled() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


}
