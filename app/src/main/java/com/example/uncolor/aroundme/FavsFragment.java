package com.example.uncolor.aroundme;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
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


public class FavsFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {


    private static final String STATUS_FAIL = "failed";
    private static final String STATUS_SUCCESS = "success";

    double latitude;
    double longitude;

    ListView listView;
    ListViewRoomsAdapter listViewFavsAdapter;
    User user;
    LocationRequest locationRequest;
    GoogleApiClient googleApiClient;
    AsyncHttpClient client = new AsyncHttpClient();
    ArrayList<Room> favsList = new ArrayList<Room>();


    public FavsFragment() {

    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPref = getActivity().getSharedPreferences("com.example.aroundme.KEYS", Context.MODE_PRIVATE);
        user.setAvatar_url(sharedPref.getString(getString(R.string.avatar_url), ""));
    }


    public static FavsFragment newInstance() {
        FavsFragment fragment = new FavsFragment();
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
        View view = inflater.inflate(R.layout.fragment_favs, container, false);
        listView = (ListView) view.findViewById(R.id.listViewFavs);
        listViewFavsAdapter = new ListViewRoomsAdapter(getActivity(), favsList);
        listView.setAdapter(listViewFavsAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), Dialog.class);
                intent.putExtra("user", user);
                intent.putExtra("room", favsList.get(position));
                startActivity(intent);
            }
        });
        googleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();

        return view;
    }

    public void loadFavs(final double latitude, final double longitude) {

        Log.i("fg", "in load favs " + Double.toString(longitude));

        String URL = new String("http://aroundme.lwts.ru/getFavs?");
        RequestParams params = new RequestParams();
        params.put("token", user.getToken());
        params.put("user_id", user.getUser_id());
        params.put("latitude", Double.toString(latitude));
        params.put("longitude", Double.toString(longitude));
        params.put("offset", "0");
        params.put("limit", "100");


        client.get(URL, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.i("fg", "favs response " + response.toString());
                try {
                    String status = response.getString("status");
                    if (Objects.equals(status, STATUS_FAIL)) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle(getString(R.string.error));
                        builder.setMessage(getString(R.string.load_rooms_failed));
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

                        favsList.clear();
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
                                double roomLatitude;
                                double roomLongitude;
                                if (data.has("latitude") && data.has("longitude")) {

                                    roomLatitude = data.getDouble("latitude");
                                    roomLongitude = data.getDouble("longitude");
                                    room.setLatitude(roomLatitude);
                                    room.setLongitude(roomLongitude);
                                    room.setMeters(data.getInt("meters"));

                                }
                                favsList.add(room);
                            }
                            listViewFavsAdapter.notifyDataSetChanged();
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
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getCurrentLocation();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i("fg", "onLocationChanged on favs" + Double.toString(location.getLatitude()) + " " + Double.toString(location.getLongitude()));
        listViewFavsAdapter.notifyDataSetChanged();
        Log.i("fg", "listViiewAdapterCount  " + Integer.toString(listViewFavsAdapter.getCount()));
        if (listViewFavsAdapter.isEmpty() && location.getLatitude() != 0.0 && location.getLongitude() != 0.0) {

              googleApiClient.disconnect();
              googleApiClient.connect();
            Log.i("fg", "Updated");
        }
        Log.i("fg", "favs list " + Integer.toString(favsList.size()));
    }

    private void getCurrentLocation() {

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
            Log.i("fg", "get Current location on favs" + Double.toString(latitude) + " " + Double.toString(longitude));
            favsList.clear();
            loadFavs(latitude, longitude);


        }
    }

    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5);
        locationRequest.setFastestInterval(10);

    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onStart() {
        Log.i("fg", "onStart");
        super.onStart();
        googleApiClient.connect();
        Log.i("fg", "onStart " + Double.toString(latitude) + " " + Double.toString(longitude));
    }

    @Override
    public void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }

}



