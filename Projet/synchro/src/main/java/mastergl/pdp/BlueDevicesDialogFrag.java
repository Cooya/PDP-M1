package mastergl.pdp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Vibrator;
import android.widget.ArrayAdapter;
import android.widget.Toast;

/**
 * this is the class who shows us a dialog
 * the dialog contains the listView of all the devices in the bTArrayAdapter
 * when we select an item, we launched a Client
 * the constructor should be called after the method BlueAdapterConfig.find() if we want to search new devices
 * Otherwise we'll have the paired devices
 */
public class BlueDevicesDialogFrag extends DialogFragment {
    private ArrayAdapter bTArrayAdapter;
    private BluetoothAdapter myBluetoothAdapter;
    private Vibrator vibrator;
    private Activity activity;

    public BlueDevicesDialogFrag() {
    }

    /**
     * we instantiate all we need for the dialog and the Client
     *
     * @param parent the parent activity
     */
    @SuppressLint("ValidFragment")
    public BlueDevicesDialogFrag(Activity parent) {

        BlueAdapterConfig bac = new BlueAdapterConfig(parent);
        bTArrayAdapter = bac.getbTArrayAdapter();
        myBluetoothAdapter = bac.getMyBluetoothAdapter();
        bac.enableVisibility(parent);
        bac.find(parent);
        activity = parent;

        vibrator = (Vibrator) parent.getSystemService(Context.VIBRATOR_SERVICE);
    }

    /**
     * this is the dialog that shows as the list of devices that we can connect to
     * as we click on an item, we launch the client
     * the client is launched always before the server
     *
     * @param savedInstanceState a bundle with value to recreate.
     * @return the Dialog created.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select a device to connect with")
                .setAdapter(bTArrayAdapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int position) {
                        ClientServices clientServices = new ClientServices(getActivity());
                        clientServices.splitter((String) bTArrayAdapter.getItem(position));
                        clientServices.startClient();
                        if (clientServices.isConnected)
                            Toast.makeText(getActivity().getApplicationContext(), "the client connected sucessfully",
                                    Toast.LENGTH_LONG).show();


                    }
                });
        return builder.create();
    }
}
