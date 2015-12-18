package pg.eti.biedrzycki.findmyfriends;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.springframework.http.HttpEntity;

import java.util.ArrayList;

import pg.eti.biedrzycki.findmyfriends.models.CameraDrawing;
import pg.eti.biedrzycki.findmyfriends.models.Param;
import pg.eti.biedrzycki.findmyfriends.models.UserLocation;
import pg.eti.biedrzycki.findmyfriends.utils.APIInterceptor;
import pg.eti.biedrzycki.findmyfriends.utils.CustomCameraView;

public class CameraPreview extends ActionBarActivity implements SensorEventListener {
    public static final String ACTION_NEW_POSITION = "pg.eti.biedrzycki.broadcastreceiver.NEW_POSITION";
    public static final String POSITION_FIELD = "position";
    static final float ALPHA = 0.1f;

    CustomCameraView customCameraView;

    boolean isRefreshRunning = false;

    static final float Radius = 6378137;
    final float cameraB[] = new float[]{0, 0, 1};
    float cameraM[] = new float[3];

    float namiarM[] = new float[]{1, 0, 0};
    float namiarB[] = new float[]{0, 1, 0};

    double userLat;
    double userLng;
    double userAlt;

    public int screenHeight;
    public int screenWidth;

    LatLng userLatLng;

    UserLocation[] resultLocations;

    ArrayList<CameraDrawing> cameraDrawingData = new ArrayList<CameraDrawing>();

    private LocationReceiver locationReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.app_toolbar);
        setSupportActionBar(myToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_activity_camera);
        }

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        screenHeight = displayMetrics.heightPixels;
        screenWidth = displayMetrics.widthPixels;

        customCameraView = (CustomCameraView) findViewById(R.id.view);

        // when map is ready start timer to refresh map
        CountDownTimer t = new CountDownTimer(Long.MAX_VALUE , 5000) {
            public void onTick(long millisUntilFinished) {
                if (!isRefreshRunning) {
                    new LoadFriendsPositions().execute();
                }
            }

            public void onFinish() {
                start();
            }
        }.start();

        initReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();

        SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor acc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor mag = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        sm.registerListener(this, acc, SensorManager.SENSOR_DELAY_GAME);
        sm.registerListener(this, mag, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();

        SensorManager sm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        sm.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        finishReceiver();
        super.onDestroy();
    }

    private void initReceiver() {
        locationReceiver = new LocationReceiver();
        IntentFilter filter = new IntentFilter(ACTION_NEW_POSITION);
        registerReceiver(locationReceiver, filter);
    }

    private void finishReceiver() {
        unregisterReceiver(locationReceiver);
    }

    public class LocationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_NEW_POSITION)) {
                String concatPosition = intent.getStringExtra(POSITION_FIELD);
                String[] parts = concatPosition.split("/");

                Double lat = Double.parseDouble(parts[0]);
                Double lng = Double.parseDouble(parts[1]);
                Double alt = Double.parseDouble(parts[2]);

                if (lat != null && lng != null && alt != null) {
                    userLat = lat;
                    userLng = lng;
                    userAlt = alt;

                    userLatLng = new LatLng(lat, lng);

                    Toast.makeText(getBaseContext(), "Received location..", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    float[] magVal = null;
    float[] accVal = null;

    float[] rotFromBtoM = new float[9];

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case (Sensor.TYPE_ACCELEROMETER):
                accVal = lowPass(event.values.clone(), accVal);
                break;
            case (Sensor.TYPE_MAGNETIC_FIELD):
                magVal = lowPass(event.values.clone(), magVal);
                break;
        }

        if (magVal != null && accVal != null) {
            boolean success = SensorManager.getRotationMatrix(rotFromBtoM, null, accVal, magVal);
            cameraDrawingData = new ArrayList<CameraDrawing>();

            if (userLatLng != null && resultLocations != null) {
                int resultLocationsSize = resultLocations.length;

                if (resultLocationsSize > 0) {

                    for (int i = 0 ; i < resultLocationsSize; i ++) {
                        float[] enu = latlonToENU(
                                (float) (resultLocations[i].getLat() / 180 * Math.PI),
                                (float) (resultLocations[i].getLng() / 180 * Math.PI),
                                (float) resultLocations[i].getAlt(),
                                (float) (userLat / 180 * Math.PI),
                                (float) (userLng / 180 * Math.PI),
                                (float) userAlt
                        );

                        namiarM = enu;

                        cameraM[0] = cameraB[0] * rotFromBtoM[0] +
                                cameraB[1] * rotFromBtoM[1] +
                                cameraB[2] * rotFromBtoM[2];
                        cameraM[1] = cameraB[0] * rotFromBtoM[0 + 3] +
                                cameraB[1] * rotFromBtoM[1 + 3] +
                                cameraB[2] * rotFromBtoM[2 + 3];
                        cameraM[2] = cameraB[0] * rotFromBtoM[0 + 6] +
                                cameraB[1] * rotFromBtoM[1 + 6] +
                                cameraB[2] * rotFromBtoM[2 + 6];

                        namiarB[0] = namiarM[0] * rotFromBtoM[0] +
                                namiarM[1] * rotFromBtoM[3] +
                                namiarM[2] * rotFromBtoM[6];
                        namiarB[1] = namiarM[0] * rotFromBtoM[1] +
                                namiarM[1] * rotFromBtoM[4] +
                                namiarM[2] * rotFromBtoM[7];
                        namiarB[2] = namiarM[0] * rotFromBtoM[2] +
                                namiarM[1] * rotFromBtoM[5] +
                                namiarM[2] * rotFromBtoM[8];

                        Location userLocation = new Location("Me");
                        userLocation.setLatitude(userLat);
                        userLocation.setLongitude(userLng);

                        Location friendLocation = new Location("Friend " + resultLocations[i].getUser_id());
                        friendLocation.setLatitude(resultLocations[i].getLat());
                        friendLocation.setLongitude(resultLocations[i].getLng());

                        Log.i("Me", "Latitude: " + userLocation.getLatitude());
                        Log.i("Me", "Longitude: " + userLocation.getLongitude());

                        Log.i("Friend", "Latitude: " + friendLocation.getLatitude());
                        Log.i("Friend", "Longitude: " + friendLocation.getLongitude());

                        float distance = userLocation.distanceTo(friendLocation);

                        Log.i("Distance", "Distance: " + distance);

                        CameraDrawing drawing = new CameraDrawing();
                        drawing.setX(namiarB[0] / namiarB[2]);
                        drawing.setY(namiarB[1] / namiarB[2]);
                        drawing.setZ(namiarB[2]);
                        drawing.setAvatar(resultLocations[i].getAvatar());
                        drawing.setFirstName(resultLocations[i].getFirstName());
                        drawing.setLastName(resultLocations[i].getLastName());
                        drawing.setSpeed(resultLocations[i].getSpeed());
                        drawing.setDistance(distance);
                        drawing.setGender(resultLocations[i].getGender());

                        Log.i("Namiar", "Namiar[0]: " + namiarB[0]);
                        Log.i("Namiar", "Namiar[1]: " + namiarB[1]);
                        Log.i("Namiar", "Namiar[2]: " + namiarB[2]);

                        cameraDrawingData.add(drawing);
                    }
                }
            }

            customCameraView.setDane(cameraDrawingData);
            customCameraView.invalidate();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected float[] lowPass( float[] input, float[] output ) {
        if ( output == null ) return input;

        for ( int i=0; i < input.length; i++ ) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }

        return output;
    }

    //lat i lon w radianach !!!!
    static float[] latLonToECEF(float lat, float lon, float h) {
        float[] ECEF = new float[3];
        ECEF[0] = (float) ((h + Radius) * Math.cos(lat) * Math.cos(lon));
        ECEF[1] = (float) ((h + Radius) * Math.cos(lat) * Math.sin(lon));
        ECEF[2] = (float) ((h + Radius) * Math.sin(lat));
        return ECEF;
    }

    //lat i lon w radianach
    static float[] latlonToENU(float lat, float lon, float h,
                               float ulat, float ulon, float uh) {
        float[] ecefX = latLonToECEF(lat, lon, h);
        float[] ecefU = latLonToECEF(ulat, ulon, uh);
        float[] d = new float[3];
        d[0] = ecefX[0] - ecefU[0];
        d[1] = ecefX[1] - ecefU[1];
        d[2] = ecefX[2] - ecefU[2];
        float[] enu = new float[3];

        enu[0] = (float) (-Math.sin(ulon) * d[0] + Math.cos(ulon) * d[1]);
        enu[1] = (float) (-Math.cos(ulon) * Math.sin(ulat) * d[0] - Math.sin(ulon) * Math.sin(ulat) * d[1] + Math.cos(ulat) * d[2]);
        enu[2] = (float) (Math.cos(ulon) * Math.cos(ulat) * d[0] + Math.sin(ulon) * Math.cos(ulat) * d[1] + Math.sin(ulat) * d[2]);
        return enu;
    }

    double getAngle(float[] a, float b[]) {
        double temp = (a[0] * b[0] + a[1] * b[1] + a[2] * b[2]) /
                Math.sqrt(a[0] * a[0] + a[1] * a[1] + a[2] * a[2]) /
                Math.sqrt(b[0] * b[0] + b[1] * b[1] + b[2] * b[2]);
        return Math.acos(temp);
    }

    private class LoadFriendsPositions extends AsyncTask<Void, Void, UserLocation[]> {
        APIInterceptor apiInterceptor = new APIInterceptor();

        @Override
        protected UserLocation[] doInBackground(Void ...params) {
            isRefreshRunning = true;

            if (appState.getCurrentUser() == null) {
                return null;
            }

            apiInterceptor.setEndpoint("locations/" + appState.getCurrentUser().getId());
            apiInterceptor.setHttpMethod("get");

            ArrayList<Param> query = new ArrayList();

            query.add(new Param("token", appState.getSecurityToken()));

            HttpEntity<UserLocation[]> result = apiInterceptor.call(query, UserLocation[].class);

            if (result != null) {
                return result.getBody();
            }

            return null;
        }

        @Override
        protected void onPostExecute(UserLocation[] result) {
            isRefreshRunning = false;
            cameraDrawingData = new ArrayList<CameraDrawing>();
            resultLocations = null;

            if (result != null && apiInterceptor.error == null) {
                resultLocations = result;
            }

            if (apiInterceptor.error != null) {
                String title;
                String message;

                switch (apiInterceptor.error) {
                    case APIInterceptor.NOT_AUTHENTICATED:
                        title = "Error";
                        message = "You are not authenticated!";

                        break;
                    default:
                        title = "Error";
                        message = "Unhandled exception";
                }

                new AlertDialog.Builder(CameraPreview.this)
                        .setTitle(title)
                        .setMessage(message)
                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
            }
        }
    }
}
