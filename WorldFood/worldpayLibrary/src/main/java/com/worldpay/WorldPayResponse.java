package com.worldpay;

/**
 * Developers have to implement this interface in order to handle response from
 * {@link WorldPay#createTokenAsyncTask(android.content.Context, Card, WorldPayResponse)} or
 * {@link WorldPay#createTokenAsyncTask(android.content.Context, ResponseCard, WorldPayResponse)}.
 *
 * @author Sotiris Chatzianagnostou - sotcha@arx.net
 * @see ResponseCard
 * @see ResponseError
 * @see WorldPayError
 */
public interface WorldPayResponse {
    /**
     * The transaction was successful.<br>
     *
     * @param responseCard Contains informations of the card responded from woldpay server.
     */
    void onSuccess(ResponseCard responseCard);

    /**
     * Worldpay server responded with an error.
     *
     * @param responseError Error details
     */
    void onResponseError(ResponseError responseError);

    /**
     * A generic error occured
     *
     * @param worldPayError
     */
    void onError(WorldPayError worldPayError);

}
