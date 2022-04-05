package Android.ex4_;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;


// https://programmer.group/solutions-to-fail-to-post-notification-on-channel-null.html
public class DelayedMessageService extends IntentService {
    public static final String EXTRA_MESSAGE = "message";
    private Handler handler;
    public static final int NOTIFICATION_ID = 5453;
    public static int timer;

    public DelayedMessageService() {
        super("DelayedMessageService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler = new Handler();
        return super.onStartCommand(intent, flags, startId);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onHandleIntent(Intent intent) {
        synchronized (this) {
            try {
                wait(timer * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        String text = intent.getStringExtra(EXTRA_MESSAGE);
        showText(text);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showText(final String text) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
            }
        });
//        M chip or Android OREO version or NEWER version NEED channl to send Notification
//        String id = "channel_1";
//        String description = "143";
//        int importance = NotificationManager.IMPORTANCE_LOW;
//        Intent actionIntent = new Intent(this, MainActivity.class);
//        PendingIntent actionPendingIntent = PendingIntent.getActivity(
//                this,
//                0,
//                actionIntent,
//                PendingIntent.FLAG_UPDATE_CURRENT);
//        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        NotificationChannel channel = new NotificationChannel(id, description, importance);
////                     channel.enableLights(true);
////                     channel.enableVibration(true);//
//        manager.createNotificationChannel(channel);
//        if (Build.VERSION.SDK_INT >= 26) {
//            //When sdk version is larger than26
//
//            Notification notification = new Notification.Builder(this, id)
//                    .setCategory(Notification.CATEGORY_MESSAGE)
//                    .setSmallIcon(R.mipmap.ic_launcher)
//                    .setContentTitle(getString(R.string.title))
//                    .setContentIntent(actionPendingIntent)
//                    .setAutoCancel(true)
//                    .build();
//            manager.notify(1, notification);
//        } else {
//            //When sdk version is less than26
//            Notification notification = new NotificationCompat.Builder(this)
//                    .setContentTitle(getString(R.string.question))
//                    .setContentIntent(actionPendingIntent)
//                    .setSmallIcon(R.mipmap.ic_launcher)
//                    .build();
//            manager.notify(1, notification);
//        }

        // Create a notification builder
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(android.R.drawable.sym_def_app_icon)
                        .setContentTitle(getString(R.string.title))
                        .setContentText(text)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setVibrate(new long[] {0, 1000})
                        .setAutoCancel(true);
        // Create an action
        Intent actionIntent = new Intent(this, MainActivity.class);
        PendingIntent actionPendingIntent = PendingIntent.getActivity(
                this,
                0,
                actionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(actionPendingIntent);
        // Issue the notification
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
