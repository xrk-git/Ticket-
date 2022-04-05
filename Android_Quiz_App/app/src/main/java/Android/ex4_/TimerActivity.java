package Android.ex4_;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import Android.ex4_.R;

// Page 130
public class TimerActivity extends Activity {
    public static int globalSeconds = 0;
    public static int localSeconds = 0;
    public static boolean running = true;
    public static boolean mcRunning = true;
    public static boolean blankRunning = true;
    public static boolean mcCDRunning = true;
    public static boolean blankCDRunning = true;
    public static boolean wasRunning;
    private EditText mcTimeLimitInput;
    private EditText blankTimeLimitInput;
    public static String mcTimeLimit;
    public static String blankTimeLimit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        mcTimeLimitInput = (EditText) findViewById(R.id.mcTimer);
        blankTimeLimitInput = (EditText) findViewById(R.id.blankTimer);

        Switch mcSwitch = (Switch) findViewById(R.id.mcSwitch);
        mcSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mcCDRunning = true;
                } else {
                    mcCDRunning = false;
                }
            }
        });

        Switch fSwitch = (Switch) findViewById(R.id.blankSwitch);
        fSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    blankCDRunning = true;
                } else {
                    blankCDRunning = false;
                }
            }
        });

        final Switch imageButton = (Switch) findViewById(R.id.toggle);
        imageButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                final ImageButton imgButton = (ImageButton)findViewById(R.id.startImageButton);
                final Button button = (Button)findViewById(R.id.startDefaultButton);
                if (isChecked) {
                    imgButton.setVisibility(View.GONE);
                    button.setVisibility(View.VISIBLE);
                } else {
                    imgButton.setVisibility(View.VISIBLE);
                    button.setVisibility(View.GONE);
                }
            }
        });

        if (savedInstanceState != null ) {
            globalSeconds = savedInstanceState.getInt("globalSeconds");
            localSeconds = savedInstanceState.getInt("localSeconds");
            running = savedInstanceState.getBoolean("running");
            wasRunning = savedInstanceState.getBoolean("wasRunning");
        }
        LocalTime();
        globalTimer();
    }

    public void onClickStart(View view) {
        mcTimeLimit = mcTimeLimitInput.getText().toString();
        blankTimeLimit = blankTimeLimitInput.getText().toString();

        if (!mcTimeLimit.equals("")) {
            DelayedMessageService.timer = Integer.parseInt(mcTimeLimit);
        }
        if (mcTimeLimit.equals("") || blankTimeLimit.equals("")) {
            Toast.makeText(TimerActivity.this, "Please set count down timer", Toast.LENGTH_SHORT).show();
        } else {
            if (mcCDRunning) {
                Intent intent = new Intent(this, DelayedMessageService.class);
                intent.putExtra(DelayedMessageService.EXTRA_MESSAGE, getResources().getString(R.string.button_response));
                startService(intent);
            }

            Intent intent2 = new Intent(this, MainActivity.class);
            startActivity(intent2);
        }
    }

    private void LocalTime() {
        final TextView timeView = (TextView)findViewById(R.id.localTimer);
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                int hours = localSeconds / 3600;
                int minutes = (localSeconds % 3600) / 60;
                int secs = localSeconds % 60;
                String time = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs);
                timeView.setText(time);
                if (running) {
                    localSeconds++;
                }
                handler.postDelayed(this,1000);
            }
        });
    }

    private void globalTimer() {
        final TextView timeView = (TextView)findViewById(R.id.globalTimer);
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                int hours = globalSeconds / 3600;
                int minutes = (globalSeconds % 3600) / 60;
                int secs = globalSeconds % 60;
                String time = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs);
                timeView.setText(time);
                globalSeconds++;
                handler.postDelayed(this,1000);
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt("globalSeconds", globalSeconds);
        savedInstanceState.putInt("localSeconds", localSeconds);
        savedInstanceState.putBoolean("running",running);
        savedInstanceState.putBoolean("wasRunning",wasRunning);

    }
}
