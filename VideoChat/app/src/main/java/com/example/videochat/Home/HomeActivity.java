package com.example.videochat.Home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.videochat.Modals.Contacts;
import com.example.videochat.Notifications.NotificationActivity;
import com.example.videochat.R;
import com.example.videochat.Settings.SettingActivity;
import com.example.videochat.Utils.BottomNavigationViewHelper;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.squareup.picasso.Picasso;

public class HomeActivity extends AppCompatActivity
{
    private static final String TAG = "HomeActivity";
    private static final int ACTIVITY_NUM = 0;

    private Context mContext;
    RecyclerView myContactList;
    private ImageView findPeopleBtn;
    private String currentUserId, userName = "", profileImage = "", calledBy = "";
    private DatabaseReference contactsRef, userRef;

    private void setInitialConfiguration()
    {
        mContext = HomeActivity.this;

        findPeopleBtn = findViewById(R.id.find_people_btn);
        myContactList = findViewById(R.id.contact_list);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        contactsRef = FirebaseDatabase.getInstance().getReference().child("contacts");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    private void setRecyclerView()
    {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        myContactList.setLayoutManager(linearLayoutManager);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        setInitialConfiguration();
        setRecyclerView();
        setupBottomNavigationView();

        findPeopleBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(mContext, FindPeopleActivity.class);
                startActivity(intent);
            }
        });
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder
    {
        TextView userNameTextView;
        Button callBtn;
        ImageView profileImageView;

        public ContactViewHolder(@NonNull View itemView)
        {
            super(itemView);

            userNameTextView = itemView.findViewById(R.id.name_contact);
            callBtn = itemView.findViewById(R.id.call_btn);
            profileImageView = itemView.findViewById(R.id.image_contact);
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        checkForReceivingCall();

        checkUserHasFilledUserDaa();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactsRef.child(currentUserId), Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, ContactViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Contacts, ContactViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull final ContactViewHolder holder, int position, @NonNull Contacts model)
            {
                // listUserId is the authId of the user ( remember we are using firebase recyclerView and not the common one ). friendRequestRef.child(currentUserId)
                // means all the children of currentUserId node, hence one by one we will get the userId of all the users inside currentUsedId node in listUserId.
                final String listUserId = getRef(position).getKey();

                assert listUserId != null;
                userRef.child(listUserId).addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if(snapshot.exists())
                        {
                            userName = snapshot.child("user_name").getValue().toString();
                            profileImage = snapshot.child("image").getValue().toString();

                            holder.userNameTextView.setText(userName);
                            Picasso.get().load(profileImage).into(holder.profileImageView);
                        }

                        holder.callBtn.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                Intent intent = new Intent(mContext, CallingActivity.class);
                                intent.putExtra("visit_user_id", listUserId);

                                startActivity(intent);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(mContext).inflate(R.layout.contact_design, parent, false);
                ContactViewHolder viewHolder = new ContactViewHolder(view);

                return viewHolder;
            }
        };

        myContactList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    private void checkForReceivingCall()
    {
        userRef.child(currentUserId).child("ringing").addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.hasChild("ringing"))
                {
                    calledBy = snapshot.child("ringing").getValue().toString();

                    Intent intent = new Intent(mContext, CallingActivity.class);
                    intent.putExtra("visit_user_id", calledBy);

                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkUserHasFilledUserDaa()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        // addValueEventListener() keep listening to query or database reference it is attached to. But addListenerForSingleValueEvent() executes onDataChange method
        // immediately and after executing that method once, it stops listening to the reference location it is attached to.
        reference.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(!snapshot.exists())
                {
                    Intent intent = new Intent(mContext, SettingActivity.class);
                    startActivity(intent);

                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
    }

    // BottomNavigationView setup.
    private void setupBottomNavigationView()
    {
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);

        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, bottomNavigationViewEx);

        Menu menu = bottomNavigationViewEx.getMenu();

        // Gets the menu item at the given index.
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}