package com.example.transitaccess.ui.rta;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.transitaccess.MainActivity;
import com.example.transitaccess.R;
import com.example.transitaccess.ui.ArrivalObj;
import com.example.transitaccess.ui.StopObj;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RouteArrivalsDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RouteArrivalsDetailFragment extends Fragment implements RouteArrivalsDetailRecyclerViewAdapter.ItemClickListener {
    private String routeId;
    private String routeName;
    private HashMap<String, ArrayList<String>> arrivals;
    private HashMap<String, StopObj> stopObjs;
    private ArrayList<ArrivalObj> listOfArrivals;

    private RouteArrivalsDetailRecyclerViewAdapter recyclerViewAdapter;

    public RouteArrivalsDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param routeId ID of route of which this fragment displays the schedules.
     * @return A new instance of fragment RouteScheduleFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RouteArrivalsDetailFragment newInstance(String routeId, String routeName,
                                                          HashMap<String, ArrayList<String>> arriv,
                                                          HashMap<String, StopObj> stopObjsMaster) {
        RouteArrivalsDetailFragment fragment = new RouteArrivalsDetailFragment();
        Bundle args = new Bundle();
        args.putString("routeId", routeId);
        args.putString("routeName", routeName);
        for (String k : arriv.keySet()) {
            args.putStringArrayList(k, arriv.get(k));
        }
        String[] stopIds = arriv.keySet().toArray(new String[0]);
        String[] stopNames = new String[stopIds.length];
        String[] stopLats = new String[stopIds.length];
        String[] stopLngs = new String[stopIds.length];
        for (int i = 0; i < stopIds.length; i++) {
            StopObj stopObj = stopObjsMaster.get(stopIds[i]);
            stopNames[i] = stopObj.getStopName();
            stopLats[i] = stopObj.getLat();
            stopLngs[i] = stopObj.getLng();
        }
        args.putStringArray("stopIds", stopIds);
        args.putStringArray("stopNames", stopNames);
        args.putStringArray("stopLats", stopLats);
        args.putStringArray("stopLngs", stopLngs);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        arrivals = new HashMap<String, ArrayList<String>>();
        stopObjs = new HashMap<String, StopObj>();
        listOfArrivals = new ArrayList<ArrivalObj>();
        if (getArguments() != null) {
            Bundle b = getArguments();
            routeId = b.getString("routeId");
            routeName = b.getString("routeName");
            String[] stopIdsArr = b.getStringArray("stopIds");
            String[] stopNamesArr = b.getStringArray("stopNames");
            String[] stopLatsArr = b.getStringArray("stopLats");
            String[] stopLngsArr = b.getStringArray("stopLngs");
            for (int i = 0; i < stopIdsArr.length; i++) {
                arrivals.put(stopIdsArr[i], b.getStringArrayList(stopIdsArr[i]));
                stopObjs.put(stopIdsArr[i], new StopObj(stopIdsArr[i], stopNamesArr[i], stopLatsArr[i], stopLngsArr[i]));
            }
        }

        for (String stopId : arrivals.keySet()) {
            ArrivalObj ao = new ArrivalObj(routeId,
                    routeName,
                    stopObjs.get(stopId),
                    arrivals.get(stopId));
            listOfArrivals.add(ao);
        }
        Collections.sort(listOfArrivals);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_route_arrivals_detail, container, false);

        // set up the RecyclerView
        RecyclerView recyclerView = root.findViewById(R.id.recyclerview_arrivals_detail);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewAdapter = new RouteArrivalsDetailRecyclerViewAdapter(getActivity(), listOfArrivals);
        recyclerViewAdapter.setClickListener(this);
        recyclerView.setAdapter(recyclerViewAdapter);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(mDividerItemDecoration);

        return root;
    }

    @Override
    public void onItemClick(View view, int position) {
        ArrivalObj ao = recyclerViewAdapter.getItem(position);
        StopObj so = ao.getStopObj();
        StopDetailFragment stopDetailFragment = StopDetailFragment.newInstance(so, routeName, ao.getArrivalsText());
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ((MainActivity) getActivity()).setActionBarTitle(so.getStopName() + " Stop");
        ft.addToBackStack(null);
        ft.replace(R.id.nav_host_fragment, stopDetailFragment);
        ft.commit();
    }
}
