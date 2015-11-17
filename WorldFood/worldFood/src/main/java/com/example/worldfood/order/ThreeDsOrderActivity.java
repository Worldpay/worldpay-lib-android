package com.example.worldfood.order;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.worldfood.R;
import com.worldpay.HttpServerResponse;
import com.worldpay.ResponseError;
import com.worldpay.SaveCardActivity;
import com.worldpay.WorldPayError;

import org.apache.http.message.BasicHeader;
import org.apache.http.util.EncodingUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW;
import static com.example.worldfood.order.DeviceUtils.getIpAddress;
import static com.example.worldfood.order.DeviceUtils.getUserAgent;
import static com.example.worldfood.order.OrderDetailsActivity.EXTRA_ORDER_DETAIL;
import static com.example.worldfood.order.OrderDetailsActivity.EXTRA_THREE_DS_RESULT;
import static com.worldpay.HttpClientUtility.HTTP_METHOD.POST;
import static com.worldpay.HttpClientUtility.HTTP_METHOD.PUT;
import static com.worldpay.HttpClientUtility.httpEntityRequest;
import static com.worldpay.WorldPayError.ERROR_CREATING_REQUEST_JSON;
import static com.worldpay.WorldPayError.ERROR_NO_NETWORK;
import static com.worldpay.WorldPayError.ERROR_RESPONSE_CONNECTION;
import static com.worldpay.WorldPayError.ERROR_RESPONSE_MALFORMED_JSON;
import static com.worldpay.WorldPayError.ERROR_RESPONSE_UNKNOWN;

/**
 * {@link Activity} that informs the user both that their order has been successfully
 * placed and when it will be delivered.
 */
public class ThreeDsOrderActivity extends Activity {

    public static final String EXTRA_CARD_TOKEN = "card-token";

    private static final String TAG = "ThreeDsOrderActivity";
    private static final String ORDERS_URL = "https://api.worldpay.com/v1/orders";
    private static final String TERM_URL = "https://online.worldpay.com/3dsr/";
    private static final String SERVER_RESPONSE = "worldpay-scheme://response?";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        final Intent intent = getIntent();
        final Bundle extras = intent.getExtras();

        final SharedPreferences sp = getDefaultSharedPreferences(getBaseContext());
        final String serviceKey = sp.getString(getString(R.string.service_key), getString(R.string.service_key_value));

        if (extras != null) {
            final String token = extras.getString(EXTRA_CARD_TOKEN);
            final OrderDetails orderDetails = (OrderDetails) intent.getSerializableExtra(EXTRA_ORDER_DETAIL);
            if (token != null && orderDetails != null) {
                runOrder(token, orderDetails, serviceKey);
                return;
            }
        }
        finish();
    }

    /**
     * Simulate a 3DS order.
     *
     * @param cardToken    The card token
     * @param orderDetails {@link OrderDetails}
     * @param serviceKey   Your service key.
     */
    private void runOrder(final String cardToken, final OrderDetails orderDetails,
                          final String serviceKey) {
        final AsyncTask<Void, Void, HttpServerResponse> threeDsOrder =
                threeDsOrder(cardToken, orderDetails, serviceKey, new ThreeDsResponse() {
                    @Override
                    public void onSuccess(String response) {
                        threeDsRedirect(response, serviceKey);
                    }

                    @Override
                    public void onResponseError(ResponseError responseError) {
                        returnWithError(null, responseError);
                    }

                    @Override
                    public void onError(WorldPayError worldPayError) {
                        returnWithError(worldPayError, null);
                    }
                });

        if (threeDsOrder != null) {
            threeDsOrder.execute();
        }
    }

    /**
     * This method returns the 3-D Secure WorldPay response with the card issuer's redirect url.
     */
    private AsyncTask<Void, Void, HttpServerResponse> threeDsOrder(final String cardToken,
                                                                   final OrderDetails orderDetails,
                                                                   final String serverKey,
                                                                   final ThreeDsResponse threeDsResponse) {
        final WorldPayError worldPayError = new WorldPayError();
        if (!DeviceUtils.isNetworkConnected(ThreeDsOrderActivity.this)) {
            worldPayError.setError(ERROR_NO_NETWORK, getString(R.string.no_network));
            return null;
        }

        return new AsyncTask<Void, Void, HttpServerResponse>() {
            @Override
            protected HttpServerResponse doInBackground(final Void... params) {
                try {
                    return executeRequest(cardToken, orderDetails, serverKey);
                } catch (JSONException e) {
                    worldPayError.setError(ERROR_CREATING_REQUEST_JSON, e.getMessage());
                } catch (IllegalStateException | IOException e) {
                    worldPayError.setError(ERROR_RESPONSE_CONNECTION, e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(final HttpServerResponse serverResponse) {
                if (serverResponse == null) {
                    if (worldPayError.getCode() == 0) {
                        worldPayError.setError(ERROR_RESPONSE_UNKNOWN, getString(R.string.error_in_response));
                    }
                    threeDsResponse.onError(worldPayError);
                    return;
                }
                if (serverResponse.getStatusCode() == 200) {
                    threeDsResponse.onSuccess(serverResponse.getResponse());
                } else {
                    final ResponseError responseError = new ResponseError();
                    final String responseString = serverResponse.getResponse();

                    try {
                        if (responseString != null) {
                            responseError.parseJsonString(responseString);
                        } else {
                            responseError.setHttpStatusCode(serverResponse.getStatusCode());
                            returnWithError(worldPayError, null);
                        }
                        threeDsResponse.onResponseError(responseError);
                    } catch (JSONException e) {
                        worldPayError.setError(ERROR_RESPONSE_MALFORMED_JSON, getString(R.string.json_parse_fail));
                        threeDsResponse.onError(worldPayError);
                    }
                }
            }
        };
    }

    /**
     * Emulates a payment initiation request to WorldPay and returns the response.
     */
    private HttpServerResponse executeRequest(final String cardToken,
                                              final OrderDetails orderDetails,
                                              final String serviceKey)
            throws IllegalStateException, IOException, JSONException {

        final String order = createOrderJson(cardToken, orderDetails).toString();
        return httpEntityRequest(POST, ORDERS_URL, order, createHeaders(serviceKey));
    }

    /**
     * Create a 3-D Secure Order {@link JSONObject}.
     * <p/>
     * The shopper name should be '3D' for testing.
     *
     * @param cardToken    The card token
     * @param orderDetails {@link OrderDetails}
     * @return JSON representation of a 3-D Secure order
     * @throws JSONException
     */
    private JSONObject createOrderJson(final String cardToken, final OrderDetails orderDetails)
            throws JSONException {
        final JSONObject order = new JSONObject();
        order.put("token", cardToken);
        order.put("orderType", "ECOM");
        order.put("orderDescription", "Goods and Services");
        order.put("amount", orderDetails.getPrice());
        order.put("currencyCode", "GBP");
        order.put("name", "3D");
        order.put("is3DSOrder", true);
        order.put("shopperAcceptHeader", "acceptheader");
        order.put("shopperUserAgent", getUserAgent());
        order.put("shopperSessionId", "123");
        order.put("shopperIpAddress", getIpAddress(this));

        final JSONObject billingAddress = new JSONObject();
        billingAddress.put("address1", orderDetails.getAddress());
        billingAddress.put("postalCode", orderDetails.getPostCode());
        billingAddress.put("city", orderDetails.getCity());
        billingAddress.put("countryCode", "GB");
        order.put("billingAddress", billingAddress);

        return order;
    }

    /**
     * This sends another emulated order request (normally it would be from your server) to WorldPay
     * containing the 3DS fields provided in the 3DS authentication response in the card issuer's
     * page and returns the response.
     */
    private HttpServerResponse threeDSResultResponse(final String paRes, final String orderCode,
                                                     final String serviceKey)
            throws IllegalStateException, IOException, JSONException {
        final String orderUrl = ORDERS_URL + "/" + orderCode;

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("threeDSResponseCode", paRes);
        jsonObject.put("shopperAcceptHeader", "acceptheader");
        jsonObject.put("shopperUserAgent", getUserAgent());
        jsonObject.put("shopperSessionId", "123");
        jsonObject.put("shopperIpAddress", getIpAddress(this));

        return httpEntityRequest(PUT, orderUrl, jsonObject.toString(), createHeaders(serviceKey));
    }

    /**
     * This method loads the card's issuers 3DS authentication page
     * and handles the result of the the customer's actions,
     */
    private void threeDsRedirect(final String response, final String serverKey) {
        final WorldPayError worldPayError = new WorldPayError();
        try {
            final JSONObject jsonObject = new JSONObject(response);

            final String paymentStatus = jsonObject.optString("paymentStatus");
            final String redirectURL = jsonObject.optString("redirectURL");
            final String oneTime3DsToken = jsonObject.optString("oneTime3DsToken");
            final String is3DSOrder = jsonObject.optString("is3DSOrder");
            final String orderCode = jsonObject.optString("orderCode");

            Log.d(TAG, "paymentStatus:" + paymentStatus + " redirectURL=" + redirectURL
                    + " oneTime3DsToken=" + oneTime3DsToken + " is3DSOrder=" + is3DSOrder);
            // if the user is pre authorized we create a web view to the 3DS Simulation page
            if (paymentStatus.equals("PRE_AUTHORIZED")) {
                final WebView webView = (WebView) findViewById(R.id.webview);

                if (Build.VERSION.SDK_INT >= 21) {
                    webView.getSettings().setMixedContentMode(MIXED_CONTENT_ALWAYS_ALLOW);
                }
                //the webView's post data for the 3DS redirection
                String postData = "PaReq=" + oneTime3DsToken + "&TermUrl=" + TERM_URL + "&MD=thisisMD";

                webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
                        Log.d(TAG, "shouldOverrideUrlLoading:" + url);
                        //check whether the URL loaded is the TermUrl we provided for redirection after the 3DS authentication
                        if (url.startsWith(SERVER_RESPONSE)) {
                            // Get the parameters
                            String parametersStr = url.substring(SERVER_RESPONSE.length(), url.length());
                            String[] split = parametersStr.split("&");
                            String threeDsResult = "";

                            if (split != null && split.length > 0) {
                                for (String value : split) {
                                    String[] keyValue = value.split("=");
                                    //get the value of the PaRes field which contains the threeDSResponseCode from the card issuer's 3DS authentication page
                                    if (keyValue[0].equals("PaRes") && keyValue.length == 2) {
                                        Log.d(TAG, keyValue[0] + "=" + keyValue[1]);
                                        threeDsResult = keyValue[1];
                                    }
                                }
                                if (!threeDsResult.equals(""))
                                    result(threeDsResult, orderCode, serverKey);
                                else {
                                    worldPayError.setError(ERROR_RESPONSE_UNKNOWN, "Received unknown response!");
                                    Log.d(TAG, worldPayError.getMessage());
                                    returnWithError(worldPayError, null);
                                }
                            }
                        }
                        return true;
                    }

                    @Override
                    public void onLoadResource(WebView view, String url) {
                        Log.d(TAG, "onLoadResource:" + url);
                        if (url.startsWith("worldpay-scheme://")) {
                            Log.d(TAG, "WebView:" + "  post: " + url);
                            return;
                        }
                        super.onLoadResource(view, url);
                    }
                });

                Log.d(TAG, "WebView : " + redirectURL + "  post: " + postData);
                //load the card issuer's redirect page and provide the Card Token data
                webView.postUrl(redirectURL, EncodingUtils.getBytes(postData, "BASE64"));
            }

        } catch (JSONException e) {
            worldPayError.setError(ERROR_RESPONSE_MALFORMED_JSON, getString(R.string.json_parse_fail));
            returnWithError(worldPayError, null);
        }
    }


    /**
     * Send another order request back to Worldpay containing the response of the 3DS authentication and then
     * send the final order status received by Worldpay  back to the {@link OrderDetailsActivity}
     */
    private void result(final String paRes, final String orderCode, final String serverKey) {
        new AsyncTask<Void, Void, HttpServerResponse>() {
            final WorldPayError worldPayError = new WorldPayError();

            @Override
            protected HttpServerResponse doInBackground(final Void... params) {
                try {
                    return threeDSResultResponse(paRes, orderCode, serverKey);
                } catch (JSONException e) {
                    Log.d(TAG, "Error: ", e);
                    worldPayError.setError(ERROR_CREATING_REQUEST_JSON,
                            "Error while trying to create the request :" + e.getMessage());
                } catch (IllegalStateException | IOException e) {
                    Log.d(TAG, "Error: ", e);
                    worldPayError.setError(ERROR_RESPONSE_CONNECTION, "Connection error : " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(HttpServerResponse serverResponse) {
                if (serverResponse == null) {
                    if (worldPayError.getCode() == 0) {
                        worldPayError.setError(ERROR_RESPONSE_UNKNOWN, "Error while trying to get response.");
                        returnWithError(worldPayError, null);
                    }
                    return;
                }
                if (serverResponse.getStatusCode() == 200) {
                    try {
                        final JSONObject jsonObject = new JSONObject(serverResponse.getResponse());
                        final String paymentStatus = jsonObject.optString("paymentStatus");
                        Log.d(TAG, "paymentStatus:" + paymentStatus);

                        final Intent intent = new Intent();
                        intent.putExtra(EXTRA_THREE_DS_RESULT, paymentStatus);
                        Log.d(TAG, "Response :" + paymentStatus);
                        setResult(RESULT_OK, intent);
                        finish();
                    } catch (JSONException e) {
                        Log.d(TAG, "Error: ", e);
                        worldPayError.setError(ERROR_RESPONSE_MALFORMED_JSON, "Json parsing failed.");
                        returnWithError(worldPayError, null);
                    }
                } else {
                    final ResponseError responseError = new ResponseError();
                    final String responseString = serverResponse.getResponse();

                    try {
                        if (responseString != null) {
                            responseError.parseJsonString(responseString);
                        } else {
                            responseError.setHttpStatusCode(serverResponse.getStatusCode());
                        }
                        returnWithError(null, responseError);
                    } catch (JSONException e) {
                        worldPayError.setError(ERROR_RESPONSE_MALFORMED_JSON, "Json parsing failed.");
                        returnWithError(worldPayError, null);
                    }
                }
            }
        }.execute();
    }

    /**
     * Return the error result back to the {@link OrderDetailsActivity}
     *
     * @param worldPayError {@link WorldPayError}
     * @param responseError {@link ResponseError}
     */
    private void returnWithError(final WorldPayError worldPayError, final ResponseError responseError) {
        Intent intent = new Intent();
        if (worldPayError != null) {
            intent.putExtra(SaveCardActivity.EXTRA_RESPONSE_WORLDPAY_ERROR, worldPayError.getMessage());
            setResult(SaveCardActivity.RESULT_WORLDPAY_ERROR, intent);
            finish();
        }
        if (responseError != null) {
            intent.putExtra(SaveCardActivity.EXTRA_RESPONSE_ERROR, responseError);
            setResult(SaveCardActivity.RESULT_RESPONSE_ERROR, intent);
            finish();
        }
    }

    private BasicHeader[] createHeaders(final String serviceKey) {
        final BasicHeader[] headers = new BasicHeader[2];
        headers[0] = new BasicHeader("Content-type", "application/json");
        headers[1] = new BasicHeader("Authorization", serviceKey);
        return headers;
    }
}
