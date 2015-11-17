package com.example.worldfood.menu;

import com.example.worldfood.R;

import java.util.ArrayList;
import java.util.List;

final class InMemoryDishRepository implements DishRepository {

    private static final List<Dish> DISHES;

    static {
        DISHES = new ArrayList<>();
        DISHES.add(new Dish("Rare roast topside with Marmite & sweet onion gravy.", "Roast beef",
                "£9.90", R.drawable.uk, R.drawable.roastbeef));
        DISHES.add(new Dish("Chicken, seafood, vegetables, rice", "Paella", "£11.20",
                R.drawable.spain, R.drawable.paella));
        DISHES.add(new Dish("Classic baked dish of layered ground beef. Served with house salad.", "Moussaka", "£8.50",
                R.drawable.greece, R.drawable.moussaka));
    }

    @Override
    public Dish get(final int index) {
        return DISHES.get(index);
    }

    @Override
    public List<Dish> findAll() {
        return DISHES;
    }
}
