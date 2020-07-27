package org.odk.collect.android.widgets;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.adapters.AbstractSelectListAdapter;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.databinding.SelectMinimalWidgetAnswerBinding;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.utilities.FormEntryPromptUtils;
import org.odk.collect.android.utilities.QuestionFontSizeUtils;
import org.odk.collect.android.widgets.interfaces.BinaryDataReceiver;
import org.odk.collect.android.widgets.interfaces.MultiChoiceWidget;

import java.util.List;

public abstract class SelectMinimalWidget extends ItemsWidget implements BinaryDataReceiver, MultiChoiceWidget {
    SelectMinimalWidgetAnswerBinding binding;

    public SelectMinimalWidget(Context context, QuestionDetails prompt) {
        super(context, prompt);
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        binding = SelectMinimalWidgetAnswerBinding.inflate(((Activity) context).getLayoutInflater());
        binding.choicesSearchBox.setTextSize(QuestionFontSizeUtils.getQuestionFontSize());
        if (prompt.isReadOnly()) {
            binding.choicesSearchBox.setEnabled(false);
        } else {
            binding.choicesSearchBox.setOnClickListener(v -> {
                FormController formController = Collect.getInstance().getFormController();
                if (formController != null) {
                    formController.setIndexWaitingForData(getFormEntryPrompt().getIndex());
                }
                showDialog();
            });
        }
        return binding.getRoot();
    }

    @Override
    public void clearAnswer() {
        getAdapter().clearAnswer();
        binding.choicesSearchBox.setText(R.string.select_answer);
        widgetValueChanged();
    }

    @Override
    public void setBinaryData(Object answer) {
        getAdapter().updateSelectedItems((List<Selection>) answer);
        updateAnswer();
    }

    @Override
    public int getChoiceCount() {
        return getAdapter().getItemCount();
    }

    protected abstract AbstractSelectListAdapter getAdapter();

    protected abstract void showDialog();

    void updateAnswer() {
        List<Selection> selectedItems = getAdapter().getSelectedItems();
        if (selectedItems != null) {
            if (selectedItems.isEmpty()) {
                binding.choicesSearchBox.setText(R.string.select_answer);
            } else {
                StringBuilder builder = new StringBuilder();
                for (Selection selectedItem : selectedItems) {
                    builder.append(FormEntryPromptUtils.getItemText(getFormEntryPrompt(), selectedItem));
                    if (selectedItems.size() - 1 > selectedItems.indexOf(selectedItem)) {
                        builder.append(", ");
                    }
                }
                binding.choicesSearchBox.setText(builder.toString());
            }
        }
    }
}