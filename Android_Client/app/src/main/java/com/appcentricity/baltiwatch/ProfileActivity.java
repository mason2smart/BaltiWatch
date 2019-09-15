package com.appcentricity.baltiwatch;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.appcentricity.baltiwatch.ui.login.LoginActivity;
import com.appcentricity.baltiwatch.ui.login.report;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private Button btnChangePwd, changePwdConf, btnDeleteAccount, btnSignOut, btnChangeUName, changeUNameConf;

    private EditText pwd, newPwd, uName;
    private TextView uNameDisp;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private FirebaseFirestore database;
    private FirebaseStorage cloudStorage = FirebaseStorage.getInstance();
    private StorageReference profPicRef;
    private boolean hasProfPic = false;
    private ImageView profPic;
    private TextView uploadText;
    private Uri profPicPath;
    private boolean pwdVisible = false;
    private boolean uNameVisible = false;
    private ProgressBar uploadProgress;
    boolean outsideTouch = false;
    final int PICK_IMAGE_REQUEST = 71;

    private final int MIN_PWD_LEN = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //get firebase auth instance
        auth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();

        //get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        //switch to login activity if logged out
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };

        uNameDisp = (TextView) findViewById(R.id.u_name_disp);

        //fetch username if exists, fetch if user profile pic has been uploaded
        database.document("users/" + auth.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot doc = task.getResult();
                String disp_uname = "";
                if (doc.contains("userName")) {
                    disp_uname += doc.get("userName");
                } else {
                    disp_uname += auth.getUid();
                }
                //look for profpic boolean
                if (doc.contains("hasProfPic")) {
                    hasProfPic = doc.getBoolean("hasProfPic");
                } else {
                    hasProfPic = false;
                }
                uNameDisp.setText(disp_uname);
                fetchProfPic();
            }
        });
        btnChangePwd = (Button) findViewById(R.id.btn_change_pwd);
        btnChangeUName = (Button) findViewById(R.id.btn_change_uName);
        changePwdConf = (Button) findViewById(R.id.btn_change_pwd_confirm);
        changeUNameConf = (Button) findViewById(R.id.btn_change_uname_confirm);
        btnDeleteAccount = (Button) findViewById(R.id.btn_delete_account);
        btnSignOut = (Button) findViewById(R.id.btn_sign_out);

        pwd = (EditText) findViewById(R.id.pwd);
        newPwd = (EditText) findViewById(R.id.newPwd);
        uName = (EditText) findViewById(R.id.newUName);

        pwd.setVisibility(View.GONE);
        newPwd.setVisibility(View.GONE);
        changePwdConf.setVisibility(View.GONE);

        uName.setVisibility(View.GONE);
        changeUNameConf.setVisibility(View.GONE);

        //Change Password Button Listener
        btnChangePwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!pwdVisible) {
                    pwd.setVisibility(View.VISIBLE);
                    newPwd.setVisibility(View.VISIBLE);
                    changePwdConf.setVisibility(View.VISIBLE);
                    pwdVisible = true;
                } else {
                    pwd.setVisibility(View.GONE);
                    newPwd.setVisibility(View.GONE);
                    changePwdConf.setVisibility(View.GONE);
                    pwdVisible = false;
                }
            }
        });

        //Change Password Confirmation Listener
        changePwdConf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!pwd.getText().toString().equals("") && !pwd.getText().toString().equals(null)){


                AuthCredential cred = EmailAuthProvider.getCredential(
                        auth.getCurrentUser().getEmail(),
                        pwd.getText().toString().trim()
                );

                if (user != null && !newPwd.getText().toString().trim().equals("") && !pwd.getText().toString().trim().equals("") && newPwd!=null) {
                    if (newPwd.getText().toString().trim().length() < MIN_PWD_LEN) {
                        newPwd.setError(getString(R.string.minimum_pwd));
                    }
                    user.reauthenticateAndRetrieveData(cred)
                            .addOnCompleteListener(ProfileActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (!task.isSuccessful()) {
                                        pwd.setError(getString(R.string.bad_pwd));
                                    } else if (pwd.getText().toString().trim().equals(newPwd.getText().toString().trim())) {
                                        newPwd.setError(getString(R.string.same_pwd));
                                    } else {
                                        new AlertDialog.Builder(ProfileActivity.this, R.style.DialogTheme)
                                                .setTitle(R.string.change_pwd_dialogue)
                                                .setMessage(R.string.change_pwd_logout)
                                                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        user.updatePassword(newPwd.getText().toString().trim())
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> innerTask) {
                                                                        if (innerTask.isSuccessful()) {
                                                                            Toast.makeText(ProfileActivity.this, R.string.pwd_update_success, Toast.LENGTH_SHORT).show();
                                                                            auth.signOut();
                                                                            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                                                                        } else {
                                                                            Toast.makeText(ProfileActivity.this, R.string.pwd_update_fail, Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }
                                                                });
                                                    }

                                                })
                                                .setNegativeButton(R.string.negative, null)
                                                .show();
                                    }
                                }
                            });
                } else if (newPwd.getText().toString().trim().equals("")) {
                    newPwd.setError("Enter password");
                } else if (pwd.getText().toString().trim().equals("")) {
                    pwd.setError("Enter password");
                }
            }
            }});


        //Change Username Listener
        btnChangeUName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!uNameVisible) {
                    uName.setVisibility(View.VISIBLE);
                    changeUNameConf.setVisibility(View.VISIBLE);
                    uNameVisible = true;
                } else {
                    uName.setVisibility(View.GONE);
                    changeUNameConf.setVisibility(View.GONE);
                    uNameVisible = false;
                }
            }
        });

        //Change Username Confirmation Listener
        changeUNameConf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String input = uName.getText().toString().trim();
                if (input.length() == 0) { //must be at least 1 character
                    uName.setError(getString(R.string.bad_uname));
                } else {
                    new AlertDialog.Builder(ProfileActivity.this, R.style.DialogTheme)
                            .setTitle(R.string.change_uname)
                            .setMessage(R.string.change_uname_msg)
                            .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String newUserName = uName.getText().toString();
                                    Map<String, Object> updateUserName = new HashMap<>();
                                    updateUserName.put("userName", newUserName);
                                    database.document("users/" + auth.getUid()).set(updateUserName, SetOptions.merge());
                                    String updatedDispUserName = getResources().getString(R.string.username_placeholder) + input;
                                    uNameDisp.setText(updatedDispUserName);
                                    uName.setText("");
                                    uName.setVisibility(View.GONE);
                                    changeUNameConf.setVisibility(View.GONE);
                                    uNameVisible = false;
                                }
                            })
                            .setNegativeButton(R.string.negative, null)
                            .show();
                }
            }
        });

        //Sign Out Button Listener
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(ProfileActivity.this, R.style.DialogTheme)
                        .setTitle(R.string.sign_out_dialog)
                        .setMessage(R.string.sign_out_confirm)
                        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                auth.signOut();
                                startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                            }

                        })
                        .setNegativeButton(R.string.negative, null)
                        .show();
            }
        });

        //Delete Account Button Listener
        btnDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(ProfileActivity.this, R.style.DialogTheme)
                        .setTitle(R.string.delete_account)
                        .setMessage(R.string.delete_account_confirm)
                        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (user != null) {
                                    user.delete()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                                                        finish();
                                                    } else {
                                                        Toast.makeText(ProfileActivity.this, R.string.delete_account_failure, Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }
                            }

                        })
                        .setNegativeButton(R.string.negative, null)
                        .show();
            }
        });
    }

    //fetchProfPic if exists, else set default image
    private void fetchProfPic() {
        fetchProfPic(hasProfPic);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void fetchProfPic(boolean shouldFetch) {
        profPic = findViewById(R.id.profPicView);
        if (shouldFetch) {
            profPicRef = cloudStorage.getReference("users").child("thumb_" + auth.getUid());

            final long ONE_MEGABYTE = 1024 * 1024;
            profPicRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] profPicBytes) {
                    byteArrayToImageView(profPic, profPicBytes);
                }
            }).addOnFailureListener(new OnFailureListener() { //failure to get compressed profile image
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.w("ProfileActivity", "Failed to get compressed pic -- getting original instead");
                    try {
                        final File localFile = File.createTempFile("images", "jpg");
                        profPicRef = cloudStorage.getReference("users").child(auth.getUid());
                        profPicRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                profPic.setImageBitmap(BitmapFactory.decodeFile(localFile.getAbsolutePath()));
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                Log.e("ProfileActivity", "Failed to get save local copy of uncompressed pic - using default instead");

                                profPicRef = cloudStorage.getReference("users").child(auth.getUid());
                                final long ONE_MEGABYTE = 1024 * 1024;
                                profPicRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] profPicBytes) {
                                        byteArrayToImageView(profPic, profPicBytes);
                                    }
                                }).addOnFailureListener(new OnFailureListener() { //failure to get original profile image
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        Log.w("ProfileActivity", "Failed to get uncompressed pic -- using default instead");

                                        int defProfPic = getResources().getIdentifier("defaultuser", "drawable", getPackageName());
                                        profPic.setImageResource(defProfPic); //if fail to get compressed or original profile image
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
                                byteArrayToImageView(profPic, profPicBytes);
                            }
                        }).addOnFailureListener(new OnFailureListener() { //failure to get original profile image
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                Log.w("ProfileActivity", "Failed to get uncompressed pic -- using default instead");

                                int defProfPic = getResources().getIdentifier("defaultuser", "drawable", getPackageName());
                                profPic.setImageResource(defProfPic); //if fail to get compressed or original profile image
                            }
                        });
                    }

                }
            });
        } else {
            //default profpic image
            int defProfPic = getResources().getIdentifier("defaultuser", "drawable", getPackageName());
            profPic.setImageResource(defProfPic);
        }

        uploadText = (TextView) findViewById(R.id.profPicViewText);
        //on touch -- display upload text
        //TODO: Fade in/out text, use text with best image contrast
        profPic.setOnTouchListener(new View.OnTouchListener() {
                                       @Override
                                       public boolean onTouch(View v, MotionEvent touchAction) {
                                           int touchType = touchAction.getAction();
                                           switch (touchType) {
                                               case MotionEvent.ACTION_DOWN:
                                                   outsideTouch = false;
                                                   uploadText.setVisibility(View.VISIBLE);
                                                   break;
                                               case MotionEvent.ACTION_UP:
                                                   uploadText.setVisibility((View.INVISIBLE));
                                                   if (profPic.hasWindowFocus()) //Has Finger moved off image?
                                                   {
                                                       profPic.callOnClick();
                                                   }
                                                   break;
                                               default:
                                                   break;
                                           }
                                           Log.d("ProfileActivity", "onTouch: Profile Image TextView");
                                           return true;
                                       }
                                   }
        );

        //New Intent -- Browse Image
        profPic.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                getNewProfPic();
            }


        });
    }

    public void getNewProfPic() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ProfileActivity.super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            profPicPath = data.getData();
               /* try {
                    //TODO: DISPLAY IMAGE WITH CONFIRMATION FIRST
                    //Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), profPicPath);

                    uploadProfPic();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }*/

            //get file size
            final ParcelFileDescriptor parcelFileDescriptor;
            try {
                parcelFileDescriptor = getContentResolver().openFileDescriptor(
                        profPicPath, "r");
                int profPicSize = (int) parcelFileDescriptor.getStatSize() / 1024 / 1024;
                Log.d("ProfileActivity", "IMAGE SIZE REPORTED " + profPicSize);
                if (profPicSize <= 8) {
                    uploadProfPic();
                } else {
                    Toast.makeText(ProfileActivity.this, "Upload Failed: " + profPicSize + "MB > 5 MB Maximum Size", Toast.LENGTH_SHORT).show();

                }
            } catch (FileNotFoundException e) {
                Log.w("ProfileActivity", "Unable to retrieve image size");
                Toast.makeText(ProfileActivity.this, "Upload Failed: Unable to Determine Image Size", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                //uploadProfPic();

            }
        }
    }


    //upload new profile image
    private void uploadProfPic() {

        if (profPicPath != null) {
            String image = "thumb_".concat(auth.getUid());
            profPicRef = cloudStorage.getReference("users").child(image);
            profPicRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("ProfileActivity", "Old Compressed ProfPic Deleted Successfully");
                    // File deleted successfully
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.w("ProfileActivity", "Old Compressed ProfPic Failed to Delete");


                }
            });
            profPicRef = cloudStorage.getReference("users").child(auth.getUid()); //delete original
            profPicRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("ProfileActivity", "Old ProfPic Deleted Successfully");
                    // File deleted successfully
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {

                }
            });
            uploadProgress = (ProgressBar) findViewById(R.id.uploadProgress); //instantiate progress bar

            profPicRef.putFile(profPicPath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            uploadProgress.setProgress(100);
                            Toast.makeText(ProfileActivity.this, "Upload Complete", Toast.LENGTH_SHORT).show();

                            //update profPic info in user profile
                            Map<String, Object> picStatus = new HashMap<>();
                            picStatus.put("hasProfPic", true);
                            database.collection("users").document(auth.getUid()).update(picStatus);
                            fetchProfPic(true);

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            uploadProgress.setProgress(0);
                            Toast.makeText(ProfileActivity.this, "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            //update profPic info in user profile
                            Map<String, Object> picStatus = new HashMap<>();
                            picStatus.put("hasProfPic", false);
                            database.collection("users").document(auth.getUid()).update(picStatus);
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() { //update progress bar
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            int progress = (int) Math.round(100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                            uploadProgress.setProgress(progress);
                        }
                    });
        }
    }


    //convert memory held byte array to image view for display
    public static void byteArrayToImageView(ImageView view, byte[] data) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        view.setImageBitmap(bitmap);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }

    @Override
    public void onBackPressed() { //so cannot get back in after signing out
        startActivity(new Intent(ProfileActivity.this, report.class));
        finish();    }
}