package com.example.worldfood.order;

import com.worldpay.AlternativePaymentMethod;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import static com.worldpay.AlternativePaymentMethod.newPayPalApm;

/**
 * WorldPay Order.
 */
public class Order implements Serializable {

    private final String orderCode;
    private final String apmToken;
    private final String orderDescription;
    private final String paymentStatus;
    private final AlternativePaymentMethod alternativePaymentMethod;
    private final int amount;
    private final String currencyCode;
    private final String pendingUrl;
    private final String failureUrl;
    private final String cancelUrl;
    private final String redirectURL;
    private final String successUrl;
    private final String environment;

    private Order(final String orderCode, final String apmToken, final String orderDescription,
                  final String paymentStatus,
                  final AlternativePaymentMethod alternativePaymentMethod, final int amount,
                  final String currencyCode, final String pendingUrl, final String failureUrl,
                  final String cancelUrl, final String redirectURL, final String successUrl,
                  final String environment) {
        this.orderCode = orderCode;
        this.apmToken = apmToken;
        this.orderDescription = orderDescription;
        this.paymentStatus = paymentStatus;
        this.alternativePaymentMethod = alternativePaymentMethod;
        this.amount = amount;
        this.currencyCode = currencyCode;
        this.pendingUrl = pendingUrl;
        this.failureUrl = failureUrl;
        this.cancelUrl = cancelUrl;
        this.redirectURL = redirectURL;
        this.successUrl = successUrl;
        this.environment = environment;
    }

    /**
     * Creates a new buy with PayPal order.
     *
     * @param payPalApmToken   PayPal APM Token.
     * @param orderDescription Order description.
     * @param amount           Order total.
     * @param currencyCode     ISO 4217 Currency code.
     * @param pendingUrl       HTTPS address of pending payment page.
     * @param failureUrl       HTTPS address of failed payment page.
     * @param cancelUrl        HTTPS address of cancelled payment page.
     * @param redirectURL      HTTPS address of merchant PayPal site.
     * @param successUrl       HTTPS address of successful payment page.
     * @return A new PayPal {@link Order}.
     */
    public static Order newPayPalOrder(final String payPalApmToken, final String orderDescription,
                                       final int amount, final String currencyCode,
                                       final String pendingUrl, final String failureUrl,
                                       final String cancelUrl, final String redirectURL,
                                       final String successUrl) {
        return new Order(null, payPalApmToken, orderDescription, null, null, amount, currencyCode,
                pendingUrl, failureUrl, cancelUrl, redirectURL, successUrl, null);
    }

    static Order valueOf(final String json) throws JSONException {
        final JSONObject apmOrderJson = new JSONObject(json);
        final String orderCode = apmOrderJson.getString("orderCode");
        final String apmToken = apmOrderJson.getString("token");
        final String orderDescription = apmOrderJson.optString("orderDescription", null);
        final int amount = apmOrderJson.getInt("amount");
        final String currencyCode = apmOrderJson.optString("currencyCode", null);
        final String paymentStatus = apmOrderJson.optString("paymentStatus", null);

        final JSONObject paymentMethodJSONObject = apmOrderJson.getJSONObject("paymentResponse");
        final String name = paymentMethodJSONObject.optString("name", null);
        final String shopperCountryCode = paymentMethodJSONObject.optString("shopperCountryCode", null);

        final String pendingUrl = apmOrderJson.optString("pendingUrl", null);
        final String failureUrl = apmOrderJson.optString("failureUrl", null);
        final String cancelUrl = apmOrderJson.optString("cancelUrl", null);
        final String redirectURL = apmOrderJson.optString("redirectURL", null);
        final String successUrl = apmOrderJson.optString("successUrl", null);
        final String environment = apmOrderJson.optString("environment", null);

        return new Order(orderCode, apmToken, orderDescription, paymentStatus,
                newPayPalApm(name, shopperCountryCode), amount, currencyCode, pendingUrl,
                failureUrl, cancelUrl, redirectURL, successUrl, environment);
    }

    /**
     * Returns a {@link JSONObject} representation of {@code this} object.
     *
     * @return A {@link JSONObject}
     * @throws JSONException
     * @see {@link JSONObject}
     */
    JSONObject getAsJSONObject() throws JSONException {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("token", apmToken);
        jsonObject.put("orderDescription", orderDescription);
        jsonObject.put("amount", amount);
        jsonObject.put("currencyCode", currencyCode);
        jsonObject.put("pendingUrl", pendingUrl);
        jsonObject.put("failureUrl", failureUrl);
        jsonObject.put("cancelUrl", cancelUrl);
        jsonObject.put("redirectURL", redirectURL);
        jsonObject.put("successUrl", successUrl);

        return jsonObject;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderCode='" + orderCode + '\'' +
                ", apmToken='" + apmToken + '\'' +
                ", orderDescription='" + orderDescription + '\'' +
                ", paymentStatus='" + paymentStatus + '\'' +
                ", alternativePaymentMethod=" + alternativePaymentMethod +
                ", amount=" + amount +
                ", currencyCode='" + currencyCode + '\'' +
                ", pendingUrl='" + pendingUrl + '\'' +
                ", failureUrl='" + failureUrl + '\'' +
                ", cancelUrl='" + cancelUrl + '\'' +
                ", redirectURL='" + redirectURL + '\'' +
                ", successUrl='" + successUrl + '\'' +
                ", environment='" + environment + '\'' +
                '}';
    }

    public String getOrderCode() {
        return orderCode;
    }

    public String getApmToken() {
        return apmToken;
    }

    public String getOrderDescription() {
        return orderDescription;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public AlternativePaymentMethod getAlternativePaymentMethod() {
        return alternativePaymentMethod;
    }

    public int getAmount() {
        return amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public String getPendingUrl() {
        return pendingUrl;
    }

    public String getFailureUrl() {
        return failureUrl;
    }

    public String getCancelUrl() {
        return cancelUrl;
    }

    public String getRedirectURL() {
        return redirectURL;
    }

    public String getSuccessUrl() {
        return successUrl;
    }

    public String getEnvironment() {
        return environment;
    }
}
