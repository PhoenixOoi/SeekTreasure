package com.inti.seektreasure;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Size;
import android.view.View;

import gr.net.maroulis.library.EasySplashScreen;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EasySplashScreen config = new EasySplashScreen(SplashScreen.this)
                .withFullScreen()
                .withTargetActivity(MainActivity.class)
                .withSplashTimeOut(4000)
                .withBackgroundColor(Color.parseColor("#00574B"))
                .withHeaderText("Your Pocket Garage Sale")
                .withFooterText("© Seek Treasure")
                .withBeforeLogoText("Seek Treasure")
                .withAfterLogoText("Online Shopping and Bargain")
                .withLogo(R.drawable.splashlogo);

        config.getHeaderTextView().setTextColor(Color.WHITE);
        config.getFooterTextView().setTextColor(Color.WHITE);
        config.getBeforeLogoTextView().setTextColor(Color.WHITE);
        config.getAfterLogoTextView().setTextColor(Color.WHITE);
        config.getBeforeLogoTextView().setTextSize(40);
        config.getAfterLogoTextView().setTextSize(20);

        View easySplashScreen = config.create();
        setContentView(easySplashScreen);
    }
}