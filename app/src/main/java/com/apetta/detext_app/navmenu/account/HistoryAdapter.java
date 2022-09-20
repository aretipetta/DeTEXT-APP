package com.apetta.detext_app.navmenu.account;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.apetta.detext_app.R;
import com.apetta.detext_app.alertDialogs.ProgressAlertDialog;
import com.apetta.detext_app.navmenu.detection.SavedImage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private Context context;
    private ArrayList<SavedImage> savedImages;
    private HashMap<String, Bitmap> imgsMap;
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    ProgressAlertDialog progressAlertDialog;

    public HistoryAdapter(Context context, ArrayList<SavedImage> savedImages) {
        setContext(context);
        setSavedImages(savedImages);
        imgsMap = new HashMap<>();
    }

    public void setContext(Context context) {
        this.context = context;
    }
    public void setSavedImages(ArrayList<SavedImage> savedImages) {
        this.savedImages = savedImages;
    }

    public Context getContext() {
        return context;
    }

    public ArrayList<SavedImage> getSavedImages() {
        return savedImages;
    }


    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.history_row, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        SavedImage savedImage = savedImages.get(position);
        String imgPath = savedImage.getStoragePath();
        StorageReference sRef = FirebaseStorage.getInstance().getReference().child(imgPath);
        sRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
            if(!imgsMap.containsKey(imgPath))
                imgsMap.put(imgPath, BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
            holder.imgHistoryRow.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
            holder.dateHistoryRow.setText(savedImage.getDate());
        });

        // onClickListener to start new Activity with card's details
        holder.cardLayout.setOnClickListener(view -> {
            Intent intent = new Intent(context, DetailsOfCardHistoryActivity.class);
            intent.putExtra("savedImage", savedImages.get(position));
            context.startActivity(intent);
        });

        holder.removeBtn.setOnClickListener(view -> {
            progressAlertDialog = new ProgressAlertDialog(context, context.getString(R.string.wait));
            progressAlertDialog.show();
            mAuth = FirebaseAuth.getInstance();
            database = FirebaseDatabase.getInstance();
            String path = savedImage.getStoragePath().substring(0, savedImage.getStoragePath().length() - 3);
            DatabaseReference dbRef = database.getReference(path);
            dbRef.removeValue()
                    .addOnSuccessListener(unused1 -> {
                        progressAlertDialog.dismiss();
                        StorageReference storageRef = FirebaseStorage.getInstance().getReference(savedImage.getStoragePath());
                        storageRef.delete().addOnSuccessListener(unused -> {
                            Toast.makeText(context, context.getString(R.string.removed), Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, context.getString(R.string.failed), Toast.LENGTH_SHORT).show();
                            progressAlertDialog.dismiss();
                        })
                        .addOnCompleteListener(task -> { });
                    })
                    .addOnFailureListener(e1 -> {
                        progressAlertDialog.dismiss();
                        Toast.makeText(context, context.getString(R.string.failed), Toast.LENGTH_SHORT).show();
                    })
                    .addOnCompleteListener(task1 -> {
                        HistoryActivity.savedImages.remove(position);
                        Activity activity = (Activity) context;
                        activity.recreate();
                    });
        });
    }

    @Override
    public int getItemCount() {
        return savedImages.size();
    }

    public void updatedHistory() {

    }

    public class HistoryViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgHistoryRow;
        private TextView dateHistoryRow;
        private ConstraintLayout cardLayout;
        private ImageButton removeBtn;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imgHistoryRow = itemView.findViewById(R.id.imgHistoryRow);
            dateHistoryRow = itemView.findViewById(R.id.dateHistoryRow);
            cardLayout = itemView.findViewById(R.id.cardLayoutHistory);
            removeBtn = itemView.findViewById(R.id.removeHistoryCard);
        }
    }
}
