package com.apetta.detext_app.navmenu.account;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.apetta.detext_app.R;
import com.apetta.detext_app.navmenu.detection.SavedImage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private TextView noHistoryTextView;
    private ArrayList<SavedImage> savedImages;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        mAuth =FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        recyclerView = findViewById(R.id.recyclerViewHistory);
        recyclerView.setVisibility(View.INVISIBLE);
        noHistoryTextView = findViewById(R.id.noHistoryTextView);
        noHistoryTextView.setVisibility(View.INVISIBLE);
        getHistoryFromDB();
    }

    public void getHistoryFromDB() {
        savedImages = new ArrayList<>();
        database.getReference("history/" + mAuth.getCurrentUser().getUid())
                .orderByValue()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot snap : snapshot.getChildren()) {
                            savedImages.add(snap.getValue(SavedImage.class));
                        }
                        // TODO some adapters
                        if(savedImages.size() == 0) noHistoryTextView.setVisibility(View.VISIBLE);
                        else {
                            recyclerView.setVisibility(View.VISIBLE);
                            HistoryAdapter historyAdapter = new HistoryAdapter(HistoryActivity.this, savedImages);
                            recyclerView.setAdapter(historyAdapter);
                            recyclerView.setLayoutManager(new LinearLayoutManager(HistoryActivity.this));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}