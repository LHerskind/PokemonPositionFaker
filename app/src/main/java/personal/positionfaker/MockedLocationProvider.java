package personal.positionfaker;

/**
 * Created by Nanochrome on 17-Jul-16.
 */

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.Timer;
import java.util.TimerTask;


public class MockedLocationProvider {

    private Context mContext;
    private String providerName;
    private final int updateTime = 500;
    private final double factor = ((double) updateTime) / 3600;
    private double speed = 10 * factor;
    private Location goal;
    private boolean isMoving;
    private LocationFaker locationFaker;

    private Timer timer;


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

    public Location getLocation() {
        LatLng position = locationFaker.getMyLocation();
        Location location = new Location(providerName);
        location.setLongitude(position.longitude);
        location.setLatitude(position.latitude);
        return location;
    }

    private double addLat;
    private double addLon;
    private double steps;
    private double distance;

    public void setSpeed(int speed) {
        double newSpeed = speed * factor;
        double newSteps = distance / newSpeed;
        addLat = addLat * steps / newSteps;
        addLon = addLon * steps / newSteps;
        this.steps = newSteps;
        this.speed = newSpeed;
    }

    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
        }
        this.isMoving = false;
    }

    public void moveToLocation(double endLat, double endLon) {
        Location start = getLocation();
        goal = new Location(providerName);
        goal.setLatitude(endLat);
        goal.setLongitude(endLon);
        distance = (double) start.distanceTo(goal);
        steps = distance / speed;
        addLat = (endLat - start.getLatitude()) / steps;
        addLon = (endLon - start.getLongitude()) / steps;
        if (!isMoving) {
            isMoving = true;
            timer = new Timer();
            timer.schedule(new TimerTask() {
                public void run() {
                    if (isMoving) {
                        moveStep(addLat, addLon);
                    }
                }
            }, 0, updateTime);
        }

    }

    public void moveStep(double addLat, double addLon) {
        Float distance = goal.distanceTo(getLocation());
        if (distance <= speed) {
            setLocation(goal.getLatitude(), goal.getLongitude());
            if (locationFaker.getIsRoute()) {
                if (locationFaker.sendMeToNextRoutePoint()) {
                    isMoving = false;
                    stopTimer();
                }
            } else {
                locationFaker.goalReached();
                isMoving = false;
                stopTimer();
            }
        } else {
            setLocation(getLocation().getLatitude() + addLat, getLocation().getLongitude() + addLon);
        }
        mHandler.sendEmptyMessage(0);
    }

    public void setLocation(double lat, double lon) {
        locationFaker.setMyLocation(lat, lon);
    }


}