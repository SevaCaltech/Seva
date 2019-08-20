package edu.caltech.seva.activities.Main.Fragments;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import edu.caltech.seva.R;
import edu.caltech.seva.helpers.DbContract;
import edu.caltech.seva.helpers.DbHelper;
import edu.caltech.seva.helpers.PrefManager;
import edu.caltech.seva.models.Toilet;
import edu.caltech.seva.models.ToiletInfoDO;

import static edu.caltech.seva.models.Toilet_Status.DISABLED;
import static edu.caltech.seva.models.Toilet_Status.ERROR;
import static edu.caltech.seva.models.Toilet_Status.HEALTHY;

public class Toilets extends Fragment {

    private PrefManager prefManager;
    ArrayList<String> toilet_ips = new ArrayList<>();
    ArrayList<Toilet> toiletObjs = new ArrayList<>();
    RelativeLayout infoCard;
    MapView mMapView;
    Marker prevMarker = null;
    DynamoDBMapper dynamoDBMapper;
    Animation up_animation, down_animation;
    private GoogleMap googleMap;
    private HashMap<Marker, Toilet> markerDataMap = new HashMap<>();
    public boolean statusCheckDone;

    //sets up the google map fragment
    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_toilets,null);
        infoCard = (RelativeLayout) rootView.findViewById(R.id.toilet_info);
        up_animation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
        down_animation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_down);

        getActivity().setTitle("My Toilets");

        prefManager = new PrefManager(getContext());
        if (prefManager.getToilets() == null || prefManager.isGuest())
        {
            Toast.makeText(getContext(),"No toilets assigned..", Toast.LENGTH_LONG).show();
            return rootView;
        }

        toilet_ips.addAll(prefManager.getToilets());
        DbHelper dbHelper = new DbHelper(getContext());
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        for(int i=0;i<toilet_ips.size();i++) {
            Cursor cursor = dbHelper.readToiletInfo(database, toilet_ips.get(i));
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
                Toilet tempToilet = new Toilet(coords,description,toiletName, toilet_ips.get(i));
                toiletObjs.add(tempToilet);
            }
        }
        dbHelper.close();

        //get toilet status info async
        AWSMobileClient.getInstance().initialize(getContext()).execute();
        AWSCredentialsProvider credentialsProvider = AWSMobileClient.getInstance().getCredentialsProvider();
        AWSConfiguration configuration = AWSMobileClient.getInstance().getConfiguration();
        AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(credentialsProvider);
        dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
        dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(configuration)
                .build();

        CheckStatus checkstatus = new CheckStatus() ;
        checkstatus.execute();

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
                for(Toilet toilet:toiletObjs){
                    String[] coordinates = toilet.getCoords();
                    latLng = new LatLng(Double.valueOf(coordinates[0]),Double.valueOf(coordinates[1]));

                    markerDataMap.put(googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(toilet.getToiletName())
                    .snippet(toilet.getDescription())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_location))), toilet);
                    bld.include(latLng);
                }
                LatLngBounds bounds = bld.build();
                if (toiletObjs.size() == 1)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
                else
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));

                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        if (!marker.equals(prevMarker) && statusCheckDone) {
                            Toilet toilet = markerDataMap.get(marker);
                            TextView title = (TextView) infoCard.findViewById(R.id.toilet_name);
                            TextView description = (TextView) infoCard.findViewById(R.id.toilet_description);
                            TextView syncTimestamp = (TextView) infoCard.findViewById(R.id.toilet_status_timestamp);
                            View statusCircle = (View) infoCard.findViewById(R.id.toilet_status_circle);

                            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm z MM/dd/yyyy");
                            String timestamp = "Last Sync " + formatter.format(toilet.getSyncTimestamp());
                            title.setText(toilet.getToiletName());
                            description.setText(toilet.getDescription());
                            syncTimestamp.setText(timestamp);

                            switch (toilet.getStatus()){
                                case ERROR:
                                    statusCircle.setBackground(ContextCompat.getDrawable(getContext(),R.drawable.status_circle_error));
                                    break;
                                case HEALTHY:
                                    statusCircle.setBackground(ContextCompat.getDrawable(getContext(),R.drawable.status_circle_healthy));
                                    break;
                                case DISABLED:
                                    statusCircle.setBackground(ContextCompat.getDrawable(getContext(),R.drawable.status_circle_disabled));
                                    break;
                            }
                            infoCard.startAnimation(up_animation);
                            infoCard.setVisibility(View.VISIBLE);
                            prevMarker = marker;
                        } else if(!statusCheckDone) {
                            Toast.makeText(getContext(), "Syncing toilets..", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    }
                });

                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        if(infoCard.getVisibility() == View.VISIBLE)
                            infoCard.startAnimation(down_animation);
                            infoCard.setVisibility(View.GONE);
                            prevMarker = null;
                    }
                });
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

    public class CheckStatus extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            for (Toilet toilet:toiletObjs) {
                //poll dynamodb to see if toilet is enabled
                String query = "aws/things/" + toilet.getToiletIp();
                Log.d("log", "loading: " + query);
                ToiletInfoDO dynamoToilet = dynamoDBMapper.load(ToiletInfoDO.class, query);
                String toilet_status = dynamoToilet.getToiletStatus();

                if(toilet_status.equals("Enabled")){
                    //poll localdb to see if there are any errors for it
                    DbHelper dbHelper = new DbHelper(getContext());
                    SQLiteDatabase database =dbHelper.getWritableDatabase();
                    int error_count = dbHelper.readNumToiletErrors(toilet.getToiletIp(),database);
                    dbHelper.close();

                    Log.d("log", "\terrors: " + error_count);
                    if(error_count > 0)
                        toilet.setStatus(ERROR);
                    else
                        toilet.setStatus(HEALTHY);
                }
                else
                    toilet.setStatus(DISABLED);
                toilet.setSyncTimestamp(new Date(System.currentTimeMillis()));
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            statusCheckDone = false;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            statusCheckDone = true;
        }
    }
}

