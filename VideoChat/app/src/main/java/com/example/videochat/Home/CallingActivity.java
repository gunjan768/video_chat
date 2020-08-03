package com.example.videochat.Home;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.videochat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class CallingActivity extends AppCompatActivity
{
    private static final String TAG = "HomeActivity";

    private Context mContext;
    private TextView nameContact;
    private ImageView profileImage;
    private ImageView cancelCallBtn, acceptCallBtn;

    private String receiverUserId = "", receiverUserName = "", receiverImage = "";
    private String senderUserId = "", senderUserName = "", senderImage = "", checker = "";
    private String callingId = "", ringingId = "";

    private MediaPlayer ringTone;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);

        mContext = CallingActivity.this;

        receiverUserId = getIntent().getStringExtra("visit_user_id");
        senderUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        ringTone = MediaPlayer.create(this, R.raw.naino_ki_baat);
        nameContact = findViewById(R.id.name_calling);
        profileImage = findViewById(R.id.profile_image_calling);
        cancelCallBtn = findViewById(R.id.cancel_call);
        acceptCallBtn = findViewById(R.id.make_call);

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        cancelCallBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                ringTone.stop();
                checker = "cancelled";

                cancelCallingUser();
            }
        });

        acceptCallBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                final HashMap<String, Object> callingPickUpMap = new HashMap<>();
                callingPickUpMap.put("picked", "picked");

                // setValue() requires you to set all the fields under the same parent node otherwise they are overwritten with no values and deleted. However, using
                // updateChildValue, you can specify which field you would like to update without altering other fields. And, if the field doesn't already exists ,
                // it will create a new field. This is especially useful if you just want to add a new field under the user like hair colour.
                userRef.child(receiverUserId).child("calling").updateChildren(callingPickUpMap)
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            userRef.child(senderUserId).child("ringing").updateChildren(callingPickUpMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {

                                }
                            });
                        }
                    }
                });
            }
        });

        getAndSetProfileInfo();
    }

    private void getAndSetProfileInfo()
    {
        userRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.child(receiverUserId).exists())
                {
                    receiverImage = snapshot.child(receiverUserId).child("image").getValue().toString();
                    receiverUserName = snapshot.child(receiverUserId).child("user_name").getValue().toString();

                    nameContact.setText(receiverUserName);
                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(profileImage);
                }

                if(snapshot.child(senderUserId).exists())
                {
                    senderImage = snapshot.child(senderUserId).child("image").getValue().toString();
                    senderUserName = snapshot.child(senderUserId).child("user_name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void cancelCallingUser()
    {
        // From sender's side.
        userRef.child(senderUserId).child("calling").addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.exists() & snapshot.hasChild("calling"))
                {
                    callingId = snapshot.child("calling").getValue().toString();

                    userRef.child(callingId).child("ringing").removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                userRef.child(senderUserId).child("calling").removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                                {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        // startActivity(new Intent(mContext, RegisterActivity.class));
                                        // finish();
                                    }
                                });
                            }
                        }
                    });
                }
//                else
//                {
//                    startActivity(new Intent(mContext, RegisterActivity.class));
//                    finish();
//                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // From receiver's side.
        userRef.child(senderUserId).child("ringing").addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.exists() & snapshot.hasChild("ringing"))
                {
                    ringingId = snapshot.child("ringing").getValue().toString();

                    userRef.child(ringingId).child("calling").removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                userRef.child(senderUserId).child("ringing").removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                                {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        // startActivity(new Intent(mContext, RegisterActivity.class));
                                        // finish();
                                    }
                                });
                            }
                        }
                    });
                }
//                else
//                {
//                    startActivity(new Intent(mContext, RegisterActivity.class));
//                    finish();
//                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        ringTone.start();

        userRef.child(receiverUserId).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(!checker.equals("cancelled") && !snapshot.hasChild("calling") && !snapshot.hasChild("ringing"))
                {
                    final HashMap<String, Object> callingInfo = new HashMap<>();
                    callingInfo.put("calling", receiverUserId);

                    userRef.child(senderUserId).child("calling").updateChildren(callingInfo)
                    .addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                final HashMap<String, Object> ringingInfo = new HashMap<>();
                                ringingInfo.put("ringing", senderUserId);

                                userRef.child(receiverUserId).child("ringing").updateChildren(ringingInfo);
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        userRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.child(senderUserId).hasChild("ringing") && !snapshot.child(senderUserId).hasChild("calling"))
                {
                    acceptCallBtn.setVisibility(View.VISIBLE);
                }

//                if(snapshot.child(receiverUserId).child("ringing").hasChild("picked"))
//                {
//                    ringTone.stop();
//
//                    Intent intent = new Intent(mContext, VideoChatActivity.class);
//                    startActivity(intent);
//                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // For receiver to go back.
        userRef.child(senderUserId).addChildEventListener(new ChildEventListener()
        {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
            {
                if((snapshot.hasChild("calling") && snapshot.hasChild("picked")))
                {
                    ringTone.stop();

                    Intent intent = new Intent(mContext, VideoChatActivity.class);
                    startActivity(intent);

                    finish();
                }

                if((snapshot.hasChild("ringing") && snapshot.hasChild("picked")))
                {
                    ringTone.stop();

                    Intent intent = new Intent(mContext, VideoChatActivity.class);
                    startActivity(intent);

                    finish();
                }
            }

            // The snapshot passed to the callback contains the data for the removed child.
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.hasChild("calling") || snapshot.hasChild("ringing"))
                {
                    if(ringTone.isPlaying())
                    ringTone.stop();

                    Intent intent = new Intent(mContext, HomeActivity.class);
                    startActivity(intent);

                    finish();
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}