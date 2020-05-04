package com.example.transitaccess;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.transitaccess.ui.RouteDesc;
import com.example.transitaccess.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {
    private final static String TRANSLOC_API_URL = "https://transloc-api-1-2.p.rapidapi.com";
    private final static String TIGER_TRANSIT_AGENCY_ID = "84";
    private final static String TRANSLOC_API_KEY = "0476f93b6amsh40e1f360456a6a1p148f14jsnbce7351ebb24";
    private OkHttpClient httpClient;

    // maps route ID to route name
    private HashMap<String, String> routeNames;
    // maps stop ID to stop name
    private HashMap<String, String> stopNames;
    // maps route ID to array of stop IDs
    private HashMap<String, String[]> routeStops;
    // data for RecyclerView
    private ArrayList<RouteDesc> listOfRouteDesc;

    private boolean gotRoutes;
    private boolean gotStops;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_schedules, R.id.navigation_rta)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        requestPermissions(new String[]{"WRITE_EXTERNAL_STORAGE","READ_EXTERNAL_STORAGE"}, 1);

//        routeNames = new HashMap<String, String>();
//        stopNames = new HashMap<String, String>();
//        routeStops = new HashMap<String, String[]>();
//        listOfRouteDesc = new ArrayList<RouteDesc>();
//        httpClient = new OkHttpClient();
//
//        boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
//                .getBoolean("isFirstRun", true);
//
//
//        if (isFirstRun) {
//            fetchRouteStopDataFromServer();
//
//            getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
//                    .putBoolean("isFirstRun", false).apply();
//        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    private void fetchRouteStopDataFromStorage() {
        FileInputStream fis = null;
        try {
            fis = getApplicationContext().openFileInput("routestopdata");
            ObjectInputStream is = null;
            is = new ObjectInputStream(fis);
            Object o = is.readObject();
            while (o != null) {
                listOfRouteDesc.add((RouteDesc) o);
                o = is.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void fetchRouteStopDataFromServer() {
        getRoutesData();
        getStopsData();
    }


    private void getRoutesData() {
        Callback callback = new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                    String jsonString = responseBody.string();
                    try {
                        JSONObject d = new JSONObject(jsonString);
                        JSONObject data = d.getJSONObject("data");
                        JSONArray routes = data.getJSONArray(TIGER_TRANSIT_AGENCY_ID);
                        for (int i = 0; i < routes.length(); i++) {
                            JSONObject route = routes.getJSONObject(i);
                            String routeId = route.getString("route_id");
                            String routeName = route.getString("long_name");
                            JSONArray stops = route.getJSONArray("stops");
                            String[] stopsArray = new String[stops.length()];
                            for (int j = 0; j < stops.length(); j++) {
                                stopsArray[j] = stops.getString(j);
                            }

                            routeNames.put(routeId, routeName);
                            routeStops.put(routeId, stopsArray);
                        }
                        gotRoutes = true;
                        updateListOfStops();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        getTransLocData("routes", callback);
    }

    private void getStopsData() {
        Callback callback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful())
                        throw new IOException("Unexpected code " + response);
                    String jsonString = responseBody.string();
                    Log.d("test", jsonString);
                    try {
                        JSONObject data = new JSONObject(jsonString);
                        JSONArray stops = data.getJSONArray("data");
                        for (int i = 0; i < stops.length(); i++) {
                            JSONObject stop = stops.getJSONObject(i);
                            String stopId = stop.getString("stop_id");
                            String stopName = stop.getString("name");
                            stopNames.put(stopId, stopName);
                        }
                        gotStops = true;
                        updateListOfStops();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        getTransLocData("stops", callback);
    }

    private void getTransLocData(String type, Callback callback) {
        String url = String.format("%s/%s.json?agencies=%s", TRANSLOC_API_URL, type, TIGER_TRANSIT_AGENCY_ID);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("x-rapidapi-host", "transloc-api-1-2.p.rapidapi.com")
                .addHeader("x-rapidapi-key", TRANSLOC_API_KEY)
                .build();

        httpClient.newCall(request).enqueue(callback);
    }
    private void updateListOfStops() {
        if (! gotRoutes || ! gotStops)
            return;

        listOfRouteDesc.clear();

        for (String routeId : routeNames.keySet()) {
            String name = routeNames.get(routeId);
            if (routeStops.containsKey(routeId)) {
                String[] stopIdsArr = routeStops.get(routeId);

                String[] stopNamesArr = new String[stopIdsArr.length];
                for (int i = 0; i < stopIdsArr.length; i++) {

                    if (stopNames.containsKey(stopIdsArr[i])) {
                        stopNamesArr[i] = stopNames.get(stopIdsArr[i]);
                    } else {
                        stopNamesArr[i] = String.format("Stop %s (unknown name)", stopIdsArr[i]);
                    }
                }

                RouteDesc rd = new RouteDesc(routeId, name, "", stopNamesArr);
                listOfRouteDesc.add(rd);
            }
        }
        saveRouteStopDataToStorage();
    }

    public void saveRouteStopDataToStorage() {
        FileOutputStream fos = null;
        try {
            fos = getApplicationContext().openFileOutput("routestopdata", Context.MODE_PRIVATE);
            ObjectOutputStream os = null;
            os = new ObjectOutputStream(fos);
            for (RouteDesc rd : listOfRouteDesc) {
                os.writeObject(rd);
            }
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }
}
