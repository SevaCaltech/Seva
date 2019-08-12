package edu.caltech.seva.activities.Main.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import edu.caltech.seva.R;
import edu.caltech.seva.models.IncomingError;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {

    //TODO: the notifications should come from sms listener, maybe sqlite db
    private ArrayList<IncomingError> errorList, filtered;
    private ClickListener clickListener;
    private Context context;
    private final int SORT_RECENT = 0;
    private final int SORT_OLDEST = 1;

    public RecyclerAdapter(Context context, ArrayList<IncomingError> incomingErrors) {
        this.errorList = incomingErrors;
        this.filtered = errorList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_item_layout, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(itemView);

        return myViewHolder;
    }

    //assign data to textView
    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        IncomingError error = filtered.get(position);
        Long time = Long.valueOf(error.getDate());
        Date d = new Date(time);
        String date = new SimpleDateFormat("hh:mm MM/dd/yyyy").format(d);

        holder.notifyText.setText(error.getToiletName());
        holder.repairTitle.setText("Error: " + error.getRepairTitle());
        holder.notifyDate.setText(date);
        holder.toolInfo.setText("Tools:" + error.getToolInfo());
        holder.totalTime.setText("Time: " + error.getTotalTime());
        holder.description.setText("Location: " + error.getDescription());

        if(holder.notifyMap != null){
            holder.notifyMap.setVisibility(View.VISIBLE);

            String lat =error.getLat();
            String lng = error.getLng();
            if(lat == null|| lng == null)
                return;
            else{
                LatLng latLng = new LatLng(Double.valueOf(error.getLat()),
                        Double.valueOf(error.getLng()));
                holder.initializeMapView();
                holder.setMapLocation(latLng);
            }
        }
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public int getItemCount() {
        return filtered != null? filtered.size() : 0;
    }

    @Override
    public void onViewRecycled(@NonNull MyViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.gMap!=null){
            holder.gMap.clear();
            holder.gMap.setMapType(GoogleMap.MAP_TYPE_NONE);
        }
    }

    public void filter(final String toilet_name, final int sort_method){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("log","filter: " + toilet_name + (sort_method == SORT_OLDEST? " by oldest" : " by recent"));
                Log.d("log","errorList size: " + errorList.size());
                filtered = new ArrayList<>();
                if(toilet_name.equals("All"))
                    filtered.addAll(errorList);
                else {
                    for (IncomingError error: errorList){
                        if(error.getToiletName().contains(toilet_name))
                            filtered.add(error);
                    }
                }
                Log.d("log","filtered size: " + filtered.size());

                try {
                    Collections.sort(filtered, new Comparator<IncomingError>() {
                        @Override
                        public int compare(IncomingError e1, IncomingError e2) {
                            if (sort_method == SORT_OLDEST)
                                return e1.getDate().compareTo(e2.getDate());
                            else
                                return e2.getDate().compareTo(e1.getDate());
                        }
                    });
                } catch (NullPointerException e) {
                    Log.d("log","no date field");
                }

                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }

//    public void filter(final String toilet_name, final int sort_method){
//        Log.d("log","filter: " + toilet_name + (sort_method == SORT_OLDEST? " by oldest" : " by recent"));
//        Log.d("log","errorList size: " + errorList.size());
//        filtered = new ArrayList<>();
//        if(toilet_name.equals("All"))
//            filtered.addAll(errorList);
//        else {
//            for (IncomingError error: errorList){
//                if(error.getToiletName().contains(toilet_name))
//                    filtered.add(error);
//            }
//        }
//        Log.d("log","filtered size: " + filtered.size());
//
//        try {
//            Collections.sort(filtered, new Comparator<IncomingError>() {
//                @Override
//                public int compare(IncomingError e1, IncomingError e2) {
//                    if (sort_method == SORT_OLDEST)
//                        return e1.getDate().compareTo(e2.getDate());
//                    else
//                        return e2.getDate().compareTo(e1.getDate());
//                }
//            });
//        } catch (NullPointerException e) {
//            Log.d("log","no date field");
//        }
//        notifyDataSetChanged();
//    }

    public void deleteNotification(int position){
        Log.d("log","attempting to remove pos:" + position);
        errorList.remove(position);
        filtered.remove(position);
        notifyDataSetChanged();
    }

//    public void updateErrorList(ArrayList<IncomingError> errorList){
//        this.errorList.clear();
//        this.errorList.addAll(errorList);
//        notifyDataSetChanged();
//    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, OnMapReadyCallback {
        private TextView notifyText;
        private TextView notifyDate;
        private Button decline;
        private Button accept;
        private ImageView directions;
        private ImageView speech;
        private TextView repairTitle;
        private TextView toolInfo;
        private TextView totalTime;
        private TextView description;

        private GoogleMap gMap;
        private MapView notifyMap;
        protected LatLng mMapLocation;
        GoogleMapOptions options = new GoogleMapOptions().liteMode(true);

        public MyViewHolder(@NonNull View view) {
            super(view);
            notifyText = (TextView) view.findViewById(R.id.notificationText);
            notifyDate = (TextView) view.findViewById(R.id.notificationDate);
            accept = (Button) view.findViewById(R.id.acceptButton);
            decline = (Button) view.findViewById(R.id.declineButton);
            directions = (ImageView) view.findViewById(R.id.mapButton);
            notifyMap = (MapView) view.findViewById(R.id.notificationMap);
            speech = (ImageView) view.findViewById(R.id.speechMapButton);
            repairTitle = (TextView) view.findViewById(R.id.titleDetail);
            toolInfo = (TextView) view.findViewById(R.id.toolDetail);
            totalTime = (TextView) view.findViewById(R.id.timeDetail);
            description = (TextView) view.findViewById(R.id.description);

            initializeMapView();

            decline.setOnClickListener(this);
            accept.setOnClickListener(this);
            directions.setOnClickListener(this);
            speech.setOnClickListener(this);

        }

        //handles the button clicks on the side of the notification layout
        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            IncomingError error = filtered.get(position);
            if(clickListener != null) {
                switch (view.getId()){
                    case R.id.acceptButton:
                        clickListener.acceptClicked(view, error);
                        break;
                    case R.id.declineButton:
                        clickListener.declineClicked(view,position,error.getId());
                        break;
                    case R.id.mapButton:
                        clickListener.mapClicked(view,error.getLat(),error.getLng());
                        break;
                    case R.id.speechMapButton:
                        clickListener.speechClicked(view,error.getErrorCode());
                        break;
                }
            }
        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            MapsInitializer.initialize(context);
            gMap = googleMap;
            gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            googleMap.getUiSettings().setMapToolbarEnabled(false);

            if (mMapLocation != null)
                updateMapContents();
        }

        public void initializeMapView() {
            if (notifyMap !=null){
                notifyMap.onCreate(null);
                notifyMap.getMapAsync(this);
            }
        }

        public void setMapLocation(LatLng mapLocation){
            mMapLocation = mapLocation;
            if(gMap != null)
                updateMapContents();
        }

        public void updateMapContents() {
            gMap.clear();
            gMap.addMarker(new MarkerOptions()
                    .position(mMapLocation)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_location)));
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mMapLocation, 16);
            gMap.moveCamera(cameraUpdate);
        }
    }

    //interface is used so that the itemClicked function is taken care of by the Notification fragment not the adapter
    public interface ClickListener {
        public void declineClicked(View view, int position, int id);
        public void acceptClicked(View view, IncomingError incomingError);
        public void mapClicked(View view, String lat, String lng);
        public void speechClicked(View view, String errorCode);
    }

}
