package mastergl.pdp;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.geometry.Point;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Abstract class managing state of the way. It determines in whatAbout methods if the location given in parameters
 * is away from the next points, if the user is well on the road, his directions,...
 *
 * @see InitialState
 * @see ToLeftState
 * @see ToRightState
 * @see RoundAboutState
 * @see WrongDirectionState
 * @see AwayFromPathState
 * @see LastState
 */
public abstract class StateDirectionsHandler implements Parcelable {

    protected static final int LIMIT_LEFT_ANGLE = 120;
    //protected static final int LIMIT_RIGHT_ANGLE = 240;
    protected static final int ACCURACY_ACTION = 5;
    protected static final double NON_ANGLE_VALUE = 400;

    /**
     * The list of all points of polyline to go through.
     * Careful, this is NOT the point in the json file.
     */
    protected List<LatLng> mPoints;

    /**
     * The last location of the user. Set to determine if he is in the wrong directions
     */
    protected LatLng mLastPosition;

    /**
     * The index in the list of points of polyline pointing the next point to go through.
     * @see #mPoints
     */
    protected int mIndexNextPolyline = 1;

    /**
     * The index of the next step in json file.
     */
    protected int mIndexNextStep = 1;

    /**
     * Constructor. Set the list of points to go through and initializes the index of the next point to 1.
     * @param pointsToGoThrough the list of points to go through.
     */
    public StateDirectionsHandler(@NotNull List<LatLng> pointsToGoThrough) {
        mPoints = pointsToGoThrough;
    }

    /**
     * Recreate a State from a parcel.
     * @param in the parcel to create the object.
     */
    public StateDirectionsHandler(Parcel in) {

    }

    /**
     * @return the geographic position of the next point to go through.
     */
    public LatLng getPositionNextPoint()
    {
        return mPoints.get(mIndexNextPolyline);
    }

    /**
     * @return the list of point where the way passes through.
     */
    public List<LatLng> getPoints() {
        return mPoints;
    }

    /**
     * @return the geographic position of the previous point the user passed through.
     */
    public LatLng getPositionPreviousPoint()
    {
        return mPoints.get(mIndexNextPolyline - 1);
    }

    /**
     * Get the direction of the user, determined by its last location and the current one.
     * @param location the current location.
     * @return a angle in the range [-180,180[
     */
    public double getHeading(LatLng location) {
        if(mLastPosition == null)
            return NON_ANGLE_VALUE;
        else
        {
            return SphericalUtil.computeHeading(mLastPosition,location);
        }
    }

    /**
     * Update the last known position, used to determine direction.
     * @param location the new last known position.
     */
    protected void updateLocation(Location location)
    {
        mLastPosition = new LatLng(location.getLatitude(),location.getLongitude());
    }

    /**
     * That function look at the point after the one pointed by the indices mIndexNext and find the angle
     * between the current location, the next point and the point after, using the law of cosinus.
     * If that angle is more than 0 and less than 120 degrees, the new state is ToLeftState.
     * If that angle is more than 240 and less than 360, the new state is ToRightState.
     *
     * \(  angle = \arccos(A^2 + B^2 - C^2 \over 2AB) \)
     *
     * Function called when the user is on a roundabout and the app has to determine where
     * to turn left to come out.
     * @param location the current location
     * @return the new state of the app.
     * @see RoundAboutState
     */
    protected boolean RoundAboutExit(LatLng location)
    {
        if(mPoints == null || mPoints.size() <= mIndexNextPolyline)
            return false;

        LatLng nextPoint = mPoints.get(mIndexNextPolyline);
        LatLng afterPoint = mPoints.get(mIndexNextPolyline + 1);

        //The distance from location to next
        double distanceLocNext = Math.toRadians(SphericalUtil.computeDistanceBetween(location, nextPoint));

        //The distance from next to after
        double distanceNextAfter = Math.toRadians(SphericalUtil.computeDistanceBetween(nextPoint, afterPoint));

        //The distance from location to after the next point
        double distanceLocAfter = Math.toRadians(SphericalUtil.computeDistanceBetween(location, afterPoint));

        double angle = Math.toDegrees(Math.acos( (distanceLocNext*distanceLocNext + distanceNextAfter*distanceNextAfter
                - distanceLocAfter*distanceLocAfter ) / (2*distanceLocNext*distanceNextAfter)));

        System.out.println(angle);

        return !(angle <= 0 || angle >= LIMIT_LEFT_ANGLE);
    }

    /**
     * Determine, parsing the json stream in the parameter, what is the next move.
     * @param parser the object storing the json stream.
     * @return the new state to work with.
     *
     * @see StateDirectionsHandler
     */
    public StateDirectionsHandler findNextState(JsonParserUtility parser)
    {
        boolean initial = (this instanceof InitialState);
        String nextManeuver = parser.getNextManeuver(mIndexNextStep);

        int inc = 1;
        if(initial)
            inc = 0;

        if(nextManeuver.equals("turn-left")
                || nextManeuver.equals("turn-sharp-left")
                || nextManeuver.equals("uturn-left")
                || nextManeuver.equals("fork-left")
                || nextManeuver.equals("ramp-left"))
        {
            Log.e("NAVIGATION", nextManeuver + " "+ this);

            return new ToLeftState(mPoints, mIndexNextPolyline +inc, mIndexNextStep +inc);
        }

        if(nextManeuver.equals("turn-right")
                || nextManeuver.equals("turn-sharp-right")
                || nextManeuver.equals("uturn-right")
                || nextManeuver.equals("fork-right")
                || nextManeuver.equals("ramp-right"))
        {
            Log.e("NAVIGATION",nextManeuver+" "+this);
            return new ToRightState(mPoints, mIndexNextPolyline +inc, mIndexNextStep +inc);
        }

        if(nextManeuver.equals("roundabout-right"))
        {
            Log.e("NAVIGATION",nextManeuver+" "+this);
            return new RoundAboutState(mPoints, mIndexNextPolyline+inc, mIndexNextStep +inc);
        }

        return this;
    }

    /**
     * Determine if the user is in the wrong directions, using the last known position before the one
     * given in parameter.
     * @param location the current location.
     * @return true if the user is in the wrong direction, false if not.
     */
    protected boolean isOnWrongDirections(Location location)
    {
        LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
        LatLng next = mPoints.get(mIndexNextPolyline);

        //Determine if the user is in the wrong directions
        return mLastPosition == null ||
                SphericalUtil.computeDistanceBetween(mLastPosition, next) <
                        SphericalUtil.computeDistanceBetween(current, next) + location.getAccuracy();
    }

    /**
     * That function determines the position of the user on the polyline, to update the index
     * of the next point (basically, the end of the current segment). That function must be capable of
     * changing index when the user move to another segment of the polyline because the information of current segment
     * is necessary to compute location on it, or the heading at the first state.
     * @param location the current location.
     * @see WayManager#getAngleFromNorth(LatLng, LatLng)
     * @see WayManager#intersectLocationToPath(Point)
     */
    public void updateIndexPolyline(Location location) {
        // TODO
    }

    /**
     * Update the state with the new location given in parameters.
     * Determines the next, the next move, the distance to it, whether the user is on the right path or not,
     * his directions and do actions if the next point is reached.
     * @param location the new location to work with.
     * @see Server
     */
    public abstract void whatAbout(StateChangeableVibrator parent,@NotNull Location location, JsonParserUtility parser);

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
