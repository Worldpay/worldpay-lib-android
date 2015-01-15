package com.worldpay;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

/**
 * Worldpay main class based on singleton pattern.
 * <p>
 * Developer can access the instance of the library using {@link Worldpay#getInstance()} . <br>
 * </p>
 * 
 * <p>
 * <h3>Settings</h3> 
 * 
 * It is mandatory to set the client key using {@link #setClientKey(String)}.
 * 
 * 
 * Other possible settings are :
 * <ul>
 * <li>Set reusable token mode using {@link #setReusable(boolean)} in order to reuse token.</li>
 * <li>Enable / disable debugging mode using {@link Worldpay#setDebug(boolean)} or
 * {@link Worldpay#setDebug(boolean, String)}.</li>
 * </ul>
 * </p>
 * 
 * <h3>Usage</h3>
 * 
 * <ol>
 * Steps:
 * 
 * <li>Set your client key :<br>
 * 
 * <pre>
 * Worldpay.getInstance().setClientKey(YOUR_CLIENT_KEY);
 * </pre>
 * 
 * </li>
 * 
 * <li>Create a card and set data.<br>
 * 
 * <pre>
 * Card card = new Card();
 * card.setHolderName(name).setCardNumber(cNumber).setCvc(cvc).setExpiryYear(expYear).setExpriryMonth(expMonth);
 * </pre>
 * 
 * </li>
 * <li>Validate the card :<br>
 * 
 * <pre>
 * CardValidationError validate = card.validate();
 * 
 * if (validate != null &amp;&amp; validate.hasErrors()) {
 * 	//validation errors exist
 * } else {
 * 	//everything is ok, you can continue
 * }
 * </pre>
 * 
 * If validation errors exist, check {@link CardValidationError} class for more information.</li>
 * <li>Create and execute {@link #createTokenAsyncTask(Context, Card, WorldpayResponse)} in order to
 * create an {@link AsyncTask}, which executes the transaction with Worldpay and returns the
 * response asynchronously to a callback interface. <br>
 * <br>
 * 
 * Implement {@link WorldpayResponse} interface, to handle response from the Worldpay. <br>
 * 
 * <pre>
 * AsyncTask&lt;Void, Void, ServerResponse&gt; createTokenAsyncTask = worldpay.createTokenAsyncTask(this, card, new WorldpayResponse() {
 * 
 * 	&#064;Override
 * 	public void onSuccess(ResponseCard responseCard) {
 * 		DebugLogger.d(&quot;# onSuccess : &quot; + responseCard);
 * 	}
 * 
 * 	&#064;Override
 * 	public void onResponseError(ResponseError responseError) {
 * 		DebugLogger.d(&quot;# onResponseError: &quot; + responseError.getMessage());
 * 	}
 * 
 * 	&#064;Override
 * 	public void onError(WorldpayError worldpayError) {
 * 		DebugLogger.d(&quot;# onError: &quot; + worldpayError.getMessage());
 * 	}
 * 
 * });
 * </pre>
 * 
 * </li>
 * </ol>
 * 
 * 
 * @see Card
 * @see CardValidationError
 * @see WorldpayResponse
 * @see ResponseCard
 * @see ResponseError
 * @see WorldpayError
 * 
 */
public class Worldpay {
	/**
	 * Default debugging tag.
	 */
	public static final String TAG = "Worldpay";

	/**
	 * The library version.
	 */
	public static final String VERSION = "1.0";

	/**
	 * The static instance.
	 */
	private static Worldpay instance;

	//store here the validation type 
	private String clientKey;

	// if the token is reusable 
	private boolean reusable = false;

	private Worldpay() {

	}

	/**
	 * Instance accessor
	 * 
	 * @return Returns the instance of the class
	 */
	public static Worldpay getInstance() {
		if (instance == null) {
			instance = new Worldpay();
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
	 * @param worldpayResponseCallback
	 * @return {@link AsyncTask}
	 */
	public AsyncTask<Void, Void, HttpServerResponse> createTokenAsyncTask(Context context, final Card card,
			final WorldpayResponse worldpayResponseCallback) {
		return createTokenAsyncTask(context, card, null, worldpayResponseCallback);
	}

	public AsyncTask<Void, Void, HttpServerResponse> createTokenAsyncTask(Context context, final ResponseCard responseCard,
			final WorldpayResponse worldpayResponseCallback) {
		return createTokenAsyncTask(context, null, responseCard, worldpayResponseCallback);
	}

	private AsyncTask<Void, Void, HttpServerResponse> createTokenAsyncTask(Context context, final Card card,
			final ResponseCard responseCard, final WorldpayResponse worldpayResponseCallback) {
		final WorldpayError worldpayError = new WorldpayError();

		if (!isNetworkConnected(context)) {
			worldpayError.setError(WorldpayError.ERROR_NO_NETWORK, "There is no network connectivity");
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

					return WorldpayHttp.getInstance().executeRequest(jsonObject.toString());
				} catch (JSONException e) {
					DebugLogger.e(e);
					worldpayError.setError(WorldpayError.ERROR_CREATING_REQUEST_JSON,
							"Error while trying to create the request :" + e.getMessage());
				} catch (IllegalStateException | IOException e) {
					DebugLogger.e(e);
					worldpayError.setError(WorldpayError.ERROR_RESPONSE_CONNECTION, "Connection error : " + e.getMessage());
				}

				return null;
			}

			@Override
			protected void onPostExecute(HttpServerResponse serverResponse) {
				if (serverResponse == null) {
					if (worldpayError.getCode() == 0) {
						worldpayError.setError(WorldpayError.ERROR_RESPONSE_UNKNOWN, "Error while trying to get response.");
					}
					worldpayResponseCallback.onError(worldpayError);
					return;
				}

				if (serverResponse.getStatusCode() == 200) {
					//success 
					ResponseCard responseCard = new ResponseCard();
					try {
						responseCard.parseJsonString(serverResponse.getResponse());
						worldpayResponseCallback.onSuccess(responseCard);

					} catch (JSONException e) {
						DebugLogger.e(e);

						worldpayError.setError(WorldpayError.ERROR_RESPONSE_MALFORMED_JSON, "Json parsing failed.");
						worldpayResponseCallback.onError(worldpayError);
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

						worldpayResponseCallback.onResponseError(responseError);
					} catch (JSONException e) {
						DebugLogger.e(e);
						worldpayError.setError(WorldpayError.ERROR_RESPONSE_MALFORMED_JSON, "Json parsing failed.");
						worldpayResponseCallback.onError(worldpayError);
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
	 * @param worldpayResponseCallback
	 * @return {@link AsyncTask}
	 */
	public AsyncTask<Void, Void, HttpServerResponse> reuseTokenAsyncTask(Context context, final ReusableToken reusableToken,
			final WorldpayResponseReusableToken worldpayResponseCallback) {
		return reuseTokenAsyncTask(context, reusableToken, null, worldpayResponseCallback);
	}

	private AsyncTask<Void, Void, HttpServerResponse> reuseTokenAsyncTask(Context context, final ReusableToken reusableToken,
			final ResponseCard responseCard, final WorldpayResponseReusableToken worldpayResponseCallback) {
		final WorldpayError worldpayError = new WorldpayError();

		if (!isNetworkConnected(context)) {
			worldpayError.setError(WorldpayError.ERROR_NO_NETWORK, "There is no network connectivity");
			return null;
		}

		return new AsyncTask<Void, Void, HttpServerResponse>() {
			@Override
			protected HttpServerResponse doInBackground(Void... params) {
				DebugLogger.d("reuseTokenAsyncTask [start] ...");

				try {
					JSONObject jsonObject = reusableToken.getAsJSONObject();

					return WorldpayHttp.getInstance().executeReuseableRequest(reusableToken.getToken(),jsonObject.toString());
				} catch (JSONException e) {
					DebugLogger.e(e);
					worldpayError.setError(WorldpayError.ERROR_CREATING_REQUEST_JSON,
							"Error while trying to create the request :" + e.getMessage());
				} catch (IllegalStateException | IOException e) {
					DebugLogger.e(e);
					worldpayError.setError(WorldpayError.ERROR_RESPONSE_CONNECTION, "Connection error : " + e.getMessage());
				}

				return null;
			}

			@Override
			protected void onPostExecute(HttpServerResponse serverResponse) {
				if (serverResponse == null) {
					if (worldpayError.getCode() == 0) {
						worldpayError.setError(WorldpayError.ERROR_RESPONSE_UNKNOWN, "Error while trying to get response.");
					}
					worldpayResponseCallback.onError(worldpayError);
					return;
				}
				DebugLogger.d("serverResponse " + serverResponse.getStatusCode());
				if (serverResponse.getStatusCode() == 200) {
					worldpayResponseCallback.onSuccess();
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

						worldpayResponseCallback.onResponseError(responseError);
					} catch (JSONException e) {
						DebugLogger.e(e);
						worldpayError.setError(WorldpayError.ERROR_RESPONSE_MALFORMED_JSON, "Json parsing failed.");
						worldpayResponseCallback.onError(worldpayError);
					}
				}
			}
			
		};
		
	}
	public String getClientKey() {
		return clientKey;
	}

	/**
	 * Set the client key provided from online.worldpay.com. You can not use the library without setting
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
