package com.example.worldfood;

import com.worldpay.ResponseError;
import com.worldpay.WorldPayError;

/**
 * An interface in order to handle the responses from the 3DS authentication
 * @see ResponseError
 * @see WorldPayError
 */
public interface ThreeDsResponse {

    /**
     * The transaction was successful.<br>
     *
     * @param response Contains information about the 3DS response.
     */
    public void onSuccess(String response);

    /**
     * 3DS response  error.
     *
     * @param responseError Error details
     */
    public void onResponseError(ResponseError responseError);

    /**
     * A generic error occured
     *
     * @param worldPayError
     */
    public void onError(WorldPayError worldPayError);

}
