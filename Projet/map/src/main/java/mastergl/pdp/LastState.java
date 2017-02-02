package mastergl.pdp;

import android.location.Location;
import android.os.Parcel;

import com.google.android.gms.maps.model.LatLng;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Class which handle the very last of the way, when the next point is the last one.
 */
public class LastState extends StateDirectionsHandler {

    /**
     * Constructor to recreate a LastState from a parcel.
     * @param in The parcel to construct the object from.
     */
    protected LastState(Parcel in) {
        super(in);
    }

    /**
     * The creator LastState to create from a parcel.
     * call the constructor LastState(Parcel in).
     * @see #LastState(Parcel)
     */
    public static final Creator<LastState> CREATOR = new Creator<LastState>() {
        @Override
        public LastState createFromParcel(Parcel in) {
            return new LastState(in);
        }

        @Override
        public LastState[] newArray(int size) {
            return new LastState[size];
        }
    };
    
    /**
     * Constructor. Set the list of points to go through and initializes the index of the next point to 1.
     * @param pointsToGoThrough the list of points to go through.
     */
    public LastState(@NotNull List<LatLng> pointsToGoThrough) {
        super(pointsToGoThrough);
    }

    /**
     * Vibrate the phone when the user is about to meet the last point.
     * @param parent the parent to change the state when it is time.
     * @param location the new location to work with.
     */
    @Override
    public void whatAbout(StateChangeableVibrator parent,@NotNull Location location,JsonParserUtility parser) {

    }

    @Override
    public String toString() {
        return "Last state";
    }
}
