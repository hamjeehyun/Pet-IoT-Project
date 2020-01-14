package com.example.teampie_2.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.teampie_2.AlarmReceiver;
import com.example.teampie_2.R;
import com.example.teampie_2.models.Feeder;
import com.example.teampie_2.models.Post;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class NewFeederActivity extends BaseActivity {

    private TimePicker feederTimePicker;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference documentReference = db.document("Feeder/Feeder Alarm");

    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_feeder);


        // 툴바 뒤로가기
        Toolbar mToolbar = findViewById(R.id.alarm_toolbar);
        setSupportActionBar(mToolbar);

        // 툴바 색
        mToolbar.setBackgroundColor(Color.rgb(255 ,255,255));
        mToolbar.setTitleTextColor(Color.BLACK);
        getSupportActionBar().setTitle("  ");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // 타임피커 설정
        feederTimePicker = findViewById(R.id.time_picker);

        // Calendar 객체 생성
        calendar = Calendar.getInstance();

    } // onCreate
    @Override
    public void onStart() {
        super.onStart();

        documentReference.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                return;
            }

            Feeder feeder = snapshot.toObject(Feeder.class);

            feederTimePicker.setHour(Integer.parseInt(feeder.getHour()));
            feederTimePicker.setMinute(Integer.parseInt(feeder.getMinute()));

        });
    }


    public void addFeeder(View v) {
        String hour = String.valueOf(feederTimePicker.getHour());
        String minute = String.valueOf(feederTimePicker.getMinute());

        Map<String, Object> feeder = new HashMap<>();
        feeder.put("hour", hour);
        feeder.put("minute", minute);

        documentReference.set(feeder);

        finish();

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

}
