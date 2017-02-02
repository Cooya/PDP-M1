package mastergl.pdp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Vibrator;

import org.mockito.Mockito;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by Florian on 03/04/2016.
 * This class is a mock of Client class.
 */
public class ClientMock extends Client {
    private Socket socket;
    private ClientServerManageDataMock manageDataMock;
    private static final int BUFFER_SIZE =1024 ;
    private static final long VIBRATION_TIME_MILLISEC =1000 ;
    private static final int PING_TIME_MILLISEC = 5000;


    /**
     * this is for instanciating a client
     * the client will be the device who want to connect to a server
     * the client will be launch always after a server is launched
     */
    public ClientMock() {
        super(Mockito.mock(BluetoothDevice.class),Mockito.mock(BluetoothAdapter.class),Mockito.mock(Vibrator.class),Mockito.mock(ClientServices.class));
        try {
            socket = new Socket(InetAddress.getLocalHost(),1030);
        } catch (IOException e) {
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
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

        manageDataMock = Mockito.spy(new ClientServerManageDataMock(Mockito.mock(BluetoothSocket.class),Mockito.mock(Vibrator.class),socket));
        Mockito.doNothing().when(manageDataMock).vibrate();
        manageDataMock.start();
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
     *this method is to write text who will be converted to bytes
     * the bytes will be send
     * @param bytes the string to write to the server
     */

    public void writeBlue(String bytes) {
        try {
            manageDataMock.writeBlue(bytes);
        } catch (Exception e) {
        }
    }

    /**
     * this function is to close a socket and stop a connection
     */
    public void cancel() {
        manageDataMock.cancel();
    }


    public ClientServerManageDataMock getManageDataMock() {
        return manageDataMock;
    }
}
