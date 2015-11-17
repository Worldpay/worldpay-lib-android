package com.example.worldfood;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.worldfood.menu.MenuActivity;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
import static android.view.Window.FEATURE_NO_TITLE;
import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;

public final class SplashScreenActivity extends Activity {

    private final long millisecondsToShowSplash = 1500L;

    /**
     * Setup the splash screen layout and launch the {@link MenuActivity} after
     * {@link #millisecondsToShowSplash a delay}.
     *
     * @param savedInstanceState {@link Bundle} state.
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLayout();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashScreenActivity.this, MenuActivity.class));
            }
        }, millisecondsToShowSplash);
    }

    private void initLayout() {
        requestWindowFeature(FEATURE_NO_TITLE);
        getWindow().setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN);
        setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_splash_screen);
    }
}
