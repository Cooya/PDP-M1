package blindgps.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import mastergl.pdp.BlueDevicesDialogFrag;

// home activity of the application
public class HomeActivity extends Activity implements View.OnClickListener, OnNetworkTestResponseListener {
    private boolean appIsActive; // it allows to know if app is active and then if we can show a dialog box or not
    private Button newItineraryButton;
    private Button launchClientButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.appIsActive = true;

        setContentView(R.layout.start_activity);
        this.newItineraryButton = (Button) findViewById(R.id.new_itinerary_button);
        this.newItineraryButton.setOnClickListener(this);
        this.launchClientButton = (Button) findViewById(R.id.launch_client_button);
        this.launchClientButton.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.appIsActive = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.appIsActive = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.appIsActive = false;
    }

    @Override
    public void onClick(View view) {
        if(view instanceof Button) {
            if(view == this.newItineraryButton) {
                new NetworkConnectivityTest(this);
            } else if(view == this.launchClientButton) {
                BlueDevicesDialogFrag devicesDialogFrag = new BlueDevicesDialogFrag(this);
                devicesDialogFrag.show(getFragmentManager(), "Devices");
            }
        }
    }

    @Override // callback that allows to retrieve the result of the network connectivity test
    public void onNetworkTestResponse(boolean isConnected) {
        if(isConnected) {
            startActivity(new Intent(this, DestinationSelectionActivity.class));
            overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left); // sliding animation during the transition
        }
        else if(this.appIsActive)
            new InformationDialog().show(getFragmentManager(), "Error"); // if app is not connected to Internet, a error box is displayed
    }
}