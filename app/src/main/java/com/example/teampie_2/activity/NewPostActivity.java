package com.example.teampie_2.activity;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.teampie_2.R;
import com.example.teampie_2.models.Comment;
import com.example.teampie_2.models.Post;
import com.example.teampie_2.models.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NewPostActivity extends BaseActivity {

    private static final int PICK_IMAGE_REQUEST = 1;


    private EditText editTextTitle;
    private EditText editTextDescription;

    private ImageView chooseImageBtn;
    private ImageView uploadImage;

    String uploadId;

    private StorageReference storageRef;
    private DatabaseReference databaseRef;
    StorageReference fileReference;
    private StorageTask upLoadTask;
    CollectionReference allpostRef;

    boolean isImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        editTextTitle = findViewById(R.id.edit_text_title);
        editTextDescription = findViewById(R.id.edit_text_description);

        chooseImageBtn = findViewById(R.id.chooseImageBtn);
        uploadImage = findViewById(R.id.uploadImage);

        // storage upload
        storageRef  = FirebaseStorage.getInstance().getReference("Upload-Images");
        databaseRef = FirebaseDatabase.getInstance().getReference("Upload-Images");

        isImage = false;


    } // end of onCreate

    String title;
    String description;
    String date;
    String author;
    public void addPost(View v) {
        title = editTextTitle.getText().toString();
        description = editTextDescription.getText().toString();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd hh:mm");
        Date now = new Date();
        date = formatter.format(now);

        final String uid = getUid();
        DocumentReference docRef = FirebaseFirestore.getInstance().collection("Users").document(uid);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            User user = documentSnapshot.toObject(User.class);
            author = user.username;
        });

        if (upLoadTask != null && upLoadTask.isInProgress()) {
            Toast.makeText(NewPostActivity.this, "Upload in progress", Toast.LENGTH_SHORT).show();

        } else {
            uploadImage();
        }

        if (title.trim().isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "제목과 내용을 채워주세요", Toast.LENGTH_SHORT).show();
            return;
        }
        allpostRef = FirebaseFirestore.getInstance()
                .collection("AllPosts");

        if(!isImage){
            allpostRef.add(new Post(author, title, description, null, date));
        }


        //allpostRef.add(new Post(null, title, description, null, number));
        Toast.makeText(this, "글이 등록되었습니다", Toast.LENGTH_SHORT).show();
        openImagesActivity();
        finish();
    }

    private void uploadImage(){

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_hh:mm");
        Date now = new Date();
        String filename = formatter.format(now) + ".png";

        fileReference = storageRef.child("/images" + filename);
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
                Log.e("download_uri", downloadURL);

                Toast.makeText(getApplicationContext(), downloadURL, Toast.LENGTH_SHORT).show();

                uploadId = databaseRef.push().getKey();

                Post uploadImage = new Post(author, title, description,
                        downloadURL, date); // getDownLoadUrl

                databaseRef.child(uploadId).setValue(uploadImage);

                allpostRef.add(new Post(author, title, description, downloadURL, date));
            } else {
                // Handle failures
                // ...
            }
        });
    }


    private void openImagesActivity() {
        Intent intent = new Intent(NewPostActivity.this, PostListActivity.class);
        startActivity(intent);
    }

    private Uri inputFileUri, outputFileUri;
    public void addImage(View v) {

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

            Picasso.with(this).load(inputFileUri).into(uploadImage);
            uploadImage.setImageURI(inputFileUri);
           // cropImage();
        }
        if (requestCode == 10003 && resultCode == RESULT_OK) {
            chooseImageBtn.setImageURI(outputFileUri);
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
}

