package com.worldpay;

/**
 * Developers have to implement this interface in order to handle response from
 * {@link WorldPay#reuseTokenAsyncTask(android.content.Context, ReusableToken, WorldPayResponseReusableToken)}
 *
 * @see ResponseError
 * @see WorldPayError
 */
public interface WorldPayResponseReusableToken {

    /**
     * The token is correct.<br>
     */
    void onSuccess();

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
