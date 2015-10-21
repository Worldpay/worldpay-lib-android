package com.example.worldfood;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebView;

/**
 * A {@link CustomWebview} custom webView that is used to load
 * the 3DS authentication page
 */
public class CustomWebview extends WebView {

    public CustomWebview(Context context) {
        super(context);
    }

    public CustomWebview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CustomWebview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * This method loads the card issuer's authentication page and makes an HTTP POST containing the TERM_URL and the encoded Card Token data
     */
    @Override
    public void postUrl(String url, byte[] postData) {

        Log.d(OrderActivity.EXTRA_CARD_TOKEN, "postData :" + url + "  data: " + postData);
        super.postUrl(url, postData);
    }

}