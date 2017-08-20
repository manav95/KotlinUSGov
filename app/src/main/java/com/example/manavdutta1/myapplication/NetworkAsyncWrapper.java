package com.example.manavdutta1.myapplication;

import android.app.Activity;
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
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import android.location.Address;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


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
    private static String[] values;
    private static final HttpRequestInitializer httpRequestInitializer = new HttpRequestInitializer() {
        @Override
        public void initialize(HttpRequest request) throws IOException {
        }
    };

    private AsyncTask<Object, Void, String> congressionalTask = new AsyncTask<Object, Void, String>() {
        private Activity theActivityContext;
        @Override
        protected String doInBackground(Object... params) {
            Address address = (Address) params[0];
            theActivityContext = (Activity) params[1];
            GoogleClientRequestInitializer keyInitializer =
                    new CivicInfoRequestInitializer(theActivityContext.getApplicationContext().getResources().getString(R.string.api_key));
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
                        .setAddress(address.getAddressLine(0))
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
            TextView congressionalView = (TextView) theActivityContext.findViewById(R.id.textView6);
            congressionalView.setText(result);
        }
    };

    private AsyncTask<Object, Void, android.location.Address> networkTask = new AsyncTask<Object, Void, android.location.Address>() {
        private Activity theActivity;

        @Override
        protected android.location.Address doInBackground(Object... params) {
            LatLng latLng = (LatLng) params[0];
            theActivity = (Activity) params[1];
            Geocoder geocoder = new Geocoder(theActivity.getApplicationContext(), Locale.getDefault());

            List<android.location.Address> addresses = null;
            android.location.Address address = null;
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
                address = addresses.get(0);
                Log.i("Stuff", "Found address: " + address.toString());
            }
            return address;
        }

        @Override
        protected void onPostExecute(android.location.Address address) {
            Log.e("The result: ", address.toString());
            String[] arr = new String[4];
            arr[0] = address.getLocality();
            arr[1] = address.getSubAdminArea();
            arr[2] = address.getAdminArea();
            arr[3] = address.getPostalCode();
            ((TextView)theActivity.findViewById(R.id.textView)).setText(arr[0]);
            ((TextView)theActivity.findViewById(R.id.textView2)).setText(arr[1]);
            ((TextView)theActivity.findViewById(R.id.textView4)).setText(arr[2]);
            ((TextView)theActivity.findViewById(R.id.textView5)).setText(arr[3]);
            congressionalTask.execute(address, theActivity);
        }
    };
    private AsyncTask<String,Void,List<Award>> databaseTask = new AsyncTask<String, Void, List<Award>>() {
        private final String host = "usgovpublics.cq9deuttyxvp.us-east-1.rds.amazonaws.com";
        private final String port = "5432";
        private final String dbName = "data_store_api";
        private final String user = "root";
        private final String pass = "password";

        @Override
        protected List<Award> doInBackground(String... params) {
            try {
                Class.forName("org.postgresql.Driver");
                String jdbcUrl = "jdbc:postgresql://" + this.host + ":" + this.port + "/" + this.dbName + "?user=" + this.user + "&password=" + this.pass;
                Log.i("Info: ", "Getting remote connection with connection string from environment variables.");
                Connection con = DriverManager.getConnection(jdbcUrl);
                List<Award> awards = new ArrayList<>();
                String awardSql = "";
                PreparedStatement ps =null;
                switch (params[0]) {
                    case "Zip": awardSql = "select a.category, a.type_description, a.description, a.create_date, recipient.recipient_name, a.total_obligation from awards a LEFT JOIN legal_entity recipient ON recipient.legal_entity_id = a.recipient_id WHERE (a.recipient_id in (select location_id from references_location l where l.zip5 = ?) or a.place_of_performance_id in (select location_id from references_location l where l.zip5 = ? )) ORDER BY a.create_date DESC LIMIT 25";
                                ps = con.prepareStatement(awardSql);
                                ps.setString(1, params[1]);
                                ps.setString(2, params[1]);
                                break;
                    case "City": awardSql = "select a.category, a.type_description, a.description, a.create_date, recipient.recipient_name, a.total_obligation from awards a LEFT JOIN legal_entity recipient ON recipient.legal_entity_id = a.recipient_id WHERE (a.recipient_id in (select location_id from references_location l where l.city_name = ?) or a.place_of_performance_id in (select location_id from references_location l where l.city_name = ? )) ORDER BY a.create_date DESC LIMIT 25";
                                 ps = con.prepareStatement(awardSql);
                                 ps.setString(1, params[1].toUpperCase());
                                 ps.setString(2, params[1].toUpperCase());
                                 break;
                    case "County": awardSql = "select a.category, a.type_description, a.description, a.create_date, recipient.recipient_name, a.total_obligation from awards a LEFT JOIN legal_entity recipient ON recipient.legal_entity_id = a.recipient_id WHERE (a.recipient_id in (select location_id from references_location l where l.county_name = ?) or a.place_of_performance_id in (select location_id from references_location l where l.county_name = ? )) ORDER BY a.create_date DESC LIMIT 25";
                                   ps = con.prepareStatement(awardSql);
                                   ps.setString(1, params[1].toUpperCase());
                                   ps.setString(2, params[1].toUpperCase());
                                   break;
                    case "State": awardSql = "select a.category, a.type_description, a.description, a.create_date, recipient.recipient_name, a.total_obligation from awards a LEFT JOIN legal_entity recipient ON recipient.legal_entity_id = a.recipient_id WHERE (a.recipient_id in (select location_id from references_location l where l.state_name = ?) or a.place_of_performance_id in (select location_id from references_location l where l.state_name = ? )) ORDER BY a.create_date DESC LIMIT 25";
                                  ps = con.prepareStatement(awardSql);
                                  ps.setString(1, params[1].toUpperCase());
                                  ps.setString(2, params[1].toUpperCase());
                                  break;
                    default:      awardSql = "select a.category, a.type_description, a.description, a.create_date, recipient.recipient_name, a.total_obligation from awards a LEFT JOIN legal_entity recipient ON recipient.legal_entity_id = a.recipient_id WHERE (a.recipient_id in (select location_id from references_location l where l.congressional_code = ? and l.state_code = ?) or a.place_of_performance_id in (select location_id from references_location l where l.congressional_code = ? AND l.state_code= ?)) ORDER BY a.create_date DESC LIMIT 25";
                                  ps = con.prepareStatement(awardSql);
                                  ps.setString(1, params[1].substring(params[1].length() - 2));
                                  ps.setString(2, params[1].substring(0, 2));
                                  ps.setString(3, params[1].substring(params[1].length() - 2));
                                  ps.setString(4, params[1].substring(0, 2));
                                  break;
                }

                ResultSet rsOne = ps.executeQuery();

                while(rsOne.next()) {
                    int i = 1;
                    Award award = new Award(rsOne.getString(i++), rsOne.getString(i++), rsOne.getString(i++), rsOne.getDate(i++), rsOne.getString(i++), rsOne.getLong(i++));
                    awards.add(award);
                }
                return awards;
            }
            catch (ClassNotFoundException e) {
                Log.e("ClassException: ", e.toString());
            }
            catch (SQLException e) {
                Log.e("SQLException: ", e.toString());
            }
            finally {
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Award> awards) {

        }
    };

    private static void setValue(String[] result) {
       values = result;
    }

    public AsyncTask<Object, Void, String> getCongressionalTask() {
        return congressionalTask;
    }


    public AsyncTask<Object, Void, android.location.Address> getNetworkTask() {
        return networkTask;
    }

    public AsyncTask<String,Void,List<Award>> getDatabaseTask() {
        return databaseTask;
    }

    public void start(LatLng latLng, Context context) {
          networkTask.execute(latLng, context);
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
