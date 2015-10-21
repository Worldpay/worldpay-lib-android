package com.example.worldfood;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.worldpay.HttpClientUtility;
import com.worldpay.HttpServerResponse;
import com.worldpay.ResponseError;
import com.worldpay.SaveCardActivity;
import com.worldpay.WorldPayError;

import org.apache.http.message.BasicHeader;
import org.apache.http.util.EncodingUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class OrderActivity extends Activity {

    // we use that file to get the Worldpay card token from DetailsActivity
    public final static String EXTRA_CARD_TOKEN = "card-token";

    // we use that file to log the order errors
    private static final String ORDER_ERROR = "orderError";


    //URL you want the shopper to be redirected to after 3D Secure authentication"
    private final static String TERM_URL = "http://ios.worldpay.io/rsp.php";

    //the location of the redirection provided by the TermUrl in the 3DS authentication response
    private final static String SERVER_RESPONSE = "worldpay-scheme://response?";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        setContentView(R.layout.activity_order);

        //your server key
        String serverKey = "T_S_1a54d3d9-0c81-4480-9483-1a3ff8b80d95";

        if (extras != null) {
            String token = extras.getString(EXTRA_CARD_TOKEN);
            OrderDetails orderDetails = (OrderDetails) intent.getSerializableExtra(DetailsActivity.ORDER_DETAIL);
            if (token != null && orderDetails != null) {

                Log.d(EXTRA_CARD_TOKEN, "--> got token: " + token);
                runOrder(token, orderDetails, serverKey);
                return;
            }
        }
        finish();
    }


    /**
     * Simulate a 3DS order
     *
     * @param cardToken
     * @param orderDetails
     * @param serverKey
     */
    private void runOrder(final String cardToken, final OrderDetails orderDetails, final String serverKey) {
        AsyncTask<Void, Void, HttpServerResponse> threeDsOrder = threeDsOrder(cardToken, orderDetails, serverKey, new ThreeDsResponse() {
            @Override
            public void onSuccess(String response) {
                //load the 3DS authentication page
                threeDsRedirect(response, serverKey);
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
     * This method returns the 3DS Worldpay response containing the card issuer's redirect url
     */
    private AsyncTask<Void, Void, HttpServerResponse> threeDsOrder(final String cardToken, final OrderDetails orderDetails, final String serverKey, final ThreeDsResponse threeDsResponse) {
        final WorldPayError worldPayError = new WorldPayError();

        if (!isNetworkConnected(OrderActivity.this)) {
            worldPayError.setError(WorldPayError.ERROR_NO_NETWORK, "There is no network connectivity");
            return null;
        }

        return new AsyncTask<Void, Void, HttpServerResponse>() {

            @Override
            protected HttpServerResponse doInBackground(Void... params) {

                try {
                    return executeRequest(cardToken, orderDetails, serverKey);
                } catch (JSONException e) {
                    Log.d(ORDER_ERROR, "Error: ", e);
                    worldPayError.setError(WorldPayError.ERROR_CREATING_REQUEST_JSON,
                            "Error while trying to create the request :" + e.getMessage());
                } catch (IllegalStateException | IOException e) {
                    Log.d(ORDER_ERROR, "Error: ", e);
                    worldPayError.setError(WorldPayError.ERROR_RESPONSE_CONNECTION, "Connection error : " + e.getMessage());
                }


                return null;
            }

            @Override
            protected void onPostExecute(HttpServerResponse serverResponse) {
                if (serverResponse == null) {
                    if (worldPayError.getCode() == 0) {
                        worldPayError.setError(WorldPayError.ERROR_RESPONSE_UNKNOWN, "Error while trying to get response.");
                    }
                    threeDsResponse.onError(worldPayError);
                    return;
                }
                if (serverResponse.getStatusCode() == 200) {
                    //success
                    threeDsResponse.onSuccess(serverResponse.getResponse());
                } else {
                    //response code is not 200
                    ResponseError responseError = new ResponseError();

                    String responseString = serverResponse.getResponse();

                    try {
                        if (responseString != null) {
                            responseError.parseJsonString(responseString);
                        } else {
                            //we add only the http code
                            responseError.setHttpStatusCode(serverResponse.getStatusCode());
                            returnWithError(worldPayError, null);
                        }

                        threeDsResponse.onResponseError(responseError);
                    } catch (JSONException e) {
                        Log.d(ORDER_ERROR, "Error: ", e);
                        worldPayError.setError(WorldPayError.ERROR_RESPONSE_MALFORMED_JSON, "Json parsing failed.");
                        threeDsResponse.onError(worldPayError);
                    }
                }


            }


        };
    }


    /**
     * This method creates an emulated payment initiation request (normally it would be from your server) to Wolrdpay and returns the response
     */
    private HttpServerResponse executeRequest(String cardToken, OrderDetails orderDetails, String serverKey) throws IllegalStateException, IOException, JSONException {

        String orderUrl = "https://api.worldpay.com/v1/orders";

        BasicHeader[] headers = new BasicHeader[2];
        headers[0] = new BasicHeader("Content-type", "application/json");
        headers[1] = new BasicHeader("Authorization", serverKey);

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("token", cardToken);

        jsonObject.put("orderType", "ECOM");
        jsonObject.put("orderDescription", "Goods and Services");
        jsonObject.put("amount", orderDetails.getPrice());
        jsonObject.put("currencyCode", "GBP");
        //SHOPPER NAME should be 3D for testing
        jsonObject.put("name", "3D");

        jsonObject.put("is3DSOrder", true);
        jsonObject.put("shopperAcceptHeader", "acceptheader");
        jsonObject.put("shopperUserAgent", "user agent 1");
        jsonObject.put("shopperSessionId", "123");
        jsonObject.put("shopperIpAddress", "127.0.0.1");

        JSONObject billingAddress = new JSONObject();
        billingAddress.put("address1", orderDetails.getAddress());
        billingAddress.put("postalCode", orderDetails.getPostCode());
        billingAddress.put("city", orderDetails.getCity());
        billingAddress.put("countryCode", "GB");

        JSONObject customerIdentifiers = new JSONObject();
        customerIdentifiers.put("email", "jimmy.now@gmail.com");


        jsonObject.put("billingAddress", billingAddress);
        jsonObject.put("customerIdentifiers", customerIdentifiers);
        Log.d(EXTRA_CARD_TOKEN, "--> json : " + jsonObject.toString());


        return HttpClientUtility.httpEntityRequest(HttpClientUtility.HTTP_METHOD.POST, orderUrl, jsonObject.toString(),
                headers);


    }


    /**
     * This sends another emulated order request (normally it would be from your server) to Worldpay containing the 3DS fields
     * provided in the 3DS authentication response in the card issuer's page and returns the response
     */
    private HttpServerResponse threeDSResultResponse(String paRes, String orderCode, String serverKey) throws IllegalStateException, IOException, JSONException {

        String orderUrl = "https://api.worldpay.com/v1/orders/" + orderCode;

        BasicHeader[] headers = new BasicHeader[2];
        headers[0] = new BasicHeader("Content-type", "application/json");
        headers[1] = new BasicHeader("Authorization", serverKey);

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("threeDSResponseCode", paRes);
        jsonObject.put("shopperAcceptHeader", "acceptheader");
        jsonObject.put("shopperUserAgent", "user agent 1");
        jsonObject.put("shopperSessionId", "123");
        jsonObject.put("shopperIpAddress", "127.0.0.1");


        return HttpClientUtility.httpEntityRequest(HttpClientUtility.HTTP_METHOD.PUT, orderUrl, jsonObject.toString(),
                headers);

    }




    /**
     * This method loads the card's issuers 3DS authentication page
     * and handles the result of the the customer's actions
     */
    private void threeDsRedirect(String response, final String serverKey) {
        final WorldPayError worldPayError = new WorldPayError();
        try {
            JSONObject jsonObject = new JSONObject(response);

            String paymentStatus = jsonObject.optString("paymentStatus");
            String redirectURL = jsonObject.optString("redirectURL");
            final String oneTime3DsToken = jsonObject.optString("oneTime3DsToken");
            String is3DSOrder = jsonObject.optString("is3DSOrder");


            final String orderCode = jsonObject.optString("orderCode");


            Log.d(EXTRA_CARD_TOKEN, "paymentStatus:" + paymentStatus + " redirectURL=" + redirectURL
                    + " oneTime3DsToken=" + oneTime3DsToken + " is3DSOrder=" + is3DSOrder);
            // if the user is pre authorized we create a web view to the 3DS Simulation page
            if (paymentStatus.equals("PRE_AUTHORIZED")) {
                //Load the webview
                //instantiate the webview
                CustomWebview webView = (CustomWebview) findViewById(R.id.webview);

                WebSettings settings= webView.getSettings();
                if (Build.VERSION.SDK_INT >= 21) {
                    webView.getSettings().setMixedContentMode( WebSettings.MIXED_CONTENT_ALWAYS_ALLOW );
                }

                //the webView's post data for the 3DS redirection
                String postData = "PaReq=" + oneTime3DsToken + "&TermUrl=" + TERM_URL + "&MD=thisisMD";

                webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        Log.d(EXTRA_CARD_TOKEN, "shouldOverrideUrlLoading:" + url);
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
                                        Log.d(EXTRA_CARD_TOKEN, keyValue[0] + "=" + keyValue[1]);
                                        threeDsResult = keyValue[1];
                                    }

                                }

                                if (!threeDsResult.equals(""))
                                    //handle the threeDSResponseCode
                                    result(threeDsResult, orderCode, serverKey);
                                else {
                                    worldPayError.setError(WorldPayError.ERROR_RESPONSE_UNKNOWN, "Received unknown response!");
                                    Log.d(ORDER_ERROR, worldPayError.getMessage());
                                    returnWithError(worldPayError, null);

                                }


                            }
                        }

                        return true;
                    }

                    @Override
                    public void onLoadResource(WebView view, String url) {
                        Log.d(EXTRA_CARD_TOKEN, "onLoadResource:" + url);
                        if (url.startsWith("worldpay-scheme://")) {
                            //do nothing
                            Log.d(EXTRA_CARD_TOKEN, "WebView : :" + "  post: " + url);

                            return;
                        }
                        super.onLoadResource(view, url);
                    }

                });

                Log.d(EXTRA_CARD_TOKEN, "WebView : :" + redirectURL + "  post: " + postData);
                //load the card issuer's redirect page and provide the  Card Token data
                webView.postUrl(redirectURL, EncodingUtils.getBytes(postData, "BASE64"));
            }

        } catch (JSONException e) {
            Log.d(ORDER_ERROR, "Error: ", e);
            worldPayError.setError(WorldPayError.ERROR_RESPONSE_MALFORMED_JSON, "Json parsing failed.");
            returnWithError(worldPayError, null);
        }


    }



    /**
     * Send another order request back to Worldpay containing the response of the 3DS authentication and then
     * send the final order status received by Worldpay  back to the {@link DetailsActivity}
     */
    private void result(final String paRes, final String orderCode, final String serverKey) {
        new AsyncTask<Void, Void, HttpServerResponse>() {
            WorldPayError worldPayError = new WorldPayError();

            @Override
            protected HttpServerResponse doInBackground(Void... params) {

                try {
                    return threeDSResultResponse(paRes, orderCode, serverKey);
                } catch (JSONException e) {
                    Log.d(ORDER_ERROR, "Error: ", e);
                    worldPayError.setError(WorldPayError.ERROR_CREATING_REQUEST_JSON,
                            "Error while trying to create the request :" + e.getMessage());
                } catch (IllegalStateException | IOException e) {
                    Log.d(ORDER_ERROR, "Error: ", e);
                    worldPayError.setError(WorldPayError.ERROR_RESPONSE_CONNECTION, "Connection error : " + e.getMessage());
                }

                return null;

            }

            @Override
            protected void onPostExecute(HttpServerResponse serverResponse) {
                if (serverResponse == null) {
                    if (worldPayError.getCode() == 0) {
                        worldPayError.setError(WorldPayError.ERROR_RESPONSE_UNKNOWN, "Error while trying to get response.");
                        returnWithError(worldPayError, null);
                    }
                    return;
                }
                if (serverResponse.getStatusCode() == 200) {
                    //success
                    try {
                        JSONObject jsonObject = new JSONObject(serverResponse.getResponse());
                        String paymentStatus = jsonObject.optString("paymentStatus");
                        Log.d(EXTRA_CARD_TOKEN, "paymentStatus:" + paymentStatus);

                        Intent intent = new Intent();
                        intent.putExtra(DetailsActivity.THREE_DS_RESULT, paymentStatus);
                        Log.d("Result Response", "Response :" + paymentStatus);
                        setResult(RESULT_OK, intent);
                        finish();

                    } catch (JSONException e) {
                        Log.d(ORDER_ERROR, "Error: ", e);
                        worldPayError.setError(WorldPayError.ERROR_RESPONSE_MALFORMED_JSON, "Json parsing failed.");
                        returnWithError(worldPayError, null);
                    }

                } else {
                    //response code is not 200
                    ResponseError responseError = new ResponseError();

                    String responseString = serverResponse.getResponse();

                    try {
                        if (responseString != null) {
                            responseError.parseJsonString(responseString);
                        } else {
                            //we add only the http code
                            responseError.setHttpStatusCode(serverResponse.getStatusCode());
                        }
                        Log.d(ORDER_ERROR, "Error: " + responseError.toString());
                        returnWithError(null, responseError);

                    } catch (JSONException e) {
                        Log.d(ORDER_ERROR, "Error: ", e);
                        worldPayError.setError(WorldPayError.ERROR_RESPONSE_MALFORMED_JSON, "Json parsing failed.");
                        returnWithError(worldPayError, null);
                    }
                }


            }

        }.execute();

    }



    /**
     * checks if the device is connected to a network
     */
    private boolean isNetworkConnected(Context context) {
        //check for connectivity permission
        PackageManager pm = context.getPackageManager();
        int hasPerm = pm.checkPermission(android.Manifest.permission.ACCESS_NETWORK_STATE, context.getPackageName());
        if (hasPerm != PackageManager.PERMISSION_GRANTED) {
            //we do not know so return true

            return true;
        }

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }


    /**
     * Return the error result back to the {@link DetailsActivity}
     *
     * @param worldPayError
     * @param responseError
     */
    private void returnWithError(WorldPayError worldPayError, ResponseError responseError) {
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

}
