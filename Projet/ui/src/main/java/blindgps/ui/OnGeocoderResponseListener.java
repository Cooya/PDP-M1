package blindgps.ui;

import android.location.Address;

import java.util.List;

/**
 * pattern listener
 */
public interface OnGeocoderResponseListener {
    void onGeocoderResponse(List<Address> addresses);
}