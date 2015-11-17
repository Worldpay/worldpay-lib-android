package com.example.worldfood.menu;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.example.worldfood.R;
import com.example.worldfood.order.OrderDetailsActivity;
import com.example.worldfood.settings.SettingsActivity;

import static android.app.ActionBar.DISPLAY_SHOW_CUSTOM;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.example.worldfood.order.OrderDetailsActivity.EXTRA_DISH_NAME;
import static com.example.worldfood.order.OrderDetailsActivity.EXTRA_DISH_PRICE;

/**
 * {@link Activity} that allows a user to order a single dish from a menu.
 * <p/>
 * This activity also provides a view pager with additional images for all the dishes on the menu.
 */
public class MenuActivity extends Activity {

    private final DishRepository dishRepository = new InMemoryDishRepository();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLayout();
        initDishImagePager();
        initDishList();
        initSettingsButton();
    }

    private void initLayout() {
        setContentView(R.layout.activity_menu);
        final ActionBar bar = getActionBar();
        if (bar != null) {
            bar.setDisplayOptions(DISPLAY_SHOW_CUSTOM);
            bar.setCustomView(R.layout.action_bar_worldfood_logo);
        }
    }

    /**
     * Bind a {@link DishImagePagerAdapter} to the view pager.
     */
    private void initDishImagePager() {
        final ViewPager pager = (ViewPager) findViewById(R.id.viewpager);
        pager.setAdapter(new DishImagePagerAdapter(getApplication(), dishRepository));
    }

    /**
     * Bind a {@link DishListAdapter} to the list of dishes.
     * <p/>
     * Additionally an {@link OnClickListener} for launching the {@link OrderDetailsActivity}
     * passing the chosen {@link Dish} and price is created and passed to the
     * {@link DishListAdapter}.
     */
    private void initDishList() {
        final ListView dishList = (ListView) findViewById(R.id.dish_list);
        dishList.setAdapter(new DishListAdapter(dishRepository, getLayoutInflater(), new OnClickListener() {
            @Override
            public void onClick(final View v) {
                final Dish dish = (Dish) v.getTag();
                if (dish != null) {
                    final Intent detailsIntent = new Intent(MenuActivity.this, OrderDetailsActivity.class);
                    detailsIntent.putExtra(EXTRA_DISH_NAME, dish.getName());
                    detailsIntent.putExtra(EXTRA_DISH_PRICE, dish.getPrice());
                    detailsIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                    startActivity(detailsIntent);
                }
            }
        }));
    }

    /**
     * Bind launching the {@link SettingsActivity} to the settings button.
     */
    private void initSettingsButton() {
        final Button settings = (Button) findViewById(R.id.settings);
        settings.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                startActivity(new Intent(MenuActivity.this, SettingsActivity.class));
            }
        });
    }

}
