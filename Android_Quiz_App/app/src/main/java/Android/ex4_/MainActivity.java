package Android.ex4_;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

// https://www.youtube.com/watch?v=5lmhxob61eg

public class MainActivity extends Activity {
    TextView score, quiz, quiz2;
    Spinner options, options2;

    public int mcTimeA = TimerActivity.localSeconds;
    public int mcTimeB = TimerActivity.globalSeconds;
    private boolean running = TimerActivity.mcRunning;
    private boolean cdRunning = TimerActivity.mcCDRunning;
    private boolean wasRunning = TimerActivity.wasRunning;

    private QuizLibrary myQuizzes = new QuizLibrary();
    private int myQuizNumber = 0;
    private String myAnswer = myQuizzes.getCorrectAnswer(myQuizNumber);
    private String myAnswer2 = myQuizzes.getCorrectAnswer(myQuizNumber + 1);
    private int chances = myQuizzes.myChoices[myQuizNumber].length - 2;
    private int chances2 = myQuizzes.myChoices[myQuizNumber + 1].length - 2;

    public static int mcTimeLimit;
//    public static int countDownTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        score = (TextView) findViewById(R.id.score);
        quiz = (TextView) findViewById(R.id.Quiz);
        quiz2 = (TextView) findViewById(R.id.Quiz2);
        options = (Spinner) findViewById(R.id.options);
        options2 = (Spinner) findViewById(R.id.options2);

        mcTimeLimit = Integer.parseInt(TimerActivity.mcTimeLimit);


        if (savedInstanceState != null) {
            mcTimeB = savedInstanceState.getInt("globalSeconds");
            mcTimeA = savedInstanceState.getInt("localSeconds");
            mcTimeLimit = savedInstanceState.getInt("mcTimerCount");
            running = savedInstanceState.getBoolean("running");
            wasRunning = savedInstanceState.getBoolean("wasRunning");
        }
//        countDownTime = mcTimeB + mcTimeLimit;
        localTimer();
        globalTimer();
        countDownTimer();
    }

    public void onClickFindAnswer(View view) {
        String optionSelected = String.valueOf(options.getSelectedItem());  // Get the selected item in the spinner
        String optionSelected2 = String.valueOf(options2.getSelectedItem());
        if (optionSelected.equals("Please Select") || optionSelected2.equals("Please Select")) {
            Toast.makeText(MainActivity.this, "Please select answer", Toast.LENGTH_SHORT).show();
        } else if (optionSelected.equals(myAnswer) || optionSelected2.equals(myAnswer2)) {
            if (optionSelected.equals(myAnswer) && optionSelected2.equals(myAnswer2)) {
                QuizLibrary.myScore++;
                score.setText("Score: " + QuizLibrary.myScore);
                Toast.makeText(MainActivity.this, "Correct", Toast.LENGTH_SHORT).show();
                nextQuestion();
            } else {
                if (optionSelected.equals(myAnswer)) {
                    QuizLibrary.myScore++;
                    score.setText("Score: " + QuizLibrary.myScore);
                    Toast.makeText(MainActivity.this, "Q1 is correct", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Q1 is wrong", Toast.LENGTH_SHORT).show();
                    chances--;
                    if (chances == 0) {
                        over();
                    }
                }
                if (optionSelected2.equals(myAnswer2)) {
                    QuizLibrary.myScore++;
                    score.setText("Score: " + QuizLibrary.myScore);
                    Toast.makeText(MainActivity.this, "Q2 is correct", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Q2 is wrong", Toast.LENGTH_SHORT).show();
                    chances2--;
                    if (chances2 == 0) {
                        over();
                    }
                }
            }
        } else {
            if (!optionSelected.equals(myAnswer)) {
                Toast.makeText(MainActivity.this, "Q1 is wrong", Toast.LENGTH_SHORT).show();
                chances--;
                if (chances == 0) {
                    over();
                }
            }
            if (!optionSelected2.equals(myAnswer2)) {
                Toast.makeText(MainActivity.this, "Q2 is wrong", Toast.LENGTH_SHORT).show();
                chances2--;
                if (chances2 == 0) {
                    over();
                }
            }
        }
    }

    public void nextQuestion() {
        cdRunning = false;
        DelayedMessageService.timer = Integer.parseInt(TimerActivity.blankTimeLimit);
        if (TimerActivity.blankCDRunning) {
            Intent intent = new Intent(this, DelayedMessageService.class);
            intent.putExtra(DelayedMessageService.EXTRA_MESSAGE, getResources().getString(R.string.button_response));
            startService(intent);
        }

        TimerActivity.globalSeconds = mcTimeB;
        TimerActivity.localSeconds = mcTimeA;
        Intent intent2 = new Intent(this, BlankQuiz.class);
        startActivity(intent2);
    }

    public void over() {
        cdRunning = false;
        QuizLibrary.fail++;
        QuizLibrary.isPassed = false;
        Intent intent = new Intent(this, ResultActivity.class);
        startActivity(intent);
    }

    private void localTimer() {
        final TextView timeView = (TextView) findViewById(R.id.localTimer);
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                int hours = mcTimeA / 3600;
                int minutes = (mcTimeA % 3600) / 60;
                int secs = mcTimeA % 60;
                String time = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs);
                timeView.setText(time);
                if (running) {
                    mcTimeA++;
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
                int hours = mcTimeB / 3600;
                int minutes = (mcTimeB % 3600) / 60;
                int secs = mcTimeB % 60;
                String time = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs);
                timeView.setText(time);
                mcTimeB++;
//                if (cdRunning && mcTimeB == countDownTime) {
//                    over();
//                    return;
//                }
                handler.postDelayed(this, 1000);
            }
        });
    }

    private void countDownTimer() {
        final TextView countdownView = (TextView) findViewById(R.id.mcCountdown);
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                int sec = mcTimeLimit;
                String time = String.format(Locale.getDefault(), "%02d", sec);
                countdownView.setText(time);
                if (cdRunning) {
                    mcTimeLimit--;
                }
                if (mcTimeLimit == 0) {
                    over();
                    running = false;
                    return;
                }
                handler.postDelayed(this, 1000);
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("globalSeconds", mcTimeB);
        savedInstanceState.putInt("localSeconds", mcTimeA);
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
