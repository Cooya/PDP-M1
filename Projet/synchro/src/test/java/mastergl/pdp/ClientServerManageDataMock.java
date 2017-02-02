package mastergl.pdp;

import android.bluetooth.BluetoothSocket;
import android.os.Vibrator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import mastergl.pdp.ClientServerManageData;

/**
 * Created by Florian on 03/04/2016.
 * This class mock ClientServerManageData.
 */
public class ClientServerManageDataMock extends ClientServerManageData {

    private static final int BUFFER_SIZE =1024 ;
    private static final long VIBRATION_TIME_MILLISEC =1000 ;
    private Socket socket;
    private InputStream inStream;
    private OutputStream outStream;
    private String stringRead;
    //this variable is to send the bytes from a socket


    /**
     * we need the socket and the vibrator for the constructor
     *
     * @param Bsocket is a mock you can't use its methods.
     * @param socket  is for getting the input and the output stream
     * @param vibrate is for the vibration
     */
    public ClientServerManageDataMock(BluetoothSocket Bsocket, Vibrator vibrate , Socket socket) {
        super(Bsocket, vibrate);
        this.socket=socket;
        inStream=null;
        outStream=null;
        try {
            inStream = socket.getInputStream();
            outStream = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void run() {
        byte[] buffer = new byte[BUFFER_SIZE];  // buffer store for the stream
        int bytes; // bytes returned from read()
        long debut;
        long fin;
        long timePing;
        boolean pingOrNot=false;


        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                debut = System.currentTimeMillis();

                while (inStream.available() < 1) {
                    fin = System.currentTimeMillis();
                    timePing = fin - debut;
                    if (timePing > 9000) {
                        cancel();
                        return;
                    }

                }




                // Read from the InputStream
                bytes = inStream.read(buffer);

                stringRead = new String(buffer, 0, bytes);
                if (stringRead.equals("left")) {
                    vibrate();
                    writeBlue("ackClient");

                }
                else if (stringRead.equals("ping")) {
                    writeBlue("ackPing");
                }
                else if (stringRead.equals("right")) {
                    vibrate();
                    writeBlue("ackServeur");
                }


            } catch (Exception e) {
                break;
            }
        }
    }



    /**
     * to write in the outputStream
     * @param bytes the string we send
     */
    public void writeBlue(String bytes) {
        try {
            outStream.write(bytes.getBytes());
        } catch (Exception e) {
        }
    }

    /* Call this to shutdown the connection */
    public void cancel() {
        try {
            inStream.close();
            outStream.close();
            socket.close();
        } catch (Exception e) {
        }
    }




}
