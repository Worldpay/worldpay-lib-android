package com.worldpay;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Contains a reusable token object
 */
public class ReusableToken implements Serializable {

    private static final long serialVersionUID = -1L;

    private String clientKey;
    private String token;
    private String cvc;

    public ReusableToken(String clientKey, String token, String cvc) {
        this.clientKey = clientKey;
        this.token = token;
        this.cvc = cvc;
    }

    public ReusableToken() {
    }

    public ReusableToken setClientKey(String clientKey) {
        this.clientKey = clientKey;
        return this;
    }

    public ReusableToken setCvc(String cvc) {
        this.cvc = cvc;
        return this;
    }

    public String getToken() {
        return token;
    }

    public ReusableToken setToken(String token) {
        this.token = token;
        return this;
    }

    /**
     * Returns the data of the reusable token as a {@link JSONObject}
     *
     * @return A {@link JSONObject}
     * @throws JSONException
     * @see {@link JSONObject}
     */
    protected JSONObject getAsJSONObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("clientKey", clientKey);
        jsonObject.put("cvc", cvc);

        return jsonObject;
    }

    public boolean validateCVC() {
        return Card.validateCVC(cvc);
    }
}
