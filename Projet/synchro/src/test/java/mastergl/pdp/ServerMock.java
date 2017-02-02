package mastergl.pdp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.os.Vibrator;

import org.mockito.Mockito;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Florian on 03/04/2016.
 * This class mock the Server class
 */
public class ServerMock extends Server {

    private ServerSocket serverSocket;
    private Socket socket;
    private ClientServerManageDataMock manageDataMock;

    public ServerMock(){
        super(Mockito.mock(BluetoothAdapter.class),Mockito.mock(Vibrator.class),Mockito.mock(Activity.class));
        Mockito.doNothing().when(this.getVibrator()).vibrate(1000);
        try {
            serverSocket=new ServerSocket(1030);
        } catch (IOException e) {
            e.printStackTrace();
        }
        posRight=true;
    }



    public void run() {// Data buffer to store reading
        byte[] buffer = new byte[1024];
        // Bytes to read the return value to determine if one has read
        int bytes;
        socket = null;
        while (true) try {
            socket = serverSocket.accept();
            if (socket != null) {
                manageDataMock = Mockito.spy(new ClientServerManageDataMock(Mockito.mock(BluetoothSocket.class),Mockito.mock(Vibrator.class),socket));
                Mockito.doNothing().when(manageDataMock).vibrate();
                manageDataMock.start();
            }
            try {
                serverSocket.close();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            return;
        } catch (IOException e) {
            e.printStackTrace();
            break;
        }
    }


    public ClientServerManageDataMock getManageDataMock() {
        return manageDataMock;
    }

    public void writeBlue(String bytes) {
        try {
            if(!socket.isClosed()){
                if((bytes.equals("right") && posRight==true)
                        || (bytes.equals("left") && posRight==false)){
                    this.getVibrator().vibrate(1000);
                }
                else
                    manageDataMock.writeBlue(bytes);
            }
            else {
                if (bytes.equals("left")) {
                    //vibrer une fois
                    this.getVibrator().vibrate(1000);
                } else if (bytes.equals("right")) {
                    //vibrer deux fois
                    this.getVibrator().vibrate(1000);
                    this.getVibrator().vibrate(1000);
                }
            }

        } catch(Exception e){

        }

    }


    // It stops listening for connections and the thread is killed
    public void cancel() {
        try {
            serverSocket.close();
        } catch (IOException e) {
        }
    }
}
