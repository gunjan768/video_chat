package com.example.videochat.Home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.videochat.Modals.Contacts;
import com.example.videochat.R;
import com.example.videochat.Utils.Helper;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class FindPeopleActivity extends AppCompatActivity
{
    private static final String TAG = "FindPeopleActivity";

    private Context mContext;
    private RecyclerView findFriendList;
    private EditText searchEditText;
    private String str = "";
    private Helper helper;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_people);

        mContext = FindPeopleActivity.this;
        searchEditText = findViewById(R.id.search_user_text);
        findFriendList = findViewById(R.id.searched_list);
        helper = new Helper(mContext);
        findFriendList.setLayoutManager(new LinearLayoutManager(mContext));
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        searchEditText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
                if(helper.isNotNull(searchEditText.getText().toString()))
                {
                    str = charSequence.toString();
                    onStart();
                }
                else
                {
                    // helper.showToastMessage("Please enter at least one character to search");
                }
            }

            @Override
            public void afterTextChanged(Editable editable)
            {

            }
        });
    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userNameTextView;
        Button videoCallBtn;
        ImageView profileImageView;
        RelativeLayout cardView;

        public FindFriendsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            userNameTextView = itemView.findViewById(R.id.name_contact);
            videoCallBtn = itemView.findViewById(R.id.call_btn);
            profileImageView = itemView.findViewById(R.id.image_contact);
            cardView = itemView.findViewById(R.id.relative_layout_card_view_contact);

            videoCallBtn.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = null;

        if(str.equals(""))
        {
            options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(userRef, Contacts.class).build();
        }
        else
        {
            options = new FirebaseRecyclerOptions.Builder<Contacts>()
                    .setQuery(userRef.orderByChild("user_name").startAt(str).endAt(str + "\uf8ff"), Contacts.class).build();
        }

        FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, final int position, @NonNull final Contacts model)
            {
                holder.userNameTextView.setText(model.getUser_name());
                Picasso.get().load(model.getImage()).into(holder.profileImageView);

                holder.itemView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        String visitUserId = getRef(position).getKey();

                        Intent intent = new Intent(mContext, ProfileActivity.class);

                        intent.putExtra("visit_user_id", visitUserId);
                        intent.putExtra("profile_image", model.getImage());
                        intent.putExtra("user_name", model.getUser_name());

                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(mContext).inflate(R.layout.contact_design, parent, false);
                FindFriendsViewHolder viewHolder = new FindFriendsViewHolder(view);

                return viewHolder;
            }
        };

        findFriendList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }
}