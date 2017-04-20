package com.johnkmartins.nopapernews.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public final class NetworkUtility {

    private NetworkUtility() {

    }

    private static NetworkInfo getActiveNetworkInfo(final Context context) {

        if (context == null) {
            throw new IllegalArgumentException("The context cannot be null.");
        }

        return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
    }

    public static boolean isConnected(final Context context) {

        final NetworkInfo info = getActiveNetworkInfo(context);

        return info != null && info.isConnected();
    }
}
