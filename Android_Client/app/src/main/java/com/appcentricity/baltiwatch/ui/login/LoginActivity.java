package com.appcentricity.baltiwatch.ui.login;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.appcentricity.baltiwatch.R;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends AppCompatActivity {
    private ParallaxView mParallaxView;
    private EditText inputEmail, inputPwd;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private Button btnSignUp, btnLogin, btnPwdReset;
    private final int MIN_PWD_LEN = 6;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mParallaxView = (ParallaxView) findViewById(R.id.parallax_view);
        mParallaxView.init();

        inputEmail = (EditText) findViewById(R.id.email);
        inputPwd = (EditText) findViewById(R.id.pwd);
        btnSignUp = (Button) findViewById(R.id.btn_sign_up);
        btnLogin = (Button) findViewById(R.id.btn_login);
        //btnPwdReset = (Button) findViewById(R.id.btn_reset_password);

        auth = FirebaseAuth.getInstance();

        // if we're already logged in go to the home activity
        if (auth.getCurrentUser() != null) {
            // startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
        }


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();



        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = inputEmail.getText().toString();
                final String pwd = inputPwd.getText().toString();

                // we're using a lot of toasts here, we should replace them in our second sprint.
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), R.string.missing_email, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(pwd)) {
                    Toast.makeText(getApplicationContext(), R.string.missing_pwd, Toast.LENGTH_SHORT).show();
                    return;
                }

                // pwd too short--I think this is a firebase default? Not sure.
                if (pwd.length() < MIN_PWD_LEN) {
                    inputPwd.setError(getString(R.string.minimum_pwd));
                    return;
                }

                //authenticate user
                auth.signInWithEmailAndPassword(email, pwd)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // Handle a failed login
                                if (!task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, getString(R.string.access_denied), Toast.LENGTH_LONG).show();
                                } else {
                                    // startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                    finish();
                                }
                            }
                        });
            }
        });


        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String email = inputEmail.getText().toString().trim();
                String password = inputPwd.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), R.string.missing_email, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), R.string.missing_pwd, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < MIN_PWD_LEN) {
                    inputPwd.setError(getString(R.string.minimum_pwd));
                    return;
                }

                //create user
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // On a failure show a message and the exception
                                // This can probably happen if the user tries to input a duplicate account?
                                if (!task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, "" + R.string.sign_up_fail + task.getException(),
                                            Toast.LENGTH_SHORT).show();
                                    // On a success, just move to the main activity
                                } else {
                                    // Create a new user in firestore db with uid and email
                                    Map<String, Object> user = new HashMap<>();
                                    //user.put("email", inputEmail.getText().toString().trim());

                                    String userName = createUserName(email); //generate username from email

                                    //user display name and email not accessible by other users

                                    FirebaseUser User = FirebaseAuth.getInstance().getCurrentUser();
                                    User.updateEmail(inputEmail.getText().toString().trim());

                                   /* UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(userName).build();
                                    User.updateProfile(profileUpdates); */

                                    //add username to database generated from login email
                                    user.put("userName", userName);
                                    user.put("eventCreated", false);
                                    user.put("hasProfPic", false);
                                    db.collection("users").document(auth.getUid()).set(user);

                                    // Add a new document with ID = userID
                                    //db.collection("users").document(auth.getUid()).set(user);
                                    //  startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                    finish();
                                }
                            }
                        });
            }
        });
    }
    private String createUserName(String email) {
        int i;
        i = email.lastIndexOf('@');
        String uName = email.substring(0,i);
        return uName;
    }




    @Override
    protected void onResume() {
        mParallaxView.registerSensorListener();
        super.onResume();
    }
    @Override
    protected void onPause() {
        mParallaxView.unregisterSensorListener();
        super.onPause();
    }
    @Override
    public void onBackPressed() { //so cannot get back in after signing out
        //do nothing
    }
}




