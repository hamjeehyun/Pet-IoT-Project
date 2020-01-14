package com.example.teampie_2.sevices;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.teampie_2.activity.MainActivity;
import com.example.teampie_2.R;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.File;

public class MyService extends Service {

    private MqttAndroidClient mqttAndroidClient;

    private boolean noti = false; //스위치 변수

    final String publishTopic = "feeder";

    String path;

    private boolean is_feeder = false;
    private boolean is_path = false;

    @Override
    public IBinder onBind(Intent intent) {

        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mqttConnect();

        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath);

        if (!file.exists())
            file.mkdirs();

        path = file.getAbsolutePath() + "/" + "sample.wav";

        String data = intent.getStringExtra("mqtt");

        if(data.equals("feeder")) {
            is_feeder = true;
        }
        else if(data.equals("noti_on")){
            Toast.makeText(this, "Noti On", Toast.LENGTH_LONG).show();
            noti = true;
        }
        else if(data.equals("path")){
            Toast.makeText(this, "path 전송", Toast.LENGTH_LONG).show();
            is_path = true;
        }

        return START_STICKY;
    }

    private DisconnectedBufferOptions getDisconnectedBufferOptions() {
        DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
        disconnectedBufferOptions.setBufferEnabled(true);
        disconnectedBufferOptions.setBufferSize(100);
        disconnectedBufferOptions.setPersistBuffer(true);
        disconnectedBufferOptions.setDeleteOldestMessages(false);
        return disconnectedBufferOptions;
    }

    private MqttConnectOptions getMqttConnectionOption() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(true);
        mqttConnectOptions.setAutomaticReconnect(true);
        return mqttConnectOptions;
    }

    public void mqttConnect(){
        mqttAndroidClient = new MqttAndroidClient(this, "tcp://" +"broker.hivemq.com"+":1883", MqttClient.generateClientId());
        // 2번째 파라메터 : 브로커의 ip 주소 , 3번째 파라메터 : client 의 id를 지정함 여기서는 paho 의 자동으로 id를 만들어주는것

        try {
            IMqttToken token = mqttAndroidClient.connect(getMqttConnectionOption());    //mqtttoken 이라는것을 만들어 connect option을 달아줌
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    mqttAndroidClient.setBufferOpts(getDisconnectedBufferOptions());    //연결에 성공한경우

                    Log.e("Connect_success", "Success");

                    try {
                        mqttAndroidClient.subscribe("sensor",0);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }

                    if(is_feeder) {//먹이주기 버튼 클릭 시 publish로 feeder를 보냄
                        publishMessage("feeder");
                        is_feeder = false;
                    }

                    if(is_path) {
                        publishMessage("path");
                        is_path = false;
                    }

                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {   //연결에 실패한경우
                    Log.e("connect_fail", "Failure " + exception.toString());
                }

            });
        } catch (
                MqttException e)
        {
            e.printStackTrace();
        }

        mqttAndroidClient.setCallback(new MqttCallback() {  //클라이언트의 콜백을 처리하는부분
            @Override
            public void connectionLost(Throwable cause) {
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {    //모든 메시지가 올때 Callback method

                if (topic.equals("sensor") && noti) {
                    String msg = new String(message.getPayload());
                    Log.d("jmlee", msg+" 센서 도착");

                    Notification();
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });
    }

    public void publishMessage(String msg){

        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(msg.getBytes());
            mqttAndroidClient.publish(publishTopic, message);

            Toast.makeText(this, "publish 성공", Toast.LENGTH_LONG).show();

            if(!mqttAndroidClient.isConnected()){
                Toast.makeText(this, "연결 실패", Toast.LENGTH_LONG).show();
            }
        } catch (MqttException e) {
            System.err.println("Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void Notification() {

        String channelId = "channel";
        String channelName = "Channel Name";

        NotificationManager notifManager

                = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

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
                .setContentText("반려견이 주인님을 찾고있어요")  // required
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
    }
}