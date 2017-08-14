package com.example.uncolor.aroundme;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import cz.msebera.android.httpclient.Header;


public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerClickListener,
        View.OnClickListener,
        com.google.android.gms.location.LocationListener {

    private static final String STATUS_FAIL = "failed";
    private static final String STATUS_SUCCESS = "success";

    GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    AsyncHttpClient client = new AsyncHttpClient();
    LocationRequest locationRequest;
    double latitude;
    double longitude;
    User user;

    public MapFragment() {

    }

    @Override
    public void onResume(){
        super.onResume();
        SharedPreferences sharedPref = getActivity().getSharedPreferences("com.example.aroundme.KEYS", Context.MODE_PRIVATE);
        user.setAvatar_url(sharedPref.getString(getString(R.string.avatar_url), ""));
    }



    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        user = getArguments().getParcelable("user");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        createLocationRequest();
        return view;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mapAllChats);
        mapFragment.getMapAsync(this);
        googleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getCurrentLocation();
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onMapLongClick(LatLng latLng) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setBuildingsEnabled(true);
        mMap.setOnMarkerDragListener(this);
        mMap.setOnMapLongClickListener(this);
    }

    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(10000);

    }

    private void loadAllChatsOnMap(double latitude, double longitude) {

        String URL = new String("http://aroundme.lwts.ru/allrooms?");
        RequestParams params = new RequestParams();
        params.put("token", user.getToken());
        params.put("user_id", user.getUser_id());
        params.put("latitude", "55");
        params.put("longitude", "55");
        params.put("offset", "0");
        params.put("limit", "100");

        client.get(URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                Log.i("fg", "RESPONSE ALLROOMS " + response.toString());
                try {
                    String status = response.getString("status");
                    if (Objects.equals(status, STATUS_FAIL)) {

                        Log.i("fg","FAIL!");

                    } else if (Objects.equals(status, STATUS_SUCCESS)) {

                        JSONArray responseArray = response.getJSONArray("response");
                        Log.i("fg", "response array " + Integer.toString(responseArray.length()));
                        mMap.clear();
                        for (int i = 0; i < responseArray.length(); i++) {
                            JSONObject data = responseArray.getJSONObject(i);
                            String title = "";
                            double latitude = 0.0;
                            double longitude = 0.0;
                            if (data.has("title")) {
                                title = data.getString("title");
                            }
                            if (data.has("latitude")) {
                                latitude = data.getDouble("latitude");
                            }

                            if (data.has("longitude")) {
                                longitude = data.getDouble("longitude");
                            }
                            addMarker(latitude, longitude, title);

                            Log.i("fg", "Position: " + Double.toString(latitude) + " " + Double.toString(latitude) + " " + title);
                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });


    }

    @Override
    public void onStart() {
        Log.i("fg", "onStart");
        super.onStart();
        googleApiClient.connect();


    }

    @Override
    public void onStop() {
        Log.i("fg", "onStop");
        super.onStop();
        googleApiClient.disconnect();

    }

    public void getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        if (location == null || (location.getLatitude() == 0.0 && location.getLongitude() == 0.0)) {
            startLocationUpdates();
        }
        if (location != null) {

            longitude = location.getLongitude();
            latitude = location.getLatitude();
            Log.i("fg", "get Current location " + Double.toString(latitude) + " " + Double.toString(longitude));
            mMap.clear();
            loadAllChatsOnMap(latitude, longitude);


        }
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

    }

    private void addMarker(double latitude, double longitude, String title) {
        LatLng latLng = new LatLng(latitude, longitude);
        if (mMap != null) {
            Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(latLng.latitude, latLng.longitude))
                    .title(title));
        }
    }

    public void updateMap(){
        getCurrentLocation();
    }

}
