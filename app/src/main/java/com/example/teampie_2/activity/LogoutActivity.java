package com.example.teampie_2.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.teampie_2.R;
import com.google.firebase.auth.FirebaseAuth;

public class LogoutActivity extends BaseActivity {

    //define firebase object
    FirebaseAuth firebaseAuth;

    Button logoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logout);

        //initializig firebase auth object
        firebaseAuth = FirebaseAuth.getInstance();



        logoutBtn = findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(v -> {
            if(firebaseAuth.getCurrentUser() != null){
                firebaseAuth.signOut();
                finish();
            }
            startActivity(new Intent(LogoutActivity.this, MainActivity.class));
        });
        Toolbar mToolbar = findViewById(R.id.logout_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("  ");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
