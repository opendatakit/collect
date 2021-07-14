package org.odk.collect.location;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.function.Supplier;

/**
 * A static helper class for obtaining the appropriate LocationClient to use.
 */
public class LocationClientProvider {
    @Nullable
    private static LocationClient testClient;

    private LocationClientProvider() {
    }  // prevent instantiation of this utility class

    /**
     * Returns a LocationClient appropriate for a given context.
     */
    // NOTE(ping): As of 2018-11-01, the GoogleFusedLocationClient never returns an
    // accuracy radius below 3m: https://issuetracker.google.com/issues/118789585
    public static LocationClient getClient(@NonNull Context context,
                                           @NonNull Supplier<GoogleFusedLocationClient> googleFusedLocationClientProvider,
                                           GoogleApiAvailability googleApiAvailability) {
        boolean playServicesAvailable = googleApiAvailability
                .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS;

        if (testClient != null) {
            return testClient;
        } else if (playServicesAvailable) {
            return googleFusedLocationClientProvider.get();
        } else {
            return new AndroidLocationClient(context);
        }
    }

    /**
     * Sets the LocationClient.  For use in tests only.
     */
    public static void setTestClient(LocationClient testClient) {
        LocationClientProvider.testClient = testClient;
    }
}
