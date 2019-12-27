package com.joecompany.pinit.ui;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.util.IOUtils;
import com.google.gson.Gson;
import com.joecompany.pinit.MainActivity;
import com.joecompany.pinit.R;
import com.joecompany.pinit.utils.IntentUtil;
import com.joecompany.pinit.utils.StorageUtil;
import com.linkedin.urls.Url;
import com.linkedin.urls.detection.UrlDetector;
import com.linkedin.urls.detection.UrlDetectorOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;

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

    private String userId;
    private Activity parentActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        parentActivity = this;

        if(StorageUtil.get(this, "fbid", String.class) != null){
            String moo = (String)StorageUtil.get(this, "fbid", String.class);
            IntentUtil.start(this, MainActivity.class);
            return;
        }

        setContentView(R.layout.activity_fullscreen);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content_controls);

        Bundle parametros = getIntent().getExtras();
        if (parametros != null){
            String fbcode = parametros.getString("fbcode");
            String moo = parametros.toString();
            UrlDetector parser = new UrlDetector(parametros.toString(), UrlDetectorOptions.ALLOW_SINGLE_LEVEL_DOMAIN.Default);
            List<Url> found = parser.detect();

            Url fbCallbackUrl = found.get(2);
            Map<String, String> query;
            try{
                String urlFragment = fbCallbackUrl.getQuery();
                String[] urlFragmentSplit = urlFragment.split("\\?code=");
                final String fbCode = urlFragmentSplit[1];
                //https://www.techiedelight.com/read-contents-of-url-into-string-java/
                new Thread() {
                    public void run() {
                        try{
                        String clientId = "2566190067002357";
                        String clientSecret = "dd006b67404848ab9b24c461eed140e3";
                        String redirectUri =   "https://myremindoapptesting.me/fblogin.html";
                        URL url = new URL("https://graph.facebook.com/v5.0/oauth/access_token?client_id=" + clientId + "&redirect_uri=" + redirectUri + "&client_secret=" + clientSecret + "&code=" + fbCode);

                        // open the url stream, wrap it an a few "readers"
                        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                        StringBuilder response = new StringBuilder();
                        String inputLine;

                        while ((inputLine = reader.readLine()) != null)
                            response.append(inputLine);

                        String rawJson = response.toString();

                        reader.close();

                        Map<String, String> json = new Gson().fromJson(rawJson, Map.class);

                        String accessToken = json.get("access_token");



                        // NOW WE NEED TO GET USER ID

                        URL url2 = new URL("https://graph.facebook.com/debug_token?input_token=" + accessToken + "&access_token=" + accessToken);

                        // open the url stream, wrap it an a few "readers"
                        BufferedReader reader2 = new BufferedReader(new InputStreamReader(url2.openStream()));
                        StringBuilder response2 = new StringBuilder();
                        String inputLine2;

                        while ((inputLine2 = reader2.readLine()) != null)
                            response2.append(inputLine2);

                        String rawJson2 = response2.toString();

                        reader2.close();

                        Map<String, Map<String, String>> json2 = new Gson().fromJson(rawJson2, Map.class);

                        userId = json2.get("data").get("user_id");

                        StorageUtil.set(parentActivity, "fbid", userId);

                        IntentUtil.start(parentActivity, MainActivity.class);

                        }catch(Exception e){
                            Log.i("joe:",e.getMessage() );
                        }
                    }
                }.start();


            }catch(Exception e){
                Log.i("joe:",e.getMessage() );
            }



            Log.i("joe:","" );
        }else{
            //no extras, get over it!!
        }

        // https://stackoverflow.com/questions/38007025/manually-build-a-login-flow-for-facebook-on-android
        // https://stackoverflow.com/questions/38007025/manually-build-a-login-flow-for-facebook-on-android
    }


    @Override
    protected void onResume() {
        super.onResume();


    }

    public void onLoginFacebookClick(View v)
    {

        String clientId = "2566190067002357";
        String redirectUri = "https://myremindoapptesting.me/fblogin.html";
        String responseType = "code";
        String authType = "reauthenticate";

        Intent intent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.facebook.com/v5.0/dialog/oauth" +
                        "?client_id=" + clientId +
                        "&redirect_uri=" + redirectUri +
                        "&auth_type=" + authType +
                        "&response_type=" + responseType));
        startActivity(intent);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }
}
