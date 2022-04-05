package Android.ex6_;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {

    final static String eventID = "eventID";
    private long ID = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        DetailsFragment fragment = (DetailsFragment) getSupportFragmentManager().findFragmentById(R.id.Detail);
        ID = getIntent().getLongExtra(eventID,0);
        fragment.setID(ID);
    }
}