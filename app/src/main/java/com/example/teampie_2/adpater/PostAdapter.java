package com.example.teampie_2.adpater;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.teampie_2.R;
import com.example.teampie_2.activity.PostDetailActivity;
import com.example.teampie_2.activity.PostListActivity;
import com.example.teampie_2.models.Post;
import com.example.teampie_2.models.User;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class PostAdapter extends FirestoreRecyclerAdapter<Post, PostAdapter.PostHolder> {
    private Context context;

    class PostHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        TextView timeTextView;
        TextView authorTextView;
        ImageView uploadImageView;

        public PostHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.titleTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            authorTextView = itemView.findViewById(R.id.authorTextView);
            uploadImageView = itemView.findViewById(R.id.uploadImageView);
        }
    }

    @Override
    public int getItemViewType(int position) {
        // Just as an example, return 0 or 2 depending on position
        // Note that unlike in ListView adapters, types don't have to be contiguous
        return position % 2 * 2;
    }

    public PostAdapter(Context context, @NonNull FirestoreRecyclerOptions<Post> options) {
        super(options);
        this.context = context;
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    @Override
    public PostHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_post, viewGroup, false);
        return new PostHolder(v);
    }

    Bitmap bitmap;
    @Override
    protected void onBindViewHolder(PostHolder holder, int position, @NonNull Post model) {
        DocumentSnapshot documentSnapshot = getSnapshots().getSnapshot(position);
        final String postKey = documentSnapshot.getId();
        PostHolder postHolder = holder;

        postHolder.textViewTitle.setText(model.getTitle());
        postHolder.timeTextView.setText(model.getDate());
        postHolder.authorTextView.setText(model.getAuthor());


        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    URL url = new URL(model.getImageUri());

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();

                    InputStream inputStream = connection.getInputStream();
                    bitmap = BitmapFactory.decodeStream(inputStream);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();

        try {
            thread.join();
            postHolder.uploadImageView.setImageBitmap(bitmap);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Set click listener for the whole post view
        postHolder.itemView.setOnClickListener(v -> {
            // Launch PostDetailActivity
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra(PostDetailActivity.EXTRA_POST_KEY, postKey);
            context.startActivity(intent);
        });
    }


}
