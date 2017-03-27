/*
 * Copyright (C) 2009 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.widgets;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import org.javarosa.core.model.data.DateTimeData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.joda.time.LocalDateTime;
/**
 * Displays a DatePicker widget. DateWidget handles leap years and does not allow dates that do not
 * exist.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class DateTimeWidget extends QuestionWidget {

    private DateWidget mDateWidget;
    private TimeWidget mTimeWidget;

    public DateTimeWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        mDateWidget = new DateWidget(context, prompt);
        mTimeWidget= new TimeWidget(context, prompt);

        mDateWidget.mQuestionMediaLayout.getView_Text().setVisibility(GONE);
        mTimeWidget.mQuestionMediaLayout.getView_Text().setVisibility(GONE);

        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(mDateWidget);
        linearLayout.addView(mTimeWidget);
        addAnswerView(linearLayout);
    }

    @Override
    public IAnswerData getAnswer() {
        clearFocus();

        boolean showCalendar = mDateWidget.isCalendarShown();
        boolean hideDay = mDateWidget.isDayHidden();
        boolean hideMonth = mDateWidget.isMonthHidden();

        int year = mDateWidget.getYear();
        int month = mDateWidget.getMonth();
        int day = mDateWidget.getDay();
        int hour = mTimeWidget.getHour();
        int minute = mTimeWidget.getMinute();

        LocalDateTime ldt = new LocalDateTime()
                .withYear(year)
                .withMonthOfYear((!showCalendar && hideMonth) ? 1 : month)
                .withDayOfMonth((!showCalendar && (hideMonth || hideDay)) ? 1 : day)
                .withHourOfDay((!showCalendar && (hideMonth || hideDay)) ? 0 : hour)
                .withMinuteOfHour((!showCalendar && (hideMonth || hideDay)) ? 0 : minute)
                .withSecondOfMinute(0);

        ldt = skipDaylightSavingGapIfExists(ldt);
        return new DateTimeData(ldt.toDate());
    }

    @Override
    public void clearAnswer() {
        mDateWidget.clearAnswer();
        mTimeWidget.clearAnswer();
    }

    @Override
    public void setFocus(Context context) {
        // Hide the soft keyboard if it's showing.
        InputMethodManager inputManager =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        mDateWidget.setOnLongClickListener(l);
        mTimeWidget.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        mDateWidget.cancelLongPress();
        mTimeWidget.cancelLongPress();
    }
}
