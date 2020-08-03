package com.example.videochat.Home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.videochat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class ProfileActivity extends AppCompatActivity
{
    private String receiverUserId = "", receiverUserImage = "", receiverUserName = "";
    private ImageView backgroundProfileView;
    private TextView profileName;
    private Button addFriend, declineFriendRequest;
    private String currentUserState = "new", senderUserId;

    private FirebaseUser currentUser;
    private DatabaseReference friendRequestRef, contactsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        receiverUserId = getIntent().getStringExtra("visit_user_id");
        receiverUserImage = getIntent().getStringExtra("profile_image");
        receiverUserName = getIntent().getStringExtra("user_name");

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        assert currentUser != null;
        senderUserId = currentUser.getUid();

        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("friend_request");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("contacts");

        backgroundProfileView = findViewById(R.id.background_profile_view);
        profileName = findViewById(R.id.name_profile);
        addFriend = findViewById(R.id.add_friend);
        declineFriendRequest = findViewById(R.id.decline_friend_request);

        Picasso.get().load(receiverUserImage).into(backgroundProfileView);
        profileName.setText(receiverUserName);

        manageOnClickListeners();
    }

    private void manageOnClickListeners()
    {
        friendRequestRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.hasChild(receiverUserId))
                {
                    String requestType = Objects.requireNonNull(snapshot.child(receiverUserId).child("request_type").getValue()).toString();

                    if(requestType.equals("sent"))
                    {
                        currentUserState = "request_sent";
                        addFriend.setText(R.string.cancel_friend_request);
                    }
                    else if(requestType.equals("received"))
                    {
                        currentUserState = "request_received";
                        addFriend.setText(R.string.accept_friend_request);

                        declineFriendRequest.setVisibility(View.VISIBLE);

                        declineFriendRequest.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                cancelFriendRequest(false);
                            }
                        });
                    }
                }
                else
                {
                    contactsRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot)
                        {
                            if(snapshot.hasChild(receiverUserId))
                            {
                                currentUserState = "friends";
                                addFriend.setText(R.string.delete_contact);
                            }
                            else
                            {
                                currentUserState = "new";
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });

        if(currentUser.getUid().equals(receiverUserId))
        {
            addFriend.setVisibility(View.GONE);
        }
        else
        {
            addFriend.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(currentUserState.equals("new"))
                    {
                        sendFriendRequest();
                    }

                    if(currentUserState.equals("request_sent"))
                    {
                        cancelFriendRequest(false);
                    }

                    if(currentUserState.equals("request_received"))
                    {
                        acceptFriendRequest();
                    }
                }
            });
        }
    }

    private void sendFriendRequest()
    {
        friendRequestRef.child(senderUserId).child(receiverUserId).child("request_type").setValue("sent")
        .addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    friendRequestRef.child(receiverUserId).child(senderUserId).child("request_type").setValue("received")
                    .addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                currentUserState = "request_sent";
                                addFriend.setText(R.string.cancel_friend_request);
                            }
                        }
                    });
                }
            }
        });
    }

    private void cancelFriendRequest(final boolean isFriend)
    {
        friendRequestRef.child(senderUserId).child(receiverUserId).removeValue()
        .addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    friendRequestRef.child(receiverUserId).child(senderUserId).removeValue()
                    .addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                if(isFriend)
                                {
                                    currentUserState = "friends";
                                    addFriend.setText(R.string.delete_contact);

                                    declineFriendRequest.setVisibility(View.GONE);
                                }
                                else
                                {
                                    currentUserState = "new";
                                    addFriend.setText(R.string.add_friend);
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    private void acceptFriendRequest()
    {
        contactsRef.child(senderUserId).child(receiverUserId).child("contact").setValue("saved")
        .addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    contactsRef.child(receiverUserId).child(senderUserId).child("contact").setValue("saved")
                    .addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                cancelFriendRequest(true);
                            }
                        }
                    });
                }
            }
        });
    }
}