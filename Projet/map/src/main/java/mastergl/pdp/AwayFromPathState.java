package mastergl.pdp;

import android.location.Location;
import android.os.Parcel;

import com.google.android.gms.maps.model.LatLng;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Class extends StateDirectionsHandler and override the whatabout method to handles actions when
 * the user is away from the path. The class recalculates a way, re-execute the async task of WayManager to
 * reparse a JSon stream.
 * @see StateDirectionsHandler
 */
public class AwayFromPathState extends StateDirectionsHandler {

    /**
     * Constructor to recreate a AwayFromPathState from a parcel.
     * @param in The parcel to construct the object from.
     */
    protected AwayFromPathState(Parcel in) {
        super(in);
    }

    /**
     * The creator AwayFromPathState to create from a parcel.
     * call the constructor AwayFromPathState(Parcel in).
     * @see #AwayFromPathState(Parcel)
     */
    public static final Creator<AwayFromPathState> CREATOR = new Creator<AwayFromPathState>() {
        @Override
        public AwayFromPathState createFromParcel(Parcel in) {
            return new AwayFromPathState(in);
        }

        @Override
        public AwayFromPathState[] newArray(int size) {
            return new AwayFromPathState[size];
        }
    };

    /**
     * Constructor. Set the list of points to go through.
     *
     * @param pointsToGoThrough the list of points to go through.
     * @param indexPolyline the index of the end of the current polyline.
     * @param indexStep the index of the next step in JSon file.
     */
    public AwayFromPathState(@NotNull List<LatLng> pointsToGoThrough,int indexPolyline,int indexStep) {
        super(pointsToGoThrough);
        mIndexNextPolyline = indexPolyline;
        mIndexNextStep = indexStep;
    }

    /**
     * That function recalculates a way, re-execute the async task of waymanger to
     * reparse a JSon stream.
     * @param parent the object to change the state.
     * @param location the new location to work with.
     * @param parser the Json Parser. NOTE: In that overridden methods, this parameter will not be used because
     *               a new JsonParserUtility will be instantiated.
     */
    @Override
    public void whatAbout(StateChangeableVibrator parent,@NotNull Location location,JsonParserUtility parser) {

    }

    @Override
    public String toString()
    {
        return"away from paste";
    }
}
