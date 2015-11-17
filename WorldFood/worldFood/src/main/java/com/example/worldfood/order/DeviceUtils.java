package com.example.worldfood.order;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.WIFI_SERVICE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.text.format.Formatter.formatIpAddress;

final class DeviceUtils {

    /**
     * Checks if the device is connected to a network.
     * <p/>
     * The response defaults to {@code true} if there is no permission granted to check the network
     * state.
     *
     * @param context Application context.
     * @return {@code true} if the device is believed to have an active network connection.
     */
    static boolean isNetworkConnected(final Context context) {
        final PackageManager pm = context.getPackageManager();
        final int hasPerm = pm.checkPermission(ACCESS_NETWORK_STATE, context.getPackageName());
        if (hasPerm != PERMISSION_GRANTED) {
            return true;
        }
        final NetworkInfo an = ((ConnectivityManager) context
                .getSystemService(CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return an != null && an.isConnectedOrConnecting();
    }

    /**
     * Returns the devices WIFI IP address.
     *
     * @param context Application context.
     * @return IP address.
     */
    static String getIpAddress(final Context context) {
        return formatIpAddress(((WifiManager) context.getSystemService(WIFI_SERVICE))
                .getConnectionInfo().getIpAddress());
    }

    /**
     * Returns a dummy user agent.
     * <p/>
     * <b>In real applications this should be the actual users user agent.</b>
     *
     * @return The shoppers user agent.
     */
    static String getUserAgent() {
        return "Mozilla/5.0 (Linux; <Android Version>; <Build Tag etc.>) AppleWebKit/<WebKit Rev> " +
                "(KHTML, like Gecko) Chrome/<Chrome Rev> Mobile Safari/<WebKit Rev>";
    }

}
