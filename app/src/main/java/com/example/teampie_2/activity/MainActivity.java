package com.example.teampie_2.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.teampie_2.R;
import com.example.teampie_2.adpater.PostAdapter;
import com.example.teampie_2.models.Post;
import com.example.teampie_2.models.User;
import com.example.teampie_2.sevices.MyService;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

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

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Nullable;


public class MainActivity extends BaseActivity implements View.OnClickListener {

    private Switch switchHouse; //강아지 알림
    private boolean is_cheked; //알림 체크 변수

    //메뉴버튼
    private TextView videoBtn;
    private TextView feederBtn;
    private TextView postBtn;

    private TextView loginBtn;
    private TextView logoutBtn;

    private TextView mydogBtn;
    private TextView freeboardBtn;

    private ToggleButton musicPlayBtnToggleButton;

    //프로필 정보
    private ImageView userImageView;
    private TextView myPetProfileTextView;
    private TextView myPetNameTextView;
    private TextView stateView;
    private TextView stateTimeView;

    String state = "???";

    //define firebase object
    FirebaseAuth firebaseAuth;
    private DocumentReference documentReference;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference allPostsRef = db.collection("AllPosts");

    private PostAdapter adapter;

    // MQTT
    private MqttAndroidClient mqttAndroidClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // 메뉴
        videoBtn = findViewById(R.id.videoBtn);
        feederBtn = findViewById(R.id.feederBtn);
        postBtn = findViewById(R.id.postBtn);

        mydogBtn = findViewById(R.id.mydogBtn);
        freeboardBtn = findViewById(R.id.recent);

        musicPlayBtnToggleButton = findViewById(R.id.musicPlayBtnToggleButton);

        // 로그인/회원가입
        loginBtn = findViewById(R.id.loginBtn);
        logoutBtn = findViewById(R.id.logoutBtn);


        // 프로필 정보
        userImageView = findViewById(R.id.userImage);
        myPetProfileTextView = findViewById(R.id.myPetProfile);
        myPetNameTextView = findViewById(R.id.myPetName);
        stateView = findViewById(R.id.state);
        stateTimeView = findViewById(R.id.stateTime);

        videoBtn.setOnClickListener(this);
        loginBtn.setOnClickListener(this);
        logoutBtn.setOnClickListener(this);
        mydogBtn.setOnClickListener(this);
        freeboardBtn.setOnClickListener(this);
        postBtn.setOnClickListener(this);
        musicPlayBtnToggleButton.setOnClickListener(this);

        musicPlayBtnToggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked == true) {
                publishMusic("music");
                Toast.makeText(MainActivity.this, "music", Toast.LENGTH_SHORT).show();
            } else {
                publishMusic("pause");
                Toast.makeText(MainActivity.this, "pause music", Toast.LENGTH_SHORT).show();
            }
        });


        //initializig firebase auth object
        firebaseAuth = FirebaseAuth.getInstance();


        logoutBtn.setVisibility(View.INVISIBLE);

        //이미 로그인 되었다면
        if (firebaseAuth.getCurrentUser() != null) {
            loginBtn.setVisibility(View.INVISIBLE);
            logoutBtn.setVisibility(View.VISIBLE);

            String userkey = firebaseAuth.getUid();
            documentReference = FirebaseFirestore.getInstance().collection("Users").document(userkey);
            documentReference.addSnapshotListener((documentSnapshot, e) -> {
                if (e != null) {
                    return;
                }

                User user = documentSnapshot.toObject(User.class);
                String profile = user.getPetage() + "살,  " + user.getPetweight() + "kg";
                myPetNameTextView.setText(user.getPetname());
                myPetProfileTextView.setText(profile);

                String u = user.getPetImageUri();
                Toast.makeText(this, u, Toast.LENGTH_SHORT).show();


                new DownloadImageTask(userImageView)
                        .execute(u);
            });

        }

        final Intent AlarmListActivity = new Intent(MainActivity.this, FeederListActivity.class);
        feederBtn.setOnClickListener(v -> {
            AlarmListActivity.putExtra("mqtt", "feeder");
            startActivity(AlarmListActivity);

        });


        // recyclerView
        setUpRecyclerView();


        //MQTT 노래
        mqttAndroidClient = new MqttAndroidClient(this, "tcp://" + "broker.mqttdashboard.com" + ":1883", MqttClient.generateClientId());
        try {
            IMqttToken token = mqttAndroidClient.connect(getMqttConnectionOption());
            token.setActionCallback(iMqttActionListener);
            
        } catch (MqttException e) {
            e.printStackTrace();
        }


        // 음성센서
        Intent intent = new Intent(MainActivity.this, MyService.class);
        intent.putExtra("mqtt", "noti_on");
        startService(intent);


    } // end of onCreate

    public void subscribe(){
        mqttAndroidClient.setCallback(new MqttCallback() {  //클라이언트의 콜백을 처리하는부분
            @Override
            public void connectionLost(Throwable cause) {
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {    //모든 메시지가 올때 Callback method

                long now = System.currentTimeMillis();
                Date date = new Date(now);
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");

                String getTime = sdf.format(date);

                stateTimeView.setText(getTime);
                if (topic.equals("state")) {
                    state = new String(message.getPayload());

                    if(state.equals("smile")) {
                        stateView.setText("지금 정말 행복해요!");
                    } else if(state.equals("sad")) {
                        stateView.setText("보고싶어요!");
                    } else if(state.equals("sleep")) {
                        stateView.setText("지금은 꿈나라 여행 중");
                    }
                    //stateView.setText(state);
                    Log.d("jmlee", state + " 센서 도착");
                }

            }


            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();

        subscribe();

       // stateView.setText(state);

    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    /** MQTT */
    IMqttActionListener iMqttActionListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            mqttAndroidClient.setBufferOpts(getDisconnectedBufferOptions());    //연결에 성공한경우
            try {
                mqttAndroidClient.subscribe("state", 0);
            } catch (MqttException e) {
                e.printStackTrace();
            }
            Log.e("hamm", "Success Connection Music");

        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {   //연결에 실패한경우
            Log.e("jmlee", "Failure " + exception.toString());
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


    //음성 전송
    public void publishMusic(String topic){
        MqttMessage message = new MqttMessage();

        try {
            mqttAndroidClient.publish(topic, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    /** 최신글 리스트 */
    private void setUpRecyclerView() {
        Query query = allPostsRef.orderBy("date", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Post> options = new FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post.class)
                .build();

        adapter = new PostAdapter(MainActivity.this, options);

        RecyclerView recyclerView = findViewById(R.id.main_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean canScrollHorizontally() {
                return false;
            }

            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        recyclerView.setAdapter(adapter);
    }




    @Override
    protected void onStop() {
        super.onStop();
        adapter.startListening();
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.videoBtn:
                startActivity(new Intent(this, VideoActivity.class));
                break;

            case R.id.loginBtn:
                startActivity(new Intent(this, LoginActivity.class));
                break;

            case R.id.logoutBtn:
                startActivity(new Intent(this, LogoutActivity.class));
                break;

            case R.id.mydogBtn:
                startActivity(new Intent(this, MyDogActivity.class));
                break;

            case R.id.recent:
                startActivity(new Intent(this, PostListActivity.class));
                break;

            case R.id.postBtn:
                startActivity(new Intent(this, PostListActivity.class));
                break;
        }
    }




}