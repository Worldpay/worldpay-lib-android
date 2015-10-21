package com.worldpay;

import android.os.Build;

import org.apache.http.message.BasicHeader;

import java.io.IOException;

final class WorldPayHttp {
    //singleton instance
    private static WorldPayHttp instance;
    private String customUserAgentHeader;

    private WorldPayHttp() {

    }

    protected static WorldPayHttp getInstance() {
        if (instance == null) {
            instance = new WorldPayHttp();
        }
        return instance;
    }

    public HttpServerResponse executeRequest(String data) throws IllegalStateException, IOException {
        //		create http error codes

        BasicHeader[] createHeaders = createHeaders();
        return HttpClientUtility.httpEntityRequest(HttpClientUtility.HTTP_METHOD.POST, Constants.API_URL_TOKEN_REQUEST, data,
                createHeaders);

    }

    public HttpServerResponse executeReuseableRequest(String token, String data) throws IllegalStateException, IOException {
        //		create http error codes

        BasicHeader[] createHeaders = createHeaders();
        return HttpClientUtility.httpEntityRequest(HttpClientUtility.HTTP_METHOD.PUT, Constants.API_URL_TOKEN_REQUEST + "/" + token, data,
                createHeaders);

    }

    private String getCustomUserAgent() {
        //X-wp-client-user-agent with following properties
        //
        //    os.name
        //    os.version
        //    os.arch

        //    lib.version
        //    api.version
        //    lang (java, php, c# etc)
        //    owner (would always be "world pay”)
        if (customUserAgentHeader == null) {
            StringBuilder userAgent = new StringBuilder();

            String systemArch = System.getProperty("os.arch");
            if (systemArch == null) {
                systemArch = "Unknown arch";
            }

            userAgent.append("android;")//os name
                    .append(Build.VERSION.SDK_INT).append(';')// os version
                    .append(systemArch).append(';')//
                    .append(Build.VERSION.RELEASE).append(';')//    lang.version e.g jvm version, php version

                    .append(WorldPay.VERSION).append(';')//lib version
                    .append(Constants.API_VERSION).append(';')//api version
                    .append("java").append(';')//lang
                    .append("world pay").append(';');//owner


            customUserAgentHeader = userAgent.toString();
        }
        return customUserAgentHeader;
    }

    private BasicHeader[] createHeaders() {
        return new BasicHeader[]{//
                new BasicHeader("Content-type", "application/json"), new BasicHeader("X-wp-client-user-agent", getCustomUserAgent()),};
    }

}
