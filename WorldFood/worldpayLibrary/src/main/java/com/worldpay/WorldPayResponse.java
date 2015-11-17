package com.worldpay;

/**
 * Developers have to implement this interface in order to handle response from
 * {@link WorldPay#createTokenAsyncTask(android.content.Context, Card, WorldPayResponse)} or
 * {@link WorldPay#createTokenAsyncTask(android.content.Context, ResponseCard, WorldPayResponse)}.
 *
 * @see ResponseCard
 * @see ResponseError
 * @see WorldPayError
 */
public interface WorldPayResponse {
    /**
     * The transaction was successful.<br>
     *
     * @param response Contains information of the card responded from WorldPay server.
     */
    void onSuccess(ResponseCard response);

    /**
     * Worldpay server responded with an error.
     *
     * @param responseError Error details
     */
    void onResponseError(ResponseError responseError);

    /**
     * A generic error occured
     *
     * @param worldPayError Error details
     */
    void onError(WorldPayError worldPayError);

}
