package com.example.worldfood.settings;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;

import com.example.worldfood.R;

/**
 * A {@link PreferenceFragment} that allows the user to:
 * <ul>
 * <li>Enable/disable 3-D Secure orders.</li>
 * <li>Pre-fill the {@link com.example.worldfood.order.OrderDetailsActivity} with a dummy
 * address.</li>
 * <li>Override the URLs associated with a PayPal order.</li>
 * </ul>
 * <p/>
 * All settings are stored as {@link SharedPreferences}.
 */
public final class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
            initSummary(getPreferenceScreen().getPreference(i));
        }
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences,
                                          final String key) {
        updatePreference(findPreference(key));
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    private void initSummary(final Preference p) {
        if (p instanceof PreferenceCategory) {
            final PreferenceCategory cat = (PreferenceCategory) p;
            for (int i = 0; i < cat.getPreferenceCount(); i++) {
                initSummary(cat.getPreference(i));
            }
        } else {
            updatePreference(p);
        }
    }

    private void updatePreference(final Preference p) {
        if (p instanceof EditTextPreference) {
            final EditTextPreference editTextPref = (EditTextPreference) p;
            if (editTextPref.getText() == null || editTextPref.getText().isEmpty()) {
                p.setSummary(getDefaultSummary(p.getKey()));
            } else {
                p.setSummary(editTextPref.getText());
            }
        }
    }

    private String getDefaultSummary(final String key) {
        String defaultSummary = null;
        if (getString(R.string.paypal_redirect_url_key).equals(key)) {
            defaultSummary = getString(R.string.paypal_redirect_url_summary);
        } else if (getString(R.string.paypal_pending_url_key).equals(key)) {
            defaultSummary = getString(R.string.paypal_pending_url_summary);
        } else if (getString(R.string.paypal_success_url_key).equals(key)) {
            defaultSummary = getString(R.string.paypal_success_url_summary);
        } else if (getString(R.string.paypal_cancel_url_key).equals(key)) {
            defaultSummary = getString(R.string.paypal_cancel_url_summary);
        } else if (getString(R.string.paypal_failure_url_key).equals(key)) {
            defaultSummary = getString(R.string.paypal_failure_url_summary);
        }
        return defaultSummary;
    }
}
