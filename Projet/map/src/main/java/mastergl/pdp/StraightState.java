package mastergl.pdp;

import android.location.Location;
import android.os.Parcel;

import com.google.android.gms.maps.model.LatLng;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * That class handles actions when the next move is to go straight.
 * @see StateDirectionsHandler
 */
public class StraightState extends StateDirectionsHandler {

    /**
     * Constructor to recreate a StraightState from a parcel.
     * @param in The parcel to construct the object from.
     */
    protected StraightState(Parcel in) {
        super(in);
    }

    /**
     * The creator StraightState to create from a parcel.
     * call the constructor StraightState(Parcel in).
     * @see #StraightState(Parcel)
     */
    public static final Creator<StraightState> CREATOR = new Creator<StraightState>() {
        @Override
        public StraightState createFromParcel(Parcel in) {
            return new StraightState(in);
        }

        @Override
        public StraightState[] newArray(int size) {
            return new StraightState[size];
        }
    };
    
    /**
     * Constructor. Set the list of points to go through and initializes the index of the next point to 1.
     * @param pointsToGoThrough the list of points to go through.
     * @param index the new index
     */
    public StraightState(@NotNull List<LatLng> pointsToGoThrough, int index) {
        super(pointsToGoThrough);
        mIndexNextPolyline = index;
    }

    /**
     * Vibrate the phone to go straight when the user is about to meet the next point.
     * @param parent the parent to change the state when it is time.
     * @param location the new location to work with.
     */
    @Override
    public void whatAbout(StateChangeableVibrator parent, @NotNull Location location,JsonParserUtility parser) {

    }

    @Override
    public String toString() {
        return "go straight";
    }
}
