package mastergl.pdp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.geometry.Point;
import com.google.maps.android.projection.SphericalMercatorProjection;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is meant to be a handler for parsing json file and drawing the way on a map.
 * At the end of the task performed in background, it draws the polyline on the map argument.
 * In the future, this class will treat all the other node of the json file.
 *
 * @see AsyncTask#execute(Object[])
 */
public class WayManager extends AsyncTask<Void,Void,Void> implements Parcelable, StateChangeableVibrator
{

    /**
     * The Json parser. A Json is an extension file which contains a structure string describing a way from point A to point B,
     * eventually passing through some waypoints. This file also contains metadata like accuracy, speed or ime guessing to
     * travel.
     * @see JsonParserUtility
     */
    private JsonParserUtility mJsonParser = null;

    /**
     * The dialog window shown when the task is performed.
     */
    private ProgressDialog mProgressDial;

    /**
     * The GoogleMap to draw on.
     */
    private GoogleMap mMap;

    /**
     * The context needed by the progress dialog.
     */
    private Context mContext;

    /**
     * Current location send by a location provider but replaces on the way defined in that class.
     */
    private Location mCurrentLocation;

    /**
     * The utility to manage LatLng and Point object.
     */
    private SphericalMercatorProjection mUtilityLatlngPoint;

    /**
     * The object which manage the state of the way, like next move, previous and next point.
     */
    private StateDirectionsHandler mStateHandler;

    /**
     * The BlueTooth server managing vibrations protocol.
     */
    private Server mServer;

    /**
     * The class launching the server.
     */
    private ServerServices mLaunchServer;

    /**
     * The parent activity, useful to BlueTooth handles in case of requesting bluetooth persmission
     * with a pop-op window.
     */
    private Activity mParent;

    /************************************************************************************************************/

    /**
     * Constructor.
     * @param map The reference of the map to draw on.
     * @param url the web link to get he json file.
     * @param c the application context.
     */
    public WayManager(@NotNull Activity parent,
                      GoogleMap map,String url,Context c,Location loc)
    {
        Log.e("JSON debug", "Instance Json parser");
        mJsonParser = new JsonParserUtility(url);
        mContext = c;
        mMap = map;
        setParent(parent);
        mCurrentLocation = loc;
        initSynchroData(mParent);
    }

    /**
     * Set the parent activity. Recall anytime the activity is rebuild to prevent
     * null pointer.
     * @param parent the parent containing the map.
     */
    public void setParent(@NotNull Activity parent)
    {
        mParent = parent;
    }

    /**
     * Constructor.
     * This constructor is just for test methods.
     */
    @VisibleForTesting
    public WayManager(List<LatLng> Points){
        mUtilityLatlngPoint = new SphericalMercatorProjection(SphericalUtil.computeArea(Points));
        mStateHandler = new InitialState(Points);
    }

    /**********************************************************************************************
     *************************** SYNCHRONISATION METHODS ******************************************
     **********************************************************************************************/
    private void initSynchroData(Activity parent)
    {
        mLaunchServer = new ServerServices(parent);
    }

    /**
     * Start the BlueTooth server.
     */
    public void startSynchronisation() {

        if (mLaunchServer != null)
            mServer = mLaunchServer.startServer();
    }

    /**
     * Stop the BlueTooth server.
     */
    public void stopSynchronisation()
    {
        if(mServer != null)
            mServer.cancel();
    }

    /**********************************************************************************************
     *************************** ASYNC TASKS OVERRIDE METHODS *************************************
     **********************************************************************************************/

    /**
     * Show a progress dialog window.
     */
    @Override
    protected void onPreExecute() {
        mProgressDial = new ProgressDialog(mContext);
        mProgressDial.setMessage("Fetching route, Please wait...");
        mProgressDial.setIndeterminate(true);
        mProgressDial.setCancelable(false);
        mProgressDial.show();
    }

    /**
     * Parse the json url in background.
     * @param params Void object. The execute() method must be called without any argument.
     * @return Void object.
     */
    @Override
    protected Void doInBackground(Void... params) {
        mJsonParser.getAndParse();
        return null;
    }

    /**
     * Hide the progress dialog window and call the draw method.
     * @param result Void object.
     */
    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        mProgressDial.hide();
        if(mJsonParser.isParsedAndReady())
            this.draw();
    }

    /**********************************************************************************************
     ******************** PARCELABLE OVERRIDE METHODS AND CONSTRUCTOR *****************************
     **********************************************************************************************/

    /**
     * Constructor to recreate a WayManager from a parcel.
     * @param in The parcel to construct the object from.
     */
    protected WayManager(Parcel in) {
        mJsonParser = in.readParcelable(JsonParserUtility.class.getClassLoader());
        mCurrentLocation = in.readParcelable(LatLng.class.getClassLoader());
        mServer = in.readParcelable(Server.class.getClassLoader());
        mLaunchServer = in.readParcelable(ServerServices.class.getClassLoader());
        mStateHandler = in.readParcelable(StateDirectionsHandler.class.getClassLoader());
    }

    /**
     * The creator WayManager to create from a parcel.
     * call the constructor WayManager(Parcel in).
     * @see #WayManager(Parcel)
     */
    public static final Creator<WayManager> CREATOR = new Creator<WayManager>() {
        @Override
        public WayManager createFromParcel(Parcel in) {
            return new WayManager(in);
        }

        @Override
        public WayManager[] newArray(int size) {
            return new WayManager[size];
        }
    };

    /**
     * Describe the content of the parcelable object.
     * @return the number of complex field to parcel.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     * @param dest The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or PARCELABLE_WRITE_RETURN_VALUE.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeParcelable(mJsonParser, flags);
        dest.writeParcelable(mCurrentLocation, flags);
        dest.writeParcelable(mServer, flags);
        dest.writeParcelable(mLaunchServer, flags);
        dest.writeParcelable(mStateHandler,flags);

    }

    /**********************************************************************************************
     ********************************** GRAPHICS METHODS ******************************************
     **********************************************************************************************/

    /**
     * Method called when the json file is stock in the JsonObject member.
     * It extracts the polyline attributes from it, draw it on the map and
     * initialize the list of points and the current location.
     */
    public void draw()
    {
        if(mMap == null || mJsonParser == null)
            return;

        Log.e("JSON debug", "draw");
        JSONArray jLegs, jSteps, jRoutes;

        PolylineOptions pList = new PolylineOptions();

        try {
            jRoutes = mJsonParser.getJSObject().getJSONArray("routes");

            /** Traversing all routes */
            for(int i=0;i<jRoutes.length();i++)
            {Log.e("JSON debug", "route "+i);
                jLegs = jRoutes.getJSONObject(i).getJSONArray("legs");

                /** Traversing all legs */
                for (int j = 0; j < jLegs.length(); j++)
                {Log.e("JSON debug", "legs "+j);
                    jSteps = jLegs.getJSONObject(j).getJSONArray("steps");

                    /** Traversing all steps */
                    for (int k = 0; k < jSteps.length(); k++)
                    {Log.e("JSON debug", "step "+k);

                        String polyline;
                        polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");

                        pList.addAll(decodePoly(polyline));
                    }
                }
            }

            pList.width(10);
            pList.color(Color.BLUE);
            pList.geodesic(true);
            pList.visible(true);

            mMap.addMarker(new MarkerOptions().position(pList.getPoints().get(0)).title("Start"));
            mMap.addMarker(new MarkerOptions().position(pList.getPoints().get(pList.getPoints().size() - 1)).title("End"));

            mMap.addPolyline(pList);

            moveCameraToBounds();

            mUtilityLatlngPoint = new SphericalMercatorProjection(SphericalUtil.computeArea(pList.getPoints()));

            mStateHandler = new InitialState(pList.getPoints());

            Log.e("JSON debug", "end draw ");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**********************************************************************************************
     ********************************** CAMERAS METHODS *******************************************
     **********************************************************************************************/

    /**
     * Move the camera to see the entire way drawn.
     */
    public void moveCameraToBounds()
    {
        List<LatLng> bounds = mJsonParser.getBounds();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds(bounds.get(0), bounds.get(1)), 100));
    }

    /**
     * Move the camera behind the current computed position.
     */
    public void moveCameraBehind(float bearing)
    {
        LatLng nPos = getLatLngCurrentLocation();

        CameraPosition cam = new CameraPosition.Builder()
                .target(nPos)
                .zoom(20)
                .bearing(bearing)
                .tilt(60)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cam));
    }

    /**********************************************************************************************
     ********************************** UTIL & LOCATION METHODS ***********************************
     **********************************************************************************************/

    /**
     * Convert a Location object to a LatLng object.
     * @return a LatLng object of the current location.
     * @see #mCurrentLocation
     */
    private LatLng getLatLngCurrentLocation()
    {
        return new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude());
    }

    /**
     * Compute the point in arguments to the way defined in that class and draw it.
     * @param current the LatLng point to compute
     */
    protected Location computeAndSetCurrentLocation(Location current) {
        /*if(mStateHandler == null || current == null)
            return null;

        float startTime = System.currentTimeMillis();

        mCurrentComputedLocation = current;

        //Turn back x y newly calculated into latitude longitude coordinates.
        LatLng tmp = mUtilityLatlngPoint.toLatLng(intersectLocationToPath(mUtilityLatlngPoint.toPoint(getLatLngCurrentLocation())));

        //Set in member.
        mCurrentComputedLocation.setLatitude(tmp.latitude);
        mCurrentComputedLocation.setLongitude(tmp.longitude);

        float endTime = System.currentTimeMillis();
        Log.e("TIME","Computing lasts " + (endTime-startTime)+ " ms");

        return mCurrentComputedLocation;*/
        return mCurrentLocation = current;
    }

    /**
     * This function moves the location given in arguments to the current path
     * following a right angle.
     * @param location a location x y format.
     * @return the intersection between the current path and the perpendicular line passing through the parameter.
     */
    public Point intersectLocationToPath(Point location)
    {
        //The previous point or the first in x y coordinates for geometry calculation.
        Point cp1 = mUtilityLatlngPoint.toPoint(mStateHandler.getPositionNextPoint());
        //the next point in x y coordinates for geometry calculation.
        Point cp2 = mUtilityLatlngPoint.toPoint(mStateHandler.getPositionPreviousPoint());

        //Finding the a and b in the equation of that line <i>y = ax + b</i>.
        double a = (cp1.y - cp2.y)/(cp1.x - cp2.x);
        double b = (cp1.y - a*cp1.x);

        //A perpendicular line to another is linked by that expression <i>a1 * a2 = -1</i>.
        double a2 = -1/a;
        //Find the b for the searched line.
        double b2 = location.y - a2*location.x;

        //Intersection point of the two line
        double lat = (b2 - b)/(a - a2);
        double lng = a2*lat + b2;

        return new Point(lat,lng);
    }

    /**
     * From the first location (basically the current one) to the next step by the north axe, give the angle between those data.
     * @param origin The first point.
     * @param to the next point to go.
     * @return an angle in degrees.
     */
    public float getAngleFromNorth(LatLng origin,LatLng to)
    {
        double lat1 = Math.toRadians(origin.latitude);
        double lat2 = Math.toRadians(to.latitude);
        double long1 = Math.toRadians(origin.longitude);
        double long2 = Math.toRadians(to.longitude);

        double longDelta = long2 - long1;
		double y = Math.sin(longDelta) * Math.cos(lat2);
		double x = Math.cos(lat1)*Math.sin(lat2) -
		Math.sin(lat1)*Math.cos(lat2)*Math.cos(longDelta);
		double angle = Math.toDegrees(Math.atan2(y, x));

		while (angle < 0) {
			angle += 360;
		}
		return (float) angle % 360;
    }

    /**
     * In a json file, the polyline attribute is encoded. This method decode it.
     * @param encoded the encoded string.
     * @return a list of LatLng object containing all the point needed to draw the polyline.
     * @see LatLng
     */
    static public List<LatLng> decodePoly(String encoded) {
        int CharHexaCodeWithUS = 0x1f;
        int CharHexaCode = 0x20;
        int conversionValue = 63;
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - conversionValue;
                result |= (b & CharHexaCodeWithUS) << shift;
                shift += 5;
            } while (b >= CharHexaCode);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - conversionValue;
                result |= (b & CharHexaCodeWithUS) << shift;
                shift += 5;
            } while (b >= CharHexaCode);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }

    /**
     * Called any time the location changes.
     * @param location the new location to work with.
     * @param onWay true if app is on way, false if not.
     * @see android.location.LocationListener
     */
    public void event(@NotNull Location location,boolean onWay)
    {
        if(onWay && mStateHandler != null)
        {
            computeAndSetCurrentLocation(location);
            mStateHandler.updateIndexPolyline(location);
            mStateHandler.whatAbout(this,location,mJsonParser);
        }
    }

    /**
     * Asks the server to vibrate the right direction protocol.
     */
    @Override
    public void vibrateRight()
    {
        mServer.writeBlue("right");
    }

    /**
     * Asks the server to vibrate the left direction protocol.
     */
    @Override
    public void vibrateLeft()
    {
        mServer.writeBlue("left");
    }

    /**
     * Asks the server to vibrate the wrong direction protocol.
     */
    @Override
    public void vibrateWrongDirections()
    {
        mServer.writeBlue("wrong");
    }

    /***********************************************************************************************
     ***************************** STATE CHANGEABLE INTERFACE **************************************
     ***********************************************************************************************/

    /**
     * Change the state of the changeable class.
     * @param state the state to set.
     * @return the new state of the changeable.
     * @see StateDirectionsHandler
     */
    public StateDirectionsHandler changeState(StateDirectionsHandler state)
    {
        if(state != null)
            return (this.mStateHandler = state);
        return this.mStateHandler;
    }

    public StateDirectionsHandler getState() {
        return mStateHandler;
    }
}
