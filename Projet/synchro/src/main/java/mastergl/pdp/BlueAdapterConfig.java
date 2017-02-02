package mastergl.pdp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.Set;

/**
 * this class is made to initialize the blueAdpater.
 * this class should be called before any using of the BlueAdapter or others classes
 * the call of the contructor should be the first in the the activity for using of the blueAdapter and the services that use the blueAdapter
 */
public class BlueAdapterConfig implements Parcelable {
    /**
     * state of bluetooth visibility
     */
    private static final int REQUEST_VISIBILITY_BT = 1;

    /**
     * state of enablility of bluetooth
     */
    private static final int ITEM_SELECTED_POSITION = 1;
    private static final int REQUEST_ENABLE_BT = 1;

    /**
     * this is the default bluetoothAdapter
     */
    private BluetoothAdapter myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    /**
     * list of all bluetooth devives that we've been paired to
     */
    private Set<BluetoothDevice> pairedDevices;
    private static final int DURATION_VISIBILITY = 300;

    /**
     * Constructor to recreate a BlueAdapterConfig from a parcel.
     *
     * @param in The parcel to construct the object from.
     */
    protected BlueAdapterConfig(Parcel in) {
    }

    /**
     * The creator BlueAdapterConfig to create from a parcel.
     * call the constructor WayManager(Parcel in).
     *
     * @see #BlueAdapterConfig(Parcel)
     */
    public static final Creator<BlueAdapterConfig> CREATOR = new Creator<BlueAdapterConfig>() {
        @Override
        public BlueAdapterConfig createFromParcel(Parcel in) {
            return new BlueAdapterConfig(in);
        }

        @Override
        public BlueAdapterConfig[] newArray(int size) {
            return new BlueAdapterConfig[size];
        }
    };

    /**
     * Get the bluetooth array adapter.
     *
     * @return the bluetooth array adapter.
     * @see ArrayAdapter
     */
    public ArrayAdapter<String> getbTArrayAdapter() {
        return bTArrayAdapter;
    }

    /**
     * the list of bluetooth currently detected and displayed in the application list
     */
    private ArrayAdapter<String> bTArrayAdapter;

    /**
     * Constructor de create ArrayAdapter of string
     * In this ArrayAdapter we'll put the informations about the devices we're interested to
     * we have the name and the address of the device
     *
     * @param context the current context of the application
     */
    public BlueAdapterConfig(Context context) {
        bTArrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1);
        if (myBluetoothAdapter == null) {

            Toast.makeText(context, "Your device does not support Bluetooth",
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * this function provides us the list of paired devices
     * we put the paired devices in our BTarrayAdapter who contains the devices information
     */
    public void setListOfPairedDevices() {
        // get paired devices
        pairedDevices = myBluetoothAdapter.getBondedDevices();

        // put it'server one to the adapter
        for (BluetoothDevice device : pairedDevices) {
            if (bTArrayAdapter.getPosition(device.getName() + " \n" + device.getAddress()) < 0) {
                bTArrayAdapter.add(device.getName() + " \n" + device.getAddress());
            }
        }
    }

    /**
     * detection of all the surrounding Bluetooth and added to the list
     * this anonymous class provides the name and the adress of the device
     * these informations are added in the bTArrayAdapter
     */
    private final BroadcastReceiver bReceive = new BroadcastReceiver() {
        @Override

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (bTArrayAdapter.getPosition(device.getName() + " \n" + device.getAddress()) < 0) {
                    bTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    bTArrayAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    /**
     * this function gives us the current devices that we detected
     *
     * @return bReveive
     */
    public BroadcastReceiver getbReceive() {
        return bReceive;
    }

    /**
     * * Boot of the search Bluetooth peripherals by clicking once
     * or stop the search by clicking on the button a second
     * view bouton search
     *
     * @param context
     */
    public void find(Context context) {
        if (myBluetoothAdapter.isDiscovering()) {
            // the button is pressed when it discovers, so cancel the discovery
            myBluetoothAdapter.cancelDiscovery();
        }
        setListOfPairedDevices();
        myBluetoothAdapter.startDiscovery();
        context.registerReceiver(bReceive, new IntentFilter(BluetoothDevice.ACTION_FOUND));
    }

    /**
     * this function shows us a dialog and ask us to enable our bluetooth
     * this function set on the visibility of our device for DURATION_VISIBILITY
     * and we turn on the bluetooth
     *
     * @param activity
     */
    public void enableVisibility(Activity activity) {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DURATION_VISIBILITY);
        activity.startActivityForResult(discoverableIntent, REQUEST_VISIBILITY_BT);
    }

    /**
     * this function allows us to activate the bluetooth if it is not already done
     * it also displays the user that Bluetooth has been activated
     *
     * @param activity
     */
    public void on(Activity activity) {
        Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        turnOnIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DURATION_VISIBILITY);

        activity.startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);

    }

    /**
     * this function disable the bluetooth adapter
     *
     * @param activity
     */
    public void off(Activity activity) {
        myBluetoothAdapter.disable();

    }

    /**
     * this function return the bluetoothAdapter
     *
     * @return the bluetooth adapter of the class.
     * @see BluetoothAdapter
     */
    public BluetoothAdapter getMyBluetoothAdapter() {
        return myBluetoothAdapter;
    }

    /**
     * this is the settter for mybluetoothAdapter
     *
     * @param myBluetoothAdapter
     */
    public void setMyBluetoothAdapter(BluetoothAdapter myBluetoothAdapter) {
        this.myBluetoothAdapter = myBluetoothAdapter;
    }

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
