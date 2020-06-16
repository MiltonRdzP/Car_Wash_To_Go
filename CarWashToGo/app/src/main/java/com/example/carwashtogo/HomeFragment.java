package com.example.carwashtogo;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.carwashtogo.Adaptadores.AdapterPost;
import com.example.carwashtogo.models.ModelPost;
import com.firebase.geofire.GeoFire;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HomeFragment extends Fragment {
    FirebaseAuth firebaseAuth;
    CardView btnmap, btnmap2;
    String userId;
    DatabaseReference databaseReference;
    RecyclerView recyclerView;
    FirebaseDatabase firebaseDatabase;
    List<ModelPost> postList;
    AdapterPost adapterPost;
    FirebaseFirestore fStore;
    RelativeLayout auth;
    Button verify;
    FirebaseUser user;
    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_home, container, false);
        firebaseAuth = FirebaseAuth.getInstance();
        //userId = firebaseAuth.getCurrentUser().getUid();
        user = firebaseAuth.getCurrentUser();
        btnmap = view.findViewById(R.id.btn_map);
        btnmap2 = view.findViewById(R.id.btn_map2);
        auth = view.findViewById(R.id.auth);
        verify = view.findViewById(R.id.verify);
        if(firebaseAuth.getCurrentUser() != null){
            if (!user.isEmailVerified()){
                auth.setVisibility(View.VISIBLE);

                verify.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(view.getContext(), R.string.veremailenviada, Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("tag", "Error:" + e.getMessage());
                            }
                        });
                    }
                });
            }else{
                auth.setVisibility(View.INVISIBLE);
            }
        }


        //recycler view
        recyclerView = view.findViewById(R.id.postRecyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //show post mas nuevos primero
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //obtener nombre telefono e imagen del usuario
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getInstance().getReference("Users");
        fStore = FirebaseFirestore.getInstance();
        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    final String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();
                    final String phone = "" + ds.child("telefono").getValue();
                    final String image = "" + ds.child("image").getValue();
                    btnmap.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getContext(), Map.class);
                            startActivity(intent);
                            String user_id = firebaseAuth.getCurrentUser().getUid();
                            DatabaseReference current_user = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(user_id);
                            //current_user.setValue(true);

                            FirebaseUser fuser = firebaseAuth.getCurrentUser();
                            String email = fuser.getEmail();
                            String uid = fuser.getUid();
                            HashMap<Object, String> hashMap = new HashMap<>();
                            hashMap.put("name", name);
                            hashMap.put("uid", uid);
                            hashMap.put("email", email);
                            hashMap.put("image", image);
                            hashMap.put("telefono", phone);
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            //DatabaseReference reference = database.getReference("Users").child("Customers");
                            //reference.child(uid).setValue(hashMap);
                        }
                    });
                    btnmap2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getContext(), MapLavar.class);
                            startActivity(intent);
                            String user_id = firebaseAuth.getCurrentUser().getUid();
                            DatabaseReference current_user = FirebaseDatabase.getInstance().getReference().child("Users").child("CarWashMan").child(user_id);
                            //current_user.setValue(true);

                            FirebaseUser fuser = firebaseAuth.getCurrentUser();
                            String email = fuser.getEmail();
                            String uid = fuser.getUid();
                            HashMap<Object, String> hashMap = new HashMap<>();
                            hashMap.put("name", name);
                            hashMap.put("uid", uid);
                            hashMap.put("email", email);
                            hashMap.put("image", image);
                            hashMap.put("telefono", phone);
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            //DatabaseReference reference = database.getReference("Users").child("Drivers");
                            //reference.child(uid).setValue(hashMap);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
            //SET LAYOUT FOR RECYCLER VIEW
        recyclerView.setLayoutManager(layoutManager);
        postList = new ArrayList<>();
        recyclerView.setVisibility(View.GONE);


        return view;
    }
    private void checkUserStatus(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user!=null){
            userId = user.getUid();

        }else {
            startActivity(new Intent(getActivity(), Login.class));
            getActivity().finish();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState){
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("carwashmanAvailable");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);
    }

    @Override
    public void onResume() {
        super.onResume();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("carwashmanAvailable");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);

        String userId2 = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference("customerRequest");
        GeoFire geoFire2 = new GeoFire(ref2);
        geoFire2.removeLocation(userId2);
    }

    @Override
    public void onStop() {
        super.onStop();
        onDestroy();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("carwashmanAvailable");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);

        String userId2 = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference("customerRequest");
        GeoFire geoFire2 = new GeoFire(ref2);
        geoFire2.removeLocation(userId2);
    }

    @Override
    public void onStart() {
        super.onStart();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("carwashmanAvailable");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);

        String userId2 = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference("customerRequest");
        GeoFire geoFire2 = new GeoFire(ref2);
        geoFire2.removeLocation(userId2);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.nav_logout).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);

        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id==R.id.nav_logout){
            firebaseAuth.getInstance().signOut();
            checkUserStatus();
        }

        return super.onOptionsItemSelected(item);
    }



}
