package com.example.worldfood.order;

import com.worldpay.ResponseError;
import com.worldpay.WorldPayError;

/**
 * Order callback methods.
 */
interface OrderResponse {
    /**
     * The transaction was successful.
     *
     * @param order The {@link Order}.
     */
    void onSuccess(final Order order);

    /**
     * WorldPay server responded with an error.
     *
     * @param responseError Error details
     */
    void onResponseError(final ResponseError responseError);

    /**
     * A generic error occurred.
     *
     * @param worldPayError Error details
     */
    void onError(final WorldPayError worldPayError);
}
