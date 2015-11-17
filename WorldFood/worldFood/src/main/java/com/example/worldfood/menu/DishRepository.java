package com.example.worldfood.menu;

import java.util.List;

interface DishRepository {

    Dish get(final int index);

    List<Dish> findAll();

}
