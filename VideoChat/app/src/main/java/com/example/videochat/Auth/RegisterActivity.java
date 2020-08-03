package com.example.videochat.Auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.example.videochat.Home.HomeActivity;
import com.example.videochat.MainActivity;
import com.example.videochat.R;
import com.example.videochat.Utils.Helper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity
{
    private String TAG = "RegisterActivity";

    private CountryCodePicker countryCodePicker;
    private EditText codeText;
    private Button continueNextButton;
    private String checker = "", phoneNumber = "", mVerificationId, message = "";
    private RelativeLayout relativeLayout;
    private Helper helper;
    private Context context;
    private ProgressBar loadingBar;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private PhoneAuthProvider.ForceResendingToken mResendingToken;
    private FirebaseAuth mAuth;

    private void setInitialConfiguration()
    {
        EditText phoneText = findViewById(R.id.phoneText);
        codeText = findViewById(R.id.codeText);
        continueNextButton = findViewById(R.id.continueNextButton);
        relativeLayout = findViewById(R.id.phoneAuth);
        loadingBar = findViewById(R.id.loadingBar);
        context = RegisterActivity.this;

        mAuth = FirebaseAuth.getInstance();
        helper = new Helper(context);

        countryCodePicker = findViewById(R.id.ccp);
        countryCodePicker.registerCarrierNumberEditText(phoneText);
    }

    private void alterProgressbar(boolean flag)
    {
        if(flag)
        {
            loadingBar.setVisibility(View.VISIBLE);
            // loadingBar.setBackgroundColor(Color.RED);

            loadingBar.setIndeterminateTintList(ColorStateList.valueOf(Color.RED));

            // To disable the user interaction you just need to add the following code.
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
        else
        {
            loadingBar.setVisibility(View.GONE);

            // To get user interaction back you just need to add the following code.
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        setInitialConfiguration();

        continueNextButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(continueNextButton.getText().equals(getString(R.string.submit)) || checker.equals(getString(R.string.code_sent)))
                {
                    String verificationCode = codeText.getText().toString();

                    if(helper.isNotNull(verificationCode))
                    {
                        alterProgressbar(true);

                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                        signInWithPhoneAuthCredential(credential);
                    }
                    else
                    {
                        message = "Verification code can't be empty";
                        helper.showToastMessage(message);
                    }
                }
                else
                {
                    phoneNumber = countryCodePicker.getFullNumberWithPlus();

                    // Log.i("valllllllllllllllllllllllllllllllllllllllllllllll", phoneNumber);

                    if(helper.isNotNull(phoneNumber))
                    {
                        alterProgressbar(true);

                        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                                phoneNumber,                // Phone number to verify
                                60,                      // Timeout duration
                                TimeUnit.SECONDS,          // Unit of timeout
                                (Activity) context,        // Activity (for callback binding)
                                mCallbacks                // OnVerificationStateChangedCallbacks
                        );
                    }
                    else
                    {
                        message = "Please enter the valid phone number";
                        helper.showToastMessage(message);
                    }
                }
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks()
        {
            // This callback will be invoked in two situations :
            // 1. Instant verification. In some cases the phone number can be instantly verified without needing to send or enter a verification code.
            // 2. Auto-retrieval. On some devices Google Play services can automatically detect the incoming verification SMS and perform verification without
            // user action.
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential)
            {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            // This callback is invoked in an invalid request for verification is made, for instance if the the phone number format is not valid.
            @Override
            public void onVerificationFailed(@NonNull FirebaseException e)
            {
                alterProgressbar(false);

                message = "Error : " + e;
                helper.showToastMessage(message);

                relativeLayout.setVisibility(View.VISIBLE);
                continueNextButton.setText(getString(R.string.continues));
                codeText.setVisibility(View.GONE);
            }

            // If the sim ( phone number which is used to sent the notification ) is not in the same device ( mobile ) which is used to run the app then
            // he will receive the code in another device ( sim is int his device ). If sim is in the same device then the code will be verified automatically.
            // The SMS verification code has been sent to the provided phone number, we now need to ask the user to enter the code and then construct a
            // credential by combining the code with a verification ID.
            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken)
            {
                super.onCodeSent(s, forceResendingToken);

                mVerificationId = s;
                mResendingToken = forceResendingToken;

                relativeLayout.setVisibility(View.GONE);
                checker = getString(R.string.code_sent);
                continueNextButton.setText(getString(R.string.submit));
                codeText.setVisibility(View.VISIBLE);

                alterProgressbar(false);

                message = "Code has been sent successfully, please check your device";
                helper.showToastMessage(message);
            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential)
    {
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
        {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                if(task.isSuccessful())
                {
                    // Sign in success, update UI with the signed-in user's information.
                    // Log.d(TAG, "signInWithCredential:success");

                    FirebaseUser user = task.getResult().getUser();
                    alterProgressbar(false);

                    message = "Congratulation, you are signed in successfully";
                    helper.showToastMessage(message);

                    sendUserToMainActivity();
                }
                else
                {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.getException());

                    if(task.getException() instanceof FirebaseAuthInvalidCredentialsException)
                    {
                        // The verification code entered was invalid.
                        alterProgressbar(false);

                        message = "Some error occurred while signing in";
                        helper.showToastMessage(message);
                    }
                }
            }
        });
    }

    private void sendUserToMainActivity()
    {
        Intent intent = new Intent(context, HomeActivity.class);
        startActivity(intent);

        finish();
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if(firebaseUser != null)
        {
            sendUserToMainActivity();
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }
}