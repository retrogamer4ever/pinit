package com.joecompany.pinit.constants;

public class Facebook {

    // Facebook app doesn't allow you use an intent for the oauth redirect url, only a secure url, so setup a temp callback from personal website
    public static final String REDIRECT_URI = "https://myremindoapptesting.me/fblogin.html";

    public static final String CLIENT_ID = "2566190067002357";
    public static final String CLIENT_SECRET = "dd006b67404848ab9b24c461eed140e3";
    public static final String RESPONSE_TYPE = "code";
    public static final String AUTH_TYPE = "reauthenticate";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String FB_ID = "id";
}
