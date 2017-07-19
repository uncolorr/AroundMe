package com.example.uncolor.aroundme;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
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
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;


public class RoomsFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private static final String STATUS_FAIL = "failed";
    private static final String STATUS_SUCCESS = "success";

    ListView listViewRooms;
    User user;
    AsyncHttpClient client = new AsyncHttpClient();
    ArrayList<Room> roomsList = new ArrayList<Room>();
    ListViewRoomsAdapter listViewRoomsAdapter;
    GoogleApiClient googleApiClient;
    LocationRequest locationRequest;
    double latitude;
    double longitude;


    public RoomsFragment() {

    }

    public static RoomsFragment newInstance() {
        RoomsFragment fragment = new RoomsFragment();
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
        View view = inflater.inflate(R.layout.fragment_rooms, container, false);
        listViewRooms = (ListView) view.findViewById(R.id.listViewRooms);
        listViewRoomsAdapter = new ListViewRoomsAdapter(getActivity(), roomsList);
        listViewRooms.setAdapter(listViewRoomsAdapter);
        googleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
        return view;
    }

    private void loadRooms(final double latitude, final double longitude) {


        Log.i("fg", "in load rooms " + Double.toString(latitude) + Double.toString(longitude));
        String URL = new String("http://aroundme.lwts.ru/getrooms?");
        RequestParams params = new RequestParams();
        params.put("token", user.getToken());
        params.put("user_id", user.getUser_id());
        params.put("latitude", Double.toString(latitude));
        params.put("longitude", Double.toString(longitude));
        params.put("offset", "0");
        params.put("limit", "100");
        params.put("shownews", "1");


        client.get(URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Log.i("fg", "rooms" + response.toString());
                    String status = response.getString("status");
                    if (Objects.equals(status, STATUS_FAIL)) {


                    } else if (Objects.equals(status, STATUS_SUCCESS)) {

                        roomsList.clear();
                        if (response.has("response")) {
                            JSONArray responseArray = response.getJSONArray("response");
                            for (int i = 0; i < responseArray.length(); i++) {

                                Room room = new Room();
                                JSONObject data = responseArray.getJSONObject(i);
                                if (data.has("title")) {
                                    room.setTitle(data.getString("title"));
                                }
                                room.setUsersCount(data.getString("usersCount"));
                                room.setRoom_id(data.getString("room_id"));
                                double roomLatitude = data.getDouble("latitude");
                                double roomLongitude = data.getDouble("longitude");
                                float[] distance = new float[1];
                                Location.distanceBetween(latitude, longitude, roomLatitude, roomLongitude, distance);
                                room.setDistance(distance[0]);
                                roomsList.add(room);
                            }
                            listViewRoomsAdapter.notifyDataSetChanged();

                        }


                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {

            }
        });


        listViewRooms.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), Dialog.class);
                intent.putExtra("user", user);
                intent.putExtra("room_id", roomsList.get(position).getRoom_id());
                intent.putExtra("room_name", roomsList.get(position).getTitle());
                startActivity(intent);
            }
        });

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
    public void onLocationChanged(Location location) {///
        Log.i("fg", "onLocationChanged " + Double.toString(location.getLatitude()) + " " + Double.toString(location.getLongitude()));
        listViewRoomsAdapter.notifyDataSetChanged();
        Log.i("fg", "listViiewAdapterCount  " + Integer.toString(listViewRoomsAdapter.getCount()));
        if (listViewRoomsAdapter.isEmpty() && location.getLatitude() != 0.0 && location.getLongitude() != 0.0) {
            roomsList.clear();
            googleApiClient.disconnect();
            googleApiClient.connect();
            Log.i("fg", "Updated");
        }
        Log.i("fg", "roooms list " + Integer.toString(roomsList.size()));
    }

    private void getCurrentLocation() {

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
            roomsList.clear();
            loadRooms(latitude, longitude);

        }
    }

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

    @Override
    public void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }

}
