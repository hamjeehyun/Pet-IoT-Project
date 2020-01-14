package com.example.teampie_2.activity;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.teampie_2.R;
import com.example.teampie_2.models.Feeder;
import com.example.teampie_2.models.Post;
import com.example.teampie_2.models.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class MyDogActivity extends BaseActivity implements View.OnClickListener {

    private Uri inputFileUri, outputFileUri;
    private ImageView profileImageView;


    private TextView EmailTextView;
    private EditText editUserName;
    private EditText editPetName;
    private EditText editPetAge;
    private EditText editPetWeight;
    private Button uploadProfileImageBtn;



    FirebaseAuth firebaseAuth;

    // upload
    private StorageReference storageRef;
    private DatabaseReference databaseRef;
    StorageReference fileReference;
    private StorageTask upLoadTask;
    CollectionReference UserRef;


    private FirebaseStorage storage;

    boolean isImage;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference documentReference;

    String userkey;
    String uploadId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_dog);

        // 툴바
        Toolbar profileToolbar = findViewById(R.id.myDog_toolbar);
        setSupportActionBar(profileToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        profileToolbar.setBackgroundColor(Color.rgb(255 ,255,255));
        profileToolbar.setTitleTextColor(Color.BLACK);

        getSupportActionBar().setTitle("  ");

        uploadProfileImageBtn = findViewById(R.id.uploadProfileImageBtn);
        uploadProfileImageBtn.setOnClickListener(this);

        // edit profile
        EmailTextView = findViewById(R.id.editTextEmail);
        editUserName = findViewById(R.id.username);
        editPetName =  findViewById(R.id.editText_petName);
        editPetAge = findViewById(R.id.editText_petAge);
        editPetWeight = findViewById(R.id.editText_petWeight);
        profileImageView = findViewById(R.id.profileImageView);

        // firebase 로그인이 되어있으면
        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() != null) {
            userkey = firebaseAuth.getUid();
            documentReference = db.document("Users/" + userkey);
            documentReference = FirebaseFirestore.getInstance().collection("Users").document(userkey);
        }

        // storage upload
        storageRef  = FirebaseStorage.getInstance().getReference("Upload-Profile");
        databaseRef = FirebaseDatabase.getInstance().getReference("Upload-Profile");

        isImage = false;

        // 현재 로그인 사용자 가져오기


    }// end of onCreate
    private void uploadImage(){
        SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd-hh:mm");
        Date now = new Date();
        String filename = formatter.format(now) + ".png";

        fileReference = storageRef.child("/profile" + filename);
        upLoadTask = fileReference.putFile(inputFileUri);

        upLoadTask.continueWithTask((Continuation<UploadTask.TaskSnapshot, Task<Uri>>) task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }

            // Continue with the task to get the download URL
            return fileReference.getDownloadUrl();
        }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();
                String downloadURL = downloadUri.toString();
                uploadId = databaseRef.push().getKey();

                User uploadImage = new User(username, useremail, downloadURL, petname, petage, petweight); // getDownLoadUrl

                databaseRef.child(uploadId).setValue(uploadImage);

                UserRef.add(new User(username, useremail, downloadURL, petname, petage, petweight));

            } else {
                // Handle failures
                // ...
            }
        });
    }

    String useremail;
    @Override
    protected void onStart() {
        super.onStart();
        documentReference.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                return;
            }

            User user = snapshot.toObject(User.class);

            EmailTextView.setText(user.getUseremail());
            useremail = user.getEmail();
            editUserName.setText(user.getUsername());
            editPetName.setText(user.getPetname());
            editPetAge.setText(user.getPetage());
            editPetWeight.setText(user.getPetweight());

            String u = user.getPetImageUri();


            new DownloadImageTask(profileImageView).execute(u);
        });
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

    String username;
    String petname;
    String petage;
    String petweight;
    String petImageUri;
    public void settingProfile(View v) {
        username = editUserName.getText().toString();
        petname = editPetName.getText().toString();
        petage = editPetAge.getText().toString();
        petweight = editPetWeight.getText().toString();

        if (upLoadTask != null && upLoadTask.isInProgress()) {
           // Toast.makeText(MyDogActivity.this, "Upload in progress", Toast.LENGTH_SHORT).show();

        } else {
            uploadImage();
        }

        if (petname.trim().isEmpty() || petage.isEmpty() || petweight.isEmpty() || username.isEmpty()) {
            Toast.makeText(this, "빈칸을 모두 채워주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("useremail", useremail);
        user.put("petname", petname);
        user.put("petage", petage);
        user.put("petweight", petweight);
        user.put("petImageUri",petImageUri);

        documentReference.set(user);

        finish();
    }



    /** 버튼 클릭*/
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.uploadProfileImageBtn:
                isImage = true;
                DialogInterface.OnClickListener cameraListener = (dialog, which) -> doTakePhotoAction();
                DialogInterface.OnClickListener albumListener = (dialog, which) -> doTakeAlbumAction();

                DialogInterface.OnClickListener cancelListener = (dialog, which) -> dialog.dismiss();

                new AlertDialog.Builder(this)
                        .setTitle("업로드할 이미지 선택")
                        .setPositiveButton("사진촬영", cameraListener)
                        .setNeutralButton("앨범선택", albumListener)
                        .setNegativeButton("취소", cancelListener)
                        .show();
                break;
        }
    }

    /**카메라에서 사진 촬영 */
    public void doTakePhotoAction() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        ContentValues values = new ContentValues(1);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
        inputFileUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, inputFileUri);
        takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        startActivityForResult(takePictureIntent, 10000);
    }

    /**앨범에서 이미지 가져오기*/
    public void doTakeAlbumAction() {
        Intent pickPictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
        pickPictureIntent.setType("image/*");
        if (pickPictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(pickPictureIntent, 10001);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10000 && resultCode == RESULT_OK) {
            cropImage();
        }
        if (requestCode == 10001 && resultCode == RESULT_OK) {
            inputFileUri = data.getData();
            cropImage();
        }
        if (requestCode == 10003 && resultCode == RESULT_OK) {
            profileImageView.setImageURI(outputFileUri);
        }
    }

    void cropImage(){

        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        cropIntent.setDataAndType(inputFileUri, "image/*");
        cropIntent.putExtra("outputX", 256); // CROP한 이미지의 x축 크기
        cropIntent.putExtra("outputY", 256); // CROP한 이미지의 y축 크기
        cropIntent.putExtra("crop", "true");
        cropIntent.putExtra("aspectX", 1);
        cropIntent.putExtra("aspectY", 1.5);
        cropIntent.putExtra("return-data", true);

        outputFileUri= Uri.fromFile(createFile());
        cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        startActivityForResult(cropIntent, 10003);
    }

    File createFile() {
        String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
        String imageFileName = "jmlee_" + timeStamp;

        File storageDir = new File(Environment.getExternalStorageDirectory()+"/pictures/");
        if (!storageDir.exists()) storageDir.mkdirs();
        try{
            return File.createTempFile(imageFileName, ".jpg", storageDir);
        }catch(IOException e){

        }
        return null;
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


}

