package com.example.worldfood;

import java.io.Serializable;

/**
 * Contains the customers details used in the 3DS order {@link OrderActivity}
 *
 */
public class OrderDetails implements Serializable {

    private String address;
    private String city;
    private String postCode;
    private String price;

    public OrderDetails(String address, String city, String postCode, String price) {
        this.address = address;
        this.city = city;
        this.postCode = postCode;
        this.price = price;
    }

    public OrderDetails() {
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }


    public void updateOrder(String address, String city, String postCode, String price) {
        this.address = address;
        this.city = city;
        this.postCode = postCode;
        this.price = price;
    }
}
