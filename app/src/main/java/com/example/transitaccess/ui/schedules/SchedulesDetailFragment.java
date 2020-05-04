package com.example.transitaccess.ui.schedules;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.transitaccess.R;
import com.example.transitaccess.ui.RouteScheduleObj;
import com.example.transitaccess.ui.ScheduleObj;
import com.example.transitaccess.ui.ScheduleTimes;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SchedulesDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SchedulesDetailFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private com.example.transitaccess.ui.schedules.SchedulesDetailRecyclerViewAdapter recyclerViewAdapter;
    private ArrayList<ScheduleTimes> listOfTimes;
    private RouteScheduleObj rso;
    private HashMap<String, ArrayList<ScheduleObj>> schedules;

    private String currentDaySpinnerValue;
    private String currentDepartSpinnerValue;
    private String currentArriveSpinnerValue;

    public SchedulesDetailFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static SchedulesDetailFragment newInstance(RouteScheduleObj rso) {
        SchedulesDetailFragment fragment = new SchedulesDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("rso", rso);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.rso = (RouteScheduleObj) getArguments().getSerializable("rso");
        }
        listOfTimes = new ArrayList<ScheduleTimes>();
        schedules = rso.getSchedules();
        currentArriveSpinnerValue = "";
        currentDaySpinnerValue = "";
        currentArriveSpinnerValue = "";
    }

    private void putDayOptionsIn() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_schedules_detail, container, false);
        Spinner daySpinner = (Spinner) root.findViewById(R.id.day_spinner);
        ArrayAdapter<String> daySpinnerAdapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(daySpinnerAdapter);
        daySpinner.setOnItemSelectedListener(this);

        HashMap<String, ArrayList<ScheduleObj>> scheds = rso.getSchedules();
        for (String dayType : scheds.keySet()) {
            if (scheds.get(dayType).size() > 0)
                daySpinnerAdapter.add(dayType);
        }


        Spinner departingSpinner = (Spinner) root.findViewById(R.id.depart_spinner);
        ArrayAdapter<String> departingSpinnerAdapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_spinner_dropdown_item);
        departingSpinner.setAdapter(departingSpinnerAdapter);
        departingSpinnerAdapter.addAll(rso.getStopsList());
        departingSpinner.setOnItemSelectedListener(this);

        Spinner arrivingSpinner = (Spinner) root.findViewById(R.id.arrive_spinner);
        ArrayAdapter<String> arrivingSpinnerAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item);
        arrivingSpinner.setAdapter(arrivingSpinnerAdapter);
        arrivingSpinnerAdapter.addAll(rso.getStopsList());
        arrivingSpinner.setOnItemSelectedListener(this);
        // set up the RecyclerView
        RecyclerView recyclerView = root.findViewById(R.id.recyclerview_schedules_detail);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewAdapter = new com.example.transitaccess.ui.schedules.SchedulesDetailRecyclerViewAdapter(getActivity(), listOfTimes);
        recyclerView.setAdapter(recyclerViewAdapter);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(mDividerItemDecoration);

        return root;
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using

        String val = (String) parent.getItemAtPosition(pos);
        switch (parent.getId()) {
            case R.id.day_spinner:
                currentDaySpinnerValue = val;
                break;
            case R.id.depart_spinner:
                currentDepartSpinnerValue = val;
                setDepartName(val);
                break;
            case R.id.arrive_spinner:
                currentArriveSpinnerValue = val;
                setArriveName(val);
                break;
        }

        listOfTimes.clear();
        listOfTimes.addAll(rso.getSchedulesFor(currentDaySpinnerValue, currentDepartSpinnerValue, currentArriveSpinnerValue));

        recyclerViewAdapter.notifyDataSetChanged();
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    private void setDepartName(String text) {
        TextView departTextView = (TextView) getActivity().findViewById(R.id.depart_name);
        departTextView.setText("FROM\n" + text);
    }

    private void setArriveName(String text) {
        TextView arriveTextView = (TextView) getActivity().findViewById(R.id.arrive_name);
        arriveTextView.setText("TO\n" + text);
    }

}

