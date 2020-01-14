package com.example.teampie_2.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.teampie_2.R;
import com.example.teampie_2.adpater.FeederAdapter;
import com.example.teampie_2.models.Feeder;
import com.example.teampie_2.models.User;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

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

import javax.annotation.Nullable;


public class FeederListActivity extends BaseActivity implements View.OnClickListener{
    final int _REQ = 100;

    private TextView feederBtn;
    private TextView addBtn;

    private TextView TimeTextView;
    private TextView AMPMTimeTextView;


    // fire store
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference documentReference;

   // private FeederAdapter feederAdapter;
    private MqttAndroidClient mqttAndroidClient;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feeder_list);


        // 툴바 뒤로가기
        Toolbar mToolbar = findViewById(R.id.feederList_toolbar);
        setSupportActionBar(mToolbar);

        // 툴바 색
        mToolbar.setBackgroundColor(Color.rgb(255 ,255,255));
        mToolbar.setTitleTextColor(Color.BLACK);
        getSupportActionBar().setTitle("  ");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        feederBtn = findViewById(R.id.feederBtn);
        addBtn = findViewById(R.id.addBtn);

        TimeTextView = findViewById(R.id.TimeTextView);
        AMPMTimeTextView = findViewById(R.id.AMPMTimeTextView);

        feederBtn.setOnClickListener(this);
        addBtn.setOnClickListener(this);


        //setUpFeederRecyclerView();


        mqttAndroidClient = new MqttAndroidClient(this,  "tcp://" +"test.mosquitto.org"+":1883", MqttClient.generateClientId());
        try {
            IMqttToken token = mqttAndroidClient.connect(getMqttConnectionOption());
            token.setActionCallback(iMqttActionListener);

        } catch (MqttException e){
            e.printStackTrace();
        }

        mqttAndroidClient.setCallback(mqttCallback);


        // fireStore


        documentReference = FirebaseFirestore.getInstance().collection("Feeder").document("Feeder Alarm");
        documentReference.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                return;
            }

            Feeder feeder = documentSnapshot.toObject(Feeder.class);

            int hour;
            String AMPM;

            if ( Integer.parseInt(feeder.getHour()) > 12 ) {
                hour = Integer.parseInt(feeder.getHour()) - 12;
                AMPM = "PM";
                String time = hour + "시 " + feeder.getMinute() + "분";
                TimeTextView.setText(time);
                AMPMTimeTextView.setText(AMPM);
            } else {
                AMPM = "AM";
                String time = feeder.getHour() + "시 " + feeder.getMinute() + "분";
                TimeTextView.setText(time);
                AMPMTimeTextView.setText(AMPM);
            }
        });

    } // end of OnCreate


    boolean isStopped = true;
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
        private DisconnectedBufferOptions getDisconnectedBufferOptions() {
            DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
            disconnectedBufferOptions.setBufferEnabled(true);
            disconnectedBufferOptions.setBufferSize(100);
            disconnectedBufferOptions.setPersistBuffer(true);
            disconnectedBufferOptions.setDeleteOldestMessages(false);
            return disconnectedBufferOptions;
        }
        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {   //연결에 실패한경우
            Log.e("jmlee", "Failure " + exception.toString());
        }
    };
    private MqttConnectOptions getMqttConnectionOption() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setWill("aaa", "I am going offline".getBytes(), 1, true);
        return mqttConnectOptions;
    }


    @Override
    protected void onStart() {
        super.onStart();
        //feederAdapter.startListening();


    }

    @Override
    protected void onStop() {
        super.onStop();
        //feederAdapter.startListening();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addBtn:
                Intent intent = new Intent(FeederListActivity.this, NewFeederActivity.class);
                startActivityForResult(intent, _REQ);
                break;

            case R.id.feederBtn:
                publishFeeder("feeder");
                Toast.makeText(FeederListActivity.this, "강아지에게 밥을 주고 있어요!", Toast.LENGTH_SHORT).show();
                break;

        }

    }

    //음성 전송
    public void publishFeeder(String topic){

        MqttMessage message = new MqttMessage();
        try {
            mqttAndroidClient.publish(topic, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ //toolbar의 back키 눌렀을 때 동작
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }




/*
    private void setUpFeederRecyclerView() {
        Query query = feederRef.orderBy("hour", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Feeder> options = new FirestoreRecyclerOptions.Builder<Feeder>()
                .setQuery(query, Feeder.class)
                .build();

        feederAdapter = new FeederAdapter(FeederListActivity.this, options);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                feederAdapter.deleteItem(viewHolder.getAdapterPosition());

            } // end of onSwiped
        }).attachToRecyclerView(recyclerView);



    }
*/


}