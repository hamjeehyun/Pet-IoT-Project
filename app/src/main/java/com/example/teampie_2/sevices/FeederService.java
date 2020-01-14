package com.example.teampie_2.sevices;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.teampie_2.activity.MainActivity;
import com.example.teampie_2.R;

public class FeederService extends Service {

    MediaPlayer mediaPlayer;
    int startId;
    boolean isRunning;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("hamm", "onStartCommand");
        Notification();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("hamm", "onDestroy of FeederService");
    }

    public void Notification() {

        String channelId = "Alarmchannel";
        String channelName = "Alarm";

        NotificationManager notifManager

                = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);

            notifManager.createNotificationChannel(mChannel);

        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext(), channelId);

        Intent notificationIntent = new Intent(getApplicationContext()

                , MainActivity.class)
                .setAction(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);

        int requestID = (int) System.currentTimeMillis();

        PendingIntent pendingIntent
                = PendingIntent.getActivity(getApplicationContext()

                , requestID

                , notificationIntent

                , PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentTitle("강아지") // required
                .setContentText("꺼억")  // required
                .setDefaults(Notification.DEFAULT_ALL) // 알림, 사운드 진동 설정
                .setAutoCancel(true) // 알림 터치시 반응 후 삭제

                .setSound(RingtoneManager

                        .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

                .setSmallIcon(android.R.drawable.btn_star)
                .setLargeIcon(BitmapFactory.decodeResource(getResources()

                        , R.drawable.ic_dog))
                .setBadgeIconType(R.drawable.ic_dog)

                .setContentIntent(pendingIntent);

        notifManager.notify(0, builder.build());

        // 사료 보내기
        Intent myServiceIntent = new Intent(FeederService.this, MyService.class);
        myServiceIntent.putExtra("mqtt", "feeder");
        startService(myServiceIntent);
    }



}
