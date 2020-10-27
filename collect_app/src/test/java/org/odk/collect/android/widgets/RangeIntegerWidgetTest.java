package org.odk.collect.android.widgets;

import android.view.MotionEvent;
import android.view.View;

import androidx.test.core.view.MotionEventBuilder;

import org.javarosa.core.model.RangeQuestion;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.listeners.WidgetValueChangedListener;
import org.robolectric.RobolectricTestRunner;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.utilities.WidgetAppearanceUtils.NO_TICKS_APPEARANCE;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.mockValueChangedListener;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithQuestionDefAndAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnlyAndQuestionDef;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;

@RunWith(RobolectricTestRunner.class)
public class RangeIntegerWidgetTest {
    private final RangeQuestion rangeQuestion = mock(RangeQuestion.class);
    private final MotionEvent motionEvent = MotionEventBuilder.newBuilder().build();

    @Before
    public void setup() {
        when(rangeQuestion.getRangeStart()).thenReturn(BigDecimal.ONE);
        when(rangeQuestion.getRangeEnd()).thenReturn(BigDecimal.TEN);
        when(rangeQuestion.getRangeStep()).thenReturn(BigDecimal.ONE);

        motionEvent.setAction(MotionEvent.ACTION_DOWN);
        motionEvent.setLocation(50, 0);
    }

    @Test
    public void getAnswer_whenPromptDoesNotHaveAnswer_returnsNull() {
        assertNull(createWidget(promptWithReadOnlyAndQuestionDef(rangeQuestion)).getAnswer());
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsAnswer() {
        RangeIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("4")));
        assertEquals(widget.getAnswer().getValue(), 4);
    }

    @Test
    public void whenWidgetIsReadOnly_sliderIsDisabled() {
        assertThat(createWidget(promptWithReadOnlyAndQuestionDef(rangeQuestion)).slider.isEnabled(), equalTo(false));
    }

    @Test
    public void whenWidgetIsInvalid_sliderIsDisabled() {
        when(rangeQuestion.getRangeStep()).thenReturn(BigDecimal.valueOf(3));
        assertThat(createWidget(promptWithReadOnlyAndQuestionDef(rangeQuestion)).slider.isEnabled(), equalTo(false));
    }

    @Test
    public void whenRangeStepIsZero_sliderIsDisabled() {
        when(rangeQuestion.getRangeStep()).thenReturn(BigDecimal.ZERO);
        assertThat(createWidget(promptWithReadOnlyAndQuestionDef(rangeQuestion)).slider.isEnabled(), equalTo(false));
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_sliderShowsNoAnswerMarked() {
        RangeIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        assertThat(widget.slider.getValue(), equalTo(1.0F));
        assertThat(widget.slider.getThumbRadius(), equalTo(0));
    }

    @Test
    public void whenPromptHasAnswer_sliderShowsCorrectAnswer() {
        RangeIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("4")));
        assertThat(widget.slider.getValue(), equalTo(4.0F));
        assertThat(widget.slider.getThumbRadius(), not(0));
    }

    @Test
    public void whenPromptHasAnswer_sliderShowsCorrectAnswer_whenStartIsGreaterThanEnd() {
        when(rangeQuestion.getRangeStart()).thenReturn(BigDecimal.TEN);
        when(rangeQuestion.getRangeEnd()).thenReturn(BigDecimal.ONE);

        RangeIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("4")));
        assertThat(widget.slider.getValue(), equalTo(7.0F));
        assertThat(widget.slider.getThumbRadius(), not(0));
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_widgetShowsNullAnswer() {
        RangeIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        assertThat(widget.currentValue.getText(), equalTo(""));
    }

    @Test
    public void whenPromptHasAnswer_widgetShouldShowCorrectAnswer() {
        RangeIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("4")));
        assertThat(widget.currentValue.getText(), equalTo("4"));
    }

    @Test
    public void widgetDisplaysCorrectStartAndEndValues() {
        RangeIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        assertThat(widget.minValue.getText(), equalTo("1"));
        assertThat(widget.maxValue.getText(), equalTo("10"));
    }

    @Test
    public void whenSliderIsDiscrete_widgetSetsUpSliderCorrectly() {
        RangeIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("4")));

        assertThat(widget.slider.getValueFrom(), equalTo(1.0F));
        assertThat(widget.slider.getValueTo(), equalTo(10.0F));
        assertThat(widget.slider.getStepSize(), equalTo(1.0F));
        assertThat(widget.slider.getValue(), equalTo(4.0F));
    }

    @Test
    public void whenSliderIsContinuous_widgetSetsUpSliderCorrectly() {
        when(rangeQuestion.getAppearanceAttr()).thenReturn(NO_TICKS_APPEARANCE);
        RangeIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("4")));

        assertThat(widget.slider.getValueFrom(), equalTo(1.0F));
        assertThat(widget.slider.getValueTo(), equalTo(10.0F));
        assertThat(widget.slider.getStepSize(), equalTo(0.0F));
        assertThat(widget.slider.getValue(), equalTo(4.0F));
    }

    @Test
    public void clearAnswer_clearsWidgetAnswer() {
        RangeIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("4")));
        widget.clearAnswer();
        assertThat(widget.currentValue.getText(), equalTo(""));
    }

    @Test
    public void clearAnswer_hidesSliderThumb() {
        RangeIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("2.5")));
        widget.clearAnswer();
        assertThat(widget.slider.getThumbRadius(), equalTo(0));
    }

    @Test
    public void clearAnswer_callsValueChangeListener() {
        RangeIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.clearAnswer();
        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void changingSliderValue_whenStartIsSmallerThanEnd_updatesWidgetAnswer() {
        RangeIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        widget.slider.onTouchEvent(motionEvent);
        assertThat(widget.getAnswer().getDisplayText(), equalTo("10"));
    }

    @Test
    public void changingSliderValue_whenStartIsGreaterThanEnd_updatesWidgetAnswer() {
        when(rangeQuestion.getRangeStart()).thenReturn(BigDecimal.TEN);
        when(rangeQuestion.getRangeEnd()).thenReturn(BigDecimal.ONE);

        RangeIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        widget.slider.onTouchEvent(motionEvent);
        assertThat(widget.getAnswer().getDisplayText(), equalTo("1"));
    }

    @Test
    public void changingSliderValue_showsSliderThumb() {
        RangeIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        widget.slider.onTouchEvent(motionEvent);
        assertThat(widget.slider.getThumbRadius(), not(0));
    }

    @Test
    public void changingSliderValue_whenStartIsSmallerThanEnd_updatesDisplayedAnswer() {
        RangeIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        widget.slider.onTouchEvent(motionEvent);
        assertThat(widget.slider.getValue(), equalTo(10.0F));
        assertThat(widget.currentValue.getText(), equalTo("10"));
    }

    @Test
    public void changingSliderValue_whenStartIsGreaterThanEnd_updatesDisplayedAnswer() {
        when(rangeQuestion.getRangeStart()).thenReturn(BigDecimal.TEN);
        when(rangeQuestion.getRangeEnd()).thenReturn(BigDecimal.ONE);

        RangeIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        widget.slider.onTouchEvent(motionEvent);
        assertThat(widget.slider.getValue(), equalTo(10.0F));
        assertThat(widget.currentValue.getText(), equalTo("1"));
    }

    @Test
    public void changingSliderValue_callsValueChangeListener() {
        RangeIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.slider.onTouchEvent(motionEvent);
        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void clickingSliderForLong_doesNotCallLongClickListener() {
        View.OnLongClickListener listener = mock(View.OnLongClickListener.class);
        RangeIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        widget.setOnLongClickListener(listener);
        widget.slider.performLongClick();
        verify(listener, never()).onLongClick(widget.slider);
    }

    private RangeIntegerWidget createWidget(FormEntryPrompt prompt) {
        return new RangeIntegerWidget(widgetTestActivity(), new QuestionDetails(prompt, "formAnalyticsID"));
    }
}
