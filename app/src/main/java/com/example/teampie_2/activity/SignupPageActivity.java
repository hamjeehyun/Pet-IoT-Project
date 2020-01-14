package com.example.teampie_2.activity;


import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.teampie_2.R;
import com.example.teampie_2.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;


public class SignupPageActivity extends BaseActivity implements View.OnClickListener{
    //define view objects
    EditText editTextEmail;
    EditText editTextPassword;
    EditText editTextPetName;
    EditText editTextPetAge;
    EditText editTextPetWeight;

    Button buttonSignup;
    ProgressDialog progressDialog;

    //define firebase object
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        //initializig firebase auth object
        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        //initializing views
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextPetName = findViewById(R.id.editText_petName);
        editTextPetAge = findViewById(R.id.editText_petAge);
        editTextPetWeight = findViewById(R.id.editText_petWeight);

        buttonSignup = findViewById(R.id.signUpBtn);

        progressDialog = new ProgressDialog(this);

        //button click event
        buttonSignup.setOnClickListener(this);

        Toolbar mToolbar = findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("  ");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }// end of onCreate

    String email;
    String password;
    String petName;
    String petAge;
    String petWeight;
    private void signUp() {
        Log.d("sign up", "signUp");
        if (!validateForm()) {
            return;
        }

        email = editTextEmail.getText().toString();
        password = editTextPassword.getText().toString();
        petName = editTextPetName.getText().toString();
        petAge = editTextPetAge.getText().toString();
        petWeight = editTextPetWeight.getText().toString();

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        onAuthSuccess(task.getResult().getUser());
                    } else {
                        Toast.makeText(SignupPageActivity.this, "Sign Up Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void onAuthSuccess(FirebaseUser user) {
        String username = petName + "주인";

        // Write new user
        writeNewUser(user.getUid(), username, user.getEmail());

        // Go to MainActivity
        startActivity(new Intent(SignupPageActivity.this, MainActivity.class));
        finish();
    }


    private void writeNewUser(String userId, String username, String email) {
        User user = new User(username, email, "https://firebasestorage.googleapis.com/v0/b/teampie-0316.appspot.com/o/Upload-Profile%2F%EA%B7%B8%EB%A6%BC2.png?alt=media&token=faa7a6e4-c217-4cd2-975d-34c4fc871048" +
                "https://firebasestorage.googleapis.com/v0/b/teampie-0316.appspot.com/o/Upload-Profile%2F%EA%B7%B8%EB%A6%BC2.png?alt=media&token=faa7a6e4-c217-4cd2-975d-34c4fc871048", petName, petAge, petWeight);

        db.collection("Users").document(userId).set(user);
    }

    private boolean validateForm() {
        boolean result = true;
        if (TextUtils.isEmpty(editTextEmail.getText().toString())) {
            editTextEmail.setError("Required");
            result = false;
        } else {
            editTextEmail.setError(null);
        }

        if (TextUtils.isEmpty(editTextPassword.getText().toString())) {
            editTextPassword.setError("Required");
            result = false;
        } else {
            editTextPassword.setError(null);
        }

        return result;
    }

    /** 툴바 뒤로가기*/
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


    @Override
    public void onClick(View v) {
       switch (v.getId()) {
           case R.id.signUpBtn :
               signUp();
               break;
       }

    }
}
