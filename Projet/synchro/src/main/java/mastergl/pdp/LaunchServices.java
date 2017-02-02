package mastergl.pdp;

import android.bluetooth.BluetoothAdapter;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Vibrator;

import java.util.StringTokenizer;

/**
 * this class is for the services that the client and the server needs both
 * this class is extended by ServerSevices and ClientServices
 */
public class LaunchServices implements Parcelable {
    protected BluetoothAdapter myBluetoothAdapter;
    protected Vibrator vibrator;

    /**
     * super constructor for clientServices and ServerServices
     *
     * @param myBluetoLaunch represents the local device bluetooth adapter.
     * @param vibr           represents the vibrator of the device.
     */
    public LaunchServices(BluetoothAdapter myBluetoLaunch, Vibrator vibr) {
        myBluetoothAdapter = myBluetoLaunch;
        vibrator = vibr;

    }

    /**
     * Constructor to recreate a LaunchServices from a parcel.
     *
     * @param in The parcel to construct the object from.
     */
    protected LaunchServices(Parcel in) {
    }

    /**
     * The creator LaunchServices to create from a parcel.
     * call the constructor WayManager(Parcel in).
     *
     * @see #LaunchServices(Parcel)
     */
    public static final Creator<LaunchServices> CREATOR = new Creator<LaunchServices>() {
        @Override
        public LaunchServices createFromParcel(Parcel in) {
            return new LaunchServices(in);
        }

        @Override
        public LaunchServices[] newArray(int size) {
            return new LaunchServices[size];
        }
    };

    /**
     * Describe the content of the parcelable object.
     *
     * @return the number of complex field to parcel.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or PARCELABLE_WRITE_RETURN_VALUE.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
