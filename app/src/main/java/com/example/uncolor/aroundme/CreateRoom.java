package com.example.uncolor.aroundme;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.client.ResponseHandler;

public class CreateRoom extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerClickListener,
        View.OnClickListener,
        com.google.android.gms.location.LocationListener {

    private static final String STATUS_FAIL = "failed";
    private static final String STATUS_SUCCESS = "success";
    EditText editTextNewRoomTitle;
    GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    AsyncHttpClient client = new AsyncHttpClient();
    LocationRequest locationRequest;
    User user;

    private double longitude;
    private double latitude;
    int markersCount = 0;
    LocationManager locationManager;


    public static CreateRoom newInstance() {
        CreateRoom fragment = new CreateRoom();
        fragment.setArguments(new Bundle());
        return fragment;
    }


    public Room createNewRoom() {


        String URL = new String("http://aroundme.lwts.ru/addroom?");
        RequestParams params = new RequestParams();
        params.put("token", user.getToken());
        params.put("user_id", user.getUser_id());
        params.put("radius", "3000");
        params.put("title", editTextNewRoomTitle.getText().toString());
        params.put("latitude", Double.toString(latitude));
        params.put("longitude", Double.toString(longitude));



        client.post(URL, params, new JsonHttpResponseHandler() {
            Room room = null;
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.i("fg", response.toString());
                try {
                    String status = response.getString("status");
                    if (Objects.equals(status, STATUS_FAIL)) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("Ошибка");
                        builder.setMessage("Не удалось создать комнату!");
                        builder.setCancelable(false);
                        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();


                    } else if (Objects.equals(status, STATUS_SUCCESS)) {
                        Log.i("fg", "was here");
                        room = new Room();
                        JSONArray responseArray = response.getJSONArray("response");
                        for (int i = 0; i < responseArray.length(); i++) {

                            JSONObject data = responseArray.getJSONObject(i);
                            room.setTitle(editTextNewRoomTitle.getText().toString());
                            room.setUsersCount("1");
                            room.setRoom_id(data.getString("room_id"));

                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

            }


        });
        if(room == null){
            Log.i("fg", "bad room");
        }
        return room;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        Log.i("fg", "onCreate");
        super.onCreate(savedInstanceState);
        user = getArguments().getParcelable("user");
        Context context = getActivity();
        ResponseHandler responseHandler;


        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.create_room, container, false);
        editTextNewRoomTitle = (EditText) view.findViewById(R.id.editTextNewRoomTitle);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mapToday);
        mapFragment.getMapAsync(this);
        createLocationRequest();

        return view;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {

        Log.i("fg", "onViewCreated");
        googleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


    }

    @Override
    public void onStart() {
        Log.i("fg", "onStart");
        super.onStart();
        googleApiClient.connect();


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i("fg", "onMapReady");
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // Add a marker in Sydney and move the camera
       /* LatLng india = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(india).title("Marker in India"));
       if (mMap != null){
            Marker hamburg = mMap.addMarker(new MarkerOptions().position(new LatLng(india.latitude, india.longitude))
                    .title("Hello Maps"));*/

        //    }
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(india));
        mMap.setOnMarkerDragListener(this);
        mMap.setOnMapLongClickListener(this);


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getCurrentLocation();
    }


    @Override
    public void onClick(View v) {

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


    private void getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        if(location == null){
            startLocationUpdates();
        }
        if (location != null) {

            longitude = location.getLongitude();
            latitude = location.getLatitude();

            moveMap();
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        googleApiClient.disconnect();


    }

    private void moveMap() {

        LatLng latLng = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(true)
                .title("Мы знаем, где ты :)"));
        markersCount++;


        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }


    @Override
    public void onLocationChanged(Location location) {

    }

    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(1000);

    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mMap.clear();
                markersCount = 0;
                moveMap();
            }
        });
    }
}
