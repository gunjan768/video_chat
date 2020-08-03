package com.example.videochat.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.example.videochat.Home.HomeActivity;
import com.example.videochat.R;
import com.example.videochat.Utils.BottomNavigationViewHelper;
import com.example.videochat.Utils.Helper;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Objects;

import dmax.dialog.SpotsDialog;

public class SettingActivity extends AppCompatActivity
{
    private static final String TAG = "SettingActivity";
    private static final int ACTIVITY_NUM = 1;

    private Button saveBtn;
    private EditText userNameEditText, userBioEditText;
    private ImageView profileImageView;
    private Helper helper;
    private RelativeLayout settingRelativeLayout;

    private static int GALLERY_PICK = 1;
    private Context context;
    private Uri imageUri;
    private StorageReference storageReference;
    private String downloadUrl;
    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;
    private ProgressBar progressBar;
    private AlertDialog progressDialog;

    private void setInitialConfiguration()
    {
        context = SettingActivity.this;
        helper = new Helper(context);
        saveBtn = findViewById(R.id.save_settings_btn);

        userNameEditText = findViewById(R.id.username_settings);
        userNameEditText.setSelection(userNameEditText.getText().length());

        userBioEditText = findViewById(R.id.bio_settings);
        userBioEditText.setSelection(userBioEditText.getText().length());

        profileImageView = findViewById(R.id.settings_profile_image);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        progressBar = findViewById(R.id.loader);
        settingRelativeLayout = findViewById(R.id.setting_relative_layout);
        settingRelativeLayout.setVisibility(View.GONE);

        progressDialog = new SpotsDialog.Builder().setContext(context).setTheme(R.style.Custom).build();
    }

    private void alterProgressbar(boolean flag)
    {
        if(flag)
        {
            progressBar.setVisibility(View.VISIBLE);
            // progressBar.setBackgroundColor(Color.BLACK);

            progressBar.setIndeterminateTintList(ColorStateList.valueOf(Color.BLACK));

            // To disable the user interaction you just need to add the following code.
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
        else
        {
            progressBar.setVisibility(View.GONE);

            // To get user interaction back you just need to add the following code.
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        setInitialConfiguration();
        retrieveCurrentUserInfo();
        setupBottomNavigationView();

        profileImageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent galleryIntent = new Intent();

                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_PICK);
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                saveUserDataToDatabase();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK && data != null)
        {
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
        }
    }

    private void saveUserDataToDatabase()
    {
        final String getUserName = userNameEditText.getText().toString().replaceAll("[^a-zA-Z0-9]", " ");
        final String getUserBio = userBioEditText.getText().toString().replaceAll("[^a-zA-Z0-9]", " ");

        if(!helper.isNotNull(getUserName))
        {
            helper.showToastMessage("User name is required.It can't be empty");

            return;
        }

        if(!helper.isNotNull(getUserBio))
        {
            helper.showToastMessage("User bio is required.It can't be empty");

            return;
        }

        if(imageUri == null)
        {
            progressDialog.show();

            HashMap<String, Object> profileMap = new HashMap<>();

            profileMap.put("uid", currentUser.getUid());
            profileMap.put("user_name", getUserName);
            profileMap.put("bio", getUserBio);

            databaseReference.child("Users").child(currentUser.getUid()).updateChildren(profileMap).
            addOnCompleteListener(new OnCompleteListener<Void>()
            {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    if(task.isSuccessful())
                    {
                        progressDialog.dismiss();

                        // Intent intent = new Intent(context, HomeActivity.class);
                        // startActivity(intent);

                        helper.showToastMessage("Profile settings has been updated");
                    }
                    else
                    {
                        progressDialog.dismiss();
                    }
                }
            });
        }
        else
        {
            progressDialog.show();

            assert currentUser != null;
            final StorageReference filepath = storageReference.child("profile_images").child(currentUser.getUid());

            final UploadTask uploadTask = filepath.putFile(imageUri);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>()
            {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
                {
                    if(!task.isSuccessful())
                    {
                        throw Objects.requireNonNull(task.getException());
                    }

                    // Continue with the task to get the download URL.
                    return filepath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>()
            {
                @Override
                public void onComplete(@NonNull Task<Uri> task)
                {
                    if(task.isSuccessful())
                    {
                        downloadUrl = task.getResult().toString();

                        HashMap<String, Object> profileMap = new HashMap<>();

                        profileMap.put("uid", currentUser.getUid());
                        profileMap.put("user_name", getUserName);
                        profileMap.put("bio", getUserBio);
                        profileMap.put("image", downloadUrl);

                        databaseReference.child("Users").child(currentUser.getUid()).updateChildren(profileMap).
                        addOnCompleteListener(new OnCompleteListener<Void>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<Void> task)
                            {
                                if(task.isSuccessful())
                                {
                                    progressDialog.dismiss();

                                    // Intent intent = new Intent(context, HomeActivity.class);
                                    // startActivity(intent);

                                    helper.showToastMessage("Profile settings has been updated");
                                }
                            }
                        });
                    }
                    else
                    {
                        progressDialog.dismiss();
                    }
                }
            });
        }
    }

    private void retrieveCurrentUserInfo()
    {
        alterProgressbar(true);

        databaseReference.child("Users").child(currentUser.getUid()).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.exists())
                {
                    String userImage = snapshot.child("image").getValue().toString();
                    String userName = snapshot.child("user_name").getValue().toString();
                    String userBio = snapshot.child("bio").getValue().toString();

                    userNameEditText.setText(userName);
                    userNameEditText.setSelection(userNameEditText.getText().length());

                    userBioEditText.setText(userBio);
                    userBioEditText.setSelection(userBioEditText.getText().length());

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(profileImageView);

                    alterProgressbar(false);
                    settingRelativeLayout.setVisibility(View.VISIBLE);
                }
                else
                {
                    alterProgressbar(false);
                    settingRelativeLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                alterProgressbar(false);
                settingRelativeLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    // BottomNavigationView setup.
    private void setupBottomNavigationView()
    {
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottomNavViewBar);

        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(context, bottomNavigationViewEx);

        Menu menu = bottomNavigationViewEx.getMenu();

        // Gets the menu item at the given index.
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}