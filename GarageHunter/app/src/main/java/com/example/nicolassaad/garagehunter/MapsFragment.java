package com.example.nicolassaad.garagehunter;

import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Iterator;

/**
 * Created by nicolassaad on 5/2/16.
 */
public class MapsFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private Button hideSearchButton;
    private Button searchButton;
    public static LinearLayout searchLayout;
    private Spinner searchByDay;

    private int buttonCounter;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private LocationRequest mLocationRequest;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private SupportMapFragment fragment;

    private Query query;
    private Firebase mFirebase;

    public MapsFragment() {
    }

    private static final String TAG = "MapsFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.activity_maps, container, false);
        searchButton = (Button) view.findViewById(R.id.search_button);
        searchLayout = (LinearLayout) view.findViewById(R.id.search_layout);
        hideSearchButton = (Button) view.findViewById(R.id.hide_search_button);
        searchByDay = (Spinner) view.findViewById(R.id.search_by_day);

        hideSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buttonCounter % 2 == 0) {
                    searchLayout.setVisibility(View.GONE);
                    hideSearchButton.setText("Show Search");
                    buttonCounter++;
                } else {
                    searchLayout.setVisibility(View.VISIBLE);
                    hideSearchButton.setText("Hide Search");
                    buttonCounter++;
                }
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

        mFirebase = new Firebase("https://garagesalehunter.firebaseio.com/");

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        searchByDay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                query = mFirebase.orderByChild("weekday").equalTo(searchByDay.getSelectedItem().toString());
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                        mMap.clear();

                        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                        if (location == null) {
                            Log.d(TAG, "Location is null");
                        } else {
                            handleNewLocation(location);
                        }

                        while (iterator.hasNext()) {
                            GarageSale daySearch = iterator.next().getValue(GarageSale.class);
                            Log.d("MapsFragment", daySearch.toString());
                                // Displays markers for all matching entries
                                mMap.addMarker(new MarkerOptions().position(new LatLng(daySearch.getLat(), daySearch.getLon())).title(daySearch.getTitle()));
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        mGoogleApiClient.connect();

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            Log.d("MainActivity", "Setting up if needed running");
            // Try to obtain the map from the SupportMapFragment.
            FragmentManager fm = getChildFragmentManager();
            fragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
            mMap = fragment.getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                Log.d(TAG, "Map is not null");
            }
        }
    }

    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());
        Log.d("MainActivity", "Handling new location");

        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();

        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        if (mMap != null) {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(new LatLng(currentLatitude, currentLongitude)).title("You Are Here"));

            Log.d(TAG, currentLatitude + " " + currentLongitude);

            CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(11).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        } else {
            Log.d(TAG, "Map is null");
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            // ask for the permission
//            requestPermissions();
            Log.d(TAG, "Permissions are gone");
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            Log.d(TAG, "Location is null");
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } else {
            handleNewLocation(location);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
   /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(getActivity(), CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }
}

