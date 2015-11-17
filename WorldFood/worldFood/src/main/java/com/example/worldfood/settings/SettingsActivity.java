package com.example.worldfood.settings;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

import com.example.worldfood.R;

import static android.app.ActionBar.DISPLAY_HOME_AS_UP;
import static android.app.ActionBar.DISPLAY_SHOW_CUSTOM;
import static android.app.ActionBar.DISPLAY_SHOW_TITLE;
import static android.support.v4.app.NavUtils.navigateUpFromSameTask;

/**
 * {@link Activity} that allows a user to control the application settings.
 *
 * @see SettingsFragment
 */
public final class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLayout();
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment()).commit();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initLayout() {
        final ActionBar bar = getActionBar();
        if (bar != null) {
            bar.setDisplayOptions(DISPLAY_HOME_AS_UP | DISPLAY_SHOW_CUSTOM | DISPLAY_SHOW_TITLE);
            bar.setTitle(R.string.back);
            bar.setDisplayShowCustomEnabled(true);
            bar.setCustomView(R.layout.action_bar_settings);
        }
    }
}
