package Android.ex6_;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "eventList";
    private static final int DB_VERSION = 2;

    public DatabaseHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        updateDatabase(db, 0, DB_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        updateDatabase(db, oldVersion, newVersion);
    }

    public static void insertData(SQLiteDatabase db, String title, String description, int status) {
        ContentValues newContent = new ContentValues();
        newContent.put("TITLE", title);
        newContent.put("DESCRIPTION", description);
        newContent.put("STATUS", status);
        db.insert("EVENTS", null, newContent);
    }

    private void updateDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= 1) {
            db.execSQL("CREATE TABLE EVENTS (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "TITLE TEXT," +
                    "DESCRIPTION TEXT," +
                    "STATUS INTEGER," +
                    "TIMESTAMP DATETIME DEFAULT CURRENT_TIMESTAMP);");
            insertData(db, "test", "This is a event test", 2);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion == 1) deleteTable(db);
    }

    private void deleteTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE EVENTS");
    }
}
