package org.odk.collect.android.views;

import android.widget.LinearLayout;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.core.view.MotionEventBuilder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.robolectric.RobolectricTestRunner;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_UP;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(RobolectricTestRunner.class)
public class TrackingTouchSliderTest {

    private TrackingTouchSlider slider;

    @Before
    public void setUp() {
        ApplicationProvider.getApplicationContext().setTheme(R.style.Theme_Collect_Light);

        LinearLayout linearLayout = new LinearLayout(ApplicationProvider.getApplicationContext());
        slider = new TrackingTouchSlider(ApplicationProvider.getApplicationContext(), null);

        linearLayout.addView(slider);
    }

    @Test
    public void onStartTrackingTouch_suppressesFlingGesture() {
        slider.onTouchEvent(MotionEventBuilder.newBuilder().setAction(ACTION_DOWN).build());
        assertThat(slider.isTrackingTouch(), equalTo(true));
    }

    @Test
    public void onStopTrackingTouch_doesNotSuppressFlingGesture() {
        slider.onTouchEvent(MotionEventBuilder.newBuilder().setAction(ACTION_UP).build());
        assertThat(slider.isTrackingTouch(), equalTo(false));
    }
}