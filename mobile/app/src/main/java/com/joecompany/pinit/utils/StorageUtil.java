package com.joecompany.pinit.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

public class StorageUtil {

    public static void set(Activity activity, String key, Object value){
        Gson g = new Gson();
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, g.toJson(value));
        editor.commit();
    }

    public static void delete(Activity activity, String key){
        StorageUtil.set(activity, key, null);
    }

    public static <T> Object get(Activity activity, String key, Class objClass){

        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        String storedJson = sharedPref.getString(key, null);

        if(storedJson == null || storedJson.equals(null)){
            return null;
        }

        Gson g = new Gson();
        return g.fromJson(storedJson, objClass);
    }
}
