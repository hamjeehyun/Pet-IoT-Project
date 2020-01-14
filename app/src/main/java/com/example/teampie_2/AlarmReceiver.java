package com.example.teampie_2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.teampie_2.sevices.FeederService;

public class AlarmReceiver extends BroadcastReceiver{

        Context context;

        @Override
        public void onReceive(Context context, Intent intent) {

            this.context = context;
            // intent로부터 전달받은 string
            String get_your_string = intent.getExtras().getString("state");
            Log.i("hamm", "onReceive of AlarmReceiver");

            // FeederService 서비스 intent 생성
            Intent service_intent = new Intent(context, FeederService.class);

            // RingtonePlayinService로 extra string값 보내기
            service_intent.putExtra("state", get_your_string);
            // start the ringtone service

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.context.startService(service_intent);
            Log.i("hamm", "end of AlarmReceiver");

        }
    }
