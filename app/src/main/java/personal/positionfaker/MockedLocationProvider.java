package personal.positionfaker;

/**
 * Created by Nanochrome on 17-Jul-16.
 */

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import com.google.android.gms.maps.model.LatLng;
import java.util.Timer;
import java.util.TimerTask;


public class MockedLocationProvider {

    private Context mContext;
    private String providerName;
    private final int updateTime = 500;
    private final double speed = 15*(updateTime)/3600;
    private Thread t;
    private Location goal;
    private boolean isMoving;
    private LocationFaker locationFaker;

    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            locationFaker.updateMyLocation();
        }
    };

    public MockedLocationProvider(LocationFaker locationFaker, Context context, String providerName) {
        this.locationFaker = locationFaker;
        this.mContext = context;
        this.providerName = providerName;
    }

    public Location getLocation(){
        LatLng position = locationFaker.getMyLocation();
        Location location = new Location(providerName);
        location.setLongitude(position.longitude);
        location.setLatitude(position.latitude);
        return location;
    }

    private double addLat;
    private double addLon;
    public void moveToLocation(double endLat, double endLon){
        Location start = getLocation();
        goal = new Location(providerName);
        goal.setLatitude(endLat);
        goal.setLongitude(endLon);
        Float distance = start.distanceTo(goal);
        double steps = distance/speed;
        addLat = (endLat-start.getLatitude())/steps;
        addLon = (endLon-start.getLongitude())/steps;
        if(!isMoving){
            isMoving = true;
            final Timer t = new Timer();
            t.schedule(new TimerTask() {
                public void run() {
                        if(!isMoving){
                            t.cancel();
                        }
                        moveStep(addLat,addLon);
                }
            }, 0, updateTime);
        }
    }

    public void moveStep(double addLat, double addLon){
        Float distance = goal.distanceTo(getLocation());
        if( distance <= speed){
            setLocation(goal.getLatitude(), goal.getLongitude());
            isMoving = false;
        } else {
            setLocation(getLocation().getLatitude()+addLat, getLocation().getLongitude()+addLon);
        }
        mHandler.sendEmptyMessage(0);
    }

    public void setLocation(double lat, double lon) {
        locationFaker.setMyLocation(lat,lon);
    }


}