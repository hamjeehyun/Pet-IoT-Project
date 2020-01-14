package com.example.teampie_2.adpater;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.teampie_2.R;
import com.example.teampie_2.models.Feeder;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

public class FeederAdapter extends FirestoreRecyclerAdapter<Feeder, FeederAdapter.FeederHolder> {
    private Context context;

    public FeederAdapter(Context context, @NonNull FirestoreRecyclerOptions<Feeder> options) {
        super(options);
        this.context = context;
    }
    @NonNull
    @Override
    public FeederHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_feeder, viewGroup, false);
        return new FeederHolder(v);
    }
    @Override
    protected void onBindViewHolder(@NonNull FeederHolder holder, int position, @NonNull Feeder model) {
        if(Integer.parseInt(model.getHour()) > 12) {
            int pmhour = Integer.parseInt(model.getHour()) - 12;

            holder.hourTimeTextView.setText(String.valueOf(pmhour));
            holder.minuteTimeTextView.setText(String.valueOf(model.getMinute()));
            holder.textViewAmPm.setText("오후");
        } else {
            holder.hourTimeTextView.setText(model.getHour());
            holder.minuteTimeTextView.setText(String.valueOf(model.getMinute()));
            holder.textViewAmPm.setText("오전");
        }


        DocumentSnapshot documentSnapshot = getSnapshots().getSnapshot(position);

        final String feederKey = documentSnapshot.getId();



    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    public void deleteItem(int position) {
        getSnapshots().getSnapshot(position).getReference().delete();

    }

    class FeederHolder extends RecyclerView.ViewHolder {
        TextView textViewAmPm;
        TextView hourTimeTextView;
        TextView minuteTimeTextView;

        public FeederHolder(@NonNull View itemView) {
            super(itemView);
            textViewAmPm = itemView.findViewById(R.id.AmPmTextView);
            hourTimeTextView = itemView.findViewById(R.id.hourTimeTextView);
            minuteTimeTextView = itemView.findViewById(R.id.minuteTimeTextView);
        }
    }
}