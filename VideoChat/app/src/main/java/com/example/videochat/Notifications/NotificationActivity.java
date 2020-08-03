package com.example.videochat.Notifications;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.videochat.Home.FindPeopleActivity;
import com.example.videochat.Home.HomeActivity;
import com.example.videochat.Modals.Contacts;
import com.example.videochat.R;
import com.example.videochat.Utils.BottomNavigationViewHelper;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.squareup.picasso.Picasso;

public class NotificationActivity extends AppCompatActivity
{
    private static final String TAG = "NotificationActivity";
    private static final int ACTIVITY_NUM = 2;

    private Context mContext;

    private String currentUserId;
    private RecyclerView notificationList;
    private DatabaseReference friendRequestRef, contactsRef, userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        mContext = NotificationActivity.this;

        notificationList = findViewById(R.id.notification_list);
        notificationList.setLayoutManager(new LinearLayoutManager(mContext));

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("friend_request");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("contacts");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        setupBottomNavigationView();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder
    {
        TextView userNameTextView;
        Button acceptBtn, cancelBtn;
        ImageView profileImageView;
        RelativeLayout cardView;

        @SuppressLint("CutPasteId")
        public NotificationViewHolder(@NonNull View itemView)
        {
            super(itemView);

            userNameTextView = itemView.findViewById(R.id.name_notification);
            acceptBtn = itemView.findViewById(R.id.request_accept_btn);
            cancelBtn = itemView.findViewById(R.id.request_accept_btn);
            profileImageView = itemView.findViewById(R.id.image_notification);
            cardView = itemView.findViewById(R.id.relative_layout_card_view);
        }
    }

    private void cancelFriendRequest(final boolean isFriend, final String listUserId)
    {
        friendRequestRef.child(currentUserId).child(listUserId).removeValue()
        .addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    friendRequestRef.child(listUserId).child(currentUserId).removeValue()
                    .addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                if(isFriend)
                                {

                                }
                                else
                                {

                                }
                            }
                        }
                    });
                }
            }
        });
    }

    private void acceptFriendRequest(final String listUserId)
    {
        contactsRef.child(currentUserId).child(listUserId).child("contact").setValue("saved")
        .addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    contactsRef.child(listUserId).child(currentUserId).child("contact").setValue("saved")
                    .addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                cancelFriendRequest(true, listUserId);
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(friendRequestRef.child(currentUserId), Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, NotificationViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Contacts, NotificationViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull final NotificationViewHolder holder, int position, @NonNull Contacts model)
            {
                holder.acceptBtn.setVisibility(View.VISIBLE);
                holder.cancelBtn.setVisibility(View.VISIBLE);

                // listUserId is the authId of the user ( remember we are using firebase recyclerView and not the common one ). friendRequestRef.child(currentUserId)
                // means all the children of currentUserId node, hence one by one we will get the userId of all the users inside currentUsedId node in listUserId.
                final String listUserId = getRef(position).getKey();

                DatabaseReference requestTypeRef = getRef(position).child("request_type").getRef();

                requestTypeRef.addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if(snapshot.exists())
                        {
                            String type = snapshot.getValue().toString();

                            if(type.equals("received"))
                            {
                                holder.cardView.setVisibility(View.VISIBLE);

                                assert listUserId != null;
                                userRef.child(listUserId).addValueEventListener(new ValueEventListener()
                                {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot)
                                    {
                                        if(snapshot.hasChild("images"))
                                        {
                                            final String imageStr = snapshot.child("image").getValue().toString();

                                            Picasso.get().load(imageStr).into(holder.profileImageView);
                                        }

                                        final String nameStr = snapshot.child("user_name").getValue().toString();
                                        holder.userNameTextView.setText(nameStr);

                                        holder.acceptBtn.setOnClickListener(new View.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(View view)
                                            {
                                                acceptFriendRequest(listUserId);
                                            }
                                        });

                                        holder.cancelBtn.setOnClickListener(new View.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(View view)
                                            {
                                                cancelFriendRequest(false, listUserId);
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                            else
                            {

                            }
                        }
                        else
                        {

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(mContext).inflate(R.layout.find_friend_design, parent, false);
                NotificationViewHolder viewHolder = new NotificationViewHolder(view);

                return viewHolder;
            }
        };

        notificationList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    // BottomNavigationView setup.
    private void setupBottomNavigationView()
    {
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottomNavViewBar);

        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, bottomNavigationViewEx);

        Menu menu = bottomNavigationViewEx.getMenu();

        // Gets the menu item at the given index.
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}