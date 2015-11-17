package com.example.worldfood.menu;

final class Dish {

    private final String price;
    private final String description;
    private final String name;
    private final int countryFlagImageId;
    private final int mealImageId;

    Dish(final String description, final String name, final String price,
         final int countryFlagImageId, final int mealImageId) {
        this.price = price;
        this.description = description;
        this.name = name;
        this.countryFlagImageId = countryFlagImageId;
        this.mealImageId = mealImageId;
    }

    public String getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public int getCountryFlagImageId() {
        return countryFlagImageId;
    }

    public int getMealImageId() {
        return mealImageId;
    }
}
