package com.example.uncolor.aroundme;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
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
    ProgressBar progressBar;
    User user;
    AsyncHttpClient client = new AsyncHttpClient();
    ArrayList<Room> roomsList = new ArrayList<Room>();
    ListViewRoomsAdapter listViewRoomsAdapter;
    GoogleApiClient googleApiClient;
    LocationRequest locationRequest;
    SwipeRefreshLayout swipeRefreshLayout;
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
        progressBar = (ProgressBar) view.findViewById(R.id.progressBarRooms);
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setIndeterminate(true);
            progressBar.getIndeterminateDrawable().setColorFilter(0xFFA20022, PorterDuff.Mode.MULTIPLY);
        }

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadRooms(latitude, longitude);
                listViewRoomsAdapter.notifyDataSetChanged();
            }
        });
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
        SharedPreferences sharedPref = getActivity().getSharedPreferences("com.example.aroundme.KEYS", Context.MODE_PRIVATE);
        boolean showNews = sharedPref.getBoolean(getString(R.string.showNews), false);
        String URL = new String("http://aroundme.lwts.ru/getrooms?");
        RequestParams params = new RequestParams();
        params.put("token", user.getToken());
        params.put("user_id", user.getUser_id());
        params.put("latitude", Double.toString(latitude));
        params.put("longitude", Double.toString(longitude));
        params.put("offset", "0");
        params.put("limit", "100");
        params.put("shownews", Boolean.toString(showNews));

        Log.i("fg", "Params: " + params.toString());


        client.get(URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Log.i("fg", "rooms" + response.toString());
                    String status = response.getString("status");
                    if (Objects.equals(status, STATUS_FAIL)) {
                        Toast.makeText(getActivity(), "Ошибка", Toast.LENGTH_SHORT).show();
                        if (swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }

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
                                if (Objects.equals(data.getString("inFavs"), "1")) {
                                    room.setInFavs(true);
                                } else {
                                    room.setInFavs(false);
                                }

                                if (Objects.equals(data.getString("isAdmin"), "1")) {
                                    room.setAdmin(true);
                                } else {
                                    room.setAdmin(false);
                                }
                                double roomLatitude = 0.0;
                                double roomLongitude = 0.0;
                                if (data.has("latitude") && data.has("longitude")) {

                                    Log.i("fg", "was here!!!");

                                    roomLatitude = data.getDouble("latitude");
                                    roomLongitude = data.getDouble("longitude");
                                    Log.i("fg", Double.toString(roomLatitude));
                                    Log.i("fg", Double.toString(roomLongitude));
                                    room.setLatitude(roomLatitude);
                                    room.setLongitude(roomLongitude);
                                    room.setRadius(data.getInt("radius"));
                                    room.setMeters(data.getInt("meters"));
                                    Log.i("fg", "room radius: " + Integer.toString(room.getRadius()));

                                }
                                roomsList.add(room);
                            }
                            listViewRoomsAdapter.notifyDataSetChanged();
                            progressBar.setVisibility(View.INVISIBLE);
                            if (swipeRefreshLayout.isRefreshing()) {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(getActivity(), "Ошибка", Toast.LENGTH_SHORT).show();
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }

            }
        });


        listViewRooms.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), Dialog.class);
                intent.putExtra("user", user);
                intent.putExtra("room", roomsList.get(position));
                intent.putExtra("room_id", roomsList.get(position).getRoom_id());
                intent.putExtra("room_name", roomsList.get(position).getTitle());
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
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
    public void onLocationChanged(Location location) {
        Log.i("fg", "onLocationChanged " + Double.toString(location.getLatitude()) + " " + Double.toString(location.getLongitude()));
        listViewRoomsAdapter.notifyDataSetChanged();
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        if (listViewRoomsAdapter.getCount() == 0 && location.getLatitude() != 0.0 && location.getLongitude() != 0.0) {
            roomsList.clear();
            progressBar.setVisibility(View.VISIBLE);
            loadRooms(location.getLatitude(), location.getLongitude());
        }
        progressBar.setVisibility(View.INVISIBLE);
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

    @Override
    public void onResume() {
        super.onResume();
        Log.i("fg", "rooms onResume");
        roomsList.clear();
        SharedPreferences sharedPref = getActivity().getSharedPreferences("com.example.aroundme.KEYS", Context.MODE_PRIVATE);
        user.setAvatar_url(sharedPref.getString(getString(R.string.avatar_url), ""));
        listViewRoomsAdapter.notifyDataSetChanged();
        progressBar.setVisibility(View.VISIBLE);
    }
}
