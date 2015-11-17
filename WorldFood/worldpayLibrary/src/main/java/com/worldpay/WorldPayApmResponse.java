package com.worldpay;

import android.content.Context;

/**
 * Developers have to implement this interface in order to handle response from
 * {@link WorldPay#createTokenAsyncTask(Context, AlternativePaymentMethod, WorldPayApmResponse)}.
 */
public interface WorldPayApmResponse {
    /**
     * The transaction was successful.
     *
     * @param alternativePaymentMethodToken The {@link AlternativePaymentMethodToken} from WorldPay.
     */
    void onSuccess(final AlternativePaymentMethodToken alternativePaymentMethodToken);

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
