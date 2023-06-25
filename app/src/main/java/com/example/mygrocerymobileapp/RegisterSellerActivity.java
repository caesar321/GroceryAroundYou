package com.example.mygrocerymobileapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class RegisterSellerActivity extends AppCompatActivity implements LocationListener {
    private ImageView SellerImage,SeGps;
    private EditText SeUserName,SePhone,SeCountry,SeState,SeCity,SeEmail,SePassword,SeConPassword,SeBusinessName;
    private Button btnSeSignUp;
   private  FirebaseAuth auth;
   private DatabaseReference databaseReference;
    private static final int LOCATION_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    private static final int IMAGE_FROM_PICK_GALLERY_REQUEST_CODE=400;
    private static final int IMAGE_FROM_PICK_CAMERA_REQUEST_CODE=500;
    private String[] locationPermission;
    private String[] cameraPermission;
    private String[] storagePermission;
    private Uri image_uri;
    private LocationManager locationManager;
    private double latitude =0.0, longititude= 0.0;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_seller);
        SellerImage = findViewById(R.id.reSellerImg);
        SeBusinessName= findViewById(R.id.reSellerShop);
        SeGps = findViewById(R.id.gpsSeller);
        SeUserName= findViewById(R.id.reSellerName);
        SePhone= findViewById(R.id.rSellerPhone);
        SeCountry= findViewById(R.id.reSellerCountry);
        SeState = findViewById(R.id.reSellerState);
        auth= FirebaseAuth.getInstance();
        progress= new ProgressDialog(this);
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        SeCity = findViewById(R.id.reSellerCity);
        SeEmail = findViewById(R.id.reSellerEmail);
        SePassword = findViewById(R.id.reSellerPassword);
        SeConPassword = findViewById(R.id.reSellerConPassword);
        btnSeSignUp = findViewById(R.id.btnSellerSignUp);
        locationPermission = new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION};
        cameraPermission = new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE};


        SeGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkLocationPermission()) {
                    detectLocation();

                } else {
                    requestLocationPermission();
                }
            }
        });
        btnSeSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AuthenticateUser();
            }

        });

        SellerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectImage();
            }
        });
    }

    private void AuthenticateUser() {
        if (SeUserName.getText().toString().isEmpty() || SePhone.getText().toString().isEmpty()||SeBusinessName.getText().toString().isEmpty()) {
            Toast.makeText(this, "please enter fields", Toast.LENGTH_SHORT).show();
        } else if (latitude==0.0||longititude==0.0) {
            Toast.makeText(this, "Location field is empty,Click icon to generate Location", Toast.LENGTH_SHORT).show();
        }else if (SeEmail.getText().toString().isEmpty()){
            Toast.makeText(this, "Enter your email address", Toast.LENGTH_SHORT).show();
        }else if (SePassword.getText().toString().isEmpty()){
            Toast.makeText(this, "enter your password", Toast.LENGTH_SHORT).show();
        }else if(SeConPassword.getText().toString().isEmpty()){
            Toast.makeText(this, "Please Confirm your password", Toast.LENGTH_SHORT).show();
        }else if(!SePassword.getText().toString().equals(SeConPassword.getText().toString())){
            Toast.makeText(this, "password does not match", Toast.LENGTH_SHORT).show();
        }else{
            SaveUserData();
        }
    }

    private void SaveUserData() {
        progress.setMessage("Signing,please wait...");
        progress.show();
        auth.createUserWithEmailAndPassword(SeEmail.getText().toString(),SePassword.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){

                            saveDataToFireBase();
                        }else {
                            progress.dismiss();
                            Toast.makeText(RegisterSellerActivity.this, "anError occured", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegisterSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void saveDataToFireBase() {
        progress.setMessage("Saving Info...");
        String uid= auth.getUid();
        String timeStamp = String.valueOf(System.currentTimeMillis());
        if (image_uri==null){
            HashMap<String,Object> hashMap = new HashMap<>();
            hashMap.put("uid",""+ uid);
            hashMap.put("username",""+SeUserName.getText().toString());
            hashMap.put("email",""+ SeEmail.getText().toString());
            hashMap.put("password",""+ SePassword.getText().toString());
            hashMap.put("country",""+ SeCountry.getText().toString());
            hashMap.put("state",""+ SeState.getText().toString());
            hashMap.put("city",""+ SeCity.getText().toString());
            hashMap.put("latitude",""+ latitude);
            hashMap.put("timestamp",""+ timeStamp);
            hashMap.put("Business",""+SeBusinessName.getText().toString());
            hashMap.put("Account","seller");
            hashMap.put("online","true");
            hashMap.put("Userimage",R.drawable.user);
            databaseReference.child(uid).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    progress.dismiss();
                    Intent intent = new Intent(RegisterSellerActivity.this,SellerActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        }else{
            String filePathAndName="profile_images/"+""+auth.getUid();
            StorageReference storageReference= FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask= taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            Uri downloadImage=uriTask.getResult();
                            if (uriTask.isSuccessful()){
                                String uid= auth.getUid();
                                HashMap<String,Object> hashMap = new HashMap<>();
                                hashMap.put("uid",""+ uid);
                                hashMap.put("username",""+SeUserName.getText().toString());
                                hashMap.put("Business",""+SeBusinessName.getText().toString());
                                hashMap.put("email",""+ SeEmail.getText().toString());
                                hashMap.put("password",""+ SePassword.getText().toString());
                                hashMap.put("country",""+ SeCountry.getText().toString());
                                hashMap.put("state",""+ SeState.getText().toString());
                                hashMap.put("city",""+ SeCity.getText().toString());
                                hashMap.put("latitude",""+ latitude);
                                hashMap.put("longitude",""+ longititude);
                                hashMap.put("timestamp",""+ timeStamp);
                                hashMap.put("Account","seller");
                                hashMap.put("online","true");
                                hashMap.put("isShopOpened","true");
                                hashMap.put("Userimage",""+downloadImage);
                                databaseReference.child(auth.getUid()).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        progress.dismiss();
                                        Intent intent = new Intent(RegisterSellerActivity.this,SellerActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progress.dismiss();
                            Toast.makeText(RegisterSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }


    }


    private void SelectImage() {
        String []options={"Camera","Gallery"};
        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setTitle("Select Image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i==0){
                            if (checkCameraPermission()){
                                pickFromCamera();
                            }else {
                                requestCameraPermission();
                            }
                        }else {
                            if (checkStoragePermission()){
                                pickImageFromGallery();
                            }else {
                                requestStoragePermission();
                            }

                        }
                    }
                }).show();
    }


    private void pickImageFromGallery(){
        Intent intent= new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_FROM_PICK_GALLERY_REQUEST_CODE);
    }
    private void pickFromCamera(){
        ContentValues contentValues= new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"Temp_Image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Temp_Image Description");
        image_uri= getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(intent,IMAGE_FROM_PICK_CAMERA_REQUEST_CODE);

    }
    private void detectLocation() {
        Toast.makeText(this, "Please wait while your location is being detected.....", Toast.LENGTH_SHORT).show();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }
    private boolean checkLocationPermission(){
        boolean result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)==(PackageManager.PERMISSION_GRANTED);
        return  result;
    }
    private void requestLocationPermission(){
        ActivityCompat.requestPermissions(this,locationPermission,LOCATION_REQUEST_CODE);
    }
    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return result;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        latitude= location.getLatitude();
        longititude= location.getLongitude();
        findAddress();

    }
    private  void requestStoragePermission(){
        ActivityCompat.requestPermissions(this,storagePermission,STORAGE_REQUEST_CODE);
    }
    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);
        boolean result2 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return result&&result2;

    }
    private  void requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermission,CAMERA_REQUEST_CODE);
    }

    private void findAddress() {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder= new Geocoder(this, Locale.getDefault());
        try {
            addresses= geocoder.getFromLocation(latitude,longititude,1);
            String address= addresses.get(0).getAddressLine(0);
            String citty= addresses.get(0).getLocality();
            String statte= addresses.get(0).getAdminArea();
            String countryy= addresses.get(0).getCountryName();
            SeCountry.setText(countryy);
            SeState.setText(statte);
            SeCity.setText(citty);
            //  ComAddress.setText(address);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        LocationListener.super.onStatusChanged(provider, status, extras);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        //  LocationListener.super.onProviderDisabled(provider);
        Toast.makeText(this, "Turn on location", Toast.LENGTH_SHORT).show();
    }









    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case LOCATION_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean locationAccepted= grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if (locationAccepted){
                        detectLocation();
                    }else {
                        Toast.makeText(this, "Location denied", Toast.LENGTH_SHORT).show();
                    }
                }
            }break;
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean cameraAccepted= grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted= grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted&&storageAccepted){
                        //detectLocation();
                        pickFromCamera();
                    }else {
                        Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
            }break;
            case STORAGE_REQUEST_CODE:{
                if (grantResults.length>0){
                    //  boolean cameraAccepted= grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted= grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted){
                        //detectLocation();
                        //pickFromCamera();
                        pickImageFromGallery();
                    }else {
                        Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
            }break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode==RESULT_OK){
            if (requestCode==IMAGE_FROM_PICK_GALLERY_REQUEST_CODE){
                image_uri= data.getData();
                SellerImage.setImageURI(image_uri);
            }else if(requestCode==IMAGE_FROM_PICK_CAMERA_REQUEST_CODE){
                SellerImage.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}