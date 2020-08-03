package com.example.videochat.Utils;

import android.content.Context;
import android.widget.Toast;

public class Helper
{
    private Context context;

    public Helper(Context context)
    {
        this.context = context;
    }

    public boolean isNotNull(String value)
    {
        return !value.equals("");
    }

    public void showToastMessage(String message)
    {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}