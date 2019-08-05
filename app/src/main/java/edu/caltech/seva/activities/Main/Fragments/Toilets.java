package edu.caltech.seva.activities.Main.Fragments;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import edu.caltech.seva.R;
import edu.caltech.seva.helpers.DbContract;
import edu.caltech.seva.helpers.DbHelper;
import edu.caltech.seva.helpers.PrefManager;
import edu.caltech.seva.models.Toilet;
import edu.caltech.seva.models.UserData;
import edu.caltech.seva.models.UsersDO;

import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;

//TODO: maybe the pin should be red if there's a problem and add a button to get to repair guide, connect to sqlite db and server
public class Toilets extends Fragment {

    private PrefManager prefManager;
    ArrayList<String> toilets = new ArrayList<>();
    ArrayList<Toilet> toiletObj = new ArrayList<>();

    MapView mMapView;
    private GoogleMap googleMap;

    //sets up the google map fragment
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_toilets,null);
        getActivity().setTitle("My Toilets");

        prefManager = new PrefManager(getContext());
        if (prefManager.getToilets() == null)
        {
            Toast.makeText(getContext(),"No toilets assigned..", Toast.LENGTH_LONG).show();
            return rootView;
        }

        toilets.addAll(prefManager.getToilets());
        Log.d("log", "toilets read: "+ toilets);

        DbHelper dbHelper = new DbHelper(getContext());
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        for(int i=0;i<toilets.size();i++) {
            Cursor cursor = dbHelper.readToiletInfo(database, toilets.get(i));
            String lat, lng, description, toiletName;
            String[] coords = new String[2];
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                lat = cursor.getString(cursor.getColumnIndex(DbContract.TOILET_LAT));
                lng = cursor.getString(cursor.getColumnIndex(DbContract.TOILET_LNG));
                description = cursor.getString(cursor.getColumnIndex(DbContract.TOILET_DESC));
                toiletName = cursor.getString(cursor.getColumnIndex(DbContract.TOILET_NAME));
                coords[0] = lat;
                coords[1] = lng;
                Toilet tempToilet = new Toilet(coords,description,toiletName);
                toiletObj.add(tempToilet);
            }
        }
        dbHelper.close();


        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
               // googleMap.setMyLocationEnabled(true);
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                googleMap.setBuildingsEnabled(true);

                int lat, lng;
                LatLng latLng = new LatLng(0,0);
                LatLngBounds.Builder bld = new LatLngBounds.Builder();
                for(Toilet toilet:toiletObj){
                    String[] coordinates = toilet.getCoords();
                    latLng = new LatLng(Double.valueOf(coordinates[0]),Double.valueOf(coordinates[1]));

                    googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(toilet.getToiletName())
                    .snippet(toilet.getDescription())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_location)));
                    bld.include(latLng);
                }
                LatLngBounds bounds = bld.build();
                if (toiletObj.size() == 1)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
                else
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));

            }
        });

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
}
