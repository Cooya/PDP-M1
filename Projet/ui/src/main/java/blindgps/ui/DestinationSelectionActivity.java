package blindgps.ui;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import mastergl.pdp.MapActivity;

@SuppressLint("InflateParams")
public class DestinationSelectionActivity extends Activity implements View.OnTouchListener, TextWatcher, OnGeocoderResponseListener, OnNetworkTestResponseListener {
    private boolean onCreate; // allows to know if the activity is in creating or in resuming
    private boolean appIsActive; // allows to fix a bug when suggestion result is returned after app was stopped
    private boolean typingMode; // indicate if the address input is empty or not
    private LinearLayout addressesListContainer; // object that represents layout container for recent addresses list layout or suggestions list layout
    private LinearLayout recentAddressesListLayout; // object that represents recent addresses list layout
    private LinearLayout suggestionsListLayout; // object that represents address suggestions list layout
    private Vector<RecentAddress> recentAddressesListData; // object that represents addresses list data
    private List<Address> suggestionsListData; // object that represents suggestions list data
    private AddressDAO addressesTable; // object representing the access to the recent addresses table
    private GeocoderDialog geocoderDialog; // thread performing the dialog with the Android geocoder
    private boolean cancelNextTouch = false; // after a touch slide, cancel the next touch detected for avoid troubles
    private TextToSpeech textToSpeech; // object representing the voice synthesis feature
    private boolean textToSpeechSupported; // indicates if TextToSpeech feature is supported by the device

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.appIsActive = true;
        this.onCreate = true;
        this.typingMode = false;

        // initialize Android geocoder and database access object
        (this.geocoderDialog = new GeocoderDialog(this)).start();
        this.addressesTable = new AddressDAO(this);

        // initialize text to speech object
        this.textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.FRENCH);
                    textToSpeechSupported = true;
                }
                else {
                    Toast.makeText(getApplicationContext(), "Text to speech feature not supported in this Android version.", Toast.LENGTH_SHORT).show();
                    textToSpeechSupported = false;
                }
            }
        });

        // set up the layout
        setContentView(R.layout.main_activity);

        // retrieve reference to the XML object for layout container
        this.addressesListContainer = (LinearLayout) findViewById(R.id.addresses_list);

        // listener for event when input is modified (addition or deletion of characters)
        ((EditText) findViewById(R.id.dest_address)).addTextChangedListener(this);
    }

    @Override // at the activity creation, onResume is called after onCreate
    public void onResume() {
        super.onResume();

        // check the Internet connection (result in callback)
        if(!this.onCreate)
            new NetworkConnectivityTest(this);
        this.onCreate = false;

        // fill the container with recent addresses saved in database
        displayAddresses();
    }

    @Override
    public void onStop() {
        super.onStop();
        this.appIsActive = false;
    }

    @SuppressWarnings("deprecation")
    @Override // triggered when recent address or suggestion is selected
    public boolean onTouch(View view, MotionEvent event) {
        long touchDuration = event.getEventTime() - event.getDownTime();
        if(touchDuration > 50 && view instanceof TextView) { // the touch must last at least 50 ms
            if(event.getHistorySize() > 0) {
                float startX = event.getHistoricalX(0);
                float endX = event.getX();
                if(endX - startX < -20 && !this.typingMode) { // slide detected (remove action)
                    this.recentAddressesListLayout.removeView(view);
                    try {
                        this.addressesTable.delete(view.getId());
                    } catch(DAOException e) {
                        e.printStackTrace();
                    }
                }
                this.cancelNextTouch = true;
            }
            else if(!this.cancelNextTouch) {
                view.setBackgroundColor(Color.GRAY);
                double[] latLng;
                String toSpeak = "";
                try {
                    if(this.typingMode) {
                        Address selectedAddress = this.suggestionsListData.get(view.getId());
                        latLng = new double[]{selectedAddress.getLatitude(), selectedAddress.getLongitude()};
                        for(int i = 0; i < selectedAddress.getMaxAddressLineIndex(); ++i)
                            toSpeak += selectedAddress.getAddressLine(i);
                        this.addressesTable.insert(selectedAddress);
                    }
                    else {
                        RecentAddress selectedAddress = getAddressFromId(this.recentAddressesListData, view.getId());
                        if(selectedAddress == null) // normally impossible
                            return true;
                        latLng = new double[]{selectedAddress.getLatitude(), selectedAddress.getLongitude()};
                        toSpeak = selectedAddress.getTitle();
                        selectedAddress.incCounter(); // increase the use counter
                        selectedAddress.updateLastUse(); // update the last use date
                        this.addressesTable.update(selectedAddress);
                    }
                    if(textToSpeechSupported) {
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                            this.textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
                        else
                            this.textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                    }
                    goToMapActivity(latLng);
                } catch(DAOException e) {
                    e.printStackTrace();
                }
            }
            else
                this.cancelNextTouch = false;
        }
        return true;
    }

    @Override
    public void beforeTextChanged(CharSequence input, int start, int count, int after) {
        // unused method
    }

    @Override // triggered when address request input is modified
    public void onTextChanged(CharSequence input, int start, int before, int count) {
        if(before == 0 && count == 0) // it happens when activity is started
            return;
        if(before != count && count == 0) { // if address input is flushed by user
            this.typingMode = false;
            if(this.addressesListContainer.indexOfChild(this.suggestionsListLayout) != -1)
                this.addressesListContainer.removeView(this.suggestionsListLayout); // remove suggestions list from the container
            if(this.addressesListContainer.indexOfChild(this.recentAddressesListLayout) == -1)
                this.addressesListContainer.addView(this.recentAddressesListLayout); // and put back recent addresses list into the container
            return;
        }
        // user is searching a new destination
        this.typingMode = true;

        // retrieve suggestions thanks to the Android geocoder (and in return, display them)
        this.geocoderDialog.submitRequest(input.toString());
    }

    @Override
    public void afterTextChanged(Editable s) {
        // unused method
    }

    @Override // when default back button is pressed
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right); // sliding animation during the transition
    }

    // method called for transition to map activity
    private void goToMapActivity(double[] latLng) {
        this.geocoderDialog.interrupt();
        Intent intent = new Intent(this, MapActivity.class); // object that allows transition to a new activity
        intent.putExtra(getString(R.string.extra_destination_key), latLng); // pass latitude and longitude to navigation activity
        startActivity(intent); // start transition to next activity
        overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left); // sliding animation during the transition
    }

    private void displayAddresses() {
        // retrieve the complete recent addresses list from database
        this.recentAddressesListData = this.addressesTable.getAll();

        // empty the container
        emptyLayoutContainer();

        // display these recent addresses (separated by a vertical line)
        this.recentAddressesListLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.list_template, null);
        TextView recentAddress;
        //View separator;
        String addressTitle;
        for(RecentAddress address : this.recentAddressesListData) {
            recentAddress = (TextView) getLayoutInflater().inflate(R.layout.addr_template, null);
            recentAddress.setId((int) address.getId());
            addressTitle = address.getTitle();
            recentAddress.setText(addressTitle);
            recentAddress.setOnTouchListener(this);
            this.recentAddressesListLayout.addView(recentAddress);
            //separator = getLayoutInflater().inflate(R.layout.separator, null);
            //separator.setLayoutParams(new LayoutParams(1000, 1));
            //recentAddressesListLayout.addView(separator);
        }
        this.addressesListContainer.addView(this.recentAddressesListLayout);
    }

    private void displaySuggestions() {
        // empty the container
        emptyLayoutContainer();

        // display suggestions from the suggestions list got
        this.suggestionsListLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.list_template, null);
        TextView suggestionTextView;
        int suggestionsNumber = this.suggestionsListData.size();
        Address suggestion;
        int suggestionLinesNumber;
        for(int i = 0; i < suggestionsNumber; ++i) {
            suggestion = this.suggestionsListData.get(i);
            suggestionTextView = (TextView) getLayoutInflater().inflate(R.layout.addr_template, null);
            suggestionTextView.setId(i);
            String str = "";
            suggestionLinesNumber = suggestion.getMaxAddressLineIndex();
            for(int j = 0; j < suggestionLinesNumber; ++j)
                str += suggestion.getAddressLine(j) + " ";
            suggestionTextView.setText(str);
            suggestionTextView.setOnTouchListener(this);
            this.suggestionsListLayout.addView(suggestionTextView);
        }
        this.addressesListContainer.addView(this.suggestionsListLayout); // put suggestions list into the container
    }

    // empty addresses or suggestions container
    private void emptyLayoutContainer() {
        if(this.addressesListContainer.indexOfChild(this.recentAddressesListLayout) != -1)
            this.addressesListContainer.removeView(this.recentAddressesListLayout);
        if(this.addressesListContainer.indexOfChild(this.suggestionsListLayout) != -1)
            this.addressesListContainer.removeView(this.suggestionsListLayout);
    }

    // update the suggestions list
    @Override // only the UI thread can modify its views so we have to execute the update function into the main loop
    public void onGeocoderResponse(final List<Address> addresses) {
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                suggestionsListData = addresses;
                displaySuggestions();
            }
        });
    }

    @Override
    public void onNetworkTestResponse(boolean isConnected) {
        if(!isConnected)
            new InformationDialog().show(getFragmentManager(), "Error"); // display a dialog box that suggests to close the app
        else {
            // rerun the background thread for geocoder requests
            if(this.geocoderDialog == null || !this.geocoderDialog.isAlive())
                (this.geocoderDialog = new GeocoderDialog(this)).start();

            // reset the address input
            ((EditText) findViewById(R.id.dest_address)).setText("");
        }
    }

    // return the recent address from vector with its id instead of its position
    // should be better with a hash table instead of a vector (fix of last minute)
    private static RecentAddress getAddressFromId(Vector<RecentAddress> addressVector, int id) {
        for(RecentAddress address : addressVector)
            if(address.getId() == id)
                return address;
        return null;
    }

    // threads that performs the dialog with the Android geocoder
    private class GeocoderDialog extends Thread {
        private Geocoder geocoder; // native object that allows to retrieve suggestions from Android database
        private String waitingRequest; // temporary string that contains the request to proceed
        private OnGeocoderResponseListener listener;

        private GeocoderDialog(Context context) {
            this.geocoder = new Geocoder(context, Locale.FRANCE);
            this.waitingRequest = null;
            this.listener = (OnGeocoderResponseListener) context;
        }

        // submit a request to be proceed by the background thread
        private synchronized void submitRequest(String request) {
            this.waitingRequest = request;
            notify();
        }

        // retrieve suggestions from Android database
        private List<Address> retrieveSuggestions(String input) {
            try {
                return this.geocoder.getFromLocationName(input, 5);
            } catch(IOException e) {
                //e.printStackTrace(); // could be an error coming from phone
                if(appIsActive)
                    new InformationDialog().show(getFragmentManager(), "Error");
            }
            return null;
        }

        public synchronized void run() {
            String currentRequest;
            List<Address> suggestions;
            while(!isInterrupted()) {
                try {
                    wait();
                } catch(InterruptedException e) {
                    return;
                }
                currentRequest = this.waitingRequest; // retrieve the request
                this.waitingRequest = null;
                suggestions = retrieveSuggestions(currentRequest); // retrieve suggestions
                if(suggestions != null && this.waitingRequest == null && this.listener != null)
                    this.listener.onGeocoderResponse(suggestions); // update the suggestions list
            }
        }
    }
}