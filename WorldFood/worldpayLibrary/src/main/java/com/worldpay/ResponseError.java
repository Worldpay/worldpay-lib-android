package com.worldpay;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Contains error response from the server.
 *
 * @author Sotiris Chatzianagnostou - sotcha@arx.net
 */
public class ResponseError implements Serializable {
    private static final long serialVersionUID = 6834532264115534663L;

    private String message;

    private int httpStatusCode;

    private String description;

    private String customCode;

    private String originalRequest;

    private String errorHelpUrl;

    public ResponseError() {
    }

    /**
     * You will find the list of all possible HTTP response codes. <br>
     * <br>
     * Values :
     * <ul>
     * <li><b>httpStatusCode</b> - The HTTP status code</li>
     * <li><b>customCode</b> - The context of the error</li>
     * <li><b>message</b> - A description of the error that occurred</li>
     * <li><b>description</b> - Support information with a description of what to do next</li>
     * <li><b>errorHelpUrl</b> - Link to website with more information about this error. This is an
     * optional field</li>
     * <li><b>originalRequest</b> - Original Request object that created this error</li>
     * </ul>
     *
     * @param jsonString
     * @throws JSONException
     */
    public void parseJsonString(String jsonString) throws JSONException {
        JSONObject mainObject = new JSONObject(jsonString);
        httpStatusCode = mainObject.optInt("httpStatusCode");
        customCode = mainObject.optString("customCode");
        message = mainObject.optString("message");
        description = mainObject.optString("description");
        errorHelpUrl = mainObject.optString("errorHelpUrl");
        originalRequest = mainObject.optString("originalRequest");
    }

    public String getMessage() {
        return message;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public String getDescription() {
        return description;
    }

    public String getCustomCode() {
        return customCode;
    }

    public String getOriginalRequest() {
        return originalRequest;
    }

    public String getErrorHelpUrl() {
        return errorHelpUrl;
    }

}
