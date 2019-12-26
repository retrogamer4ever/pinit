package com.joecompany.pinit.ui;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.joecompany.pinit.MainActivity;
import com.joecompany.pinit.R;
import com.joecompany.pinit.utils.IntentUtil;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content_controls);

        // https://stackoverflow.com/questions/38007025/manually-build-a-login-flow-for-facebook-on-android
        // https://stackoverflow.com/questions/38007025/manually-build-a-login-flow-for-facebook-on-android
    }

    @Override
    protected void onResume() {
        super.onResume();

        // the intent filter defined in AndroidManifest will handle the return from ACTION_VIEW intent
        Uri uri = getIntent().getData();
        if (uri != null) {
            // use the parameter your API exposes for the code (mostly it's "code")
            String code = uri.getQueryParameter("code");
            if (code != null) {
                Log.i("code", code);
                // get access token
                // we'll do that in a minute
            } else if (uri.getQueryParameter("error") != null) {
                // show an error message here
            }
        }
    }

    public void onLoginFacebookClick(View v)
    {
/*
        String clientId = "2566190067002357";
        String redirectUri = "http://pinit.today/test.php";
        String responseType = "code";

        Intent intent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.facebook.com/v5.0/dialog/oauth" +
                        "?client_id=" + clientId +
                        "&redirect_uri=" + redirectUri +
                        "&response_type=" + responseType));
        startActivity(intent);
*/



        IntentUtil.start(this, MainActivity.class);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }
}
