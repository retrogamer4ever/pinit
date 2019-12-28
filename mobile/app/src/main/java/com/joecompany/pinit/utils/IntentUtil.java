package com.joecompany.pinit.utils;

import android.app.Activity;
import android.content.Intent;


public class IntentUtil {
    public static void start(Activity activity, Class activityClassToStart){
        Intent intent = new Intent(activity, activityClassToStart);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        activity.startActivity(intent);
        activity.finish();
    }
}
