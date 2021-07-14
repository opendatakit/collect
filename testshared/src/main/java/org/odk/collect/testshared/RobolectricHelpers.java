package org.odk.collect.testshared;

import android.app.Application;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;

import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowEnvironment;
import org.robolectric.shadows.ShadowMediaMetadataRetriever;
import org.robolectric.shadows.ShadowMediaPlayer;
import org.robolectric.shadows.util.DataSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.os.Looper.getMainLooper;
import static org.robolectric.Shadows.shadowOf;

public class RobolectricHelpers {

    public static Map<Class, ServiceScenario> services = new HashMap<>();

    private RobolectricHelpers() {
    }

    public static <T extends FragmentActivity> T createThemedActivity(Class<T> clazz, int theme) {
        ActivityController<T> activity = Robolectric.buildActivity(clazz);
        activity.get().setTheme(theme);

        return activity.setup().get();
    }

    public static int getCreatedFromResId(ImageButton button) {
        return shadowOf(button.getDrawable()).getCreatedFromResId();
    }

    public static int getCreatedFromResId(Drawable drawable) {
        return shadowOf(drawable).getCreatedFromResId();
    }

    public static DataSource setupMediaPlayerDataSource(String testFile) {
        return setupMediaPlayerDataSource(testFile, 322450);
    }

    public static DataSource setupMediaPlayerDataSource(String testFile, Integer duration) {
        DataSource dataSource = DataSource.toDataSource(testFile);
        ShadowMediaMetadataRetriever.addMetadata(dataSource, MediaMetadataRetriever.METADATA_KEY_DURATION, duration.toString());
        ShadowMediaPlayer.addMediaInfo(dataSource, new ShadowMediaPlayer.MediaInfo(duration, 0));
        return dataSource;
    }

    public static void mountExternalStorage() {
        ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);
    }

    public static <T extends ViewGroup> T populateRecyclerView(T view) {
        for (int i = 0; i < view.getChildCount(); i++) {
            View child = view.getChildAt(i);

            if (child instanceof RecyclerView) {
                child.measure(0, 0);
                child.layout(0, 0, 100, 10000);
                break;
            } else if (child instanceof ViewGroup) {
                populateRecyclerView((ViewGroup) child);
            }
        }

        return view;
    }

    @Nullable
    public static <F extends Fragment> F getFragmentByClass(FragmentManager fragmentManager, Class<F> fragmentClass) {
        List<Fragment> fragments = fragmentManager.getFragments();
        for (Fragment fragment : fragments) {
            if (fragment.getClass().isAssignableFrom(fragmentClass)) {
                return (F) fragment;
            }
        }

        return null;
    }

    /**
     * @deprecated use `FragmentsTest.launchDialogFragment` instead
     */
    @Deprecated
    public static <F extends Fragment> FragmentScenario<F> launchDialogFragment(Class<F> fragmentClass, int theme) {
        return FragmentScenario.launch(fragmentClass, null, theme, (FragmentFactory) null);
    }

    /**
     * @deprecated use `FragmentsTest.launchDialogFragment` and `FragmentsTest.onViewWithDialog` instead
     */
    @Deprecated
    public static <F extends Fragment> FragmentScenario<F> launchDialogFragmentInContainer(Class<F> fragmentClass, int theme) {
        return launchDialogFragmentInContainer(fragmentClass, null, theme);
    }

    /**
     * @deprecated use `FragmentsTest.launchDialogFragment` and `FragmentsTest.onViewWithDialog` instead
     */
    @Deprecated
    public static <F extends Fragment> FragmentScenario<F> launchDialogFragmentInContainer(Class<F> fragmentClass, Bundle fragmentArgs, int theme) {
        /*
          Needed to avoid explosion (NullPointerException) inside internal platform code (WindowDecorActionBar).
          For some reason AppCompat.Light or AppCompat.Light.NoActionBar don't work. Our theme must declare
          something that is missing in those base themes when they are used in Robolectric.

          This is probably something that should be fixed within Robolectric.
         */
        ApplicationProvider.getApplicationContext().setTheme(theme);
        return FragmentScenario.launchInContainer(fragmentClass, fragmentArgs);
    }

    public static void runLooper() {
        shadowOf(getMainLooper()).idle();
    }

    public static void clearServices() {
        services.clear();
    }

    public static void runServices() {
        runServices(false);
    }

    public static void runServices(boolean keepServices) {
        Application application = ApplicationProvider.getApplicationContext();

        // Run pending start commands
        while (shadowOf(application).peekNextStartedService() != null) {
            Intent intent = shadowOf(application).getNextStartedService();

            Class serviceClass;
            try {
                serviceClass = Class.forName(intent.getComponent().getClassName());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            if (keepServices) {
                if (services.containsKey(serviceClass)) {
                    services.get(serviceClass).startWithNewIntent(intent);
                } else {
                    ServiceScenario serviceController = ServiceScenario.launch(serviceClass, intent);
                    services.put(serviceClass, serviceController);
                }
            } else {
                ServiceScenario.launch(serviceClass, intent);
            }
        }

        // Run pending stops - only need to stop previously started services
        if (keepServices) {
            while (true) {
                Intent intent = shadowOf(application).getNextStoppedService();
                if (intent == null) {
                    break;
                }

                Class serviceClass;
                try {
                    serviceClass = Class.forName(intent.getComponent().getClassName());
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }


                if (services.containsKey(serviceClass)) {
                    services.get(serviceClass).moveToState(Lifecycle.State.DESTROYED);
                    services.remove(serviceClass);
                }
            }
        }
    }
}
