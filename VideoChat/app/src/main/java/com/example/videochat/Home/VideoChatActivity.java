package com.example.videochat.Home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.example.videochat.Auth.RegisterActivity;
import com.example.videochat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import java.util.Objects;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class VideoChatActivity extends AppCompatActivity implements Session.SessionListener, PublisherKit.PublisherListener
{
    public static final String TAG = VideoChatActivity.class.getSimpleName();

    private Context mContext;
    public static String API_KEY = "";
    public static String SESSION_ID = "";
    public static String TOKEN = "";

    public static final int RC_VIDEO_APP_PERMISSION = 124;

    private ImageView closeVideoChatBtn;
    private DatabaseReference userRef;
    private String userId;

    private Session session;
    private Publisher publisher;
    private Subscriber subscriber;

    private FrameLayout publisherViewController, subscriberViewController;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat);

        mContext = VideoChatActivity.this;
        userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        closeVideoChatBtn = findViewById(R.id.close_video_chat_btn);

//        closeVideoChatBtn.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View view)
//            {
//                userRef = FirebaseDatabase.getInstance().getReference().child("Users");
//
//                userRef.addValueEventListener(new ValueEventListener()
//                {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot)
//                    {
//                        if(snapshot.child(userId).hasChild("ringing"))
//                        {
//                            userRef.child(userId).child("ringing").removeValue();
//
//                            if(publisher != null)
//                            {
//                                publisher.destroy();
//                            }
//
//                            if(subscriber != null)
//                            {
//                                subscriber.destroy();
//                            }
//
//                            startActivity(new Intent(mContext, RegisterActivity.class));
//                            finish();
//                        }
//                        else if(snapshot.child(userId).hasChild("calling"))
//                        {
//                            userRef.child(userId).child("calling").removeValue();
//
//                            if(publisher != null)
//                            {
//                                publisher.destroy();
//                            }
//
//                            if(subscriber != null)
//                            {
//                                subscriber.destroy();
//                            }
//
//                            startActivity(new Intent(mContext, RegisterActivity.class));
//                            finish();
//                        }
//                        else
//                        {
//                            if(publisher != null)
//                            {
//                                publisher.destroy();
//                            }
//
//                            if(subscriber != null)
//                            {
//                                subscriber.destroy();
//                            }
//
//                            startActivity(new Intent(mContext, RegisterActivity.class));
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
//            }
//        });

        requestPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, mContext);
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERMISSION)
    private void requestPermissions()
    {
        String[] permissions = { Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO };

        if(EasyPermissions.hasPermissions(mContext, permissions))
        {
            publisherViewController = findViewById(R.id.publisher_container);
            subscriberViewController = findViewById(R.id.subscriber_container);

            // 1) Initialize and connect to the session.
            session = new Session.Builder(this, API_KEY, SESSION_ID).build();

            session.setSessionListener(this);
            session.connect(TOKEN);
        }
        else
        {
            EasyPermissions.requestPermissions(this, "Hey this apps needs Mic and Camera, Please allow...", RC_VIDEO_APP_PERMISSION, permissions);
        }
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }

    // 2) Publishing a stream to the session.
    @Override
    public void onConnected(Session session)
    {
        // Log.i(TAG, "Session Connected");

        publisher = new Publisher.Builder(this).build();
        publisher.setPublisherListener(VideoChatActivity.this);

        publisherViewController.addView(publisher.getView());

        // GLSurfaceView is the class in android which makes it easy to choose appropriate frame buffer excel format. Basically in easy words if we don't use
        // GLSurfaceView then we can't see both frame layout while video calling. Your's ( publisher ) frameLayout will get hidden and only able to see receiver's
        // ( subscriber ) screen or frameLayout.
        if(publisher.getView() instanceof GLSurfaceView)
        {
            // setZOrderOnTop() method will make the z-index of publisher's frameLayout higher so that it can't be hidden anymore.
            ((GLSurfaceView) publisher.getView()).setZOrderOnTop(true);
        }

        session.publish(publisher);
    }

    @Override
    public void onDisconnected(Session session)
    {
        // Log.i(TAG, "Stream disconnected");
    }

    // 3) Subscribing to the stream.
    @Override
    public void onStreamReceived(Session session, Stream stream)
    {
        // Log.i(TAG, "Stream received");

        if(subscriber == null)
        {
            subscriber = new Subscriber.Builder(this, stream).build();
            session.subscribe(subscriber);

            subscriberViewController.addView(subscriber.getView());
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream)
    {
        // Log.i(TAG, "Stream dropped");

        if(subscriber != null)
        {
            subscriber = null;
            subscriberViewController.removeAllViews();
        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError)
    {
        // Log.i(TAG, "Error");
    }
}
