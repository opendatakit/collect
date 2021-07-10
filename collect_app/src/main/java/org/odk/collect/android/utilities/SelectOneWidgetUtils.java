package org.odk.collect.android.utilities;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryPrompt;
import org.jetbrains.annotations.Nullable;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.exception.JavaRosaException;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.widgets.items.ItemsWidget;
import org.odk.collect.android.widgets.items.SelectOneMinimalWidget;
import org.odk.collect.android.widgets.items.SelectOneWidget;

import java.util.List;
import java.util.function.Supplier;

import timber.log.Timber;

public class SelectOneWidgetUtils {

    private SelectOneWidgetUtils() {

    }

    public static void checkFastExternalCascade(ItemsWidget caller) {
        //Exclude untested usage
        if (!(caller instanceof SelectOneWidget || caller instanceof SelectOneMinimalWidget)) {
            throw new IllegalArgumentException("This method is only tested for calls from " +
                    SelectOneWidget.class.getSimpleName() + " or " + SelectOneMinimalWidget.class.getSimpleName());
        }

        FormController fc = Collect.getInstance().getFormController();
        if (fc == null //Impossible?
                || fc.indexIsInFieldList(fc.getFormIndex()) //In field list?
                || fc.getQuestionPrompt() == null) { //In unit test?
            return;
        }

        //Mini method
        Supplier<String> getCheckName = () -> {
            String raw = fc
                    .getQuestionPrompt()
                    .getFormElement()
                    .getBind().getReference().toString();
            return raw.replaceAll(".+/([^/]+)$", "$1");
        };

        try {
            //Remember where we started
            FormIndex startIndex = fc.getFormIndex();

            //To search for in query string
            String checkName = getCheckName.get();

            //Loop until non-question
            while (true) {
                int event = fc.stepToNextScreenEvent();
                if (event != FormEntryController.EVENT_QUESTION) {
                    break;
                }

                //Next question
                FormEntryPrompt question = fc.getQuestionPrompt();

                //Skip if not FEI…
                String query = question.getFormElement()
                        .getAdditionalAttribute(null, "query");
                if (query == null) {
                    continue;
                }

                //…or no match
                if (!query.matches(".*\\b" + checkName + "\\b.*")) {
                    continue;
                }

                //Otherwise reset
                fc.saveAnswer(question.getIndex(), null);

                //Prepare to move down cascade
                checkName = getCheckName.get();
            }

            //Back to start
            fc.jumpToIndex(startIndex);

        } catch (JavaRosaException e) {
            Timber.d(e);
        }
    }

    public static void checkFastExternalCascadeInFieldList(FormIndex lastChangedIndex,
                                                           FormEntryPrompt[] questionsAfterSave) {
        FormController fc = Collect.getInstance().getFormController();
        //Formality
        if (fc == null) {
            return;
        }
        //Find the index in the field list, get its form label
        FormIndex seekIndex = lastChangedIndex;
        FormIndex nextLevel;
        int offset = 1;
        for (; (nextLevel = seekIndex.getNextLevel()) != null; offset++) {
            seekIndex = nextLevel;
        }
        String matchIndexLabel = "(\\w+) .+";
        String checkName = seekIndex.getReference().getSubReference(offset).toShortString()
                .replaceAll(matchIndexLabel, "$1");

        //Check each current question in turn
        for (FormEntryPrompt question : questionsAfterSave) {

            //FEI?
            String query = question.getFormElement().getAdditionalAttribute(null, "query");
            if (query == null) {
                continue;
            }

            //Next cascade member?
            boolean matches = query.matches(".*\\b" + checkName + "\\b.*");
            if (!matches) {
                continue;
            }

            //Reset it
            try {
                fc.saveAnswer(question.getIndex(), null);
            } catch (JavaRosaException e) {
                Timber.d(e);
            }

            //Ready for next question
            String matchQuestionLabel = ".+/([^/]+)$";
            checkName = question.getQuestion().getBind().getReference().toString().replaceAll(matchQuestionLabel, "$1");
        }
    }

    public static @Nullable Selection getSelectedItem(FormEntryPrompt prompt, List<SelectChoice> items) {
        IAnswerData answer = prompt.getAnswerValue();
        if (answer == null) {
            return null;
        } else if (answer instanceof SelectOneData) {
            return (Selection) answer.getValue();
        } else if (answer instanceof StringData) { // Fast external itemset
            for (SelectChoice item : items) {
                if (answer.getValue().equals(item.selection().xmlValue)) {
                    return item.selection();
                }
            }
            return null;
        }
        return null;
    }
}
