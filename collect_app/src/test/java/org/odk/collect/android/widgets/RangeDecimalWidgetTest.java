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
import static org.hamcrest.Matchers.nullValue;
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
public class RangeDecimalWidgetTest {
    private final RangeQuestion rangeQuestion = mock(RangeQuestion.class);
    private final MotionEvent motionEvent = MotionEventBuilder.newBuilder().build();

    @Before
    public void setup() {
        when(rangeQuestion.getRangeStart()).thenReturn(BigDecimal.valueOf(1.5));
        when(rangeQuestion.getRangeEnd()).thenReturn(BigDecimal.valueOf(5.5));
        when(rangeQuestion.getRangeStep()).thenReturn(BigDecimal.valueOf(0.5));

        motionEvent.setAction(MotionEvent.ACTION_DOWN);
        motionEvent.setLocation(50, 0);
    }

    @Test
    public void getAnswer_whenPromptDoesNotHaveAnswer_returnsNull() {
        assertThat(createWidget(promptWithReadOnlyAndQuestionDef(rangeQuestion)).getAnswer(), nullValue());
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsAnswer() {
        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("2.5")));
        assertThat(widget.getAnswer().getValue(), equalTo(2.5));
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
        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        assertThat(widget.slider.getValue(), equalTo(1.5F));
        assertThat(widget.slider.getThumbRadius(), equalTo(0));
    }

    @Test
    public void whenPromptHasAnswer_sliderShowsCorrectAnswer() {
        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("2.5")));
        assertThat(widget.slider.getValue(), equalTo(2.5F));
        assertThat(widget.slider.getThumbRadius(), not(0));
    }

    @Test
    public void whenPromptHasAnswer_sliderShowsCorrectAnswer_whenStartIsGreaterThanEnd() {
        when(rangeQuestion.getRangeEnd()).thenReturn(BigDecimal.valueOf(1.5));
        when(rangeQuestion.getRangeStart()).thenReturn(BigDecimal.valueOf(5.5));

        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("2.5")));
        assertThat(widget.slider.getValue(), equalTo(4.5F));
        assertThat(widget.slider.getThumbRadius(), not(0));
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_widgetShouldShowNullAnswer() {
        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        assertThat(widget.currentValue.getText(), equalTo(""));
    }

    @Test
    public void whenPromptHasAnswer_widgetShouldShowCorrectAnswer() {
        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("2.5")));
        assertThat(widget.currentValue.getText(), equalTo("2.5"));
    }

    @Test
    public void widgetDisplaysCorrectStartAndEndValues() {
        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        assertThat(widget.minValue.getText(), equalTo("1.5"));
        assertThat(widget.maxValue.getText(), equalTo("5.5"));
    }

    @Test
    public void whenSliderIsDiscrete_widgetShowsCorrectSlider() {
        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("2.5")));

        assertThat(widget.slider.getValueFrom(), equalTo(1.5F));
        assertThat(widget.slider.getValueTo(), equalTo(5.5F));
        assertThat(widget.slider.getStepSize(), equalTo(0.5F));
        assertThat(widget.slider.getValue(), equalTo(2.5F));
    }

    @Test
    public void whenSliderIsContinuous_widgetShowsCorrectSlider() {
        when(rangeQuestion.getAppearanceAttr()).thenReturn(NO_TICKS_APPEARANCE);
        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("2.5")));

        assertThat(widget.slider.getValueFrom(), equalTo(1.5F));
        assertThat(widget.slider.getValueTo(), equalTo(5.5F));
        assertThat(widget.slider.getStepSize(), equalTo(0.0F));
        assertThat(widget.slider.getValue(), equalTo(2.5F));
    }

    @Test
    public void clearAnswer_clearsWidgetAnswer() {
        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("2.5")));
        widget.clearAnswer();
        assertThat(widget.currentValue.getText(), equalTo(""));
    }

    @Test
    public void clearAnswer_hidesSliderThumb() {
        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("2.5")));
        widget.clearAnswer();
        assertThat(widget.slider.getThumbRadius(), equalTo(0));
    }

    @Test
    public void clearAnswer_callsValueChangeListener() {
        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("2.5")));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.clearAnswer();
        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void changingSliderValue_updatesAnswer() {
        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        widget.slider.onTouchEvent(motionEvent);
        assertThat(widget.currentValue.getText(), equalTo("5.5"));
    }

    @Test
    public void changingSliderValue_showsSliderThumb() {
        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        widget.slider.onTouchEvent(motionEvent);
        assertThat(widget.slider.getThumbRadius(), not(0));
    }

    @Test
    public void changingSliderValue_whenStartIsSmallerThanEnd_updatesWidgetAnswer() {
        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        widget.slider.onTouchEvent(motionEvent);
        assertThat(widget.getAnswer().getDisplayText(), equalTo("5.5"));
    }

    @Test
    public void changingSliderValue_whenStartIsGreaterThanEnd_updatesAnswer() {
        when(rangeQuestion.getRangeStart()).thenReturn(BigDecimal.valueOf(5.5));
        when(rangeQuestion.getRangeEnd()).thenReturn(BigDecimal.valueOf(1.5));

        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        widget.slider.onTouchEvent(motionEvent);
        assertThat(widget.currentValue.getText(), equalTo("1.5"));
    }

    @Test
    public void changingSliderValue_callsValueChangeListener() {
        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.slider.onTouchEvent(motionEvent);
        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void clickingSliderForLong_doesNotCallLongClickListener() {
        View.OnLongClickListener listener = mock(View.OnLongClickListener.class);
        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        widget.setOnLongClickListener(listener);
        widget.slider.performLongClick();
        verify(listener, never()).onLongClick(widget.slider);
    }

    private RangeDecimalWidget createWidget(FormEntryPrompt prompt) {
        return new RangeDecimalWidget(widgetTestActivity(), new QuestionDetails(prompt, "formAnalyticsID"));
    }
}
