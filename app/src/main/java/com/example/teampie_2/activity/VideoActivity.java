package com.example.teampie_2.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.io.FileInputStream;
import java.io.IOException;


public class VideoActivity extends BaseActivity implements View.OnClickListener {

   private MqttAndroidClient mqttAndroidClient;
    WebView webView;
    MediaPlayer player;
    MediaRecorder recorder;
    String path;
    TextView infoTextView;

    public int RECORD_AUDIO_REQUEST_CODE = 10;

    FloatingActionButton fab_play, fab_record, fab_send, fab_stop, fab_pause, fab_send_before;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

       // AlertDialog(); // 시작할 때 영상통화 확인

        // 비디오
        webView = findViewById(R.id.webView);


        webView.setPadding(0,0,0,0);
        //webView.setInitialScale(100);
        //webView.getSettings().setBuiltInZoomControls(false);
        //webView.getSettings().setLoadWithOverviewMode(true);
        //webView.getSettings().setUseWideViewPort(true);
        //webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        webView.getSettings().setJavaScriptEnabled(true);

        String url ="http://192.168.43.55:8091/javascript_simple.html";
        webView.loadUrl(url);

        //String imgSrcHtml = "<html><img src='" + url + "' /></html>";
        // String imgSrcHtml = url;
        //webView.loadData(imgSrcHtml, "text/html", "UTF-8");


        infoTextView = findViewById(R.id.monitor);
        infoTextView.setVisibility(View.VISIBLE);

        mqttAndroidClient = new MqttAndroidClient(this,  "tcp://" +"test.mosquitto.org"+":1883", MqttClient.generateClientId());
        try {
            IMqttToken token = mqttAndroidClient.connect(getMqttConnectionOption());
            token.setActionCallback(iMqttActionListener);

        } catch (MqttException e){
            e.printStackTrace();
        }

        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath);

        if (!file.exists())
            file.mkdirs();

        path = file.getAbsolutePath() + "/" + "sample.wav";

        mqttAndroidClient.setCallback(mqttCallback);

        fab_record = findViewById(R.id.fab_record);
        fab_play = findViewById(R.id.fab_play);
        fab_stop = findViewById(R.id.fab_stop);
        fab_send = findViewById(R.id.fab_send);
        fab_send_before = findViewById(R.id.fab_send_before);
        fab_pause = findViewById(R.id.fab_pause);

        fab_record.setOnClickListener(this);
        fab_play.setOnClickListener(this);
        fab_stop.setOnClickListener(this);
        fab_send.setOnClickListener(this);
        fab_pause.setOnClickListener(this);

        permissionCheck(); //권한

        fab_stop.hide();
        fab_play.hide();
        fab_pause.hide();
        fab_send.hide();
    } // onCreate 끝



    IMqttActionListener iMqttActionListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            mqttAndroidClient.setBufferOpts(getDisconnectedBufferOptions());    //연결에 성공한경우
            Log.e("jmlee", "Success Connection");

            try {
                mqttAndroidClient.subscribe("jmlee/image", 0 );   //연결에 성공하면 jmlee 라는 토픽으로 subscribe함
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {   //연결에 실패한경우
            Log.e("jmlee", "Failure " + exception.toString());
        }
    };

    boolean isStopped = true;
    public void AlertDialog() {
        AlertDialog.Builder Rbuilder = new AlertDialog.Builder(this);
        Rbuilder.setTitle("영상통화")
                .setMessage("영상통화를 시작할까요?")
                .setPositiveButton("확인", (dialogInterface, i) -> {
                    Log.i("jmlee", "requestStart");
                    try {
                        isStopped = false;
                        mqttAndroidClient.publish("video", "imageStart".getBytes(), 1, false);
                        Log.i("hamm", "mqttAndroidClient");
                    }catch (MqttException e) {
                        e.printStackTrace();
                    }

                    infoTextView.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(),"주인님! 기다리고 있었어요!",Toast.LENGTH_SHORT)
                            .show();
                })
                .setNegativeButton("취소", null)
                .show();
    }


    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("알람");
        builder.setMessage("영상통화를 종료하시겠습니까");
        builder.setNegativeButton("취소", null);
        builder.setPositiveButton("종료", (dialog, which) -> {
            Log.i("hamm", "onBackPressed : requestStop");
            try {
                isStopped = true;
                mqttAndroidClient.publish("video", "imageStop".getBytes(), 0, false);
            }catch (MqttException e) {
                e.printStackTrace();
            }
            Toast.makeText(getApplicationContext(),"주인님! 이따 봐요!",Toast.LENGTH_SHORT)
                    .show();
            finish();
        });
        builder.show();
    }

    int count = 0;
    MqttCallback mqttCallback = new MqttCallback() {  //클라이언트의 콜백을 처리하는부분
        @Override
        public void connectionLost(Throwable cause) {

        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            if (topic.equals("jmlee/image") && isStopped == false){
                byte[] buffer = message.getPayload();
                Bitmap bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
                //Toast.makeText(getApplicationContext(), "사진 도착", Toast.LENGTH_LONG).show();
                Log.e("jmlee", "count="+ count++ +"received len="+buffer.length);
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {

        }
    };

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
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setWill("aaa", "I am going offline".getBytes(), 1, true);
        return mqttConnectOptions;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_record:
                fab_stop.show();
                fab_record.hide();
                startRecord();
                break;

            case R.id.fab_stop:
                fab_play.show();
                fab_stop.hide();
                fab_send.show();
                fab_send_before.hide();
                stopRecord();
                break;

            case R.id.fab_play:
                fab_pause.show();
                fab_play.hide();
                startPlay();
                break;

            case R.id.fab_pause:
                fab_play.show();
                fab_pause.hide();
                stopPlay();
                break;

            case R.id.fab_send:
                //0Intent intent = new Intent(VideoActivity.this, MyService.class);
                //intent.putExtra("mqtt", "voice");
                publishAudio("voice");
                //startService(intent);

                break;


        }
    }


    //음성 전송
    public void publishAudio(String topic){
        File f = new File(Environment.getExternalStorageDirectory()+"/sample.wav");

        byte[] bFile = readBytesFromFile(f.getPath());

        MqttMessage message = new MqttMessage();
        message.setPayload(bFile);
        try {
            mqttAndroidClient.publish(topic, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private static byte[] readBytesFromFile(String filePath) {

        FileInputStream fileInputStream = null;
        byte[] bytesArray = null;

        try {
            File file = new File(filePath);
            bytesArray = new byte[(int) file.length()];

            //read file into bytes[]
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bytesArray);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bytesArray;
    }

    public void startRecord() {
        //recorder가 null이 아니면 recorder를 중지시키고 녹음 시작
        if (recorder != null) {
            recorder.stop();
            recorder.release();
            recorder = null;
        }

        //마이크의 소리를 녹음하고, 저장할 파일 포멧을 설정
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

        recorder.setOutputFile(path);

        try {
            Toast.makeText(getApplicationContext(), "녹음을 시작합니다.", Toast.LENGTH_LONG).show();

            //녹음 준비 및 시작
            recorder.prepare();
            recorder.start();
        } catch (Exception e) {
            Log.e("SampleAudioRecorder", "Exception :", e);
        }

    }

    public void stopRecord() {
        if (recorder == null) //녹음중인 레코드가 없으면 return
            return;

        //녹음중이면 정지하고 recorder null로 초기화
        recorder.stop();
        recorder.release();
        recorder = null;


        Toast.makeText(getApplicationContext(), "녹음을 중지합니다.", Toast.LENGTH_LONG).show();
    }

    public void startPlay() {
        //player가 null이 아니면 정지 한 후 player를 null로 초기화
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
        Toast.makeText(getApplicationContext(), "녹음된 파일을 재생합니다.", Toast.LENGTH_LONG).show();

        try {
            //player에 녹음파일이 저장된 경로를 넣어주고 play
            player = new MediaPlayer();

            player.setDataSource(path);
            player.prepare();
            player.start();
        } catch (Exception e) {
            Log.e("SampleAudioRecorder", "Audio play failed :", e);
        }
    }

    public void stopPlay() {
        if (player == null)
            return;
        Toast.makeText(getApplicationContext(), "재생이 중지되었습니다.", Toast.LENGTH_LONG).show();

        player.stop();
        player.release();
        player = null;
    }


    /** 권한 확인*/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 10) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[2] == PackageManager.PERMISSION_GRANTED) {
            } else {
            }
        }
    }

    public void permissionCheck() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    RECORD_AUDIO_REQUEST_CODE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mqttAndroidClient.unregisterResources();
        mqttAndroidClient.close();
    }



}
