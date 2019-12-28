package com.joecompany.pinit.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;


public class StorageUtil {

    public static void set(Activity activity, String key, Object value){
        SharedPreferences sharedPref = activity.getSharedPreferences(key, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, new Gson().toJson(value));
        editor.commit();
    }

    public static <T> Object get(Activity activity, String key, Class objClass){

        SharedPreferences sharedPref = activity.getSharedPreferences(key, Context.MODE_PRIVATE);
        String storedJson = sharedPref.getString(key, null);

        if(storedJson == null || storedJson.equals(null)){
            return null;
        }

        return new Gson().fromJson(storedJson, objClass);
    }
}
