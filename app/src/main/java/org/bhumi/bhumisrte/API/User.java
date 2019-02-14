package org.bhumi.bhumisrte.API;

import android.content.Context;
import android.content.SharedPreferences;

/*
 * Class that handles the user aspect of the program
 */
public class User {
    private static User instance = new User();
    public static String SHARED_PREFS_NAME = "user";
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    /*
    Singleton implementation
     */
    public static User getCurrentUser(Context context) {
        sharedPreferences  = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        return instance;
    }

    /*
    Check if the user is logged in
    @return true if user is logged in
     */
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean("isLoggedIn", false);
    }

    /*
    Get the user email
    @return get the email of the logged in user
     */
    public String getEmail() {
        return sharedPreferences.getString("email", null);
    }

    /*
    Get the JWT Token
    @return JWT Token
     */
    public String getToken() {
        return sharedPreferences.getString("token", null);
    }

    /*
    perform user login
    @param email: user email
    @param token: JWT token
     */
    public void login(String email, String token) {
        editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("token", token );
        editor.putString("email",email);
        editor.commit();
    }

    /*
    Perform user login
     */
    public void logout() {
        editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.putString("token","");
        editor.putString("email","");
        editor.commit();
    }
}