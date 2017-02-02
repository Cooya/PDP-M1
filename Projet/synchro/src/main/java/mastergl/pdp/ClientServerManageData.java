package mastergl.pdp;

import android.bluetooth.BluetoothSocket;
import android.os.Vibrator;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * this is the class who is in charged of the managing the datas
 * this is where we get the input and the output of the socket
 * all the exchange between two devices should use this class
 * we instantiate this class after The Client or the the Server
 * we need to call start() to run the thread of this class
 */
public class ClientServerManageData extends Thread {
    private static final int BUFFER_SIZE = 1024;
    private static final long VIBRATION_TIME_MILLISEC = 1000;
    /**
     * the socket is necessary to get the inputStream or the OutputStream
     */
    private final BluetoothSocket blueSocket;

    /**
     * this variable get the the bytes that coming in
     */
    private final InputStream blueInStream;

    /**
     * this variable is to send the bytes from a socket
     */
    private final OutputStream blueOutStream;
    private Vibrator vibrator;

    /**
     * The string we read from the input Stream
     */
    private String stringRead;

    /**
     * this boolean is for the choice of side vibrating by the user
     */
    private boolean posRight;

    /**
     * setter for posRight
     *
     * @param posRight
     */
    public void setPosRight(boolean posRight) {
        this.posRight = posRight;
    }

    /**
     * waiting 15 secconds before closing the socket because of non reponse
     */
    private static int TIMEOUT_PING_DURATION = 15000;

    /**
     * we need the socket and the vibrator for the constructor
     *
     * @param socket  is for getting the input and the output stream
     * @param vibrate is for the vibration
     */
    public ClientServerManageData(BluetoothSocket socket, Vibrator vibrate) {
        blueSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        vibrator = vibrate;

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (Exception e) {
        }

        blueInStream = tmpIn;
        blueOutStream = tmpOut;
    }

    public void run() {
        byte[] buffer = new byte[BUFFER_SIZE];  // buffer store for the stream
        int bytes; // bytes returned from read()
        long debut;
        long fin;
        long timePing;
        boolean pingOrNot = false;

        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                debut = System.currentTimeMillis();
                //checking the connection
                //this is where we should launch solo mode with the client
                while (blueInStream.available() < 1) {
                    fin = System.currentTimeMillis();
                    timePing = fin - debut;
                    if (timePing > TIMEOUT_PING_DURATION) {
                        blueInStream.close();
                        blueOutStream.close();
                        blueSocket.close();
                        return;
                    }

                }

                // Read from the InputStream
                bytes = blueInStream.read(buffer);

                stringRead = new String(buffer, 0, bytes);
                if ((stringRead.equals("left") && posRight == true)
                        || (stringRead.equals("right") && posRight == false)) {
                    vibrate();
                    writeBlue("ackClient");
                } else if (stringRead.equals("ackClient"))
                    System.out.println("ackiClient");
                else if (stringRead.equals("ping")) {
                    writeBlue("ackiPing");
                } else if (stringRead.equals("ackiPing")) {
                    System.out.println("ackiPing");

                }
            } catch (Exception e) {
                break;
            }
        }
    }

    /**
     * to make vibrate the device
     */
    public void vibrate() {
        // TODO Auto-generated method stub
        vibrator.vibrate(VIBRATION_TIME_MILLISEC);
    }

    /**
     * to write in the outputStream
     *
     * @param bytes the string we send
     */
    public void writeBlue(String bytes) {
        try {
            blueOutStream.write(bytes.getBytes());
        } catch (Exception e) {
        }
    }

    /**
     * Call this to shutdown the connection
     */
    public void cancel() {
        try {
            blueSocket.close();
        } catch (Exception e) {
        }
    }
}
