package com.example.worldfood.order;

import java.io.Serializable;

/**
 * Contains the customers details used in the 3DS order {@link ThreeDsOrderActivity}
 */
final class OrderDetails implements Serializable {

    private final String address;
    private final String city;
    private final String postCode;
    private final int price;

    OrderDetails(final String address, final String city, final String postCode,
                 final int price) {
        this.address = address;
        this.city = city;
        this.postCode = postCode;
        this.price = price;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getPostCode() {
        return postCode;
    }

    public int getPrice() {
        return price;
    }
}
