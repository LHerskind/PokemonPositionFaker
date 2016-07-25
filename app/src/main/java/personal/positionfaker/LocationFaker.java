package personal.positionfaker;

/**
 * Created by Nanochrome on 23-Jul-16.
 */

import android.*;
import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class LocationFaker extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private Context mContext;
    private MockedLocationProvider mMockedLocationProvider;
    final String providerName = "MyFancyGPSProvider";
    private MarkerOptions myPositionMarker;
    private MarkerOptions endPositionMarker;
    private Marker myPositionAtMap;
    private Marker endPostionAtMap;

    private double mLatitude = 55.675989;
    private double mLongtitude = 12.568932;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private ImageButton pokemonPosition;
    private ImageButton teleportPosition;
    private ImageButton coordinatesPosition;

    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        setContentView(R.layout.activity_location_picker);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        pokemonPosition = (ImageButton) findViewById(R.id.pokemonLocation);
        teleportPosition = (ImageButton) findViewById(R.id.teleportLocation);
        coordinatesPosition = (ImageButton) findViewById(R.id.coordinatsLocation);

        sharedPreferences = getSharedPreferences("pokemon", Context.MODE_WORLD_WRITEABLE | Context.MODE_WORLD_READABLE);
        editor = sharedPreferences.edit();
        mMockedLocationProvider = new MockedLocationProvider(this, mContext, providerName);

        // ATTENTION: This "addApi(AppIndex.API)"was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        googleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(AppIndex.API).build();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);

        if(checkPermission()) {
            mMap.setMyLocationEnabled(true);
        }

        myPositionMarker = new MarkerOptions().position(new LatLng(mLatitude,mLongtitude))
                .title("Here is your spoofed position");
        myPositionMarker.icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("pokeball_icon", 50, 50))).anchor(0.5f, 0.5f);
        myPositionAtMap = mMap.addMarker(myPositionMarker);

        endPositionMarker = new MarkerOptions().title("Your end distination")
                .position(new LatLng(mLatitude,mLongtitude));
        endPostionAtMap = mMap.addMarker(endPositionMarker);
        endPostionAtMap.setVisible(false);

        pokemonPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMap.getCameraPosition().zoom < 15) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLatitude, mLongtitude), 15));
                } else {
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(mLatitude, mLongtitude)));
                }
            }
        });

        teleportPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMyLocation(endPositionMarker.getPosition().latitude, endPositionMarker.getPosition().longitude);
                updateMyLocation();
            }
        });

        // Endnu en knap som dog gør det muligt at skrive koordinater direkte ind.
        coordinatesPosition.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(LocationFaker.this);
                builder.setTitle("Input destination");

                LinearLayout linearLayout = new LinearLayout(mContext);
                linearLayout.setOrientation(LinearLayout.VERTICAL);

                final EditText inputLatitude = new EditText(mContext);
                final EditText inputLongtitude = new EditText(mContext);
                inputLatitude.setHint("Latitude");
                inputLongtitude.setHint("Longtitude");
                inputLatitude.setHintTextColor(getColor(R.color.darkText));
                inputLongtitude.setHintTextColor(getColor(R.color.darkText));
                inputLatitude.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                inputLongtitude.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                linearLayout.addView(inputLatitude);
                linearLayout.addView(inputLongtitude);
                builder.setView(linearLayout);

                builder.setPositiveButton("GO!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (inputLatitude.getText().toString().length() > 0 && inputLongtitude.getText().toString().length() > 0) {
                            double mInputLatitude = Double.parseDouble(inputLatitude.getText().toString());
                            double mInputLongtitude = Double.parseDouble(inputLongtitude.getText().toString());
                            setGoalLocation(new LatLng(mInputLatitude, mInputLongtitude));
                        } else {
                            dialog.cancel();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });


        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                setGoalLocation(latLng);
            }
        });
    }

    public void setGoalLocation(LatLng latLng) {
        endPositionMarker.position(latLng);
        endPostionAtMap.setPosition(endPositionMarker.getPosition());
        endPostionAtMap.setVisible(true);
        mMockedLocationProvider.moveToLocation(latLng.latitude, latLng.longitude);
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
        return !((ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        googleApiClient.disconnect();
    }

    @Override
    public void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (checkPermission()) {
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (mLastLocation != null) {
                setMyLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                updateMyLocation();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }



}
