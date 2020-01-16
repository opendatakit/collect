package org.odk.collect.android.formentry.repeats;

import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

import org.odk.collect.android.R;
import org.odk.collect.android.logic.FormController;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_POSITIVE;

public class AddRepeatDialog {

    private AddRepeatDialog() {}

    public static void show(Context context, FormController formController, Listener listener) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        DialogInterface.OnClickListener repeatListener = (dialog, i) -> {
            switch (i) {
                case BUTTON_POSITIVE: // yes, repeat
                    listener.onAddRepeatClicked();
                    break;
                case BUTTON_NEGATIVE: // no, no repeat
                    listener.onCancelClicked();
                    break;
            }
        };

        if (formController.getLastRepeatCount() > 0) {
            alertDialog.setTitle(context.getString(R.string.leaving_repeat_ask));
            alertDialog.setMessage(context.getString(R.string.add_another_repeat,
                    formController.getLastGroupText()));
            alertDialog.setButton(BUTTON_POSITIVE, context.getString(R.string.add_another),
                    repeatListener);
            alertDialog.setButton(BUTTON_NEGATIVE, context.getString(R.string.leave_repeat_yes),
                    repeatListener);

        } else {
            alertDialog.setTitle(context.getString(R.string.entering_repeat_ask));
            alertDialog.setMessage(context.getString(R.string.add_repeat,
                    formController.getLastGroupText()));
            alertDialog.setButton(BUTTON_POSITIVE, context.getString(R.string.entering_repeat),
                    repeatListener);
            alertDialog.setButton(BUTTON_NEGATIVE, context.getString(R.string.add_repeat_no),
                    repeatListener);
        }

        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    public interface Listener {
        void onAddRepeatClicked();

        void onCancelClicked();
    }
}
