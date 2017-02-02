package blindgps.ui;

import android.location.Address;

import java.util.Date;
import java.util.Locale;

/**
 * class that represents an recent address (extending the native class "Address") manipulated by the application
 */
public class RecentAddress extends Address {
    @SuppressWarnings("unused")
    private Creator CREATOR;

    private final long id;
    private String title;
    private int counter;
    private Date lastUse;

    public RecentAddress(long id, String title, int counter, long lastUse, double longitude, double latitude) {
        super(Locale.getDefault());
        this.id = id;
        this.title = title;
        this.counter = counter;
        this.lastUse = new Date(lastUse);
        super.setLongitude(longitude);
        super.setLatitude(latitude);
    }

    public String getTitle() {
        return this.title;
    }

    public Date getLastUse() {
        return this.lastUse;
    }

    public long getId() {
        return this.id;
    }

    public int getCounter() {
        return this.counter;
    }

    // increment use counter
    public void incCounter() {
        this.counter++;
    }

    // update the last use date to now
    public void updateLastUse() {
        this.lastUse = new Date();
    }
}