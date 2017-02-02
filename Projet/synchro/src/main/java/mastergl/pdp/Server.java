package mastergl.pdp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Vibrator;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by ouaraka on 04/02/2016.
 * * this class provides us all we need to instanciate the Server
 * the Server supposed to accept  the client connection
 * we need to call start after we instantiate the Server
 * the method start is implemented in the upper class of Server
 * this method will launch the thread
 * one thread will execute the method run() and another one will continue the execution
 */
public class Server extends Thread implements Parcelable {

    private static final int BUFFER_SIZE = 1024;
    private final BluetoothServerSocket blueServerSocket;

    private ClientServerManageData ServerManage;
    private Vibrator vibrator;
    private BluetoothSocket blueSocket;
    private String MY_UUID = new String("01e2ce9b-a28a-4108-bcab-79dd7a1dce2c");
    private DialogLeftRightHandle dialogFragment;
    private Activity activity;
    protected boolean posRight;
    /**
     * this is for two vibrations
     */
    private long[] pattern = {0, 200, 500, 200, 500};
    private static int TIME_VIBRATION = 1000;


    /**
     * * this is for instanciating a server
     * the server has to accept a client connection
     * the Server will be launch always before a client
     *
     * @param blueAdapter
     * @param vib
     * @param act
     */
    public Server(BluetoothAdapter blueAdapter, Vibrator vib, Activity act) {
        vibrator = vib;
        BluetoothServerSocket tmp = null;
        activity = act;
        try {
            // My_UUID is the UUID (read server identifier ) of the application. This value is required client side too
            tmp = blueAdapter.listenUsingRfcommWithServiceRecord("htc", UUID.fromString(MY_UUID));


        } catch (IOException e) {
        }
        blueServerSocket = tmp;
    }

    /**
     * Constructor to recreate a Server from a parcel.
     *
     * @param in The parcel to construct the object from.
     */
    protected Server(Parcel in) {
        MY_UUID = in.readString();
        blueServerSocket = null;
    }

    /**
     * The creator Server to create from a parcel.
     * call the constructor Server(Parcel in).
     *
     * @see #Server(Parcel)
     */
    public static final Creator<Server> CREATOR = new Creator<Server>() {
        @Override
        public Server createFromParcel(Parcel in) {
            return new Server(in);
        }

        @Override
        public Server[] newArray(int size) {
            return new Server[size];
        }
    };

    /**
     * Run the server thread
     * this where we accept a connection
     * We abort the connection if we cant connect
     * it is ClientServerManagerData that manage the exchange of the datas
     */
    public synchronized void run() {

        // Data buffer to store reading
        byte[] buffer = new byte[BUFFER_SIZE];
        // Bytes to read the return value to determine if one has read
        int bytes;
        blueSocket = null;
        while (true) try {
            blueSocket = blueServerSocket.accept();
            dialogFragment = new DialogLeftRightHandle();
            dialogFragment.show(activity.getFragmentManager(), "text");
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            posRight = dialogFragment.isPosRight();
            System.out.println(posRight);
            if (blueSocket != null) {
                ServerManage = new ClientServerManageData(blueSocket, vibrator);
                ServerManage.start();
                ServerManage.setPosRight(posRight);

            }
            try {
                blueServerSocket.close();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            return;
        } catch (IOException e) {
            e.printStackTrace();
            break;
        }

    }

    /**
     * this method is to write text who will be converted to bytes
     * the bytes will be send
     *
     * @param bytes the string to write to the server
     */
    public void writeBlue(String bytes) {
        if (blueSocket != null && blueSocket.isConnected()) {
            if ((bytes.equals("right") && posRight == true)
                    || (bytes.equals("left") && posRight == false))
                vibrator.vibrate(TIME_VIBRATION);
            else
                try {
                    ServerManage.writeBlue(bytes);
                } catch (Exception e) {
                }
        } else {
            if (bytes.equals("left")) {
                //vibrate once
                vibrator.vibrate(TIME_VIBRATION);
            } else if (bytes.equals("right")) {
                //vibrer twice
                vibrator.vibrate(pattern, -1);


            }
        }

    }

    /**
     * It stops listening for connections and the thread is killed
     */
    public void cancel() {
        try {
            blueServerSocket.close();
        } catch (IOException e) {
        }
    }

    public synchronized void notif() {
        notify();
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
        dest.writeString(MY_UUID);
    }

    /**
     * get the vibrator object representing the device.
     *
     * @return the vibrator.
     */
    public Vibrator getVibrator() {
        return vibrator;
    }


    /**
     * this inner class is for the dialog who shows suggested the choice of side vibrating
     * we notify the server after the choice
     */

    public class DialogLeftRightHandle extends DialogFragment {
        public boolean isPosRight() {
            return posRight;
        }

        private boolean posRight;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setMessage("Choose the direction this phone should vibrate")
                    .setPositiveButton("Right", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            posRight = true;
                            notif();

                        }
                    })
                    .setNegativeButton("Left", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            posRight = false;
                            notif();
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }
}

