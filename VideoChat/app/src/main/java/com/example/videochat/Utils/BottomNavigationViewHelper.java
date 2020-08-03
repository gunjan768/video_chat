package com.example.videochat.Utils;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.videochat.Auth.RegisterActivity;
import com.example.videochat.Home.HomeActivity;
import com.example.videochat.Notifications.NotificationActivity;
import com.example.videochat.R;
import com.example.videochat.Settings.SettingActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

public class BottomNavigationViewHelper extends AppCompatActivity
{
    private static final String TAG = "BottomNavigationViewHelper";

    public static void setupBottomNavigationView(BottomNavigationViewEx bottomNavigationViewEx)
    {
//        bottomNavigationViewEx.enableAnimation(false);
//        bottomNavigationViewEx.enableItemShiftingMode(false);
//        bottomNavigationViewEx.enableShiftingMode(false);
//        bottomNavigationViewEx.setTextVisibility(false);
    }

    public static void enableNavigation(final Context context, BottomNavigationViewEx bottomNavigationViewEx)
    {
        bottomNavigationViewEx.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item)
            {
                switch(item.getItemId())
                {
                    case R.id.navigation_home:
                        Intent intent1 = new Intent(context, HomeActivity.class);
                        context.startActivity(intent1);

                        break;

                    case R.id.navigation_dashboard:
                        Intent intent2  = new Intent(context, SettingActivity.class);
                        context.startActivity(intent2);

                        break;

                    case R.id.navigation_notification:
                        Intent intent3 = new Intent(context, NotificationActivity.class);
                        context.startActivity(intent3);

                        break;

                    case R.id.navigation_logout:
                        FirebaseAuth.getInstance().signOut();

                        Intent intent4 = new Intent(context, RegisterActivity.class);
                        context.startActivity(intent4);

                        break;
                }

                return true;
            }
        });
    }
}