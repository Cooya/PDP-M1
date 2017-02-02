package mastergl.pdp;

import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ouaraka.synchro.R;

import java.util.Set;

/**
 * Test activity of the synchro activity.
 */
public class SynchroActivity extends AppCompatActivity {
    /**
     * state of bluetooth visibility
     */
    private static final int REQUEST_VISIBILITY_BT = 1;

    /**
     * state of enablility of bluetooth
     */
    private static final int REQUEST_ENABLE_BT = 1;
    BlueAdapterConfig blueAdapterConfig;//=new BlueAdapterConfig();

    /**
     * variable of server
     */
    private static Server s;
    ServerServices serverServices;
    DialogFragment newFragment;
    private Button onBtn;
    private Button offBtn;
    private Button listBtn;
    private Button findBtn;
    private TextView text;

    /**
     * BluetoothAdapter lets us perform fundamental Bluetooth tasks
     */
    private BluetoothAdapter myBluetoothAdapter;

    /**
     * list of all bluetooth devives that we've been paired to
     */
    private Set<BluetoothDevice> pairedDevices;

    /**
     * List to show for the view of our interface
     */
    private ListView myListView;

    /**
     * Called when the activity is starting.
     *
     * @param savedInstanceState the saved instance of the object
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        blueAdapterConfig = new BlueAdapterConfig(this);

        myBluetoothAdapter = blueAdapterConfig.getMyBluetoothAdapter();
        serverServices = new ServerServices(this);

        if (myBluetoothAdapter == null) {


            Toast.makeText(getApplicationContext(), "Your device does not support Bluetooth",
                    Toast.LENGTH_LONG).show();
        } else {
            text = (TextView) findViewById(R.id.text);
            //by clicking the button TurnOn we execute the action on(vibrator)
            onBtn = (Button) findViewById(R.id.turnOn);
            onBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    s = serverServices.startServer();
                }
            });
            //by clicking the button TurnOff we execute the action off(vibrator)
            offBtn = (Button) findViewById(R.id.turnOff);
            offBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    blueAdapterConfig.off(getParent());
                }
            });
            //by clicking the button "show paired devices", we execute the action list(vibrator)
            listBtn = (Button) findViewById(R.id.paired);
            listBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    list(v);

                }
            });
            //en cliquant sur le bouton search, on execute l action find(vibrator)
            findBtn = (Button) findViewById(R.id.search);
            findBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    blueAdapterConfig.find(getApplicationContext());
                    newFragment = new BlueDevicesDialogFrag(getParent());
                    newFragment.show(getFragmentManager(), "Devices");


                }
            });

            //by clicking the button gauche we execute the action gauche
            listBtn = (Button) findViewById(R.id.gauche);
            listBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    if (s != null)
                        s.writeBlue("left");
                }

            });
            //by clicking the button droite we execute the action droite
            listBtn = (Button) findViewById(R.id.droite);
            listBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    if (s != null)
                        s.writeBlue("left");


                }
            });


        }
        blueAdapterConfig.enableVisibility(this);

    }


    /**
     * list of paired devices
     *
     * @param view the current view.
     */
    public void list(View view) {
        // get paired devices
        blueAdapterConfig.setListOfPairedDevices();

        Toast.makeText(getApplicationContext(), "Show Paired Devices",
                Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }
}
