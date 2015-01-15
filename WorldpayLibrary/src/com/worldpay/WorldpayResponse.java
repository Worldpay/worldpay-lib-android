package com.worldpay;

/**
 * Developers have to implement this interface in order to handle response from
 * {@link Worldpay#createTokenAsyncTask(android.content.Context, Card, WorldpayResponse)} or
 * {@link Worldpay#createTokenAsyncTask(android.content.Context, ResponseCard, WorldpayResponse)}.
 * 
 * 
 * @see ResponseCard
 * @see ResponseError
 * @see WorldpayError
 * 
 */
public interface WorldpayResponse {
	/**
	 * The transaction was successful.<br>
	 * 
	 * @param responseCard
	 *            Contains informations of the card responded from woldpay server.
	 */
	public void onSuccess(ResponseCard responseCard);

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
