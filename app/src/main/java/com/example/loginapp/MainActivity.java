package com.example.loginapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity<accessTokenTracker> extends AppCompatActivity {
    private LoginButton loginButton;
    private TwitterLoginButton loginButton1;
    private CircleImageView circleImageView;
    private TextView txtName,txtMail,myProfile;
    private CallbackManager callbackManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Twitter.initialize(this);
        setContentView(R.layout.activity_main);
        loginButton = findViewById(R.id.login_button);
        loginButton1 = (TwitterLoginButton) findViewById(R.id.login_button1);
        circleImageView = findViewById(R.id.profile_pic);
        txtName = findViewById(R.id.profile_name);
        myProfile =findViewById(R.id.id_top);
        txtMail = findViewById(R.id.profile_email);
        callbackManager=CallbackManager.Factory.create();
        loginButton.setPermissions(Arrays.asList("email","public_profile"));
        checkLoginStatus();

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });
        loginButton1.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                // Do something with result, which provides a TwitterSession for making API calls
                TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();
                TwitterAuthToken authToken = session.getAuthToken();
                // String token = authToken.token;
                // String secret = authToken.secret;

                loginMethod(session);
            }

            @Override
            public void failure(TwitterException exception) {
                // Do something on failure
                Toast.makeText(getApplicationContext(),"Login failed",Toast.LENGTH_LONG).show();
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode,@Nullable Intent data) {
        if(resultCode==10){
            super.onActivityResult(requestCode, resultCode, data);

            // Pass the activity result to the login button.
            loginButton1.onActivityResult(requestCode, resultCode, data);
        }
        else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    public void loginMethod(TwitterSession twitterSession){
        String userName=twitterSession.getUserName();
        Intent intent= new Intent(MainActivity.this,HomeActivity.class);
        intent.putExtra("username",userName);
        startActivityForResult(intent,10);
    }
    AccessTokenTracker tokenTracker= new AccessTokenTracker() {
        @SuppressLint("ResourceAsColor")
        @Override
        protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken)
        {
            if(currentAccessToken==null){
                txtName.setText("");
                txtMail.setText("");
                myProfile.setText("Social Media Integration");
                circleImageView.setImageResource(0);
                txtName.setBackgroundColor(Color.TRANSPARENT);
                txtMail.setBackgroundColor(Color.TRANSPARENT);
                loginButton1.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this,"User Logged Out",Toast.LENGTH_LONG).show();
            }
            else{
                loginButton1.setVisibility(View.INVISIBLE);
               txtName.setBackgroundColor(Color.WHITE);
               txtMail.setBackgroundColor(Color.WHITE);
                loudUserProfile(currentAccessToken);
            }

        }
    };
    private void loudUserProfile(AccessToken newAccessToken){
        GraphRequest request=GraphRequest.newMeRequest(newAccessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response)
            {
                try {
                    myProfile.setText("MY PROFILE");
                    if (object.has("name")) {
                        txtName.setText("NAME: "+object.getString("name"));
                    }
                    if (object.has("email")) {
                        txtMail.setText("EMAIL: " + object.getString("email"));
                    }
                    if (object.has("id")) {
                        String image_url="https://graph.facebook.com/"+object.getString("id")+"/picture?type=normal";
                        RequestOptions requestOptions=new RequestOptions();
                        requestOptions.dontAnimate();
                        Glide.with(MainActivity.this).load(image_url).into(circleImageView);
                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        });
        Bundle parameters= new Bundle();
        parameters.putString("fields","name,email");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void checkLoginStatus(){
        if(AccessToken.getCurrentAccessToken()!=null)
        {
            loudUserProfile(AccessToken.getCurrentAccessToken());
        }
    }
}