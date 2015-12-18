package pg.eti.biedrzycki.findmyfriends;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.springframework.http.HttpEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pg.eti.biedrzycki.findmyfriends.models.Friend;
import pg.eti.biedrzycki.findmyfriends.models.Param;
import pg.eti.biedrzycki.findmyfriends.models.UserLocation;
import pg.eti.biedrzycki.findmyfriends.utils.APIInterceptor;
import pg.eti.biedrzycki.findmyfriends.utils.FriendsListAdapter;

public class Map extends ActionBarActivity implements OnMapReadyCallback {
    public static final String ACTION_NEW_POSITION = "pg.eti.biedrzycki.broadcastreceiver.NEW_POSITION";
    public static final String POSITION_FIELD = "position";

    private GoogleMap mMap;
    boolean isRefreshRunning = false;
    boolean centeredOnUserPosition = false;
    Marker userMarker;
    LatLng userPosition;

    private LocationReceiver locationReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.app_toolbar);
        setSupportActionBar(myToolbar);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                Context context = getApplicationContext(); //or getActivity(), YourActivity.this, etc.

                LinearLayout info = new LinearLayout(context);
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(context);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(context);
                snippet.setTextColor(Color.BLACK);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
            }
        });

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
                    userPosition = new LatLng(lat, lng);

                    if (userMarker != null) {
                        userMarker.remove();
                    }

                    userMarker = mMap.addMarker(
                            new MarkerOptions()
                                    .position(userPosition)
                                    .title("You are here!")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin))
                    );

                    if (!centeredOnUserPosition) {
                        centeredOnUserPosition = true;
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userPosition, 12.0f));
                    }
                }
            }
        }
    }

    private class LoadFriendsPositions extends AsyncTask<Void, Void, UserLocation[]> {
        APIInterceptor apiInterceptor = new APIInterceptor();

        @Override
        protected UserLocation[] doInBackground(Void ...params) {
            isRefreshRunning = true;

            if (appState.getUserStatus() == App.OFFLINE_STATUS) {
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
            // clear all markers from map
            mMap.clear();

            if (userPosition != null) {
                userMarker = mMap.addMarker(
                        new MarkerOptions()
                                .position(userPosition)
                                .title("You are here!")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin))
                );
            }

            isRefreshRunning = false;

            if (result != null && apiInterceptor.error == null) {
                if (result.length > 0) {
                    int locationsCount = result.length;

                    for (int i = 0; i < locationsCount; i++) {
                        UserLocation uLoc = result[i];

                        MarkerOptions marker = new MarkerOptions()
                                .position(new LatLng(uLoc.getLat(), uLoc.getLng()))
                                .title(uLoc.getFirstName() + " " + uLoc.getLastName());

                        marker.snippet("E-mail: " + uLoc.getEmail() +
                                "\n" + "Altitude: " + Math.round(uLoc.getAlt()) +
                                "\n" + "Speed: " + Math.round(uLoc.getSpeed() * 100)/100 + "m/s");

                        if (uLoc.getGender().equals("M")) {
                            marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_male));
                        } else {
                            marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_female));
                        }

                        mMap.addMarker(marker);
                    }
                }
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

                new AlertDialog.Builder(Map.this)
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
