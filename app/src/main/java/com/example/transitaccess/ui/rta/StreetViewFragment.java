package com.example.transitaccess.ui.rta;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;

import com.example.transitaccess.R;
import com.google.android.gms.maps.StreetViewPanoramaOptions;
import com.google.android.gms.maps.StreetViewPanoramaView;
import com.google.android.gms.maps.model.LatLng;

public class StreetViewFragment extends Fragment {
    private static final String STREETVIEW_BUNDLE_KEY = "StreetViewBundleKey";

    private LatLng latLng;
    private StreetViewPanoramaView mStreetViewPanoramaView;


    public StreetViewFragment() {
        // Required empty public constructor
    }


    public static StreetViewFragment newInstance(LatLng latLng) {
        StreetViewFragment fragment = new StreetViewFragment();
        Bundle args = new Bundle();
        args.putDouble("lat", latLng.latitude);
        args.putDouble("lng", latLng.longitude);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle b = getArguments();
            this.latLng = new LatLng(b.getDouble("lat"), b.getDouble("lng"));
        }
        StreetViewPanoramaOptions options = new StreetViewPanoramaOptions();
        if (savedInstanceState == null) {
            options.position(latLng);
        }


        mStreetViewPanoramaView = new StreetViewPanoramaView(getActivity(), options);
        getActivity().addContentView(mStreetViewPanoramaView,
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        // *** IMPORTANT ***
        // StreetViewPanoramaView requires that the Bundle you pass contain _ONLY_
        // StreetViewPanoramaView SDK objects or sub-Bundles.
        Bundle mStreetViewBundle = null;
        if (savedInstanceState != null) {
            mStreetViewBundle = savedInstanceState.getBundle(STREETVIEW_BUNDLE_KEY);
        }
        mStreetViewPanoramaView.onCreate(mStreetViewBundle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_street_view, container, false);
    }

    @Override
    public void onResume() {
        mStreetViewPanoramaView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        mStreetViewPanoramaView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mStreetViewPanoramaView.onDestroy();
        super.onDestroy();
        ((ViewGroup) mStreetViewPanoramaView.getParent()).removeView(mStreetViewPanoramaView);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mStreetViewBundle = outState.getBundle(STREETVIEW_BUNDLE_KEY);
        if (mStreetViewBundle == null) {
            mStreetViewBundle = new Bundle();
            outState.putBundle(STREETVIEW_BUNDLE_KEY, mStreetViewBundle);
        }

        mStreetViewPanoramaView.onSaveInstanceState(mStreetViewBundle);
    }


}
