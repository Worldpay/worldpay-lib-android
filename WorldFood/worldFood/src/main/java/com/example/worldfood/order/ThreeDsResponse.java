package com.example.worldfood.order;

import com.worldpay.ResponseError;
import com.worldpay.WorldPayError;

/**
 * 3-D Secure callback methods.
 */
interface ThreeDsResponse {

    /**
     * Callback invoked if the transaction was successful.
     *
     * @param response The 3-D Secure response.
     */
    void onSuccess(final String response);

    /**
     * Callback invoked if the transaction failed.
     *
     * @param responseError The {@link ResponseError}.
     */
    void onResponseError(final ResponseError responseError);

    /**
     * Callback invoked if the transaction failed.
     *
     * @param worldPayError The {@link WorldPayError}.
     */
    void onError(final WorldPayError worldPayError);

}
