package com.colorblind.uncolor.aroundme;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import cz.msebera.android.httpclient.Header;

public class CreateRoom extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerClickListener,
        com.google.android.gms.location.LocationListener {

    EditText editTextNewRoomTitle;
    SeekBar seekBarRadius;
    TextView textViewRadius;
    GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    AsyncHttpClient client = new AsyncHttpClient();
    LocationRequest locationRequest;
    User user;
    String room_name;
    String room_id;
    int radius;
    boolean isEdit;

    private double longitude;
    private double latitude;

    Circle circle;


    LocationManager locationManager;


    /**
     * Create a UI fragment for create new room.
     * @return a fragment
     */
    public static CreateRoom newInstance() {
        CreateRoom fragment = new CreateRoom();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    /**
     * It's means that we can create a new room
     */
    public boolean isGoodData() {
        return (!editTextNewRoomTitle.getText().toString().isEmpty() && latitude != 0.0 && longitude != 0.0);
    }

    /**
     *  Method for edit existing room
     */
    public void editRoom() {

        int radius = (int) circle.getRadius();

        String URL = getString(R.string.domain) + getString(R.string.url_edit_room);
        RequestParams params = new RequestParams();
        params.put(getString(R.string.token), user.getToken());
        params.put(getString(R.string.user_id), user.getUser_id());
        params.put(getString(R.string.radius), Integer.toString(radius));
        params.put(getString(R.string.title), editTextNewRoomTitle.getText().toString());
        params.put(getString(R.string.latitude), Double.toString(latitude));
        params.put(getString(R.string.longitude), Double.toString(longitude));
        params.put(getString(R.string.room_id), room_id);


        client.post(URL, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {

                   // Log.i("fg", "edit room: " + response.toString());
                    String status = response.getString(getString(R.string.status));
                    if (Objects.equals(status, getString(R.string.failed))) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle(getString(R.string.error));
                        builder.setMessage(getString(R.string.create_room_failed));
                        builder.setCancelable(false);
                        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();


                    } else if (Objects.equals(status, getString(R.string.success))) {
                        getActivity().finish();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

            }

            @Override
            protected Object parseResponse(byte[] responseBody) throws JSONException {
                return super.parseResponse(responseBody);

            }
        });

    }


    /**
     * Method for create new room
     */
    public void createNewRoom() {

        int radius = (int) circle.getRadius();

        String URL = getString(R.string.domain) + getString(R.string.add_room);
        RequestParams params = new RequestParams();
        params.put(getString(R.string.token), user.getToken());
        params.put(getString(R.string.user_id), user.getUser_id());
        params.put(getString(R.string.radius), Integer.toString(radius));
        params.put(getString(R.string.title), editTextNewRoomTitle.getText().toString());
        params.put(getString(R.string.latitude), Double.toString(latitude));
        params.put(getString(R.string.longitude), Double.toString(longitude));


        client.post(URL, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    String status = response.getString(getString(R.string.status));
                    if (Objects.equals(status, getString(R.string.failed))) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle(getString(R.string.error));
                        builder.setMessage(getString(R.string.create_room_failed));
                        builder.setCancelable(false);
                        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();


                    } else if (Objects.equals(status, getString(R.string.success))) {
                        Room room = new Room();
                        JSONArray responseArray = response.getJSONArray(getString(R.string.response));
                        for (int i = 0; i < responseArray.length(); i++) {

                            JSONObject data = responseArray.getJSONObject(i);
                            room.setTitle(editTextNewRoomTitle.getText().toString());
                            room.setUsersCount("1");
                            room.setRoom_id(data.getString(getString(R.string.room_id)));
                            room.setAdmin(true);
                           // Log.i("fg", "Room id " + room.getRoom_id());
                        }

                        Intent intent = new Intent(getActivity(), Dialog.class);
                        intent.putExtra(getString(R.string.user), user);
                        intent.putExtra(getString(R.string.room), room);
                        intent.putExtra(getString(R.string.room_id), room.getRoom_id());
                        intent.putExtra(getString(R.string.room_name), room.getTitle());
                        intent.putExtra(getString(R.string.latitude), latitude);
                        intent.putExtra(getString(R.string.longitude), longitude);
                        startActivity(intent);
                        getActivity().finish();


                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

            }

            @Override
            protected Object parseResponse(byte[] responseBody) throws JSONException {
                return super.parseResponse(responseBody);

            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = getArguments().getParcelable(getString(R.string.user));
        room_name = getArguments().getString(getString(R.string.room_name));
        room_id = getArguments().getString(getString(R.string.room_id));
        radius = getArguments().getInt(getString(R.string.radius));
        isEdit = getArguments().getBoolean(getString(R.string.is_edit));
        Context context = getActivity();
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.create_room, container, false);
        editTextNewRoomTitle = (EditText) view.findViewById(R.id.editTextNewRoomTitle);
        if(isEdit){
            editTextNewRoomTitle.setText(room_name);
        }
        textViewRadius = (TextView) view.findViewById(R.id.textViewRadius);
        textViewRadius.setText(Integer.toString(3000) + getString(R.string.m));
        if(isEdit){
            textViewRadius.setText(Integer.toString(radius) + getString(R.string.m));
        }
        seekBarRadius = (SeekBar) view.findViewById(R.id.seekBarRadius);
        seekBarRadius.setMax(25000);
        seekBarRadius.post(new Runnable() {
            @Override
            public void run() {
                if (isEdit) {
                    seekBarRadius.setProgress(radius);
                } else {
                    seekBarRadius.setProgress(3000);
                }
            }
        });

        seekBarRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (circle != null) {
                    if (progress == 0) {
                        progress = 1;
                    }
                    textViewRadius.setText(Integer.toString(progress) + getString(R.string.m));
                    circle.setRadius(progress);

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                moveMap();
            }
        });

        createLocationRequest();

        return view;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {

       // Log.i("fg", "onViewCreated");
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mapToday);
        mapFragment.getMapAsync(this);
        googleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

    }

    @Override
    public void onStart() {
       // Log.i("fg", "onStart");
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
       // Log.i("fg", "onMapReady");
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setOnMarkerDragListener(this);
        mMap.setOnMapLongClickListener(this);


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
    public void onMapLongClick(LatLng latLng) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return true;
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


    /**
     * Start getting user's location
     */
    private void getCurrentLocation() {

      //  Log.i("fg", "current loc");

        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        if (location == null) {
            startLocationUpdates();
        }
        if (location != null) {


            longitude = location.getLongitude();
            latitude = location.getLatitude();

            if (circle == null) {

                circle = mMap.addCircle(new CircleOptions()
                        .center(new LatLng(latitude, longitude))
                        .radius(seekBarRadius.getProgress())
                        .strokeWidth(2.0f)
                        .strokeColor(0xFFA20022)
                        .fillColor(0x33A20022));

            } else {
                circle.setCenter(new LatLng(latitude, longitude));

            }

            moveMap();

        }
    }


    @Override
    public void onStop() {
        super.onStop();
        googleApiClient.disconnect();


    }

    /**
     * Set camera position above blue dot on map
     */
    private void moveMap() {

        if (latitude != 0.0 && longitude != 0.0) {

            LatLng latLng = new LatLng(latitude, longitude);

            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(getZoomLevel(circle)));
            mMap.getUiSettings().setZoomControlsEnabled(true);

        }
    }


    @Override
    public void onLocationChanged(Location location) {
        //Log.i("fg", "update");
        getCurrentLocation();

        if (circle != null) {
            circle.setCenter(new LatLng(location.getLatitude(), location.getLongitude()));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
        }
        if (circle == null) {

            int radius = 0;
            if (seekBarRadius.getProgress() == 0) {
                radius = 1;
            } else {
                radius = seekBarRadius.getProgress();
            }

            circle = mMap.addCircle(new CircleOptions()
                    .center(new LatLng(latitude, longitude))
                    .radius(radius)
                    .strokeColor(Color.RED));
        }
        moveMap();

        Log.i("fg", Double.toString(latitude) + " " + Double.toString(longitude));

    }




    /**
     * Settings for location requests
     */
    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);

    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }


    public void resetCircle() {
        circle = null;
    }

    /**
     * Get zoom level for better circle presentation
     */
    public float getZoomLevel(Circle circle) {
        float zoomLevel = 0.0f;
        if (circle != null) {
            double radius = circle.getRadius();
            double scale = radius / 500;
            zoomLevel = (float) (16 - Math.log(scale) / Math.log(2)) - 1.2f;
        }
        return zoomLevel;
    }
}
