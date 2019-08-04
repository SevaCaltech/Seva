package edu.caltech.seva.activities.Main.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
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
    private ArrayList<IncomingError> arrayList = new ArrayList<>();
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
            holder.initializeMapView();
            if (holder.gMap != null)
                moveMap(holder.gMap,Double.valueOf(arrayList.get(position).getLat()), Double.valueOf(arrayList.get(position).getLng()),arrayList.get(position).getDescription());
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
                switch (view.getId()){
                    case R.id.acceptButton:
                        clickListener.acceptClicked(view, arrayList.get(getAdapterPosition()));
                        break;
                    case R.id.declineButton:
                        clickListener.declineClicked(view,getAdapterPosition(),arrayList.get(getAdapterPosition()).getId());
                        break;
                    case R.id.mapButton:
                        clickListener.mapClicked(view,arrayList.get(getAdapterPosition()).getLat(),arrayList.get(getAdapterPosition()).getLng());
                    case R.id.speechMapButton:
                        clickListener.speechClicked(view,arrayList.get(getAdapterPosition()).getErrorCode());
                }

            }
        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            MapsInitializer.initialize(context);
            gMap = googleMap;
            googleMap.getUiSettings().setMapToolbarEnabled(false);
            moveMap(gMap,Double.valueOf(arrayList.get(getAdapterPosition()).getLat()), Double.valueOf(arrayList.get(getAdapterPosition()).getLng()),arrayList.get(getAdapterPosition()).getDescription());
        }

        public void initializeMapView() {
            if (notifyMap !=null){
                notifyMap.onCreate(null);
                notifyMap.getMapAsync(this);
            }
        }
    }

    public void moveMap(GoogleMap gMap, double latitude, double longitude, String description) {
        LatLng latLng = new LatLng(latitude,longitude);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 12);
        gMap.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_location)));
        gMap.moveCamera(cameraUpdate);
    }

    //interface is used so that the itemClicked function is taken care of by the Notification fragment not the adapter
    public interface ClickListener {
        public void declineClicked(View view, int position, int id);
        public void acceptClicked(View view, IncomingError incomingError);
        public void mapClicked(View view, String lat, String lng);
        public void speechClicked(View view, String errorCode);
    }

}
