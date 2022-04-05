package Android.ex6_;

import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class DetailsFragment extends Fragment {
    private long eventID;
    private LayoutInflater inflater;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            eventID = (int) savedInstanceState.getLong("id");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.inflater = inflater;
        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getView() == null) return;
        getEvent();
        setupDeleteButton();
        setupEditButton();
    }

    private void getEvent() {
        View view = getView();
        try {
            SQLiteOpenHelper listHelper = new DatabaseHelper(inflater.getContext());
            SQLiteDatabase db = listHelper.getReadableDatabase();

            Cursor cursor = db.query("EVENTS", new String[]{"TITLE", "DESCRIPTION", "STATUS"},
                    "_id=?", new String[]{String.valueOf(eventID)},
                    null, null, null, null);
            if (cursor.moveToFirst()) {
                String title = cursor.getString(0);
                String description = cursor.getString(1);
                int status = Integer.parseInt(cursor.getString(2));
                TextView titleView = view.findViewById(R.id.eventTitle);
                titleView.setText(title);
                TextView textView = view.findViewById(R.id.eventDescription);
                textView.setText(description);
                TextView statusView = view.findViewById(R.id.eventStatus);
                String res;
                switch (status) {
                    case 1:
                        res = "To do";
                        break;
                    case 2:
                        res = "In progress";
                        break;
                    case 3:
                        res = "Completed";
                        break;
                    default:
                        res = "To do";
                }
                statusView.setText(res);

            }
            db.close();
        } catch (SQLException e) {
            Toast toast = Toast.makeText(inflater.getContext(), "Database Unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private void setupEditButton() {
        View view = getView();
        Button edit_button = (Button) view.findViewById(R.id.edit);
        edit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(view.getContext(), AddEvent.class);
                intent2.putExtra("eventID", (int) eventID);
                intent2.putExtra("addOrEdit", 0);
                startActivity(intent2);
            }
        });
    }

    private void setupDeleteButton() {
        View view = getView();
        Button delete_button = (Button) view.findViewById(R.id.delete);
        delete_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    SQLiteOpenHelper todolistHelper = new DatabaseHelper(inflater.getContext());
                    SQLiteDatabase db = todolistHelper.getReadableDatabase();
                    db.delete("EVENTS", "_id=?", new String[]{String.valueOf(eventID)});
                    db.close();
                    Intent intent = new Intent(view.getContext(), MainActivity.class);
                    startActivity(intent);
                } catch (SQLException e) {
                    Toast toast = Toast.makeText(inflater.getContext(), "Database Unavailable", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("id", eventID);
    }

    public void setID(long id) {
        this.eventID = id;
    }
}