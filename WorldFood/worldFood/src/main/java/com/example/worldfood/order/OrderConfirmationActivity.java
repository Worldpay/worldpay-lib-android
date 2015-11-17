package com.example.worldfood.order;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.example.worldfood.R;
import com.example.worldfood.menu.MenuActivity;

import static android.app.ActionBar.DISPLAY_SHOW_CUSTOM;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

/**
 * {@link Activity} that informs the user both that their order has been successfully
 * placed and when it will be delivered.
 */
public final class OrderConfirmationActivity extends Activity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLayout();
        initHomeButton();
    }

    private void initLayout() {
        setContentView(R.layout.activity_order_confirmation);
        final ActionBar bar = getActionBar();
        if (bar != null) {
            bar.setDisplayOptions(DISPLAY_SHOW_CUSTOM);
            bar.setCustomView(R.layout.action_bar_worldfood_logo);
        }
    }

    /**
     * Bind launching the {@link OrderConfirmationActivity} to the home button (restarting the order
     * flow).
     */
    private void initHomeButton() {
        final Button homeButton = (Button) findViewById(R.id.homeButton);
        homeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
                intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

}
