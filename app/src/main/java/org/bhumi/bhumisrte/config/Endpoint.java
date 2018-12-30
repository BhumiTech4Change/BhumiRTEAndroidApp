/*
 * A singleton class that supplies the endpoint of the RESTful api
 */
package org.bhumi.bhumisrte.config;

public class Endpoint {

    public static Endpoint instance = new Endpoint();
    private static String url = "http://13.233.214.238:3000";

    public static Endpoint getInstance() {
        return instance;
    }

    public String getEndpoint() {
        return url;
    }
}
