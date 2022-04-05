package Android.ex6_;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class ListFragment extends androidx.fragment.app.ListFragment {

    private SQLiteDatabase db;
    private Listener listener;
    private View myView;
    private LayoutInflater myInflater;
    private Cursor cursor;
    private CursorAdapter adapter;
    private static int orderIdx = 0;
    private static String type;
    String searchText = "";

    interface Listener {
        void itemClicked(long id, int position);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        myView = view;
        myInflater = inflater;

        if (savedInstanceState != null) {
            searchText = savedInstanceState.getString("searchText");
        }

        try {
            SQLiteOpenHelper todolistHelper = new DatabaseHelper(myInflater.getContext());
            db = todolistHelper.getReadableDatabase();
            cursor = db.query("EVENTS", new String[]{"_id", "TITLE", "DESCRIPTION"},
                    null, null, null, null, type);
            adapter = new SimpleCursorAdapter(myInflater.getContext(),
                    android.R.layout.simple_list_item_1, cursor,
                    new String[]{"TITLE"}, new int[]{android.R.id.text1}, 0);
            setListAdapter(adapter);
        } catch (SQLException e) {
            Toast toast = Toast.makeText(myInflater.getContext(), "Database Unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }
        updateDBWithInput(searchText);
        addButtonListener();
        addSpinnerListener();
        addSearchListener();
        return myView;
    }

    private void addSearchListener() {
        EditText editTextView = myView.findViewById(R.id.searching);
        editTextView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    searchText = editTextView.getText().toString();
                    updateDBWithInput(searchText);
                    return true;
                }
                return false;
            }
        });
    }

    private void updateDBWithInput(String searchText) {
        Cursor newCursor;
        if (searchText.length() == 0) {
            newCursor = db.query("EVENTS", new String[]{"_id", "TITLE", "DESCRIPTION"},
                    null, null, null, null, type);
        } else {
            newCursor = db.query("EVENTS", new String[]{"_id", "TITLE", "DESCRIPTION"},
                    "TITLE=?", new String[]{searchText}, null, null, type);
        }
        adapter.changeCursor(newCursor);
//        cursor = newCursor;
    }

    private void addSpinnerListener() {
        Spinner orderView = myView.findViewById(R.id.order_spinner);
        orderView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != orderIdx) {
                    orderIdx = position;
                    updateDBWithOrder(orderIdx);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

        });
    }

    private void updateDBWithOrder(int orderIdx) {
        if (orderIdx == 0) {
            type = "TIMESTAMP ASC";  // DESC
        } else {
            type = "TITLE";
        }
        Cursor newCursor = db.query("EVENTS", new String[]{"_id", "TITLE", "DESCRIPTION"},
                null, null, null, null, type);
        adapter.changeCursor(newCursor);
//        cursor = newCursor;
    }

    private void addButtonListener() {
        Button button = (Button) myView.findViewById(R.id.add_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(myInflater.getContext(), AddEvent.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.listener = (Listener) context;
    }

    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        // super.onListItemClick(l, v, position, id);
        int dbID = -1;
        if (listener != null) {
            SQLiteCursor pos = (SQLiteCursor) getListView().getItemAtPosition(position);
            pos.moveToPosition(position);
            dbID = pos.getInt(0);
            listener.itemClicked(dbID, position);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("searchText", searchText);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cursor.close();
        db.close();
    }
}