package com.example.teampie_2.activity;

import android.app.DownloadManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.teampie_2.R;
import com.example.teampie_2.adpater.PostAdapter;
import com.example.teampie_2.models.Comment;
import com.example.teampie_2.models.Post;
import com.example.teampie_2.models.User;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PostDetailActivity extends BaseActivity implements View.OnClickListener {

    public static final String EXTRA_POST_KEY = "post_key";

    private TextView textViewTitle;
    private TextView textViewDescription;
    private TextView textViewDate;
    private TextView textViewAuthor;
    private ImageView imageViewUpload;

    private DocumentReference documentReference;
    private String postKey;

    //comment
    private EditText commentField;
    private Button commentBtn;
    private RecyclerView commentRecycler;

    private CommentAdapter commentAdapter;

    private FirebaseStorage storage;
    private StorageReference storageRef;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // Get post key from intent
        postKey = getIntent().getStringExtra(EXTRA_POST_KEY);
        if (postKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_POST_KEY");
        }

        documentReference = FirebaseFirestore.getInstance().collection("AllPosts").document(postKey);

        textViewTitle = findViewById(R.id.detail_title);
        textViewDescription = findViewById(R.id.detail_description);
        textViewDate = findViewById(R.id.detail_time);
        textViewAuthor = findViewById(R.id.detail_author);
        imageViewUpload = findViewById(R.id.detail_imageView);

        commentField = findViewById(R.id.field_comment_text);
        commentBtn = findViewById(R.id.button_post_comment);
        commentRecycler = findViewById(R.id.recycler_comments);

        commentBtn.setOnClickListener(this);
        commentRecycler.setLayoutManager(new LinearLayoutManager(this));

        Toolbar mToolbar = findViewById(R.id.post_detail_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("  ");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();


        String d = documentReference.getPath();
        Log.e("sujeong", d);

    } // end of OnCreate

    @Override
    public void onStart() {
        super.onStart();

        // Add value event listener to the post
        // [START post_value_event_listener]
        documentReference.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                return;
            }
            Post post = snapshot.toObject(Post.class);
            // [START_EXCLUDE]
            textViewTitle.setText(post.getTitle());
            textViewDescription.setText(post.getDescription());
            textViewDate.setText(post.getDate());
            textViewAuthor.setText(post.getAuthor());

            //String u = post.getImageUri();
            //Toast.makeText(this, u, Toast.LENGTH_SHORT).show();


            new DownloadImageTask(imageViewUpload)
                    .execute(post.getImageUri());


            //comments
            commentAdapter = new CommentAdapter(documentReference.collection("post-comments"));
            commentRecycler.setAdapter(commentAdapter);

        });
    }

    public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
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

    @Override
    public void onStop() {
        super.onStop();
        if (commentAdapter != null) {
            commentAdapter.cleanupListener();
        }
    }


    /** Comment */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_post_comment:
                postComment();
                break;
        }
    }

    private void postComment() {
        final String uid = getUid();
        DocumentReference docRef = FirebaseFirestore.getInstance().collection("Users").document(uid);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            User user = documentSnapshot.toObject(User.class);
            if (user == null) {
                // Log.e("hamm", "User " + uid + " is unexpectedly null");
                Toast.makeText(PostDetailActivity.this,
                        "Error: could not fetch user.",
                        Toast.LENGTH_SHORT).show();
            } else {
                Comment comment = new Comment(uid, user.username, commentField.getText().toString());
                documentReference.collection("post-comments").document().set(comment);

                commentField.setText(null);
            }
        });
    }




    private static class CommentViewHolder extends RecyclerView.ViewHolder {

        public TextView authorView;
        public TextView bodyView;

        CommentViewHolder(View itemView) {
            super(itemView);

            authorView = itemView.findViewById(R.id.comment_author);
            bodyView = itemView.findViewById(R.id.comment_body);
        }
    }

    //private static class FirestoreAdapter extends RecyclerView.Adapter<CommentViewHolder> {
    private static class CommentAdapter extends RecyclerView.Adapter<CommentViewHolder> {
        private List<String> mCommentIds = new ArrayList<>();
        private List<Comment> mComments = new ArrayList<>();

        private ListenerRegistration listenerRegistration;

        public CommentAdapter(Query query) {
            // Create child event listener
            // [START child_event_listener_recycler]
            EventListener childEventListener = (EventListener<QuerySnapshot>) (snapshots, e) -> {
                if (e != null) {return;}
                String commentKey;
                int commentIndex;
                Comment comment;

                for (DocumentChange dc : snapshots.getDocumentChanges()) {
                    switch (dc.getType()) {
                        case ADDED:
                            // A new comment has been added, add it to the displayed list
                            comment = dc.getDocument().toObject(Comment.class);
                            // [START_EXCLUDE]
                            // Update RecyclerView
                            mCommentIds.add(dc.getDocument().getId());
                            mComments.add(comment);
                            notifyItemInserted(mComments.size() - 1);
                            break;
                        case MODIFIED:
                            // A comment has changed, use the key to determine if we are displaying this
                            // comment and if so displayed the changed comment.
                            comment = dc.getDocument().toObject(Comment.class);
                            commentKey = dc.getDocument().getId();
                            // [START_EXCLUDE]
                            commentIndex = mCommentIds.indexOf(commentKey);
                            if (commentIndex > -1) {
                                // Replace with the new data
                                mComments.set(commentIndex, comment);

                                // Update the RecyclerView
                                notifyItemChanged(commentIndex);
                            } else {
                                Log.w("hamm", "onChildChanged:unknown_child:" + commentKey);
                            }
                            // [END_EXCLUDE]
                            break;
                        case REMOVED:
                            // A comment has changed, use the key to determine if we are displaying this
                            // comment and if so remove it.
                            commentKey = dc.getDocument().getId();
                            // [START_EXCLUDE]
                            commentIndex = mCommentIds.indexOf(commentKey);
                            if (commentIndex > -1) {
                                // Remove data from the list
                                mCommentIds.remove(commentIndex);
                                mComments.remove(commentIndex);

                                // Update the RecyclerView
                                notifyItemRemoved(commentIndex);
                            } else {
                                Log.w("hamm", "onChildRemoved:unknown_child:" + commentKey);
                            }
                            // [END_EXCLUDE]
                            break;
                    }
                }

            };
            // [END child_event_listener_recycler]
            listenerRegistration = query.addSnapshotListener(childEventListener);
        }

        @Override
        public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.item_comment, parent, false);
            return new CommentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CommentViewHolder holder, int position) {
            Comment comment = mComments.get(position);
            holder.authorView.setText(comment.author);
            holder.bodyView.setText(comment.text);
        }

        @Override
        public int getItemCount() {
            return mComments.size();
        }

        public void cleanupListener() {
            listenerRegistration.remove();
        }
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
