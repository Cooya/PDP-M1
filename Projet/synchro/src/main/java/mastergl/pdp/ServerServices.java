package mastergl.pdp;

import android.app.Activity;
import android.content.Context;
import android.os.Parcel;
import android.os.Vibrator;

/**
 * this class run a server
 * this is where we launched the thread
 * by this way, we execute in background some tasks
 */
public class ServerServices extends LaunchServices {

    private Activity act;

    /**
     * Constructor of ServerServices.
     *
     * @param activity the calling activity.
     */
    public ServerServices(Activity activity) {
        super(new BlueAdapterConfig(activity).getMyBluetoothAdapter(), (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE));
        act = activity;
    }

    /**
     * Constructor to recreate a ServerServices from a parcel.
     *
     * @param in The parcel to construct the object from.
     */
    public ServerServices(Parcel in) {
        super(in);
    }

    /**
     * The creator ServerServices to create from a parcel.
     * call the constructor ServerServices(Parcel in).
     *
     * @see #ServerServices(Parcel)
     */
    public static final Creator<ServerServices> CREATOR = new Creator<ServerServices>() {
        @Override
        public ServerServices createFromParcel(Parcel in) {
            return new ServerServices(in);
        }

        @Override
        public ServerServices[] newArray(int size) {
            return new ServerServices[size];
        }
    };

    /**
     * instantiate the server
     *
     * @return the newly instantiated server.
     */
    public Server startServer() {

        if (myBluetoothAdapter.isEnabled()) {

            Server server = new Server(myBluetoothAdapter, vibrator, act);
            server.start();

            return server;
        }
        return null;
    }

}
