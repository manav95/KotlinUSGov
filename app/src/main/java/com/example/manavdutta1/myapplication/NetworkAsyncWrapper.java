package com.example.manavdutta1.myapplication;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.identity.intents.Address;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by manavdutta1 on 7/15/17.
 */

//stores asynchronous tasks for network operations
public class NetworkAsyncWrapper {

    private AsyncTask<Object, Void, String> congressionalTask = new AsyncTask<Object, Void, String>() {
        @Override
        protected String doInBackground(Object... params) {
            Address address = (Address) params[0];
            Context context = (Context) params[1];

        }
    };

    private AsyncTask<Object, Void, String[]> networkTask = new AsyncTask<Object, Void, String[]>() {
        @Override
        protected String[] doInBackground(Object... params) {
            LatLng latLng = (LatLng) params[0];
            Context context = (Context) params[1];
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());

            List<android.location.Address> addresses = null;
            String[] arr = new String[4];
            try {
                addresses = geocoder.getFromLocation(
                        latLng.latitude,
                        latLng.longitude,
                        // In this sample, get just a single address.
                        1);
            } catch (IOException ioException) {
                // Catch network or other I/O problems.
                String errorMessage = "Not available I/O";
                Log.e("Error", errorMessage, ioException);
            } catch (IllegalArgumentException illegalArgumentException) {
                // Catch invalid latitude or longitude values.
                String errorMessage = "Invalid lat long";
                Log.e("Latitude Error: ", errorMessage + ". " +
                        "Latitude = " + latLng.latitude +
                        ", Longitude = " +
                        latLng.longitude, illegalArgumentException);
            }

            // Handle case where no address was found.
            if (addresses == null || addresses.size()  == 0) {
                String errorMessage = "No address found.";
                Log.e("Error in address: ", errorMessage);
            } else {
                android.location.Address address = addresses.get(0);
                Log.i("Stuff", "Found address: " + address.toString());
                arr[0] = address.getPostalCode();
                arr[1] = address.getLocality();
                arr[2] = address.getAdminArea();
                arr[3] = address.getSubAdminArea();
            }
            return arr;
        }
    };

    public AsyncTask<Object, Void, String> getCongressionalTask() {
        return congressionalTask;
    }


    public AsyncTask<Object, Void, String[]> getNetworkTask() {
        return networkTask;
    }

    public void start(LatLng latLng, Context context) {

    }


    public final class Constants {
        public static final int SUCCESS_RESULT = 0;
        public static final int FAILURE_RESULT = 1;
        public static final String PACKAGE_NAME =
                "com.google.android.gms.location.sample.locationaddress";
        public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
        public static final String RESULT_DATA_KEY = PACKAGE_NAME +
                ".RESULT_DATA_KEY";
        public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME +
                ".LOCATION_DATA_EXTRA";
    }

}
