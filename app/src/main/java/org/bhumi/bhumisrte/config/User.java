package org.bhumi.bhumisrte.config;

import android.content.Context;
import android.content.SharedPreferences;

public class User {
    private static User instance = new User();
    private static String SHARED_PREFS_NAME = "user";
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    public static User getCurrentUser(Context context) {
        sharedPreferences  = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        return instance;
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean("isLoggedIn", false);
    }

    public String getEmail() {
        return sharedPreferences.getString("email", null);
    }

    public String getToken() {
        return sharedPreferences.getString("token", null);
    }

    public void login(String email, String token) {
        editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("token", token );
        editor.putString("email",email);
        editor.commit();
    }

    public void logout() {
        editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.putString("token","");
        editor.putString("email","");
        editor.commit();
    }
}
