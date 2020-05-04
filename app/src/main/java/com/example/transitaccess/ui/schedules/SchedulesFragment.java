package com.example.transitaccess.ui.schedules;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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
import com.example.transitaccess.ui.RouteScheduleObj;
import com.example.transitaccess.ui.RouteType;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class SchedulesFragment extends Fragment implements SchedulesRecyclerViewAdapter.ItemClickListener {

    private static final int PICKFILE_RESULT_CODE = 1;
    private SchedulesRecyclerViewAdapter schedulesRecyclerViewAdapter;

    private ArrayList<RouteDesc> listOfRouteDesc;
    private HashMap<String, RouteDesc> routeDescHashMap;
    private HashMap<String, RouteScheduleObj> routeScheduleObjs;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_schedules, container, false);

        Button btnChooseFile = (Button) root.findViewById(R.id.btn_choose_file);
        btnChooseFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                chooseFile.setType("*/*");
                chooseFile = Intent.createChooser(chooseFile, "Choose a file");

                startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);
            }
        });

        routeScheduleObjs = new HashMap<String, RouteScheduleObj>();

        listOfRouteDesc = new ArrayList<RouteDesc>();
        routeDescHashMap = new HashMap<String, RouteDesc>();

        // set up the RecyclerView
        RecyclerView recyclerView = root. findViewById(R.id.recyclerview_schedules);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        schedulesRecyclerViewAdapter = new SchedulesRecyclerViewAdapter(getActivity(), listOfRouteDesc);
        schedulesRecyclerViewAdapter.setClickListener(this);
        recyclerView.setAdapter(schedulesRecyclerViewAdapter);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(mDividerItemDecoration);

        fetchSchedulesData();

        return root;
    }

    private void fetchSchedulesData() {
        FileInputStream fis = null;
        try {
            fis = getActivity().getApplicationContext().openFileInput("schedulefilenames");
            BufferedReader sr = new BufferedReader(new InputStreamReader(fis));
            String filename = null;
            while ((filename = sr.readLine()) != null) {
                try {
                    FileInputStream innerFIS = getActivity().getApplicationContext().openFileInput(filename);
                    parseSchedule(innerFIS, false);
                } catch (FileNotFoundException ignored) {
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveScheduleFileToStorage(ArrayList<String> fileContents) {
        Random r = new Random();
        String filename = "schedule_" + r.nextInt();
        FileOutputStream fos = null;
        try {
            // write 'filename' to file of filenames
            fos = getActivity().getApplicationContext().openFileOutput("schedulefilenames", Context.MODE_APPEND);
            PrintWriter pw = new PrintWriter(fos);
            pw.write(filename);
            pw.write("\n");
            pw.flush();
            pw.close();

            // write schedule to file with name 'filename'
            fos = getActivity().getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE);
            pw = new PrintWriter(fos);
            for (String line : fileContents) {
                pw.write(line);
                pw.write("\n");
            }
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseSchedule(InputStream inputStream, boolean save) {
       ArrayList<String> linesToSave = new ArrayList<String>();
        String line = "";
        String cvsSplitBy = ",";
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String line0 = br.readLine();
            linesToSave.add(line0);
            String line1 = br.readLine();
            linesToSave.add(line1);

            String routeName = line0.split(cvsSplitBy)[0];
            String lineOrLoop = line1.split(cvsSplitBy)[0];
            RouteType routeType = (lineOrLoop.equals("LOOP")) ? RouteType.LOOP : RouteType.LINE;

            RouteScheduleObj rso = new RouteScheduleObj(routeName, routeType);

            String[] dayTypes = null;
            String[] stopsList = null;

            ArrayList<String[]> scheduleLines = new ArrayList<String[]>();
            while ((line = br.readLine()) != null) {
                linesToSave.add(line);
                String[] lineArr = line.split(cvsSplitBy);
                // start of new table
                if (lineArr[0].equals("--")) {
                    // add prev table to rso
                    if (dayTypes != null && stopsList != null && scheduleLines.size() > 0) {
                        String[][] s = new String[0][];
                        String[][] sched = scheduleLines.toArray(s);
                        for (String dayType : dayTypes) {
                            rso.addSchedule(dayType, stopsList, sched, routeType);
                        }
                    }

                    String dayTypesLne = br.readLine();
                    linesToSave.add(dayTypesLne);
                    String stopsListLine = br.readLine();
                    linesToSave.add(stopsListLine);
                    dayTypes = dayTypesLne.split(cvsSplitBy);
                    stopsList = stopsListLine.split(cvsSplitBy);
                    scheduleLines.clear();
                } else {
                    scheduleLines.add(lineArr);
                }
            }

            // add last table to rso
            if (dayTypes != null && stopsList != null && scheduleLines.size() > 0) {
                String[][] s = new String[0][];
                String[][] sched = scheduleLines.toArray(s);
                for (String dayType : dayTypes) {
                    rso.addSchedule(dayType, stopsList, sched, routeType);
                }
            }
            routeScheduleObjs.put(routeName, rso);
            updateList(routeName);

            if (save)
                saveScheduleFileToStorage(linesToSave);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateList(String routeName) {
        if (routeDescHashMap.containsKey(routeName)) {
            listOfRouteDesc.remove(routeDescHashMap.get(routeName));
        }
        RouteScheduleObj rso = routeScheduleObjs.get(routeName);
        RouteDesc rd = new RouteDesc(null, routeName, null, rso.getStopsList());
        listOfRouteDesc.add(rd);
        routeDescHashMap.put(routeName, rd);
        schedulesRecyclerViewAdapter.notifyDataSetChanged();
    }

    // user added a file
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICKFILE_RESULT_CODE) {
            if (resultCode == -1) {
                Uri fileUri = data.getData();
                try {
                    InputStream inputStream = getActivity().getApplicationContext().getContentResolver().openInputStream(fileUri);
                    parseSchedule(inputStream, true);
                } catch (FileNotFoundException e) {
                    // error opening file
                }
            }
        }
    }

    // user clicked on recyclerview list item
    @Override
    public void onItemClick(View view, int position) {
        RouteDesc rd = schedulesRecyclerViewAdapter.getItem(position);
        String routeName = rd.getName();
        RouteScheduleObj rso = routeScheduleObjs.get(routeName);
        SchedulesDetailFragment schedulesDetailFragment = SchedulesDetailFragment.newInstance(rso);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ((MainActivity) getActivity()).setActionBarTitle(rso.getRouteName() + " Schedule");
        ft.addToBackStack(null);
        ft.replace(R.id.nav_host_fragment, schedulesDetailFragment);
        ft.commit();
    }

}