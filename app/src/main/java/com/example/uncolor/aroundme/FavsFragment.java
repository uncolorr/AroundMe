package com.example.uncolor.aroundme;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
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
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;


public class FavsFragment extends Fragment implements  GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {


    private static final String STATUS_FAIL = "failed";
    private static final String STATUS_SUCCESS = "success";

    double latitude;
    double longitude;

    ListView listView;
    ListViewRoomsAdapter listViewRoomsAdapter;
    User user;
    LocationRequest locationRequest;
    GoogleApiClient googleApiClient;
    AsyncHttpClient client = new AsyncHttpClient();
    ArrayList<Room> favsList = new ArrayList<Room>();


    public FavsFragment() {

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
        listView = (ListView)view.findViewById(R.id.listViewFavs);
        listViewRoomsAdapter = new ListViewRoomsAdapter(getActivity(),favsList);
        listView.setAdapter(listViewRoomsAdapter);
        googleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();



        return view;
    }

    public void loadFavs(double latitude, double longitude) {

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
                Log.i("fg" ,response.toString());
                try {
                    String status = response.getString("status");
                    if (Objects.equals(status, STATUS_FAIL)) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("Ошибка");
                        builder.setMessage("Не удалось загрузить список комнат");
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

                        JSONArray responseArray = response.getJSONArray("response");
                        for (int i = 0; i < responseArray.length(); i++) {

                            Room room = new Room();
                            JSONObject data = responseArray.getJSONObject(i);
                            if (data.has("title")) {
                                room.setTitle(data.getString("title"));
                            }
                            room.setUsersCount(data.getString("usersCount"));
                            room.setRoom_id(data.getString("room_id"));
                            favsList.add(room);


                        }

                        listViewRoomsAdapter.notifyDataSetChanged();


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
            Log.i("fg","get Current location " + Double.toString(latitude) + " " + Double.toString(longitude));
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
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.i("fg", "onLocationChanged " + Double.toString(location.getLatitude()) + " " + Double.toString(location.getLongitude()));

            }
        });
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



