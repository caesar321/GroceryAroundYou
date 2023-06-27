package com.example.mygrocerymobileapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class BuyerActivity extends AppCompatActivity {
    private ImageView navImage;
    private TextView navTextName;
    private FirebaseAuth auth;
    private NavigationView navigationView;
    private  ImageView toolBarImg;
    private DrawerLayout drawerLayout;
    View headerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buyer);
        drawerLayout= findViewById(R.id.drawer_layout_buyer);
        toolBarImg= findViewById(R.id.tool_menu_buyer);
        navigationView=findViewById(R.id.navigation_view_buyer);
        headerView  = navigationView.getHeaderView(0);
        navImage = headerView.findViewById(R.id.nav_img_buyer);
        auth = FirebaseAuth.getInstance();
        navTextName = headerView.findViewById(R.id.nav_txt_name_buyer);
        toolBarImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        MyInfo();
        navigationView.setItemIconTintList(null);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.nav_edit:{
                        Toast.makeText(BuyerActivity.this, "Edit clicked", Toast.LENGTH_SHORT).show();
                        Intent intent= new Intent(BuyerActivity.this,EditBuyerActivity.class);
                        startActivity(intent);
                        break;
                    }

                    case R.id.nav_logout:{
                        auth.signOut();
                        if (auth.getCurrentUser()==null){
                            Intent intent= new Intent(BuyerActivity.this,LoginActivity.class);
                            startActivity(intent);

                        }
                        break;
                    }
                }
                return true;
            }
        });


    }
    private void MyInfo(){
        DatabaseReference def = FirebaseDatabase.getInstance().getReference("Users");
        def.orderByChild("uid").equalTo(auth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                            String nameOfUser = ""+dataSnapshot.child("username").getValue();
                            String Account = ""+dataSnapshot.child("Account").getValue();
                            String image = ""+dataSnapshot.child("Userimage").getValue();
                            Picasso.get().load(image).into(navImage);
                            navTextName.setText(nameOfUser+ "("+Account+")");

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}