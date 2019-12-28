package com.joecompany.pinit;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.joecompany.pinit.constants.Facebook;
import com.joecompany.pinit.constants.LogTags;
import com.joecompany.pinit.constants.StorageKeys;
import com.joecompany.pinit.utils.FacebookUtil;
import com.joecompany.pinit.utils.IntentUtil;
import com.joecompany.pinit.utils.StorageUtil;
import com.linkedin.urls.Url;
import com.linkedin.urls.detection.UrlDetector;
import com.linkedin.urls.detection.UrlDetectorOptions;

import java.util.List;


public class LoginActivity extends AppCompatActivity {

    private Activity parentActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Need to set this just in case we do some inline handling and lose scope
        parentActivity = this;

        if(StorageUtil.get(this, StorageKeys.FB_ID, String.class) != null){
            IntentUtil.start(this, MainActivity.class);
            return;
        }

        setContentView(R.layout.activity_login);

        // When we return back to the app from facebook redirect we need to break up the url to get the fb code from the login process
        Bundle intentParams = getIntent().getExtras();
        if (intentParams != null){

            //TODO: This way currently works but is very hacky, need to find a better way to break up the url
            String fbcode = intentParams.getString("fbcode"); //dummy code should be eventually removed

            UrlDetector parser = new UrlDetector(intentParams.toString(), UrlDetectorOptions.ALLOW_SINGLE_LEVEL_DOMAIN.Default);

            List<Url> found = parser.detect();

            // TODO: should always be second returned value, but need to discover better way to handle this
            Url fbCallbackUrl = found.get(2);

            try{

                String urlFragment = fbCallbackUrl.getQuery();

                // TODO: the fb callback url should always have code value but can't depend on it forever, need to find better way
                String[] urlFragmentSplit = urlFragment.split("\\?code=");

                final String fbCode = urlFragmentSplit[1];

                // Since we are now doing some graph api networking calls need to do it on another thread besides the main ui thread
                // Getting the access token using the code from user login, then using token to get fb user id so we can keep track of users pins
                new Thread() {
                    public void run() {

                    String accessToken = FacebookUtil.getGraphAPIValue("https://graph.facebook.com/v5.0/oauth/access_token?client_id=" + Facebook.CLIENT_ID + "&redirect_uri=" + Facebook.REDIRECT_URI + "&client_secret=" + Facebook.CLIENT_SECRET + "&code=" + fbCode, Facebook.ACCESS_TOKEN);

                    String fbUserId = FacebookUtil.getGraphAPIValue("https://graph.facebook.com/me?fields=id&input_token=" + accessToken + "&access_token=" + accessToken, Facebook.FB_ID);

                    StorageUtil.set(parentActivity, StorageKeys.FB_ID, fbUserId);
                    StorageUtil.set(parentActivity, StorageKeys.FB_ACCESS_TOKEN, accessToken);

                    IntentUtil.start(parentActivity, MainActivity.class);

                    }
                }.start();

            }catch(Exception e){
                Log.i(LogTags.PINIT, e.getMessage() );
            }
        }
    }

    public void onLoginFacebookClick(View v)
    {
        // This intent will redirect the app to the web view so user can login and then redirect back to app
        Intent intent = new Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.facebook.com/v5.0/dialog/oauth" +
                    "?client_id=" + Facebook.CLIENT_ID +
                    "&redirect_uri=" + Facebook.REDIRECT_URI +
                    "&auth_type=" + Facebook.AUTH_TYPE +
                    "&response_type=" + Facebook.RESPONSE_TYPE));

        startActivity(intent);
    }
}
