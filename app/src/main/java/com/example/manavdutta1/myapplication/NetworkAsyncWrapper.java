package com.example.manavdutta1.myapplication;

import android.app.Service;
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
    protected Location mLastLocation;
    private AddressResultReceiver mResultReceiver;

    protected void startIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);
        IntroActivity.getMain().startService(intent);
    }

    private AsyncTask<LatLng, Void, String> congressionalTask = new AsyncTask<LatLng, Void, String>() {
        @Override
        protected String doInBackground(LatLng... latLngs) {
            LatLng latlng = latLngs[0];
        }
    };

    public AsyncTask<LatLng, Void, String> getCongressionalTask() {
        return congressionalTask;
    }

    public void setCongressionalTask(AsyncTask<LatLng, Void, String> congressionalTask) {
        this.congressionalTask = congressionalTask;
    }

    public void start(LatLng latLng) {

    }

    public class FetchAddressIntentService extends Service {
        protected ResultReceiver mReceiver;

        public FetchAddressIntentService() {

        }

        private void deliverResultToReceiver(int resultCode, String message) {
            Bundle bundle = new Bundle();
            bundle.putString(Constants.RESULT_DATA_KEY, message);
            mReceiver.send(resultCode, bundle);
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            Location location = intent.getParcelableExtra(
                    Constants.LOCATION_DATA_EXTRA);

            List<android.location.Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(
                        location.getLatitude(),
                        location.getLongitude(),
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
                        "Latitude = " + location.getLatitude() +
                        ", Longitude = " +
                        location.getLongitude(), illegalArgumentException);
            }

            // Handle case where no address was found.
            if (addresses == null || addresses.size()  == 0) {
                String errorMessage = "No address found.";
                Log.e("Error in address: ", errorMessage);
                deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
            } else {
                android.location.Address address = addresses.get(0);
                ArrayList<String> addressFragments = new ArrayList<String>();

                // Fetch the address lines using getAddressLine,
                // join them, and send them to the thread.
                for(int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    addressFragments.add(address.getAddressLine(i));
                }
                Log.i("Stuff", "Found address");
                deliverResultToReceiver(Constants.SUCCESS_RESULT,
                        TextUtils.join(System.getProperty("line.separator"),
                                addressFragments));
            }
            return null;
        }
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

    private class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string
            // or an error message sent from the intent service.
            String addressOutput = resultData.getString(Constants.RESULT_DATA_KEY);

            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {

            }

        }
    }
    }
