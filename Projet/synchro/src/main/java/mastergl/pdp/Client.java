package mastergl.pdp;

import android.app.Activity;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import android.os.Vibrator;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

/**
 * this class provides us all we need to instanciate the client
 * the client supposed to connect to the server
 * we need to call start after we instantiate the Client
 * the method start is implemented in the upper class of Client
 * this method will launch the thread
 * one thread will execute the method run() and another one will continue the execution
 */
public class Client extends Thread {
    private static final int PING_TIME_MILLISEC = 5000;
    BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket blueSocket = null;
    private final BluetoothDevice device;
    /**
     * the manager of the data.
     */
    private ClientServerManageData clientManage;
    private static final int BUFFER_SIZE = 1024;
    private Vibrator vibrator;
    private ClientServices clientSer;

    public boolean isConnected() {
        return connected;
    }

    private boolean connected;

    /**
     * MY_UUID is the same as the serve one.
     * It allows to guarantee the access and the security of the exchange of data.
     */
    private String MY_UUID = new String("01e2ce9b-a28a-4108-bcab-79dd7a1dce2c");

    /**
     * this is for instantiating a client
     * the client will be the device who want to connect to a server
     * the client will be launch always after a server is launched
     *
     * @param dev            represents a remote bluetooth device.
     * @param blue           represents the local device Bluetooth adapter
     * @param vib            class that operates the vibrator on the device.
     * @param clientServices the services of the client.
     */
    public Client(BluetoothDevice dev, BluetoothAdapter blue, Vibrator vib, ClientServices clientServices) {

        BluetoothSocket tmp = null;
        device = dev;
        mBluetoothAdapter = blue;
        vibrator = vib;
        clientSer = clientServices;

        //to connect with the selected device
        try {

            //creating the socket
            tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));


        } catch (IOException e) {
        }
        blueSocket = tmp;


    }

    /**
     * Run the client thread
     * this where we try the connection top a server
     * We abort the connection if we cant connect
     * it is ClientServerManagerData that manage the exchange of the datas
     * We send a ping to the server each
     */
    public void run() {

        //string read by reception cof what the server would send
        String read;

        byte[] buffer = new byte[BUFFER_SIZE];
        int bytes; // back in from reading


        mBluetoothAdapter.cancelDiscovery();

        try {
            ///Trying to connect with another device

            blueSocket.connect();

        } catch (IOException connectException) {
            //if any connexion is established, the socket is closed
            try {

                blueSocket.close();
            } catch (IOException closeException) {
            }
            return;
        }

        connected = true;
        clientSer.notif();

        clientManage = new ClientServerManageData(blueSocket, vibrator);
        clientManage.start();

        //send the ping to the server
        while (true) {
            try {
                sleep(PING_TIME_MILLISEC);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            writeBlue("ping");
        }


    }


    /**
     * this method is to write text who will be converted to bytes
     * the bytes will be send
     *
     * @param bytes the string to write to the server
     */
    public void writeBlue(String bytes) {
        try {
            clientManage.writeBlue(bytes);
        } catch (Exception e) {
        }
    }

    /**
     * this function is to close a socket and stop a connection
     */
    public void cancel() {
        try {
            blueSocket.close();
        } catch (IOException e) {
        }
    }

}

