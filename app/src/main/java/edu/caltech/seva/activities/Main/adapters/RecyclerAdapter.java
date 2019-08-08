package edu.caltech.seva.activities.Main.adapters;

import android.content.Context;
import android.graphics.Bitmap;
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
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import edu.caltech.seva.R;
import edu.caltech.seva.activities.Main.Fragments.DeleteDialog;
import edu.caltech.seva.models.IncomingError;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {

    //TODO: the notifications should come from sms listener, maybe sqlite db
    private ArrayList<IncomingError> arrayList;
    private ClickListener clickListener;
    private Context context;

    public RecyclerAdapter(Context context, ArrayList<IncomingError> arrayList) {
        this.arrayList = arrayList;
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
        Long time = Long.valueOf(arrayList.get(position).getDate());
        Date d = new Date(time);
        String date = new SimpleDateFormat("hh:mm MM/dd/yyyy").format(d);

        holder.notifyText.setText(arrayList.get(position).getToiletName());
        holder.repairTitle.setText("Error: " + arrayList.get(position).getRepairTitle());
        holder.notifyDate.setText(date);
        holder.toolInfo.setText("Tools:" + arrayList.get(position).getToolInfo());
        holder.totalTime.setText("Time: " + arrayList.get(position).getTotalTime());
        holder.description.setText("Location: " + arrayList.get(position).getDescription());

        if(holder.notifyMap != null){
            holder.notifyMap.setVisibility(View.VISIBLE);

            String lat =arrayList.get(position).getLat();
            String lng = arrayList.get(position).getLng();
            Log.d("log",arrayList.get(position).getToiletName() + ": " +lat+", "+lng);
            if(lat == null|| lng == null)
                return;
            else{
                LatLng latLng = new LatLng(Double.valueOf(arrayList.get(position).getLat()), Double.valueOf(arrayList.get(position).getLng()));
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
        return arrayList.size();
    }

    @Override
    public void onViewRecycled(@NonNull MyViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.gMap!=null){
            holder.gMap.clear();
            holder.gMap.setMapType(GoogleMap.MAP_TYPE_NONE);
        }
    }


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
            if(clickListener != null) {
                int position = getAdapterPosition();
                switch (view.getId()){
                    case R.id.acceptButton:
                        clickListener.acceptClicked(view, arrayList.get(position));
                        break;
                    case R.id.declineButton:
                        clickListener.declineClicked(view,position,arrayList.get(position).getId());
                        break;
                    case R.id.mapButton:
                        clickListener.mapClicked(view,arrayList.get(position).getLat(),arrayList.get(position).getLng());
                    case R.id.speechMapButton:
                        clickListener.speechClicked(view,arrayList.get(position).getErrorCode());
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
