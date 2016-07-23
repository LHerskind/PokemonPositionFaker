package personal.positionfaker;

/**
 * Created by Nanochrome on 23-Jul-16.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class LocationFaker extends FragmentActivity implements OnMapReadyCallback{

    private GoogleMap mMap;
    private Context mContext;
    private MockedLocationProvider mMockedLocationProvider;
    final String providerName = "MyFancyGPSProvider";
    private MarkerOptions myPositionMarker;
    private MarkerOptions endPositionMarker;
    private Marker myPositionAtMap;
    private Marker endPostionAtMap;

    private double mLatitude = 55.859421;
    private double mLongtitude = 12.330067;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        setContentView(R.layout.activity_location_picker);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        sharedPreferences = getSharedPreferences("pokemon", Context.MODE_WORLD_WRITEABLE|Context.MODE_WORLD_READABLE);
        editor = sharedPreferences.edit();

        mMockedLocationProvider = new MockedLocationProvider(this, mContext, providerName);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(checkPermission()) {
            mMap.setMyLocationEnabled(true);
        }

        myPositionMarker = new MarkerOptions().position(new LatLng(mLatitude,mLongtitude))
                .title("Here is your spoofed position");
        myPositionMarker.icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("pokeball_icon", 50, 50)));
        myPositionAtMap = mMap.addMarker(myPositionMarker);

        endPositionMarker = new MarkerOptions().title("Your end distination")
                .position(new LatLng(mLatitude,mLongtitude));
        endPostionAtMap = mMap.addMarker(endPositionMarker);
        endPostionAtMap.setVisible(false);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                endPositionMarker.position(latLng);
                endPostionAtMap.setPosition(endPositionMarker.getPosition());
                endPostionAtMap.setVisible(true);
                mMockedLocationProvider.moveToLocation(latLng.latitude, latLng.longitude);
            }
        });
    }

    public void setMyLocation(double lat, double lon){
        mLatitude = lat;
        mLongtitude = lon;
        editor.putString("latitude", ""+mLatitude);
        editor.putString("longtitude", ""+mLongtitude);
        editor.apply();
    }

    public LatLng getMyLocation(){
        return myPositionMarker.getPosition();
    }

    public void updateMyLocation(){
        myPositionMarker.position(new LatLng(mLatitude, mLongtitude));
        myPositionAtMap.setPosition(getMyLocation());
    }

    public Bitmap resizeMapIcons(String iconName,int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    public boolean checkPermission(){
        return !((ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


}
