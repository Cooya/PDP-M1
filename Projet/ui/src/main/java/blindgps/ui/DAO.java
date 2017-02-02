package blindgps.ui;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * abstract class that contains generic methods for database handling, specific DAO classes must extends this class
 */
public abstract class DAO {
    private final static int VERSION = 1;
    private final static String NAME = "database.db";

    /**
     * represents the access to the database (generally unique)
     */
    protected DatabaseHandler dbHandler = null;

    /**
     * represents the database object that is manipulating
     */
    protected SQLiteDatabase database = null;

    protected DAO(Context context) {
        this.dbHandler = new DatabaseHandler(context, NAME, null, VERSION);
    }

    protected SQLiteDatabase open() {
        this.database = this.dbHandler.getWritableDatabase();
        return this.database;
    }

    @SuppressWarnings("unused")
    protected void close() {
        this.database.close();
    }

    @SuppressWarnings("unused")
    protected SQLiteDatabase getDatabase() {
        return this.database;
    }
}