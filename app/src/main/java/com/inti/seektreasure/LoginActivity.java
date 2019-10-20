package com.inti.seektreasure;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import org.w3c.dom.Text;

public class LoginActivity extends AppCompatActivity
{
    //initialise the field
    private Button LoginButton;
    private ImageView googleSignInButton;
    private EditText UserEmail, UserPassword;
    private TextView NeedNewAccountLink, ForgetPasswordLink;
    private ProgressDialog loadingBar;


    //get the firebase auth
    private FirebaseAuth mAuth;

    //google signin
    private static final int RC_SIGN_IN = 1;
    private GoogleApiClient mGoogleSignInClient;
    private static final String TAG = "LoginActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        NeedNewAccountLink = (TextView) findViewById(R.id.register_account_link);
        UserEmail = (EditText) findViewById(R.id.login_email);
        UserPassword = (EditText) findViewById(R.id.login_password);
        LoginButton = (Button) findViewById(R.id.login_button);
        ForgetPasswordLink = (TextView) findViewById(R.id.forget_password_link);
        googleSignInButton = (ImageView) findViewById(R.id.google_signin_button);
        loadingBar = new ProgressDialog(this);




        //send user to register activity
        NeedNewAccountLink.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SendUserToRegisterActivity();

            }
        });

        ForgetPasswordLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(LoginActivity.this,ResetPasswordActivity.class));
            }
        });

        //check user exists or not and then login
        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                AllowingUserToLogin();

            }
        });


        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        //set google api
        mGoogleSignInClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
                    {
                        Toast.makeText(LoginActivity.this,"Connection to Google sign in failed.",Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                signIn();

            }
        });

    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleSignInClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN)
        {
            loadingBar.setTitle("Google Sign In");
            loadingBar.setMessage("Please wait , while we are login using your Google Account.");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data); //Intent data into here which is google intent

            if (result.isSuccess())
            {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account); // GoogleSignInAccount account variable
                Toast.makeText(this,"Please wait, while we are getting your auth result.",Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this, "Can't get Auth result.",Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            Log.d(TAG, "signInWithCredential:success");
                            //send user to main activity
                            SendUserToMainActivity(); //use validaiton of the main activity need user to provide setup information
                            loadingBar.dismiss();
                        }
                        else
                            {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            String message = task.getException().getMessage();
                            SendUserToLoginActivity();
                            Toast.makeText(LoginActivity.this,"Not Authenticated: " + message,Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                            }

                    }
                });
    }


    //validation for check the user is already login we will send user to the main activity no need to login and login


    @Override
    protected void onStart()
    {
        super.onStart();

        //current online user who will be using seek treasure
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //check user if already login then we are going to send user to the main activity
        if(currentUser != null)
        {

            SendUserToMainActivity();
            //then add validation on the register activity
        }
    }

    private void AllowingUserToLogin()
    {
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();

        //validation on above two field
        if (TextUtils.isEmpty(email))
        {
            Toast.makeText(this, "Please write your email.",Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "Please write your password.",Toast.LENGTH_SHORT).show();
        }
        else
        {

            loadingBar.setTitle("Login");
            loadingBar.setMessage("Please wait , while we are login into your account.");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            //once the loading bar appeared , if the user click on the screen it  will not disappeared until the error happened or authentic successfully.

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if(task.isSuccessful())
                            {
                                //send user to main activity
                                SendUserToMainActivity();

                                Toast.makeText(LoginActivity.this,"You are Login successfully.", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                            else
                            {
                                //get error message
                                String message = task.getException().getMessage();
                                Toast.makeText(LoginActivity.this,"Error occurred: "+ message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void SendUserToLoginActivity()
    {
        Intent mainIntent = new Intent(LoginActivity.this, LoginActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void SendUserToRegisterActivity()
    {
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
    }
}
