package com.appcentricity.baltiwatch.ui.login;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.appcentricity.baltiwatch.ProfileActivity;
import com.appcentricity.baltiwatch.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class report extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    FirebaseFirestore db;
    FirebaseAuth auth;
    private FirebaseStorage cloudStorage = FirebaseStorage.getInstance();
    DatabaseReference myRef;
    LocationManager locationManager;
    private boolean trashVal = false;
    private boolean bioHazard = false;
    private boolean customActive = false;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    Map<String, Object> Report;
    View header;
    TextView navUname;
    TextView navRewards;
    ImageView navProfPic;
    StorageReference profPicRef;
    String userName = "";
    boolean hasProfPic = true;
    TextView custom;
    ImageButton biohazard;
    ImageButton trashImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_report, R.id.nav_neighborhood, R.id.nav_verify,
                R.id.nav_rewards, R.id.nav_share, R.id.nav_send)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        trashVal = false;

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // System.out.println("test");
        locationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    // ...
                    addReport("trash");
                }
            };
        };

        //inflate header to access elements
        header = navigationView.getHeaderView(0);
        navUname = (TextView) header.findViewById(R.id.navUname);
        navRewards = header.findViewById(R.id.navUemail);
        navProfPic = header.findViewById(R.id.navUserPic);
        loadUserNavData();
        updateRewards();


        navProfPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(report.this, ProfileActivity.class));
                finish();
            }
        });
        navUname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(report.this, ProfileActivity.class));
                finish();
            }
        });
        navRewards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(report.this, ProfileActivity.class));
                finish();
            }
        });

            }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.report, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
    public void submit(View view) {

        if (trashVal) {
            addReport("trash");
        } else if (bioHazard) {
            addReport("biohazard");
        } else if (customActive) {
            custom = (TextView) findViewById(R.id.custom);
            addReport(custom.getText().toString());
        } else {
            Toast selectToast = Toast.makeText(this, "need to select  a type", Toast.LENGTH_LONG);
            selectToast.show();
        }
    }

    private void addReport(final String type) {
        Report = new HashMap<>();
        Report.put("Id", "dummy");
        // Task<Location> loc = fusedLocationClient.getLastLocation();
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(report.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object

                            Report.put("Type", type);
                            Report.put("Location", new GeoPoint(location.getLatitude(), location.getLongitude()));
                            Log.d("STATE", Report.toString());
                        } else {
                            Report.put("Type", type);
                            Report.put("Location", new GeoPoint(23.0, 23.0));
                        }
                        DocumentReference ref = db.collection("Reports").document();
                        Report.put("Id", ref.getId());
                        ref.set(Report);

                    }
                });
        addRewards(50);
    }

    private void addRewards(int points) {
        FirebaseUser usr = FirebaseAuth.getInstance().getCurrentUser();
        if (usr != null) {
            String uid = usr.getUid();
            DocumentReference userRewardsRef = db.collection("users").document(uid);
            userRewardsRef.update("rewards", FieldValue.increment(points));
        }
    }

    public void updateRewards() {
       try {
           final DocumentReference docRef = db.collection("users").document(auth.getUid());
           docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
               @Override
               public void onEvent(@Nullable DocumentSnapshot snapshot,
                                   @Nullable FirebaseFirestoreException e) {
                   if (e != null) {
                       Log.w("report", "Listen failed.", e);
                       return;
                   }

                   if (snapshot != null && snapshot.exists()) { //update rewards on change
                       navRewards.setText("Rewards Points: " + snapshot.getDouble("rewards").intValue() + " pts");
                   } else {
                       Log.d("report", "Current data: null");
                   }
               }
           });
       }
       catch (NullPointerException e){
            Log.e("report", "ERROR UPDATING REWARDS: "+e.toString());
       }
    }
    private void loadUserNavData(){
try{
    db.document("users/" + auth.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
        @Override
        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
            DocumentSnapshot doc = task.getResult();
                    if (doc.contains("hasProfPic")) {
                        hasProfPic = doc.getBoolean("hasProfPic").booleanValue();
                    }
                    if (doc.contains("userName")) {
                        navUname.setText(doc.getString("userName"));
                    }
                     if (doc.contains("rewards")) {
                        navRewards.setText("Rewards Points: " + doc.getDouble("rewards").intValue()+" pts");
                    }
            }
    });}
catch (NullPointerException e)
            {
                Log.e("error",e.toString());
                userName = auth.getUid();
                navUname.setText(userName);
            }
        if (hasProfPic){
             profPicRef = cloudStorage.getReference("users").child("thumb_" + auth.getUid());

                    final long ONE_MEGABYTE = 1024 * 1024;
                    profPicRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] profPicBytes) {
                            byteArrayToImageView(navProfPic, profPicBytes);
                        }
                    }).addOnFailureListener(new OnFailureListener() { //failure to get compressed profile image
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.w("report", "Failed to get compressed pic -- getting original instead");
                            try {
                                final File localFile = File.createTempFile("images", "jpg");
                                profPicRef = cloudStorage.getReference("users").child(auth.getUid());
                                profPicRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                        navProfPic.setImageBitmap(BitmapFactory.decodeFile(localFile.getAbsolutePath()));
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        Log.e("report", "Failed to get save local copy of uncompressed pic - using default instead");

                                        profPicRef = cloudStorage.getReference("users").child(auth.getUid());
                                        final long ONE_MEGABYTE = 1024 * 1024;
                                        profPicRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                            @Override
                                            public void onSuccess(byte[] profPicBytes) {
                                                byteArrayToImageView(navProfPic, profPicBytes);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() { //failure to get original profile image
                                            @Override
                                            public void onFailure(@NonNull Exception exception) {
                                                Log.w("report", "Failed to get uncompressed pic -- using default instead");

                                                int defProfPic = getResources().getIdentifier("defaultuser_trimmed", "drawable", getPackageName());
                                                navProfPic.setImageResource(defProfPic); //if fail to get compressed or original profile image
                                            }
                                        });

                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                profPicRef = cloudStorage.getReference("users").child(auth.getUid());
                                final long ONE_MEGABYTE = 1024 * 1024;
                                profPicRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] profPicBytes) {
                                        byteArrayToImageView(navProfPic, profPicBytes);
                                    }
                                }).addOnFailureListener(new OnFailureListener() { //failure to get original profile image
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        Log.w("report", "Failed to get uncompressed pic -- using default instead");

                                        int defProfPic = getResources().getIdentifier("defaultuser_trimmed", "drawable", getPackageName());
                                        navProfPic.setImageResource(defProfPic); //if fail to get compressed or original profile image
                                    }
                                });
                            }

                        }
                    });
        }
        else if(!hasProfPic)
        {
            Log.w("report", "has prof pic? "+hasProfPic);
            int defProfPic = getResources().getIdentifier("defaultuser_trimmed", "drawable", getPackageName());
            navProfPic.setImageResource(defProfPic); //if fail to get compressed or original profile image
        }
        }
//    private void redeemRewards(int points) {
//        FirebaseUser usr = FirebaseAuth.getInstance().getCurrentUser();
//        if (usr != null) {
//            String uid = usr.getUid();
//            DocumentReference userRewardsRef = db.collection("users").document(uid);
//            userRewardsRef.update("rewards", FieldValue.increment(points));
//        }
//    }
    public static void byteArrayToImageView(ImageView view, byte[] data) {
    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
    view.setImageBitmap(bitmap);
    }
    public void toggleTrash(View view) {
        biohazard = findViewById(R.id.biohazard);
        custom = findViewById(R.id.custom);
        if (trashVal) {
            view.setBackgroundColor(Color.LTGRAY);
        } else {
            view.setBackgroundColor(Color.GREEN);
            bioHazard = false;
            biohazard.setBackgroundColor(Color.LTGRAY);
            customActive = false;
        }
        trashVal = !trashVal;
        custom.setActivated(false);

    }

    public void toggleBioHazzard(View view) {
        trashImage = findViewById(R.id.trashImage);
        custom = findViewById(R.id.custom);

        if (bioHazard) {
            view.setBackgroundColor(Color.LTGRAY);
        } else {
            view.setBackgroundColor(Color.GREEN);
            trashVal = false;
            trashImage.setBackgroundColor(Color.LTGRAY);
            customActive = false;
            custom.setActivated(false);
        }

        bioHazard = !bioHazard;
    }

    public void customOnClick(View view) {
        biohazard = findViewById(R.id.biohazard);
        trashImage = findViewById(R.id.trashImage);
        if (!customActive) {
            customActive = true;
            bioHazard = false;
            biohazard.setBackgroundColor(Color.LTGRAY);
            trashVal = false;
            trashImage.setBackgroundColor(Color.LTGRAY);
        }
    }


    @Override
    public void onResume() {
        loadUserNavData();
        super.onResume();
        updateRewards();
    }
    @Override
    public void onBackPressed() { //so cannot get back in after signing out
        //do nothing

    }
}
