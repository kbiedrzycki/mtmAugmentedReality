package pg.eti.biedrzycki.findmyfriends.utils;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import org.springframework.http.HttpEntity;

import java.util.ArrayList;

import pg.eti.biedrzycki.findmyfriends.App;
import pg.eti.biedrzycki.findmyfriends.Map;
import pg.eti.biedrzycki.findmyfriends.models.Param;
import pg.eti.biedrzycki.findmyfriends.models.UserLocation;

public class GPSService extends Service {
    private LocationManager lm;
    private LocationListener locationListener;

    private long refreshTimeoutMills = 5000;
    private int lastServiceStatus = 0;
    private static float minAccuracyMeters = 35;

    App appState;

    private boolean isDebugging = false;

    private void startLoggerService() {
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        appState = ((App) getApplicationContext());

        locationListener = new MyLocationListener();

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, refreshTimeoutMills, 0, locationListener);
    }

    private void shutdownLoggerService() {
        lm.removeUpdates(locationListener);
    }

    private void sendMessageToActivity(String message) {
        Intent intent = new Intent(Map.ACTION_NEW_POSITION);
        intent.putExtra(Map.POSITION_FIELD, message);
        sendBroadcast(intent);
    }

    public class MyLocationListener implements LocationListener {
        public void onLocationChanged(Location loc) {
            if (loc != null) {
                if (isDebugging) {
                    Toast.makeText(getBaseContext(),
                            "Location has changed and acc is " + (loc.hasAccuracy() ? loc.getAccuracy()+"m":"?"),
                            Toast.LENGTH_SHORT).show();
                }

                if (loc.hasAccuracy() && loc.getAccuracy() <= minAccuracyMeters) {
                    double lat = loc.getLatitude();
                    double lng = loc.getLongitude();
                    double alt = loc.getAltitude();
                    double speed = (double) loc.getSpeed();

                    new LogLocation().execute(lat, lng, alt, speed);

                    sendMessageToActivity(lat + "/" + lng + "/" + alt);

                    if (isDebugging) {
                        Toast.makeText(
                                getBaseContext(),
                                "Latitude: " + Double.toString(lat) +
                                        " Longitude: " + Double.toString(lng) +
                                        " Altitude: " + Double.toString(alt) +
                                        " Speed: " + Double.toString(speed),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
            }
        }

        public void onProviderDisabled(String provider) {
            // if changed ...
        }

        public void onProviderEnabled(String provider) {
            // if changed ...
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch(status) {
                case LocationProvider.AVAILABLE:
                    // if changed ...
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    // if changed ...
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    // if changed ...
                    break;
            }

            lastServiceStatus = status;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Toast.makeText(this, "You are now online", Toast.LENGTH_SHORT).show();

        startLoggerService();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Toast.makeText(this, "You are now offline", Toast.LENGTH_SHORT).show();

        shutdownLoggerService();
    }

    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        GPSService getService() {
            return GPSService.this;
        }
    }

    private class LogLocation extends AsyncTask<Double, Void, Void> {
        APIInterceptor apiInterceptor = new APIInterceptor();

        @Override
        protected Void doInBackground(Double ...params) {
            if (appState.getCurrentUser() == null) {
                return null;
            }

            apiInterceptor.setEndpoint("locations/" + appState.getCurrentUser().getId());
            apiInterceptor.setHttpMethod("post");

            ArrayList<Param> query = new ArrayList();

            query.add(new Param("token", appState.getSecurityToken()));
            query.add(new Param("lat", Double.toString(params[0])));
            query.add(new Param("lng", Double.toString(params[1])));
            query.add(new Param("alt", Double.toString(params[2])));
            query.add(new Param("speed", Double.toString(params[3])));

            apiInterceptor.call(query, UserLocation.class);

            return null;
        }
    }
}
