package Android.ex4_;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import Android.ex4_.R;

public class ResultActivity extends Activity {
    TextView scoreText;
    TextView passNum;
    TextView failNum;
    TextView info;
    Button sendButton;
    Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        scoreText = (TextView) findViewById(R.id.answer);
        passNum = (TextView) findViewById(R.id.passes);
        failNum = (TextView) findViewById(R.id.fails);
        info = (TextView) findViewById(R.id.info);
        sendButton = (Button) findViewById(R.id.send);
        backButton = (Button) findViewById(R.id.back);

        scoreText.setText("Score: " + QuizLibrary.myScore);
        passNum.setText("Passed: " + QuizLibrary.pass);
        failNum.setText("Failed: " + QuizLibrary.fail);

        TimerActivity.running = false;

        if (QuizLibrary.isPassed) {
            info.setText("You have passed the quiz");
            QuizLibrary.isPassed = false;
        } else {
            info.setText("You have failed the quiz");
        }

        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onClickSendScore();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onClickBack();
            }
        });
    }

    // Head First Android Page 146
    public void onClickSendScore() {
        String results = ("You have passed " + QuizLibrary.pass + " times and failed " + QuizLibrary.fail + " times.");
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, results);
        String chooserTitle = getString(R.string.chooser);
        Intent chosenIntent = Intent.createChooser(intent, chooserTitle);
        startActivity(chosenIntent);
    }

    public void onClickBack() {
        DelayedMessageService.timer = MainActivity.mcTimeLimit;
        if (TimerActivity.mcCDRunning) {
            Intent intent = new Intent(this, DelayedMessageService.class);
            intent.putExtra(DelayedMessageService.EXTRA_MESSAGE, getResources().getString(R.string.button_response));
            startService(intent);
        }
        TimerActivity.running = true;
        QuizLibrary.myScore = 0;
        Intent intent2 = new Intent(this, MainActivity.class);
        startActivity(intent2);
    }
}
