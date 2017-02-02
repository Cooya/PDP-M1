package mastergl.pdp;

import android.app.Activity;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Parcel;
import android.os.Vibrator;
import android.util.Log;

import java.util.StringTokenizer;

/**
 * this class run a client
 * this is where we launch the thread
 * by this way, we execute in background some tasks
 */
public class ClientServices extends LaunchServices {
    /**
     * this tab contains the itemSelected
     * we have the name of the device at posName and
     * the adress at posAdress
     */
    private String[] itemSelectedTab = new String[2];
    private static final int posName = 0;
    private static final int posAddress = 1;
    private Client client;
    private Activity act;
    /**
     * checking if the client connected to the server
     */
    boolean isConnected;

    /**
     * we need vibrator and the adapter to launch the client
     *
     * @param activity
     */
    public ClientServices(Activity activity) {
        super(new BlueAdapterConfig(activity).getMyBluetoothAdapter(), (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE));
        act = activity;
    }

    /**
     * Constructor to recreate a ClientServices from a parcel.
     *
     * @param in The parcel to construct the object from.
     */
    public ClientServices(Parcel in) {
        super(in);
    }

    /**
     * The creator WayManager to create from a parcel.
     * call the constructor WayManager(Parcel in).
     *
     * @see #ClientServices(Parcel)
     */
    public static final Creator<ClientServices> CREATOR = new Creator<ClientServices>() {
        @Override
        public ClientServices createFromParcel(Parcel in) {
            return new ClientServices(in);
        }

        @Override
        public ClientServices[] newArray(int size) {
            return new ClientServices[size];
        }
    };

    /**
     * instantiate the client
     */
    public synchronized void startClient() {
        if (itemSelectedTab[posAddress] != null && myBluetoothAdapter.isEnabled()) {
            client = new Client(myBluetoothAdapter.getRemoteDevice(itemSelectedTab[posAddress]), myBluetoothAdapter, vibrator, this);
            client.start();
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (client.isConnected())
                isConnected = client.isConnected();
        }
    }

    public synchronized void notif() {
        notify();
    }

    /**
     * this function help us to get the address we need for the client
     * as we get the address, we can get the remote device, we want to connect to
     *
     * @param current the current string to split
     */
    public void splitter(String current) {
        StringTokenizer tokens = new StringTokenizer(current, "\n");
        itemSelectedTab[posName] = tokens.nextToken();
        itemSelectedTab[posAddress] = tokens.nextToken();
    }
}
