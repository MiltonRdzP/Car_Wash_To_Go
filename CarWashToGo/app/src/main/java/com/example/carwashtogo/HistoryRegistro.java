package com.example.carwashtogo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carwashtogo.HistoryRecyclerVier.HistoryAdapter;
import com.example.carwashtogo.HistoryRecyclerVier.HistoryObject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HistoryRegistro extends AppCompatActivity {

        private String customerOrDriver, userId;

        private RecyclerView mHistoryRecyclerView;
        private RecyclerView.Adapter mHistoryAdapter;
        private RecyclerView.LayoutManager mHistoryLayoutManager;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_history_registro);
            mHistoryRecyclerView = findViewById(R.id.historyRecyclerView2);
            mHistoryRecyclerView.setNestedScrollingEnabled(false);
            mHistoryRecyclerView.setHasFixedSize(true);
            mHistoryLayoutManager = new LinearLayoutManager(HistoryRegistro.this);
            mHistoryRecyclerView.setLayoutManager(mHistoryLayoutManager);
            mHistoryAdapter = new HistoryAdapter(getDataSetHistory(), HistoryRegistro.this);
            mHistoryRecyclerView.setAdapter(mHistoryAdapter);

            customerOrDriver = getIntent().getExtras().getString("customerOrDriver");
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            getUserRideIds();
        }

        private void getUserRideIds() {
            DatabaseReference userHistoryDatabase = FirebaseDatabase.getInstance().getReference().child("Users2HistoryInfo").child(customerOrDriver).child(userId).child("history");
            userHistoryDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        for(DataSnapshot history : dataSnapshot.getChildren()){
                            FetchRideInformation(history.getKey());
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }

        private void FetchRideInformation(String rideKey) {
            DatabaseReference historyDatabase = FirebaseDatabase.getInstance().getReference().child("history").child(rideKey);
            historyDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        Long timestamp = 0L;
                        String rideId = dataSnapshot.getKey();
                        Double ridePrice = 0.0;

                        if(dataSnapshot.child("timestamp").getValue() != null){
                            timestamp = Long.valueOf(dataSnapshot.child("timestamp").getValue().toString());
                        }
                        HistoryObject obj = new HistoryObject(rideId, getDate(timestamp));
                        resultsHistory.add(obj);
                        mHistoryAdapter.notifyDataSetChanged();
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }

        private ArrayList resultsHistory = new ArrayList<HistoryObject>();
        private List<HistoryObject> getDataSetHistory() {
            return resultsHistory;
        }
        private String getDate(Long time) {
            Calendar cal = Calendar.getInstance(Locale.getDefault());
            cal.setTimeInMillis(time*1000);
            String date = DateFormat.format("MM-dd-yyyy hh:mm", cal).toString();
            return date;
        }
    }