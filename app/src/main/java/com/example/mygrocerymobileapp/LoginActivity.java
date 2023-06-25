package com.example.mygrocerymobileapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {
    private TextView Signup,ForgotPassword;
    private EditText EdEmail,EdPassword;
    private ProgressDialog progressDialog;
    private Button btnSignUp;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        EdEmail= findViewById(R.id.lgEdEmail);
        progressDialog= new ProgressDialog(this);
        EdPassword= findViewById(R.id.lgEdPassword);
        btnSignUp = findViewById(R.id.btnLg);
        auth= FirebaseAuth.getInstance();
        Signup=  findViewById(R.id.signUp);
        Signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });
       btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (EdEmail.getText().toString().isEmpty()||EdPassword.getText().toString().isEmpty()){
                    Toast.makeText(LoginActivity.this, "Enter all fields", Toast.LENGTH_SHORT).show();
                }else{
                    progressDialog.setMessage("Signing in...");
                    progressDialog.show();
                    auth.signInWithEmailAndPassword(EdEmail.getText().toString(),EdPassword.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()){
                                        ConfirmOnline();
                                    }else {
                                        Toast.makeText(LoginActivity.this, "an error occured", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                }
            }

            private void ConfirmOnline() {
                HashMap<String,Object> hashMap = new HashMap<>();
                hashMap.put("online","true");
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                databaseReference.child(auth.getUid()).updateChildren(hashMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                                CheckUserIfSellerOrBuyer();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            private void CheckUserIfSellerOrBuyer() {
                DatabaseReference databaseReference =FirebaseDatabase.getInstance().getReference("Users");
                databaseReference.orderByChild("uid").equalTo(auth.getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                progressDialog.setMessage("Checking...");
                                for (DataSnapshot ds:snapshot.getChildren()){
                                    String TypeOfAccount=""+ds.child("Account").getValue();
                                    if (TypeOfAccount.equals("Seller")){
                                        progressDialog.dismiss();
                                        Intent intent= new Intent(LoginActivity.this,SellerActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }else{
                                        progressDialog.dismiss();
                                        Intent intent= new Intent(LoginActivity.this,BuyerActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

            }
        });
    }
}