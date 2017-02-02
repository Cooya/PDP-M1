package mastergl.pdp;

import android.location.Location;
import android.os.Parcel;

import com.google.android.gms.maps.model.LatLng;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Class which actions when the user is in the wrong directions.
 */
public class WrongDirectionState extends StateDirectionsHandler {

    /**
     * Constructor to recreate a WrongDirectionState from a parcel.
     * @param in The parcel to construct the object from.
     */
    protected WrongDirectionState(Parcel in) {
        super(in);
    }

    /**
     * The creator WrongDirectionState to create from a parcel.
     * call the constructor WrongDirectionState(Parcel in).
     * @see #WrongDirectionState(Parcel)
     */
    public static final Creator<WrongDirectionState> CREATOR = new Creator<WrongDirectionState>() {
        @Override
        public WrongDirectionState createFromParcel(Parcel in) {
            return new WrongDirectionState(in);
        }

        @Override
        public WrongDirectionState[] newArray(int size) {
            return new WrongDirectionState[size];
        }
    };
    
    /**
     * The state which were the current before the user went to a wring direction.
     * That state needs to be restored when the user start walking in a good way again.
     */
    private StateDirectionsHandler mLastStateToRecover;

    /**
     * Constructor. Set the list of points to go through and initializes the index of the next point to 1.
     * @param pointsToGoThrough the list of points to go through.
     * @param indexNextPolyline the index of the point.
     * @param indexNextStep the index of the next step in json file.
     * @param last the last state to recover if the user get back to the right way.
     */
    public WrongDirectionState(@NotNull List<LatLng> pointsToGoThrough, int indexNextPolyline, int indexNextStep, StateDirectionsHandler last) {
        super(pointsToGoThrough);
        mIndexNextPolyline = indexNextPolyline;
        mLastStateToRecover = last;
        mIndexNextStep = indexNextStep;
    }

    /**
     * Vibrate the phone when the user is not in the good directions.
     * @param parent the parent to change the state when it is time.
     * @param location the new location to work with.
     */
    @Override
    public void whatAbout(StateChangeableVibrator parent,@NotNull Location location,JsonParserUtility parser) {

        parent.vibrateWrongDirections();

        if(!isOnWrongDirections(location))
            parent.changeState(mLastStateToRecover);

        updateLocation(location);
    }

    @Override
    public String toString() {
        return "The user is on a wrong direction";
    }
}
