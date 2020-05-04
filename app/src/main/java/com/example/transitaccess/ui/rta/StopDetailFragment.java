package com.example.transitaccess.ui.rta;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.transitaccess.R;
import com.example.transitaccess.ui.StopObj;
import com.google.android.gms.maps.model.LatLng;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StopDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StopDetailFragment extends Fragment {
    private static final String GOOGLE_DIRECTIONS_URL = "https://www.google.com/maps/dir/?api=1";
    public final static String GOOGLE_API_KEY = "AIzaSyCc38lUBuQrj0UGehy4W0qbXA22yKn0sks";


    private String stopId;
    private String stopName;
    private String stopLat;
    private String stopLng;
    private String routeName;
    private String arrivalsText;

    public StopDetailFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static StopDetailFragment newInstance(StopObj stopObj, String routeName, String arrivalsText) {
        StopDetailFragment fragment = new StopDetailFragment();
        Bundle args = new Bundle();
        args.putString("stopId", stopObj.getStopId());
        args.putString("stopName", stopObj.getStopName());
        args.putString("stopLat", stopObj.getLat());
        args.putString("stopLng", stopObj.getLng());
        args.putString("routeName", routeName);
        args.putString("arrivalsText", arrivalsText);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            Bundle b = getArguments();
            stopId = b.getString("stopId");
            stopName = b.getString("stopName");
            stopLat = b.getString("stopLat");
            stopLng = b.getString("stopLng");
            routeName = b.getString("routeName");
            arrivalsText = b.getString("arrivalsText");
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_stop_detail, container, false);
        TextView stopNameTextView = root.findViewById(R.id.stop_name);
        stopNameTextView.setText(stopName);
        TextView stopArrivalsTextView = root.findViewById(R.id.stop_arrival_info);
        stopArrivalsTextView.setText(arrivalsText);

        root.findViewById(R.id.btn_get_directions).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(createDirectionsQueryURL()));
                startActivity(i);
            }
        });

        root.findViewById(R.id.btn_open_streetview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double lat = Double.parseDouble(stopLat);
                double lng = Double.parseDouble(stopLng);
                LatLng latLng = new LatLng(lat, lng);
                StreetViewFragment streetViewFragment = StreetViewFragment.newInstance(latLng);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.addToBackStack(null);
                ft.replace(R.id.nav_host_fragment, streetViewFragment);
                ft.commit();
            }
        });


        return root;
    }

    private String createDirectionsQueryURL() {
        StringBuilder sb = new StringBuilder();
        sb.append(GOOGLE_DIRECTIONS_URL);
        sb.append("&destination=");
        sb.append(stopLat);
        sb.append(",");
        sb.append(stopLng);
        sb.append("&travelmode=walking");
        return sb.toString();
    }

}
