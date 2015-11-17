package com.worldpay;

import android.os.Build;

import org.apache.http.message.BasicHeader;

import java.io.IOException;

import static com.worldpay.Constants.API_URL_TOKENS;
import static com.worldpay.HttpClientUtility.HTTP_METHOD.POST;
import static com.worldpay.HttpClientUtility.HTTP_METHOD.PUT;
import static com.worldpay.HttpClientUtility.httpEntityRequest;

final class WorldPayHttp {

    private static final char SEMI_COLON = ';';
    private static WorldPayHttp instance;

    private String worldPayUserAgent;

    private WorldPayHttp() {

    }

    protected static WorldPayHttp getInstance() {
        if (instance == null) {
            instance = new WorldPayHttp();
        }
        return instance;
    }

    public HttpServerResponse createToken(final String data) throws IllegalStateException, IOException {
        return httpEntityRequest(POST, API_URL_TOKENS, data, getHeaders());
    }

    public HttpServerResponse reuseToken(final String token, final String data) throws IllegalStateException, IOException {
        return httpEntityRequest(PUT, API_URL_TOKENS + "/" + token, data, getHeaders());
    }

    private BasicHeader[] getHeaders() {
        return new BasicHeader[]{
                new BasicHeader("Content-type", "application/json"),
                getCustomUserAgent()
        };
    }

    /**
     * Create the WorldPay custom user agent HTTP header (X-wp-client-user-agent).
     * <p/>
     * Requires the following information:
     * <ol>
     * <li>os.name</li>
     * <li>os.version</li>
     * <li>os.arch</li>
     * <li>lang.version (jvm version, php version)</li>
     * <li>lib.version</li>
     * <li>api.version</li>
     * <li>lang (java, php, c# etc)</li>
     * <li>owner (would always be "world pay”)</li>
     * </ol>
     *
     * @return The 'X-wp-client-user-agent' header.
     */
    private BasicHeader getCustomUserAgent() {
        if (worldPayUserAgent == null) {
            final StringBuilder userAgent = new StringBuilder();
            String systemArch = System.getProperty("os.arch");
            if (systemArch == null) {
                systemArch = "Unknown arch";
            }
            userAgent.append("android;")
                    .append(Build.VERSION.SDK_INT).append(SEMI_COLON)
                    .append(systemArch).append(SEMI_COLON)
                    .append(Build.VERSION.RELEASE).append(SEMI_COLON)
                    .append(WorldPay.VERSION).append(SEMI_COLON)
                    .append(Constants.API_VERSION).append(SEMI_COLON)
                    .append("android").append(SEMI_COLON)
                    .append("worldpay").append(SEMI_COLON);

            worldPayUserAgent = userAgent.toString();
        }
        return new BasicHeader("X-wp-client-user-agent", worldPayUserAgent);
    }
}
