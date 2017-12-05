package org.odk.collect.android.location.usecases;


import android.app.Activity;
import android.support.annotation.NonNull;

import org.odk.collect.android.R;
import org.odk.collect.android.injection.config.scopes.PerActivity;

import javax.inject.Inject;

import timber.log.Timber;

@PerActivity
public class OnMapError {

    @NonNull
    private final Activity activity;

    @NonNull
    private final ToastUtil toastUtil;

    @Inject
    OnMapError(@NonNull Activity activity, @NonNull ToastUtil toastUtil) {
        this.activity = activity;
        this.toastUtil = toastUtil;
    }

    public void onError(Throwable error) {
        Timber.e("Error loading map.", error);
        toastUtil.showShortToast(R.string.google_play_services_error_occured);

        activity.finish();
    }
}
