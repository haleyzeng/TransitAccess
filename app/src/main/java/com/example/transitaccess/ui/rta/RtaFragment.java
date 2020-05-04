package com.example.transitaccess.ui.rta;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.transitaccess.MainActivity;
import com.example.transitaccess.R;
import com.example.transitaccess.ui.RouteDesc;
import com.example.transitaccess.ui.StopObj;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class RtaFragment extends Fragment implements com.example.transitaccess.ui.rta.RtaRecyclerViewAdapter.ItemClickListener {

    private RtaRecyclerViewAdapter rtaRecyclerViewAdapter;

    private final static String TRANSLOC_API_URL = "https://transloc-api-1-2.p.rapidapi.com";
    // Princeton 84
    // Rutgers 1323
    private final static String AGENCY_ID = "84";
    private final static String TRANSLOC_API_KEY = "0476f93b6amsh40e1f360456a6a1p148f14jsnbce7351ebb24";
    private OkHttpClient httpClient;

    private Handler refreshHandler;

    // maps route ID to route name
    private HashMap<String, String> routeNames;
    // maps stop ID to stop name
    private HashMap<String, String> stopNames;
    // maps route ID to array of stop IDs
    private HashMap<String, String[]> routeStops;
    private HashMap<String, RouteDesc> routeDescHM;
    // data for RecyclerView
    private ArrayList<RouteDesc> listOfRouteDesc;

    private HashMap<String, StopObj> stopObjs;

    // {routeId : {stopId: [arrival, times]}}
    private HashMap<String, HashMap<String, ArrayList<String>>> arrivalData;

    private boolean gotRoutes;
    private boolean gotStops;
    private boolean gotCurrentlyRunning;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_rta, container, false);

        httpClient = new OkHttpClient();
        routeNames = new HashMap<String, String>();
        stopNames = new HashMap<String, String>();
        routeStops = new HashMap<String, String[]>();
        routeDescHM = new HashMap<String, RouteDesc>();
        listOfRouteDesc = new ArrayList<RouteDesc>();
        stopObjs = new HashMap<String, StopObj>();
        arrivalData = new HashMap<String, HashMap<String, ArrayList<String>>>();

        // set up the RecyclerView
        RecyclerView recyclerView = root.findViewById(R.id.recyclerview_rta);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        rtaRecyclerViewAdapter = new RtaRecyclerViewAdapter(getActivity(), listOfRouteDesc);
        rtaRecyclerViewAdapter.setClickListener(this);
        recyclerView.setAdapter(rtaRecyclerViewAdapter);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(mDividerItemDecoration);

        fetchRouteStopData();
        fetchCurrentlyRunning();

        refreshHandler = new Handler();
        Runnable refreshCode = new Runnable() {
            @Override
            public void run() {
                fetchCurrentlyRunning();
                refreshHandler.postDelayed(this, 60000);
            }
        };
        refreshHandler.postDelayed(refreshCode, 60000);
        return root;
    }


    private void fetchRouteStopData() {
        FileInputStream fis = null;
        try {
            fis = getActivity().getApplicationContext().openFileInput("routestopdata");
            ObjectInputStream is = null;
            is = new ObjectInputStream(fis);
            Object o = is.readObject();
            while (o != null) {
                RouteDesc rd = (RouteDesc) o;
                routeDescHM.put(rd.getRouteId(), rd);
                o = is.readObject();
            }
            gotRoutes = true;
            gotStops = true;
            updateListOfStops();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            fetchRouteStopDataFromServer();
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
                        JSONArray routes = data.getJSONArray(AGENCY_ID);
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
                            JSONObject location = stop.getJSONObject("location");
                            String lat = location.getString("lat");
                            String lng = location.getString("lng");
                            stopNames.put(stopId, stopName);
                            StopObj stopObj = new StopObj(stopId, stopName, lat, lng);
                            stopObjs.put(stopId, stopObj);
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

    private void fetchCurrentlyRunning() {
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
                        JSONArray data = d.getJSONArray("data");
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject stopArrivals = data.getJSONObject(i);
                            String stopId = stopArrivals.getString("stop_id");
                            JSONArray arrivals = stopArrivals.getJSONArray("arrivals");
                            for (int j = 0; j < arrivals.length(); j++) {
                                JSONObject busInfo = arrivals.getJSONObject(j);
                                String routeId = busInfo.getString("route_id");
                                String arrivalTime = busInfo.getString("arrival_at");

                                if (!arrivalData.containsKey(routeId)) {
                                    arrivalData.put(routeId, new HashMap<String, ArrayList<String>>());
                                }
                                if (!arrivalData.get(routeId).containsKey(stopId)) {
                                    arrivalData.get(routeId).put(stopId, new ArrayList<String>());
                                }
                                arrivalData.get(routeId).get(stopId).add(arrivalTime);
                            }
                        }
                        gotCurrentlyRunning = true;
                        updateListOfStops();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        getTransLocData("arrival-estimates", callback);
    }

    private void getTransLocData(String type, Callback callback) {
        String url = String.format("%s/%s.json?agencies=%s", TRANSLOC_API_URL, type, AGENCY_ID);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("x-rapidapi-host", "transloc-api-1-2.p.rapidapi.com")
                .addHeader("x-rapidapi-key", TRANSLOC_API_KEY)
                .build();

        httpClient.newCall(request).enqueue(callback);
    }
    private void updateListOfStops() {
        if (! gotRoutes || ! gotStops || ! gotCurrentlyRunning)
            return;

        listOfRouteDesc.clear();

        for (String routeId : arrivalData.keySet()) {
            if (routeDescHM.containsKey(routeId)) {
                listOfRouteDesc.add(routeDescHM.get(routeId));
            } else {
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
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                rtaRecyclerViewAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        RouteDesc rd = rtaRecyclerViewAdapter.getItem(position);
        String routeId = rd.getRouteId();
        String routeName = rd.getName();
        HashMap<String, ArrayList<String>> arriv = arrivalData.get(routeId);

        RouteArrivalsDetailFragment routeArrivalsDetailFragment = RouteArrivalsDetailFragment.newInstance(routeId, routeName, arriv, stopObjs);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ((MainActivity) getActivity()).setActionBarTitle(routeName + " Arrivals");
        ft.addToBackStack(null);
        ft.replace(R.id.nav_host_fragment, routeArrivalsDetailFragment);
        ft.commit();
    }
}