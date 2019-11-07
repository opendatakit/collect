package org.odk.collect.android.widgets;

import java.io.File;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatRadioButton;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryCaption;
import org.odk.collect.android.R;
import org.odk.collect.android.external.ExternalSelectChoice;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.ViewIds;

import timber.log.Timber;

@SuppressLint("ViewConstructor")
public class LikertWidget extends ItemsWidget {

    private LinearLayout view;
    private RadioButton checkedButton;
    private final LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1);
    private final LayoutParams textViewParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    private final LayoutParams imageViewParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    private final LayoutParams radioButtonsParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    private final LayoutParams buttonViewParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    private final LayoutParams leftLineViewParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2);
    private final LayoutParams rightLineViewParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2);

    HashMap<RadioButton, String> buttonsToName;

    public LikertWidget(Context context, QuestionDetails questionDetails) {
        super(context, questionDetails);

        setMainViewLayoutParameters();
        setStructures();

        setButtonListener();
        setSavedButton();
        addAnswerView(view);
    }

    public void setMainViewLayoutParameters() {
        view = new LinearLayout(getContext());
        view.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(params);
    }

    // Inserts the selected button from a saved state
    public void setSavedButton() {
        if (getFormEntryPrompt().getAnswerValue() != null) {
            String name = ((Selection) getFormEntryPrompt().getAnswerValue()
                    .getValue()).getValue();
            for (RadioButton bu: buttonsToName.keySet()) {
                if (buttonsToName.get(bu).equals(name)) {
                    checkedButton = bu;
                    checkedButton.setChecked(true);
                }
            }
        }
    }

    @Override
    public IAnswerData getAnswer() {
        if (checkedButton == null) {
            return null;
        } else {
            int selectedIndex = -1;
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).getValue().equals(buttonsToName.get(checkedButton))) {
                    selectedIndex = i;
                }
            }
            if (selectedIndex == -1) {
                return null;
            }
            SelectChoice sc = items.get(selectedIndex);
            return new SelectOneData(new Selection(sc));
        }
    }

    /**
     * Default place to put the answer
     * (below the help text or question text if there is no help text)
     * If you have many elements, use this first
     * and use the standard addView(view, params) to place the rest
     */
    protected void addAnswerView(View v) {
        if (v == null) {
            Timber.e("cannot add a null view as an answerView");
            return;
        }
        // default place to add answer
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.BELOW, getHelpTextLayout().getId());
        params.setMargins(10, 0, 10, 0);
        addView(v, params);
    }

    public void setStructures() {
        buttonsToName = new HashMap<>();
        for (int i = 0; i < items.size(); i++) {
            RelativeLayout buttonView = new RelativeLayout(this.getContext());
            buttonViewParams.addRule(CENTER_IN_PARENT, TRUE);
            buttonView.setLayoutParams(buttonViewParams);

            RadioButton button = getRadioButton(i);

            buttonsToName.put(button, items.get(i).getValue());
            buttonView.addView(button);

            if (i == 0) {
                addLine(true, false, button, buttonView);
            } else if (i == items.size() - 1) {
                addLine(false, true, button, buttonView);
            } else {
                addLine(false, false, button, buttonView);
            }

            LinearLayout optionView = getLinearLayout();
            optionView.addView(buttonView);

            ImageView imgView = getImageAsImageView(i);
            // checks if image is set or valid
            if (imgView != null) {
                optionView.addView(imgView);
            }
            TextView choice = getTextView();
            choice.setText(getFormEntryPrompt().getSelectChoiceText(items.get(i)));

            optionView.addView(choice);
            view.addView(optionView);
        }
    }

    // Adds lines to the button's side
    public void addLine(boolean left, boolean right, RadioButton button, RelativeLayout buttonView) {
        // left line
        View leftLineView = new View(this.getContext());
        leftLineViewParams.addRule(RelativeLayout.LEFT_OF, button.getId());
        leftLineViewParams.addRule(CENTER_IN_PARENT, TRUE);
        leftLineView.setLayoutParams(leftLineViewParams);
        leftLineView.setBackgroundColor(getResources().getColor(R.color.gray600));
        if (left) {
            leftLineView.setBackgroundColor(getResources().getColor(R.color.white));
        }
        buttonView.addView(leftLineView);

        // right line
        View rightLineView = new View(this.getContext());
        rightLineViewParams.addRule(RelativeLayout.RIGHT_OF, button.getId());
        rightLineViewParams.addRule(CENTER_IN_PARENT, TRUE);
        rightLineView.setLayoutParams(rightLineViewParams);
        rightLineView.setBackgroundColor(getResources().getColor(R.color.gray600));

        if (right) {
            rightLineView.setBackgroundColor(getResources().getColor(R.color.white));
        }
        buttonView.addView(rightLineView);
    }

    // Creates image view for choice
    public ImageView getImageView() {
        ImageView view = new ImageView(getContext());
        view.setLayoutParams(imageViewParams);
        return view;
    }

    public RadioButton getRadioButton(int i) {
        AppCompatRadioButton button = new AppCompatRadioButton(getContext());
        button.setId(ViewIds.generateViewId());
        button.setTag(i);
        button.setEnabled(!getFormEntryPrompt().isReadOnly());
        button.setFocusable(!getFormEntryPrompt().isReadOnly());
        radioButtonsParams.addRule(CENTER_HORIZONTAL, TRUE);
        button.setLayoutParams(radioButtonsParams);
        // This the adds the negated margins to reduce the extra padding of the button.
        // It is done this way to get the width of the button which has to be done after rendering
        ViewTreeObserver vto = button.getViewTreeObserver();
        // This variable is to prevent an infinite loop for rendering the button.
        final Boolean[] paramsSet = {false};
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (!paramsSet[0]) {
                    int width = button.getWidth();
                    radioButtonsParams.setMargins(-width / 5, 0, -width / 5, 0);
                    button.setLayoutParams(radioButtonsParams);
                    paramsSet[0] = true;
                }
            }
        });
        button.setGravity(Gravity.CENTER);
        return button;
    }

    // Creates text view for choice
    public TextView getTextView() {
        TextView view = new TextView(getContext());
        view.setGravity(Gravity.CENTER);
        view.setPadding(2, 2, 2, 2);
        view.setLayoutParams(textViewParams);
        view.setTextColor(Color.BLACK);
        return view;
    }

    // Linear Layout for new choice
    public LinearLayout getLinearLayout() {
        LinearLayout optionView = new LinearLayout(getContext());
        optionView.setGravity(Gravity.CENTER);
        optionView.setLayoutParams(linearLayoutParams);
        optionView.setOrientation(LinearLayout.VERTICAL);
        return optionView;
    }

    public void setButtonListener() {
        for (RadioButton button: buttonsToName.keySet()) {
            button.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    RadioButton r = (RadioButton) v;
                    if (checkedButton != null) {
                        checkedButton.setChecked(false);
                    }
                    checkedButton = r;
                    checkedButton.setChecked(true);
                }
            });
        }
    }

    public ImageView getImageAsImageView(int index) {
        ImageView view = getImageView();
        view.setTag(index);
        String imageURI;
        if (items.get(index) instanceof ExternalSelectChoice) {
            imageURI = ((ExternalSelectChoice) items.get(index)).getImage();
        } else {
            imageURI = getFormEntryPrompt().getSpecialFormSelectChoiceText(items.get(index),
                    FormEntryCaption.TEXT_FORM_IMAGE);
        }
        if (imageURI != null) {
            String error = setImageFromOtherSource(imageURI, view);
            if (error != null) {
                return null;
            }
            return view;
        } else {
            return null;
        }
    }

    public String setImageFromOtherSource(String imageURI, ImageView imageView) {
        String errorMsg = null;
        try {
            String imageFilename =
                    ReferenceManager.instance().DeriveReference(imageURI).getLocalURI();
            final File imageFile = new File(imageFilename);
            if (imageFile.exists()) {
                Bitmap b = null;
                try {
                    DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
                    int screenWidth = metrics.widthPixels;
                    int screenHeight = metrics.heightPixels;
                    b = FileUtils.getBitmapScaledToDisplay(imageFile, screenHeight, screenWidth);
                } catch (OutOfMemoryError e) {
                    errorMsg = "ERROR: " + e.getMessage();
                }

                if (b != null) {
                    imageView.setAdjustViewBounds(true);
                    imageView.setImageBitmap(b);
                } else if (errorMsg == null) {
                    // Loading the image failed. The image work when in .jpg format
                    errorMsg = getContext().getString(R.string.file_invalid, imageFile);

                }
            } else {
                errorMsg = getContext().getString(R.string.file_missing, imageFile);
            }
            if (errorMsg != null) {
                Timber.e(errorMsg);
            }

        } catch (InvalidReferenceException e) {
            Timber.e(e, "Invalid image reference due to %s ", e.getMessage());
        }
        return errorMsg;
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        for (RadioButton r : buttonsToName.keySet()) {
            r.setOnLongClickListener(l);
        }
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        for (RadioButton r : buttonsToName.keySet()) {
            r.cancelLongPress();
        }
    }

    @Override
    public void clearAnswer() {
        if (checkedButton != null) {
            checkedButton.setChecked(false);
        }
        checkedButton = null;
    }
}
