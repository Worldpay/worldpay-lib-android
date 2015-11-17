package com.example.worldfood.order;


import android.content.Context;

import com.worldpay.ResponseCard;

import java.util.List;

interface CardRepository {

    ResponseCard get(final Context context, final int index);

    List<ResponseCard> findAll(final Context context);

    void save(final List<ResponseCard> cards, final Context context);

}
