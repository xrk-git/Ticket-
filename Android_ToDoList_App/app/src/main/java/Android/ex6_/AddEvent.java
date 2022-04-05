package Android.ex6_;

import android.content.ContentValues;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddEvent extends AppCompatActivity {
    private int ID;
    private int addOrEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        Intent intent = getIntent();
        ID = intent.getIntExtra("eventID", -1);
        addOrEdit = intent.getIntExtra("addOrEdit", -1); // -1: add;  NOT -1: edit
    }


    public void onClickConfirmButton(View view) {
        String title = ((TextView) findViewById(R.id.title)).getText().toString();
        String description = ((TextView) findViewById(R.id.text)).getText().toString();
        Spinner spinner = (Spinner) findViewById(R.id.choice);
        int status = spinner.getSelectedItemPosition();

        if (addOrEdit == -1) {
            addEvent(title, description, status);
        } else if (addOrEdit != -1 && ID != -1) {
            updateEvent(ID, title, description, status);
        }
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


    private void addEvent(String title, String description, int status) {
        try {
            SQLiteOpenHelper dbHelper = new DatabaseHelper(this);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            DatabaseHelper.insertData(db, title, description, status);
            db.close();
        } catch (SQLException e) {
            Toast toast = Toast.makeText(this, "Database Unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private void updateEvent(int eventID, String title, String description, int status) {
        try {
            SQLiteOpenHelper dbHelper = new DatabaseHelper(this);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues newContent = new ContentValues();
            newContent.put("TITLE", title);
            newContent.put("DESCRIPTION", description);
            newContent.put("STATUS", status);
            db.update("EVENTS", newContent, "_id=?", new String[]{String.valueOf(eventID)});
            db.close();
        } catch (SQLException e) {
            Toast toast = Toast.makeText(this, "Database Unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}