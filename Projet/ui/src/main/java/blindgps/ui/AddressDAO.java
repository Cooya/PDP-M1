package blindgps.ui;

import java.util.Collections;
import java.util.Date;
import java.util.Vector;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.location.Address;

public class AddressDAO extends DAO {
    private static final String TABLE_NAME = "RecentAddresses";
    private static final String KEY = "id";
    private static final String TITLE = "address";
    private static final String COUNTER = "counter";
    private static final String LAST_USE = "lastUse";
    private static final String LONGITUDE = "longitude";
    private static final String LATITUDE = "latitude";

    public AddressDAO(Context context) {
        super(context);
    }

    public void insert(Address address) throws DAOException {
        if(database == null)
            open();

        // retrieve the address title
        int addressLinesNumber = address.getMaxAddressLineIndex();
        String title = "";
        for(int i = 0; i < addressLinesNumber; ++i)
            title += address.getAddressLine(i) + " ";

        // check if this address title already exists into the database
        RecentAddress recentAddress = get(title);
        if(recentAddress != null) { // it exists
            recentAddress.incCounter();
            recentAddress.updateLastUse();
            update(recentAddress); // so we update its "counter" and "last use" fields)
        }
        else { // otherwise we insert it
            // insert values in a container for database insertion
            ContentValues values = new ContentValues();
            values.put(TITLE, title);
            values.put(LAST_USE, new Date().getTime());
            values.put(LONGITUDE, address.getLongitude());
            values.put(LATITUDE, address.getLatitude());

            // insertion into database
            if(database.insert(TABLE_NAME, null, values) == -1)
                throw new DAOException("Insertion address into database has failed.");
        }
    }

    public void delete(long id) throws DAOException {
        if(database == null)
            open();

        if(database.delete(TABLE_NAME, KEY + " = " + id, null) == -1)
            throw new DAOException("Deletion address into database has failed.");
    }

    public void update(RecentAddress address) throws DAOException {
        if(database == null)
            open();

        ContentValues values = new ContentValues();
        values.put(TITLE, address.getTitle());
        values.put(COUNTER, address.getCounter());
        values.put(LAST_USE, address.getLastUse().getTime());
        values.put(LONGITUDE, address.getLongitude());
        values.put(LATITUDE, address.getLatitude());

        if(database.update(TABLE_NAME, values, KEY + " = ?", new String[]{String.valueOf(address.getId())}) == 0)
            throw new DAOException("Update address into database has failed.");
    }

    public RecentAddress get(long id) {
        if(database == null)
            open();

        Cursor cursor = database.rawQuery(
                "SELECT " + TITLE + ", " + COUNTER +
                        " FROM " + TABLE_NAME +
                        " WHERE " + KEY + " = ?", new String[]{String.valueOf(id)});
        if(!cursor.moveToNext())
            return null;
        RecentAddress recentAddress = new RecentAddress(id, cursor.getString(0), cursor.getInt(1), cursor.getLong(2), cursor.getDouble(3), cursor.getDouble(4));
        cursor.close();
        return recentAddress;
    }

    public RecentAddress get(String title) {
        if(database == null)
            open();

        Cursor cursor = database.rawQuery(
                "SELECT " + KEY + ", " + COUNTER + ", " + LAST_USE + ", " + LONGITUDE + ", " + LATITUDE +
                        " FROM " + TABLE_NAME +
                        " WHERE " + TITLE + " = ?", new String[]{title});
        if(!cursor.moveToNext())
            return null;
        RecentAddress recentAddress = new RecentAddress(cursor.getLong(0), title, cursor.getInt(1), cursor.getLong(2), cursor.getDouble(3), cursor.getDouble(4));
        cursor.close();
        return recentAddress;
    }

    public Vector<RecentAddress> getAll() {
        if(database == null)
            open();

        Vector<RecentAddress> addresses = new Vector<>();
        Cursor cursor = database.rawQuery(
                "SELECT " + KEY + ", " + TITLE + ", " + COUNTER + ", " + LAST_USE + ", " + LONGITUDE + ", " + LATITUDE +
                        " FROM " + TABLE_NAME, null);
        while(cursor.moveToNext())
            addresses.add(new RecentAddress(cursor.getLong(0), cursor.getString(1), cursor.getInt(2), cursor.getLong(3), cursor.getDouble(4), cursor.getDouble(5)));
        cursor.close();
        sortAddressVectorByDate(addresses);
        return addresses;
    }

    // very basic sort algorithm for get most recent addresses in first
    private void sortAddressVectorByDate(Vector<RecentAddress> addressVector) {
        int vectorSize = addressVector.size();
        for(int i = 0; i < vectorSize; ++i)
            for(int j = 0; j < vectorSize; ++j)
                if(addressVector.get(i).getLastUse().after(addressVector.get(j).getLastUse()))
                    Collections.swap(addressVector, i, j);
    }

    /*
    public void incrementCounter(int id) {
        if(database == null)
            open();

        database.execSQL("UPDATE " + TABLE_NAME +
                " SET " + COUNTER + " = " + COUNTER + " + 1" +
                " WHERE " + KEY + " = " + id);
    }

    public void incrementCounter(String address) {
        if(database == null)
            open();

        database.execSQL("UPDATE " + TABLE_NAME +
                " SET " + COUNTER + " = " + COUNTER + " + 1" +
                " WHERE " + TITLE + " = \"" + address + "\"");
    }
    */
}