package mastergl.pdp;

import android.location.Location;
import android.os.Parcel;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Class handle actions when the next move is to turn right.
 */
public class ToRightState extends StateDirectionsHandler {

    /**
     * Constructor to recreate a ToRightState from a parcel.
     * @param in The parcel to construct the object from.
     */
    protected ToRightState(Parcel in) {
        super(in);
    }

    /**
     * The creator ToRightState to create from a parcel.
     * call the constructor ToRightState(Parcel in).
     * @see #ToRightState(Parcel)
     */
    public static final Creator<ToRightState> CREATOR = new Creator<ToRightState>() {
        @Override
        public ToRightState createFromParcel(Parcel in) {
            return new ToRightState(in);
        }

        @Override
        public ToRightState[] newArray(int size) {
            return new ToRightState[size];
        }
    };
    
    /**
     * Constructor. Set the list of points to go through and
     * initializes the index of the next point to 1.
     * @param pointsToGoThrough the list of points to go through.
     * @param indexPolyline the index of the next point of polyline.
     * @param indexStep the index of the next step in json.
     */
    public ToRightState(@NotNull List<LatLng> pointsToGoThrough, int indexPolyline, int indexStep) {
        super(pointsToGoThrough);
        mIndexNextPolyline = indexPolyline;
        mIndexNextStep = indexStep;
    }

    /**
     * Vibrate the phone to turn right when the user is about to meet the next point.
     * @param parent the parent to change the state when it is time.
     * @param location the new location to work with.
     */
    @Override
    public void whatAbout(StateChangeableVibrator parent,@NotNull Location location, JsonParserUtility parser) {
        LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
        LatLng next = mPoints.get(mIndexNextPolyline);
        double distanceTo;
        //Determine if the user is in the wrong directions
        if(isOnWrongDirections(location))
        {
            parent.changeState(new WrongDirectionState(mPoints, mIndexNextPolyline, mIndexNextStep,this));
        }
        else if((distanceTo = SphericalUtil.computeDistanceBetween(current, next)) < ACCURACY_ACTION)
        {
            updateLocation(location);

            //Vibrate to left
            parent.vibrateRight();

            if(distanceTo < 1)
            {
                //TODO Find another tests to determine when the user turns

                //change state after finding the next move
                parent.changeState(findNextState(parser));
            }
        }
    }

    @Override
    public String toString() {
        return "next maneuver is a right turn";
    }
}
