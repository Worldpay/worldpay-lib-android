package com.example.worldfood.order;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.worldfood.R;
import com.worldpay.HttpServerResponse;
import com.worldpay.ResponseError;
import com.worldpay.SaveCardActivity;
import com.worldpay.WorldPayError;

import org.apache.http.message.BasicHeader;
import org.json.JSONException;

import java.io.IOException;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW;
import static com.example.worldfood.order.OrderDetailsActivity.EXTRA_PAYPAL_RESULT;
import static com.worldpay.HttpClientUtility.HTTP_METHOD.POST;
import static com.worldpay.HttpClientUtility.httpEntityRequest;

/**
 * {@link Activity} that allows a user to place an order via PayPal.
 */
public class PayPalOrderActivity extends Activity {

    public static final String EXTRA_APM_ORDER = "apm-order";

    private static final String PRE_AUTHORIZED = "PRE_AUTHORIZED";
    private static final String ORDERS_URL = "https://api.worldpay.com/v1/orders";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        final Intent intent = getIntent();
        final Bundle extras = intent.getExtras();

        final SharedPreferences sp = getDefaultSharedPreferences(getBaseContext());
        final String serviceKey = sp.getString(getString(R.string.service_key), getString(R.string.service_key_value));
        if (extras != null) {
            final Order order = (Order) intent.getSerializableExtra(EXTRA_APM_ORDER);
            if (order != null) {
                runOrder(order, serviceKey);
                return;
            }
        }
        finish();
    }

    /**
     * Place an APM order.
     *
     * @param order      The {@link Order} to place.
     * @param serviceKey Your service key.
     */
    private void runOrder(final Order order, final String serviceKey) {
        createOrderAsyncTask(order, serviceKey, new OrderResponse() {
            @Override
            public void onSuccess(Order order) {
                payPalRedirect(order);
            }

            @Override
            public void onResponseError(ResponseError responseError) {
                returnWithError(null, responseError);
            }

            @Override
            public void onError(WorldPayError worldPayError) {
                returnWithError(worldPayError, null);
            }
        }).execute();
    }

    /**
     * This method loads the PayPal redirect URL received from the placed order.
     */
    private void payPalRedirect(final Order order) {
        if (PRE_AUTHORIZED.equals(order.getPaymentStatus())) {
            final WebView webView = (WebView) findViewById(R.id.webview);

            if (Build.VERSION.SDK_INT >= 21) {
                webView.getSettings().setMixedContentMode(MIXED_CONTENT_ALWAYS_ALLOW);
            }
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
                    if (url.equals(order.getSuccessUrl())) {
                        final Intent intent = new Intent();
                        intent.putExtra(EXTRA_PAYPAL_RESULT, "SUCCESS");
                        setResult(RESULT_OK, intent);
                        finish();
                    } else if (url.equals(order.getCancelUrl())) {
                        final Intent intent = new Intent();
                        intent.putExtra(EXTRA_PAYPAL_RESULT, "CANCELLED");
                        setResult(RESULT_OK, intent);
                        finish();
                    } else if (url.equals(order.getFailureUrl())) {
                        final Intent intent = new Intent();
                        intent.putExtra(EXTRA_PAYPAL_RESULT, "FAILED");
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                    return true;
                }
            });

            webView.loadUrl(order.getRedirectURL());
        }
    }

    /**
     * Asynchronously creates an {@link Order}.
     *
     * @param order      The {@link Order} to place.
     * @param serviceKey The merchant service key.
     * @param callback   The {@link OrderResponse} callback.
     * @return {@link AsyncTask}
     */
    private AsyncTask<Void, Void, HttpServerResponse> createOrderAsyncTask(final Order order,
                                                                           final String serviceKey,
                                                                           final OrderResponse callback) {
        final WorldPayError worldPayError = new WorldPayError();

        return new AsyncTask<Void, Void, HttpServerResponse>() {
            @Override
            protected HttpServerResponse doInBackground(Void... params) {
                try {
                    return httpEntityRequest(POST, ORDERS_URL, order.getAsJSONObject().toString(), createHeaders(serviceKey));
                } catch (JSONException e) {
                    worldPayError.setError(WorldPayError.ERROR_CREATING_REQUEST_JSON,
                            "Error while trying to create the request :" + e.getMessage());
                } catch (IllegalStateException | IOException e) {
                    worldPayError.setError(WorldPayError.ERROR_RESPONSE_CONNECTION, "Connection error : " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(final HttpServerResponse serverResponse) {
                if (serverResponse == null) {
                    if (worldPayError.getCode() == 0) {
                        worldPayError.setError(WorldPayError.ERROR_RESPONSE_UNKNOWN, "Error while trying to get response.");
                    }
                    callback.onError(worldPayError);
                    return;
                }
                if (serverResponse.getStatusCode() == 200) {
                    try {
                        callback.onSuccess(Order.valueOf(serverResponse.getResponse()));
                    } catch (JSONException e) {
                        worldPayError.setError(WorldPayError.ERROR_RESPONSE_MALFORMED_JSON, "Json parsing failed.");
                        callback.onError(worldPayError);
                    }
                } else {
                    ResponseError responseError = new ResponseError();
                    String responseString = serverResponse.getResponse();
                    try {
                        if (responseString != null) {
                            responseError.parseJsonString(responseString);
                        } else {
                            responseError.setHttpStatusCode(serverResponse.getStatusCode());
                        }
                        callback.onResponseError(responseError);
                    } catch (JSONException e) {
                        worldPayError.setError(WorldPayError.ERROR_RESPONSE_MALFORMED_JSON, "Json parsing failed.");
                        callback.onError(worldPayError);
                    }
                }
            }
        };
    }

    /**
     * Return the error result back to the {@link OrderDetailsActivity}
     *
     * @param worldPayError {@link WorldPayError}
     * @param responseError {@link ResponseError}
     */
    private void returnWithError(final WorldPayError worldPayError, final ResponseError responseError) {
        final Intent intent = new Intent();
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
