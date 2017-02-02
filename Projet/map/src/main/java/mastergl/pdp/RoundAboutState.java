package mastergl.pdp;

import android.location.Location;
import android.os.Parcel;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Class which handles actions when users has to walk through a roundabout.
 */
public class RoundAboutState extends StateDirectionsHandler {

    /**
     * Constructor to recreate a RoundAboutState from a parcel.
     * @param in The parcel to construct the object from.
     */
    protected RoundAboutState(Parcel in) {
        super(in);
    }

    /**
     * The creator RoundAboutState to create from a parcel.
     * call the constructor RoundAboutState(Parcel in).
     * @see #RoundAboutState(Parcel)
     */
    public static final Creator<RoundAboutState> CREATOR = new Creator<RoundAboutState>() {
        @Override
        public RoundAboutState createFromParcel(Parcel in) {
            return new RoundAboutState(in);
        }

        @Override
        public RoundAboutState[] newArray(int size) {
            return new RoundAboutState[size];
        }
    };
    
    /**
     * Constructor. Set the list of points to go through and initializes the index of the next point to 1.
     * @param pointsToGoThrough the list of points to go through.
     * @param indexPolyline the index of the end of the current polyline.
     * @param indexStep the index of the next step in JSon file.
     */
    public RoundAboutState(@NotNull List<LatLng> pointsToGoThrough, int indexPolyline, int indexStep){
        super(pointsToGoThrough);
        mIndexNextPolyline = indexPolyline;
        mIndexNextStep = indexStep;
    }

    /**
     * As the other class, determine where to turn left (A walking user has to turn in a clockwise direction, to the left so.
     * When the user meet the exit of the roundabout, a new vibration to the left will be performed and the next state is
     * searched.
     * @param parent the parent to change the state when it is time.
     * @param location the new location to work with.
     * @param parser the json parser storing the way.
     */
    @Override
    public void whatAbout(StateChangeableVibrator parent, @NotNull Location location, JsonParserUtility parser) {
        LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
        LatLng next = mPoints.get(mIndexNextPolyline);

        if(SphericalUtil.computeDistanceBetween(current, next) < ACCURACY_ACTION)
        {
            parent.vibrateLeft();

            // Determine where the users turned

            if(RoundAboutExit(current))
                parent.changeState(findNextState(parser));

        }
    }

    @Override
    public String toString() {
        return "next maneuver is a round about";
    }
}
