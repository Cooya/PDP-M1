package blindgps.ui;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * class used by DAO class for access, creation and upgrading the database.
 */
public class DatabaseHandler extends SQLiteOpenHelper {
    private static final String ADDR_TABLE_NAME = "RecentAddresses";
    private static final String ADDR_KEY = "id";
    private static final String ADDR_TITLE = "address";
    private static final String ADDR_COUNTER = "counter";
    private static final String ADDR_LAST_USE = "lastUse";
    private static final String ADDR_LONGITUDE = "longitude";
    private static final String ADDR_LATITUDE = "latitude";

    private static final String CREATE_ADDR_TABLE =
            "CREATE TABLE " + ADDR_TABLE_NAME + "(" +
                    ADDR_KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ADDR_TITLE + " TEXT NOT NULL UNIQUE, " +
                    ADDR_COUNTER + " INTEGER DEFAULT 0, " +
                    ADDR_LAST_USE + " INTEGER NOT NULL, " +
                    ADDR_LONGITUDE + " REAL NOT NULL, " +
                    ADDR_LATITUDE + " REAL NOT NULL);";

    private static final String DROP_ADDR_TABLE = "DROP TABLE IF EXISTS " + ADDR_TABLE_NAME + ";";

    public DatabaseHandler(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_ADDR_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_ADDR_TABLE);
        onCreate(db);
    }
}
