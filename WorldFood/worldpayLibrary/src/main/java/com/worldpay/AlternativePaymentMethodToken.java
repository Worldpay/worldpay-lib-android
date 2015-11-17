package com.worldpay;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * An {@link AlternativePaymentMethod} token.
 */
public class AlternativePaymentMethodToken implements Serializable {

    private final String token;
    private final boolean reusable;
    private final AlternativePaymentMethod alternativePaymentMethod;

    private AlternativePaymentMethodToken(final String token, final boolean reusable,
                                          final AlternativePaymentMethod alternativePaymentMethod) {
        this.reusable = reusable;
        this.alternativePaymentMethod = alternativePaymentMethod;
        this.token = token;
    }

    /**
     * Creates an {@link AlternativePaymentMethodToken} from an APM token JSON string.
     * <p>
     * Example JSON:
     * <pre>
     *         {
     *             "token": "TOKEN_ID",
     *             "reusable": true/false,
     *             "paymentMethod": {
     *                 "name": "First Last",
     *                 "apmName": "paypal",
     *                 "shopperCountryCode": "GB"
     *             }
     *         }
     *     </pre>
     * </p>
     *
     * @param json String represention of an APM JSON token.
     * @return New {@link AlternativePaymentMethodToken}.
     * @throws JSONException
     */
    static AlternativePaymentMethodToken valueOf(final String json) throws JSONException {
        final JSONObject apmTokenJson = new JSONObject(json);
        final String token = apmTokenJson.getString("token");
        final boolean reusable = apmTokenJson.optBoolean("reusable", false);

        final JSONObject paymentMethodJSONObject = apmTokenJson.getJSONObject("paymentMethod");
        final String name = paymentMethodJSONObject.optString("name", null);
        final String apmName = paymentMethodJSONObject.optString("apmName", null);
        final String shopperCountryCode = paymentMethodJSONObject.optString("shopperCountryCode", null);

        return new AlternativePaymentMethodToken(token, reusable, AlternativePaymentMethod.newApm(name, apmName, shopperCountryCode));
    }

    public String getToken() {
        return token;
    }

    public boolean isReusable() {
        return reusable;
    }

    public AlternativePaymentMethod getAlternativePaymentMethod() {
        return alternativePaymentMethod;
    }

    @Override
    public String toString() {
        return "AlternativePaymentMethodToken{" +
                "token='" + token + '\'' +
                ", reusable=" + reusable +
                ", alternativePaymentMethod=" + alternativePaymentMethod +
                '}';
    }
}
