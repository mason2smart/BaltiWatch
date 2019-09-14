package com.appcentricity.baltiwatch.ui.login;

import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;

public class report extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    FirebaseFirestore db;
    DatabaseReference myRef;
    LocationManager locationManager;
    boolean trashVal = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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
        db = FirebaseFirestore.getInstance();

        //inflate header to access elements
        View header = navigationView.getHeaderView(0);
        TextView navUname = (TextView) header.findViewById(R.id.navUname);
        TextView navEmail = header.findViewById(R.id.navUemail);
        ImageView navProfPic = header.findViewById(R.id.navUserPic);


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
        navEmail.setOnClickListener(new View.OnClickListener() {
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
    public void test(View view) {

        if (trashVal) {
            addReport("trash");
        } else {
            Toast selectToast = Toast.makeText(this, "need to select  a type", Toast.LENGTH_LONG);
            selectToast.show();

        }
    }

    private void addReport(String type) {
        Map<String, Object> Report = new HashMap<>();
        Report.put("Type", type);
        Report.put("Location", new GeoPoint(1.0, 1.0));
        DocumentReference ref = db.collection("Reports").document();
        db.collection("Reports")
                .add(Report)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("something", "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("something", "Error adding document", e);
                    }
                });
        addRewards(50);
    }

    private void addRewards(int points) {
        FirebaseUser usr = FirebaseAuth.getInstance().getCurrentUser();
        if (usr != null) {
            String uid = usr.getUid();
            DocumentReference userRewardsRef = db.collection("Users").document(uid);
            userRewardsRef.update("rewards", FieldValue.increment(points));
        }
    }

//    private void redeemRewards(int points) {
//        FirebaseUser usr = FirebaseAuth.getInstance().getCurrentUser();
//        if (usr != null) {
//            String uid = usr.getUid();
//            DocumentReference userRewardsRef = db.collection("Users").document(uid);
//            userRewardsRef.update("rewards", FieldValue.increment(points));
//        }
//    }

    public void toggleTrash(View view) {
        if (trashVal) {
            view.setBackgroundColor(Color.LTGRAY);
        } else {
            view.setBackgroundColor(Color.GREEN);
        }
        trashVal = !trashVal;

    }
}
