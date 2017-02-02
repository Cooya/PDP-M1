package mastergl.pdp;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DateFormat;
import java.util.Date;

/**
 * Activity which handle maps, directions and location.
 * <p/>
 * It can show a map with some point of interest or a way to go from a point to another.
 */
public class MapActivity extends FragmentActivity
        implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 100;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    /**
     * Keys for storing activity state in the Bundle.
     */
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";
    protected final static String ON_WAY_KEY = "on_way_key";
    protected final static String WAY_MANAGER_KEY = "way_manager_key";
    protected final static String DESTINATION_KAY = "destination_key";

    /**
     * Request code for enabling location.
     */
    protected final static int REQUEST_CODE_LOCATION = 1000;
    /**
     * The current map displayed by the app.
     */
    private GoogleMap mMap;

    /**
     * Provides the entry point to Google Play services.
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequest;

    /**
     * Represents the last known geographical location.
     */
    private Location mLastLocation;

    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    protected Boolean mRequestingLocationUpdates;

    /**
     * Time when the location was updated represented as a String.
     */
    protected String mLastUpdateTime;

    /**
     * The object handling the way services. It contains the json parser and the reference of
     * mPolyline which is drawn on the map.
     * @see PolylineOptions
     */
    private WayManager mWayManager;

    /**
     * If true, the activity is on a way and have to do deal with WayManager object, Bluetooth and vibrations.
     */
    private boolean onWay = false;

    /**
     * Geopoint of the destination address.
     */
    private LatLng mDestination;

    /**********************************************************************************************
     ******************** SUPERCLASS METHOD'S OVERRIDE ********************************************
     **********************************************************************************************/

    /**
     * Called when the activity is starting.
     * Set the content view and initialize all objects needed, like Google Map and callback method button.
     * @param savedInstanceState the saved instance of the object
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("DEBUG", "on Create");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent intent = getIntent();
        double[] dest = intent.getExtras().getDoubleArray(getString(R.string.extra_destination_key));

        if(dest != null)
            mDestination = new LatLng(dest[0],dest[1]);

        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        createLocationRequest();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        final Button beginNav = (Button) findViewById(R.id.start_nav_button);

        beginNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!onWay) {
                    startNavigation();
                    beginNav.setText(getString(R.string.stop_nav));
                } else {
                    stopNavigation();
                    beginNav.setText(getString(R.string.start_nav));
                }
            }
        });

        //Retain the map state
        mapFragment.setRetainInstance(true);
        updateValuesFromBundle(savedInstanceState);
    }

    /**
     * Called after onCreate(Bundle) — or after onRestart() when the activity had been stopped,
     * but is now again being displayed to the user. It will be followed by onResume().
     * Connect to the Google API client.
     */
    @Override
    protected void onStart() {
        Log.e("DEBUG", "on Start");
        mGoogleApiClient.connect();
        super.onStart();
    }

    /**
     * Called when you are no longer visible to the user. You will next receive either onRestart(),
     * onDestroy(), or nothing, depending on later user activity.
     * Disconnect the Google API client if it is connected.
     * @see #stopLocationUpdates()
     */
    @Override
    protected void onStop() {
        Log.e("DEBUG", "on Stop");
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
        super.onStop();
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into the background,
     * but has not (yet) been killed. The counterpart to onResume().
     * Stop the location updates.
     */
    @Override
    protected void onPause() {
        Log.e("DEBUG", "on Pause");
        super.onPause();
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates)
            stopLocationUpdates();
    }

    /**
     * Called after onRestoreInstanceState(Bundle), onRestart(), or onPause(),
     * for your activity to start interacting with the user. This is a good place to begin animations,
     * open exclusive-access devices (such as the camera), etc.
     * Start the location updates.
     */
    @Override
    protected void onResume() {
        Log.e("DEBUG", "on Resume");
        super.onResume();

        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates)
            startLocationUpdates();
    }

    /**********************************************************************************************
     *************************** ON MAP READY LISTENER'S OVERRIDE *********************************
     **********************************************************************************************/

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.e("DEBUG", "On map ready");

        mMap = googleMap;
    }

    /**********************************************************************************************
     ****************************** CONNECTIONS CALLBACK'S OVERRIDE *******************************
     **********************************************************************************************/

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.e("DEBUG", "on connected " + mRequestingLocationUpdates);
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            updateUI();

            startLocationUpdates();

            //Draw path when first access to the map
            if (mWayManager == null && mLastLocation != null && mDestination != null) {
                String url = "http://maps.googleapis.com/maps/api/directions/json?origin=" + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude() + "&" +
                        "destination="+mDestination.latitude+","+mDestination.longitude+"&" +
                        "avoid=tolls|highways|ferries&" +
                        "mode=walking&";
                    /*"key="+R.string.google_maps_key*/

                mWayManager = new WayManager(this, mMap, url, MapActivity.this, mLastLocation);
                mWayManager.execute();
            }
        }
    }

    /***********************************************************************************************
     *********************** ON CONNECTION FAILED LISTENER'S OVERRIDE ******************************
     ***********************************************************************************************/

    /**
     * Called when the client is temporarily in a disconnected state.
     * This can happen if there is a problem with the remote service
     * (e.g. a crash or resource problem causes it to be killed by the system).
     * When called, all requests have been canceled and no outstanding listeners will be executed.
     * GoogleApiClient will automatically attempt to restore the connection.
     * Applications should disable UI components that require the service,
     * and wait for a call to onConnected(Bundle) to re-enable them.
     * @param cause The reason for the disconnection. Defined by constants CAUSE_*.
     */
    @Override
    public void onConnectionSuspended(int cause) {

    }

    /**
     * Called when there was an error connecting the client to the service.
     * @param connectionResult A ConnectionResult that can be used for resolving the error,
     *                         and deciding what sort of error occurred. To resolve the error,
     *                         the resolution must be started from an activity with a non-negative
     *                         requestCode passed to startResolutionForResult(Activity, int).
     *                         Applications should implement onActivityResult in their Activity
     *                         to call connect() again if the user has resolved the issue (resultCode is RESULT_OK).
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {


    }

    /***********************************************************************************************
     ******************************* LOCATION LISTENER'S OVERRIDE **********************************
     **********************************************************************************************/

    /**
     * Call whenever the location of the device changed.
     * @param location the new location to work with
     */
    @Override
    public void onLocationChanged(Location location) {
        Log.e("DEBUG", "location changed");
        if (mWayManager == null && mLastLocation != null && mDestination != null)
            instantiateAndRequestWay();
        mLastLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        if(mWayManager != null)
        {
            mWayManager.event(mLastLocation,onWay);
        }
        updateUI();
    }

    /**********************************************************************************************
     ************************************** METHODS CREATED ***************************************
     **********************************************************************************************/

    /**
     * Create the way manager and request the way to the Google services.
     */
    private void instantiateAndRequestWay()
    {
        String url = "http://maps.googleapis.com/maps/api/directions/json?origin=" + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude() + "&" +
                "destination="+mDestination.latitude+","+mDestination.longitude+"&" +
                "avoid=tolls|highways|ferries&" +
                "mode=walking&";
                    /*"key="+R.string.google_maps_key*/

        mWayManager = new WayManager(this, mMap, url, MapActivity.this, mLastLocation);
        mWayManager.execute();
    }

    /**********************************************************************************************
     ************************************** METHODS CREATED ***************************************
     **********************************************************************************************/

    /**
     * Create the instance of a location request and store it in the mLocationRequest field.
     * @see #mLocationRequest
     */
    protected void createLocationRequest() {
        Log.e("DEBUG", "create location request");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mRequestingLocationUpdates = true;
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {

            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                //final LocationSettingsStates lss = result.getLocationSettingsStates();
                Log.e("REQUEST","status "+status.getStatusCode());
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(MapActivity.this, REQUEST_CODE_LOCATION);

                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.

                        break;
                }
            }
        });
    }

    /**
     * Handles to the dialog when user activate his bluetooth within the app.
     * @param requestCode the code to determine what was the question.
     * @param resultCode the code to determine what is the result.
     * @param data optional data.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode,Intent data)
    {
        Log.e("CHECK", "request " + requestCode + " result " + resultCode);
        if(requestCode == REQUEST_CODE_LOCATION)
            if(resultCode == RESULT_OK)
                startLocationUpdates();
    }

    /**
     * Set the requesting flag to true, and begin the requesting location updates of the fused location API.
     */
    protected void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Log.e("DEBUG", "Start Location Updates");

        mRequestingLocationUpdates = true;
        mMap.setMyLocationEnabled(true);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    /**
     * Set the the requesting flag to false, and stop the location updates of the fused location API.
     */
    protected void stopLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mRequestingLocationUpdates = false;

        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    /**
     * Show on the interface the current location and last time it was updated.
     * @see TextView
     */
    TextView mLatitudeTextView, mLongitudeTextView, mLastUpdateTimeTextView = null;

    /**
     * Update the information text view and the map.
     * @see mastergl.pdp.R.layout
     */
    private void updateUI() {
        Log.e("DEBUG", "Update UI");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation == null || mMap == null)
            return;

        if (mLatitudeTextView == null || mLongitudeTextView == null || mLastUpdateTimeTextView == null) {
            mLatitudeTextView = (TextView) findViewById(R.id.LatitudeText);
            mLongitudeTextView = (TextView) findViewById(R.id.LongitudeText);
            mLastUpdateTimeTextView = (TextView) findViewById(R.id.TimeText);
        }

        //CONSTANT DISPLAYING MESSAGE LOADED FROM RESOURCES
        String DIS_LAT = getString(R.string.display_lat)+ String.valueOf(mLastLocation.getLatitude());
        String DIS_LNG = getString(R.string.display_lng)+ String.valueOf(mLastLocation.getLongitude());
        String DIS_LASTSPEEDSTATE = getString(R.string.display_last)+ mLastUpdateTime
                +getString(R.string.display_speed)+ mLastLocation.getSpeed()
                +getString(R.string.display_state)
                +( mWayManager != null && mWayManager.getState() != null ? mWayManager.getState() : "");

        mLatitudeTextView.setText( DIS_LAT);
        mLongitudeTextView.setText( DIS_LNG);
        mLastUpdateTimeTextView.setText( DIS_LASTSPEEDSTATE );
    }

    /**********************************************************************************************
     ********************************** NAVIGATION METHODS ****************************************
     **********************************************************************************************/

    /**
     * Start the navigation. Basically, put the camera behind the location and start working with WayManager.
     */
    public void startNavigation() {
        onWay = true;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        if (mWayManager != null) {
            mWayManager.moveCameraBehind(mLastLocation.getBearing());
            mWayManager.startSynchronisation();
        }
    }

    /**
     * Stop the navigation.
     */
    public void stopNavigation() {
        onWay = false;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mWayManager.stopSynchronisation();

    }

    /**********************************************************************************************
     ********************************** PERSISTENCE METHODS ***************************************
     **********************************************************************************************/

    /**
     * Save the location, the time of the last update and the value of the requesting flag in the bundle
     * and call the super method.
     * @param savedInstanceState the bundle to put retainable value into, to save and old state.
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mLastLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        savedInstanceState.putBoolean(ON_WAY_KEY,onWay);
        savedInstanceState.putParcelable(WAY_MANAGER_KEY,mWayManager);
        savedInstanceState.putParcelable(DESTINATION_KAY,mDestination);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Updates fields based on data stored in the bundle.
     *
     * @param savedInstanceState The activity state saved in the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.e("DEBUG", "Updating values from bundle");
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        REQUESTING_LOCATION_UPDATES_KEY);
            }

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mLastLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(ON_WAY_KEY)) {
                onWay = savedInstanceState.getBoolean(ON_WAY_KEY);
            }

            if (savedInstanceState.keySet().contains(WAY_MANAGER_KEY)) {
                mWayManager = savedInstanceState.getParcelable(WAY_MANAGER_KEY);
                assert mWayManager != null;
                mWayManager.setParent(this);
            }

            if(savedInstanceState.keySet().contains(DESTINATION_KAY))
                mDestination  = savedInstanceState.getParcelable(DESTINATION_KAY);
        }
    }
}
