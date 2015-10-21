package com.worldpay;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * WorldPay main class based on singleton pattern.
 * <p>
 * Developer can access the instance of the library using {@link WorldPay#getInstance()} . <br>
 * </p>
 * <p>
 * <p>
 * <h3>Settings</h3>
 * <p>
 * It is mandatory to set the client key using {@link #setClientKey(String)}.
 * <p>
 * <p>
 * Other possible settings are :
 * <ul>
 * <li>Set reusable card mode using {@link #setReusable(boolean)} in order reuse card details.</li>
 * <li>Enable / disable debugging mode using {@link WorldPay#setDebug(boolean)} or
 * {@link WorldPay#setDebug(boolean, String)}.</li>
 * </ul>
 * </p>
 * <p>
 * <h3>Usage</h3>
 * <p>
 * <ol>
 * Steps:
 * <p>
 * <li>Set your client key :<br>
 * <p>
 * <pre>
 * WorldPay.getInstance().setClientKey(YOUR_CLIENT_KEY);
 * </pre>
 * <p>
 * </li>
 * <p>
 * <li>Create a card and set data.<br>
 * <p>
 * <pre>
 * Card card = new Card();
 * card.setHolderName(name).setCardNumber(cNumber).setCvc(cvc).setExpiryYear(expYear).setExpriryMonth(expMonth);
 * </pre>
 * <p>
 * </li>
 * <li>Validate the card :<br>
 * <p>
 * <pre>
 * CardValidationError validate = card.validate();
 *
 * if (validate != null &amp;&amp; validate.hasErrors()) {
 * 	//validation errors exist
 * } else {
 * 	//everything is ok, you can continue
 * }
 * </pre>
 * <p>
 * If validation errors exist, check {@link CardValidationError} class for more information.</li>
 * <li>Create and execute {@link #createTokenAsyncTask(Context, Card, WorldPayResponse)} in order to
 * create an {@link AsyncTask}, which executes the transaction with WorldPay servers and returns the
 * response asynchronously to a callback interface. <br>
 * <br>
 * <p>
 * Implement {@link WorldPayResponse} interface, to handle response from the WorldPay. <br>
 * <p>
 * <pre>
 * AsyncTask&lt;Void, Void, ServerResponse&gt; createTokenAsyncTask = worldPay.createTokenAsyncTask(this, card, new WorldPayResponse() {
 *
 * 	&#064;Override
 * 	public void onSuccess(ResponseCard responseCard) {
 * 		DebugLogger.d(&quot;# onSuccess : &quot; + responseCard);
 *    }
 *
 * 	&#064;Override
 * 	public void onResponseError(ResponseError responseError) {
 * 		DebugLogger.d(&quot;# onResponseError: &quot; + responseError.getMessage());
 *    }
 *
 * 	&#064;Override
 * 	public void onError(WorldPayError worldPayError) {
 * 		DebugLogger.d(&quot;# onError: &quot; + worldPayError.getMessage());
 *    }
 *
 * });
 * </pre>
 * <p>
 * </li>
 * </ol>
 *
 * @author Sotiris Chatzianagnostou - sotcha@arx.net
 * @see Card
 * @see CardValidationError
 * @see WorldPayResponse
 * @see ResponseCard
 * @see ResponseError
 * @see WorldPayError
 */
public class WorldPay {
    /**
     * Default debugging tag.
     */
    public static final String TAG = "WorldPay";

    /**
     * The library version.
     */
    public static final String VERSION = "0.1";

    /**
     * The static instance.
     */
    private static WorldPay instance;

    //store here the validation type
    private String clientKey;

    // if the token is reusable
    private boolean reusable = false;


    private WorldPay() {

    }

    /**
     * Instance accessor
     *
     * @return Returns the instance of the class
     */
    public static WorldPay getInstance() {
        if (instance == null) {
            instance = new WorldPay();

        }
        return instance;
    }

    /**
     * Enable or disable debugging messages on android console. <br>
     * Debugging is disabled by default.
     *
     * @param enable
     */
    public static void setDebug(boolean enable) {
        DebugLogger.setDebug(enable);
        DebugLogger.setTag(TAG);
    }

    /**
     * Enable or disable debugging messages on android console using a custom tag. <br>
     * Debugging is disabled by default.
     *
     * @param enable
     * @param customTag
     */
    public static void setDebug(boolean enable, String customTag) {
        DebugLogger.setDebug(enable);
        DebugLogger.setTag(customTag);
    }

    /**
     * Asynchronously creates token
     *
     * @param context
     * @param card
     * @param worldPayResponseCallback
     * @return {@link AsyncTask}
     */
    public AsyncTask<Void, Void, HttpServerResponse> createTokenAsyncTask(Context context, final Card card,
                                                                          final WorldPayResponse worldPayResponseCallback) {
        return createTokenAsyncTask(context, card, null, worldPayResponseCallback);
    }

    public AsyncTask<Void, Void, HttpServerResponse> createTokenAsyncTask(Context context, final ResponseCard responseCard,
                                                                          final WorldPayResponse worldPayResponseCallback) {
        return createTokenAsyncTask(context, null, responseCard, worldPayResponseCallback);
    }

    private AsyncTask<Void, Void, HttpServerResponse> createTokenAsyncTask(Context context, final Card card,
                                                                           final ResponseCard responseCard, final WorldPayResponse worldPayResponseCallback) {
        final WorldPayError worldPayError = new WorldPayError();

        if (!isNetworkConnected(context)) {
            worldPayError.setError(WorldPayError.ERROR_NO_NETWORK, "There is no network connectivity");
            return null;
        }

        return new AsyncTask<Void, Void, HttpServerResponse>() {
            @Override
            protected HttpServerResponse doInBackground(Void... params) {
                DebugLogger.d("createTokenAsyncTask [start] ...");

                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("reusable", reusable);
                    jsonObject.put("clientKey", clientKey);
                    jsonObject.put("paymentMethod", card.getAsJSONObject());

                    DebugLogger.d("Created req: " + jsonObject.toString());

                    return WorldPayHttp.getInstance().executeRequest(jsonObject.toString());
                } catch (JSONException e) {
                    DebugLogger.e(e);
                    worldPayError.setError(WorldPayError.ERROR_CREATING_REQUEST_JSON,
                            "Error while trying to create the request :" + e.getMessage());
                } catch (IllegalStateException | IOException e) {
                    DebugLogger.e(e);
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
                    worldPayResponseCallback.onError(worldPayError);
                    return;
                }

                if (serverResponse.getStatusCode() == 200) {
                    //success
                    ResponseCard responseCard = new ResponseCard();
                    try {
                        responseCard.parseJsonString(serverResponse.getResponse());
                        worldPayResponseCallback.onSuccess(responseCard);

                    } catch (JSONException e) {
                        DebugLogger.e(e);

                        worldPayError.setError(WorldPayError.ERROR_RESPONSE_MALFORMED_JSON, "Json parsing failed.");
                        worldPayResponseCallback.onError(worldPayError);
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

                        worldPayResponseCallback.onResponseError(responseError);
                    } catch (JSONException e) {
                        DebugLogger.e(e);
                        worldPayError.setError(WorldPayError.ERROR_RESPONSE_MALFORMED_JSON, "Json parsing failed.");
                        worldPayResponseCallback.onError(worldPayError);
                    }
                }
            }
        };
    }

    /**
     * Checks reusable token
     *
     * @param context
     * @param reusableToken
     * @param worldPayResponseCallback
     * @return {@link AsyncTask}
     */
    public AsyncTask<Void, Void, HttpServerResponse> reuseTokenAsyncTask(Context context, final ReusableToken reusableToken,
                                                                         final WorldPayResponseReusableToken worldPayResponseCallback) {
        return reuseTokenAsyncTask(context, reusableToken, null, worldPayResponseCallback);
    }

    private AsyncTask<Void, Void, HttpServerResponse> reuseTokenAsyncTask(Context context, final ReusableToken reusableToken,
                                                                          final ResponseCard responseCard, final WorldPayResponseReusableToken worldPayResponseCallback) {
        final WorldPayError worldPayError = new WorldPayError();

        if (!isNetworkConnected(context)) {
            worldPayError.setError(WorldPayError.ERROR_NO_NETWORK, "There is no network connectivity");
            return null;
        }

        return new AsyncTask<Void, Void, HttpServerResponse>() {
            @Override
            protected HttpServerResponse doInBackground(Void... params) {
                DebugLogger.d("reuseTokenAsyncTask [start] ...");

                try {
                    JSONObject jsonObject = reusableToken.getAsJSONObject();

                    return WorldPayHttp.getInstance().executeReuseableRequest(reusableToken.getToken(), jsonObject.toString());
                } catch (JSONException e) {
                    DebugLogger.e(e);
                    worldPayError.setError(WorldPayError.ERROR_CREATING_REQUEST_JSON,
                            "Error while trying to create the request :" + e.getMessage());
                } catch (IllegalStateException | IOException e) {
                    DebugLogger.e(e);
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
                    worldPayResponseCallback.onError(worldPayError);
                    return;
                }
                DebugLogger.d("serverResponse " + serverResponse.getStatusCode());
                if (serverResponse.getStatusCode() == 200) {
                    worldPayResponseCallback.onSuccess();
                    //success

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

                        worldPayResponseCallback.onResponseError(responseError);
                    } catch (JSONException e) {
                        DebugLogger.e(e);
                        worldPayError.setError(WorldPayError.ERROR_RESPONSE_MALFORMED_JSON, "Json parsing failed.");
                        worldPayResponseCallback.onError(worldPayError);
                    }
                }
            }

        };

    }

    public String getClientKey() {
        return clientKey;
    }

    /**
     * Set the client key provided from worldpay.com. You can not use the library without setting
     * the client key.
     *
     * @param clientKey
     */
    public void setClientKey(String clientKey) {
        this.clientKey = clientKey;
    }

    /**
     * Retrieves if the library uses stored cards multiple times or not.
     *
     * @return
     */
    public boolean isReusable() {
        return reusable;
    }

    /**
     * Set if the library will use stored cards multiple times or not.
     *
     * @param reusable
     */
    public void setReusable(boolean reusable) {
        this.reusable = reusable;
    }

    private boolean isNetworkConnected(Context context) {
        //check for connectivity permission
        PackageManager pm = context.getPackageManager();
        int hasPerm = pm.checkPermission(android.Manifest.permission.ACCESS_NETWORK_STATE, context.getPackageName());
        if (hasPerm != PackageManager.PERMISSION_GRANTED) {
            //we do not know so return true
            DebugLogger.d("There is no connectivity permission.");
            return true;
        }

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }


}
