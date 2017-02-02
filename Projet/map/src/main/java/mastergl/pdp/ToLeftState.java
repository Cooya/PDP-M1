package mastergl.pdp;

import android.location.Location;
import android.os.Parcel;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Class handle actions when the next move is to turn left.
 */
public class ToLeftState extends StateDirectionsHandler {

    /**
     * Constructor to recreate a ToLeftState from a parcel.
     * @param in The parcel to construct the object from.
     */
    protected ToLeftState(Parcel in) {
        super(in);
    }

    /**
     * The creator ToLeftState to create from a parcel.
     * call the constructor ToLeftState(Parcel in).
     * @see #ToLeftState(Parcel)
     */
    public static final Creator<ToLeftState> CREATOR = new Creator<ToLeftState>() {
        @Override
        public ToLeftState createFromParcel(Parcel in) {
            return new ToLeftState(in);
        }

        @Override
        public ToLeftState[] newArray(int size) {
            return new ToLeftState[size];
        }
    };
    
    /**
     * Constructor. Set the list of points to go through and initializes the index of the next point to 1.
     * @param pointsToGoThrough the list of points to go through.
     * @param indexPolyline the index of the end of the current polyline.
     * @param indexStep the index of the next step in JSon file.
     */
    public ToLeftState(@NotNull List<LatLng> pointsToGoThrough, int indexPolyline, int indexStep) {
        super(pointsToGoThrough);
        mIndexNextPolyline = indexPolyline;
        mIndexNextStep = indexStep;
    }

    /**
     * Vibrate the phone to turn left when the user is about to meet the next point.
     * @param parent the parent to change the state when it is time.
     * @param location the new location to work with.
     */
    @Override
    public void whatAbout(StateChangeableVibrator parent,@NotNull Location location,
                          JsonParserUtility parser) {

        LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
        LatLng next = parser.getNextPoint(mIndexNextStep);
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
            parent.vibrateLeft();

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
        return "next maneuver is a left turn";
    }
}
