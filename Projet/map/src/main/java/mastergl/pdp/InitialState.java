package mastergl.pdp;

import android.location.Location;
import android.os.Parcel;

import com.google.android.gms.maps.model.LatLng;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Initial state of the application. That class basically find the first state
 * searching the next move.
 */
public class InitialState extends StateDirectionsHandler {

    /**
     * Constructor to recreate a InitialState from a parcel.
     * @param in The parcel to construct the object from.
     */
    protected InitialState(Parcel in) {
        super(in);
    }

    /**
     * The creator InitialState to create from a parcel.
     * call the constructor InitialState(Parcel in).
     * @see #InitialState(Parcel)
     */
    public static final Creator<InitialState> CREATOR = new Creator<InitialState>() {
        @Override
        public InitialState createFromParcel(Parcel in) {
            return new InitialState(in);
        }

        @Override
        public InitialState[] newArray(int size) {
            return new InitialState[size];
        }
    };
    
    /**
     * Constructor. Set the list of points to go through and initializes the index of the next point to 1.
     * @param pointsToGoThrough the list of points to go through.
     */
    public InitialState(List<LatLng> pointsToGoThrough) {
        super(pointsToGoThrough);
    }

    /**
     * Initial handle of that method.
     * @param parent the parent to change the state when it is time.
     * @param location the new location to work with.
     */
    @Override
    public void whatAbout(StateChangeableVibrator parent,@NotNull Location location,JsonParserUtility parser) {
        mLastPosition = new LatLng(location.getLatitude(),location.getLongitude());
        parent.changeState(findNextState(parser));
    }

    @Override
    public String toString()
    {
        return "Initial state";
    }
}
