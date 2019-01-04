/*
 * A singleton class that supplies the url pointing to the RESTful api (Bhumi-Authenticator)
 */
package org.bhumi.bhumisrte.API;

import android.content.Context;

import org.bhumi.bhumisrte.R;

public class Endpoint {

    public static Endpoint instance = new Endpoint();
    private static Context context;

    /*
    * Static method that returns the fixed instance to all the callees
     */
    public static Endpoint getInstance(Context mContext) {
        context = mContext;
        return instance;
    }

    /*
    * Method that returns the url of the restful api
     */
    public String getEndpoint() {
        return context.getString(R.string.api_url);
    }
}
