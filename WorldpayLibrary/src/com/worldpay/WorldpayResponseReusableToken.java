package com.worldpay;

/**
 * Developers have to implement this interface in order to handle response from
 * {@link Worldpay#reuseTokenAsyncTask(android.content.Context, ReusableToken, WorldpayResponseReusableToken)}
 * 
 * 
 * @see ResponseError
 * @see WorldpayError
 * 
 */
public interface WorldpayResponseReusableToken {
	
	/**
	 * The token is correct.<br>
	 */
	public void onSuccess();

	/**
	 * Worldpay server responded with an error.
	 * 
	 * @param responseError
	 *            Error details
	 */
	public void onResponseError(ResponseError responseError);

	/**
	 * A generic error occured
	 * 
	 * @param worldpayError
	 */
	public void onError(WorldpayError worldpayError);

}
