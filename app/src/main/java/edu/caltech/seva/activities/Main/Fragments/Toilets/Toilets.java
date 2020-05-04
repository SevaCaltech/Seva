package edu.caltech.seva.activities.Main.Fragments.Toilets;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import edu.caltech.seva.R;
import edu.caltech.seva.activities.Main.MainActivity;
import edu.caltech.seva.helpers.PrefManager;
import edu.caltech.seva.models.Toilet;

/**
 * Represents the Toilets fragment that is the which displays a map interface showing the user's
 * assigned toilets locations, information, as well as status.
 */
public class Toilets extends Fragment implements ToiletsContract.View {
    //utility helpers
    private PrefManager prefManager;
    private GoogleMap googleMap;

    //ui elements
    private RelativeLayout infoCard;
    private MapView mMapView;
    private Marker prevMarker = null;
    private Animation up_animation, down_animation;
    private TextView title, description, syncTimestamp;
    private View statusCircle;

    //data obj
    private HashMap<Marker, Toilet> markerDataMap = new HashMap<>();

    //presenter
    private ToiletsPresenter presenter;

    //sets up the google map fragment
    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_toilets, null);
        infoCard = rootView.findViewById(R.id.toilet_info);
        title = infoCard.findViewById(R.id.toilet_name);
        description = infoCard.findViewById(R.id.toilet_description);
        syncTimestamp = infoCard.findViewById(R.id.toilet_status_timestamp);
        statusCircle = infoCard.findViewById(R.id.toilet_status_circle);
        up_animation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
        down_animation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_down);

        //setup mapview
        mMapView = rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        getActivity().setTitle("My Toilets");

        prefManager = new PrefManager(getContext());
        if (prefManager.getToilets() == null || prefManager.isGuest()) {
            Toast.makeText(getContext(), "No toilets assigned..", Toast.LENGTH_LONG).show();
            return rootView;
        }
        presenter = new ToiletsPresenter(this, prefManager);
        presenter.getToiletInfo();

        //get toilet status info async
        if (((MainActivity) getActivity()).isConnected) {
            presenter.getToiletStatus();
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (prefManager.getToilets() != null)
            mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (prefManager.getToilets() != null)
            mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (prefManager.getToilets() != null)
            mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (prefManager.getToilets() != null)
            mMapView.onLowMemory();
    }

    @Override
    public void displayToilets(final ArrayList<Toilet> toiletList) {
        //populate mapview async
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                // googleMap.setMyLocationEnabled(true);
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                googleMap.setBuildingsEnabled(true);

                //create marker objects for each toilet
                LatLng latLng = new LatLng(0, 0);
                LatLngBounds.Builder bld = new LatLngBounds.Builder();
                for (Toilet toilet : toiletList) {
                    String[] coordinates = toilet.getCoords();
                    latLng = new LatLng(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]));

                    markerDataMap.put(googleMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(toilet.getToiletName())
                            .snippet(toilet.getDescription())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_location))), toilet);
                    bld.include(latLng);
                }
                LatLngBounds bounds = bld.build();
                if (toiletList.size() == 1)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                else
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));

                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        if (!((MainActivity) getActivity()).isConnected) {
                            displayEmptyInfoCard(marker);
                        } else if (!marker.equals(prevMarker) && presenter.isStatusCheckDone()) {
                            displayInfoCard(marker);
                        } else if (!presenter.isStatusCheckDone()) {
                            Toast.makeText(getContext(), "Syncing toilets..", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    }
                });

                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        if (infoCard.getVisibility() == View.VISIBLE)
                            infoCard.startAnimation(down_animation);
                        infoCard.setVisibility(View.GONE);
                        prevMarker = null;
                    }
                });
            }
        });
    }

    @Override
    public void displayInfoCard(Marker marker) {
        Toilet toilet = markerDataMap.get(marker);
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm z MM/dd/yyyy");
        String timestamp = "Last Sync " + formatter.format(toilet.getSyncTimestamp());
        title.setText(toilet.getToiletName());
        description.setText(toilet.getDescription());
        syncTimestamp.setText(timestamp);

        switch (toilet.getStatus()) {
            case ERROR:
                statusCircle.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.status_circle_error));
                break;
            case HEALTHY:
                statusCircle.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.status_circle_healthy));
                break;
            case DISABLED:
                statusCircle.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.status_circle_disabled));
                break;
        }
        infoCard.startAnimation(up_animation);
        infoCard.setVisibility(View.VISIBLE);
        prevMarker = marker;
    }

    @Override
    public void displayEmptyInfoCard(Marker marker) {
        Toilet toilet = markerDataMap.get(marker);
        String timestamp = "No connection..";
        title.setText(toilet.getToiletName());
        description.setText(toilet.getDescription());
        syncTimestamp.setText(timestamp);
        statusCircle.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.status_circle_empty));
        infoCard.startAnimation(up_animation);
        infoCard.setVisibility(View.VISIBLE);
        prevMarker = marker;
    }

}

