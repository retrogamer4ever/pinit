package com.joecompany.pinit.utils;

import android.util.Log;

import com.google.gson.Gson;
import com.joecompany.pinit.constants.LogTags;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;


public class FacebookUtil {

    public static String getGraphAPIValue(String uri, String valueName){

        try{
            URL url = new URL(uri);

            HttpURLConnection myURLConnection = (HttpURLConnection)url.openConnection();

            BufferedReader reader = new BufferedReader(new InputStreamReader(myURLConnection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = reader.readLine()) != null)
                response.append(inputLine);

            String rawJson = response.toString();

            reader.close();

            Map<String, String> json = new Gson().fromJson(rawJson, Map.class);

            return json.get(valueName);

        }catch(Exception e){
            Log.i(LogTags.PINIT, e.getMessage() );
        }

        return "";
    }
}
