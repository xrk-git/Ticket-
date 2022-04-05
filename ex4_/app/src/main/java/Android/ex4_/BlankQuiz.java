package Android.ex4_;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import Android.ex4_.R;


public class BlankQuiz extends Activity {
    QuizLibrary myQuiz = new QuizLibrary();
    Button submit;
    TextView blankQuiz;
    EditText blankAnswer;
    TextView score;
    private boolean isCorrect = false;
    private int chances = 2;

    public int blankTimerA = TimerActivity.localSeconds;
    public int blankTimerB = TimerActivity.globalSeconds;
    public static boolean mcRunning = TimerActivity.blankCDRunning;
    private boolean running = TimerActivity.blankRunning;
    private boolean cdRunning = TimerActivity.blankCDRunning;
    private boolean wasRunning = TimerActivity.wasRunning;

    public static int blankTimeLimit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank);

        blankQuiz = (TextView) findViewById(R.id.Quiz);
        blankAnswer = (EditText) findViewById(R.id.answer);
        score = (TextView) findViewById(R.id.score);
        score.setText("Score: " + QuizLibrary.myScore);
        submit = (Button) findViewById(R.id.submit);

        blankTimeLimit = Integer.parseInt(TimerActivity.blankTimeLimit);

        if (savedInstanceState != null) {
            blankTimerB = savedInstanceState.getInt("globalSeconds");
            blankTimerA = savedInstanceState.getInt("localSeconds");
            running = savedInstanceState.getBoolean("running");
            wasRunning = savedInstanceState.getBoolean("wasRunning");
        }
        localTimer();
        globalTimer();
        countDownTimer();
    }

    public void onClickGetRequest(View view) {
        if (blankAnswer.getText().toString().equalsIgnoreCase(myQuiz.blankAnswer)) {
            isCorrect = true;
        }
        TimerActivity.globalSeconds = blankTimerB;
        TimerActivity.localSeconds = blankTimerA;
        if (isCorrect == true) {
            Toast.makeText(BlankQuiz.this, "Correct", Toast.LENGTH_SHORT).show();
            QuizLibrary.myScore += 1;
            score.setText("Score: " + QuizLibrary.myScore);
            QuizLibrary.isPassed = true;
            Intent intent = new Intent(this, ResultActivity.class);
            startActivity(intent);
            QuizLibrary.pass += 1;
        } else {
            Toast.makeText(BlankQuiz.this, "Wrong", Toast.LENGTH_SHORT).show();
            chances--;
            if (chances == 0) {
                // jump to result page
                over();
            }
        }
    }

    private void localTimer() {
        final TextView timeView = (TextView) findViewById(R.id.localTimer);
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                int hours = blankTimerA / 3600;
                int minutes = (blankTimerA % 3600) / 60;
                int secs = blankTimerA % 60;
                String time = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs);
                timeView.setText(time);
                if (running) {
                    blankTimerA++;
                }
                handler.postDelayed(this, 1000);
            }
        });
    }

    private void globalTimer() {
        final TextView timeView = (TextView) findViewById(R.id.globalTimer);
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                int hours = blankTimerB / 3600;
                int minutes = (blankTimerB % 3600) / 60;
                int secs = blankTimerB % 60;
                String time = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs);
                timeView.setText(time);
                blankTimerB++;
                handler.postDelayed(this, 1000);
            }
        });
    }

    private void countDownTimer() {
        final TextView countdownView = (TextView) findViewById(R.id.blankCountdown);
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                int sec = blankTimeLimit;
                String time = String.format(Locale.getDefault(), "%02d", sec);
                countdownView.setText(time);
                if (cdRunning) {
                    blankTimeLimit--;
                    if (blankTimeLimit == 0) {
                        over();
                        return;
                    }
                }
                handler.postDelayed(this, 1000);
            }
        });
    }

    public void over() {
        running = false;
        cdRunning = false;
        QuizLibrary.fail++;
        QuizLibrary.isPassed = false;
        Intent intent = new Intent(this, ResultActivity.class);
        startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("globalSeconds", blankTimerB);
        savedInstanceState.putInt("localSeconds", blankTimerA);
        savedInstanceState.putBoolean("running", running);
        savedInstanceState.putBoolean("wasRunning", wasRunning);
    }

    @Override
    protected void onPause() {
        super.onPause();
        wasRunning = running;
        running = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (wasRunning) {
            running = true;
        }
    }
}
