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


import com.google.android.gms.auth.GoogleAuthException;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.civicinfo.CivicInfo;
import com.google.api.services.civicinfo.CivicInfoRequestInitializer;
import com.google.api.services.civicinfo.model.DivisionSearchResponse;
import com.google.api.services.civicinfo.model.Office;
import com.google.api.services.civicinfo.model.RepresentativeInfoResponse;
import com.google.api.services.tasks.TasksScopes;


/**
 * Created by manavdutta1 on 7/15/17.
 */

//stores asynchronous tasks for network operations
public class NetworkAsyncWrapper {
    private static final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();
    private static final GsonFactory JSON_FACTORY = new GsonFactory();
    private static final HttpRequestInitializer httpRequestInitializer = new HttpRequestInitializer() {
        @Override
        public void initialize(HttpRequest request) throws IOException {
        }
    };

    private AsyncTask<Object, Void, String> congressionalTask = new AsyncTask<Object, Void, String>() {
        @Override
        protected String doInBackground(Object... params) {
            Address address = (Address) params[0];
            Context context = (Context) params[1];
            GoogleClientRequestInitializer keyInitializer =
                    new CivicInfoRequestInitializer(context.getResources().getString(R.string.api_key));
            CivicInfo civicInfo =
                    new CivicInfo.Builder(HTTP_TRANSPORT, JSON_FACTORY, httpRequestInitializer)
                            .setApplicationName("Places")
                            .setGoogleClientRequestInitializer(keyInitializer)
                            .build();
            RepresentativeInfoResponse representativeInfoResponse = null;
            String districtName = "";
            try {
                representativeInfoResponse = civicInfo.representatives()
                        .representativeInfoByAddress()
                        .setAddress(address.toString())
                        .execute();

                List<Office> offices = representativeInfoResponse.getOffices();
                for (Office office: offices) {
                    if (office.getName().contains("United States House of Representatives")) {
                        int startIndex = office.getName().length() - 5;
                        districtName = office.getName().substring(startIndex);
                    }
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
            return districtName;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.e("The result: ", result);
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

        @Override
        protected void onPostExecute(String[] result) {
            Log.e("The result: ", result.toString());
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
