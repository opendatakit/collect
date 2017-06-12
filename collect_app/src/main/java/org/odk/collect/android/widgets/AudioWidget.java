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

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore.Audio;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.MediaUtils;

import java.io.File;

import timber.log.Timber;

/**
 * Widget that allows user to take pictures, sounds or video and add them to the
 * form.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */

public class AudioWidget extends QuestionWidget implements IBinaryWidget {

    CustomMediaController mediaController;
    private Button captureButton;
    private Button chooseButton;
    private String binaryName;
    private String instanceFolder;
    private RelativeLayout mediaPlayerLayout;

    public AudioWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        instanceFolder = Collect.getInstance().getFormController()
                .getInstancePath().getParent();

        mediaController = new CustomMediaController(context, player, formEntryPrompt);

        initLayout(context);

        // setup capture button
        captureButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontsize);
        captureButton.setEnabled(!prompt.isReadOnly());

        // launch capture intent on click
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collect.getInstance()
                        .getActivityLogger()
                        .logInstanceAction(this, "captureButton", "click",
                                formEntryPrompt.getIndex());
                Intent i = new Intent(
                        android.provider.MediaStore.Audio.Media.RECORD_SOUND_ACTION);
                i.putExtra(
                        android.provider.MediaStore.EXTRA_OUTPUT,
                        android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                                .toString());
                try {
                    Collect.getInstance().getFormController()
                            .setIndexWaitingForData(formEntryPrompt.getIndex());
                    ((Activity) getContext()).startActivityForResult(i,
                            FormEntryActivity.AUDIO_CAPTURE);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(
                            getContext(),
                            getContext().getString(R.string.activity_not_found,
                                    "audio capture"), Toast.LENGTH_SHORT)
                            .show();
                    Collect.getInstance().getFormController()
                            .setIndexWaitingForData(null);
                }

            }
        });

        // setup capture button
        chooseButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontsize);
        chooseButton.setEnabled(!prompt.isReadOnly());

        // launch capture intent on click
        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collect.getInstance()
                        .getActivityLogger()
                        .logInstanceAction(this, "chooseButton", "click",
                                formEntryPrompt.getIndex());
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("audio/*");
                try {
                    Collect.getInstance().getFormController()
                            .setIndexWaitingForData(formEntryPrompt.getIndex());
                    ((Activity) getContext()).startActivityForResult(i,
                            FormEntryActivity.AUDIO_CHOOSER);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(
                            getContext(),
                            getContext().getString(R.string.activity_not_found,
                                    "choose audio"), Toast.LENGTH_SHORT).show();
                    Collect.getInstance().getFormController()
                            .setIndexWaitingForData(null);
                }

            }
        });

        // retrieve answer from data model and update ui
        binaryName = prompt.getAnswerText();
        if (binaryName != null) {
            mediaPlayerLayout.setVisibility(VISIBLE);
            addMediaToPlayer();
        } else {
            mediaPlayerLayout.setVisibility(GONE);
        }

        // and hide the capture and choose button if read-only
        if (formEntryPrompt.isReadOnly()) {
            captureButton.setVisibility(View.GONE);
            chooseButton.setVisibility(View.GONE);
        }
    }

    private void initLayout(Context context) {
        View answerLayout = inflate(context, R.layout.audio_widget_layout, null);

        mediaPlayerLayout = (RelativeLayout) answerLayout.findViewById(R.id.mediaPlayer);
        captureButton = (Button) answerLayout.findViewById(R.id.recordBtn);
        chooseButton = (Button) answerLayout.findViewById(R.id.chooseBtn);

        //initialize media player controls
        mediaController.initLayout(answerLayout);

        // finish complex layout
        addAnswerView(answerLayout);
    }

    private void deleteMedia() {
        // get the file path and delete the file
        String name = binaryName;
        // clean up variables
        binaryName = null;
        // delete from media provider
        int del = MediaUtils.deleteAudioFileFromMediaProvider(
                instanceFolder + File.separator + name);
        Timber.i("Deleted %d rows from media content provider", del);
    }

    @Override
    public void clearAnswer() {
        // remove the file
        deleteMedia();

        // reset buttons
        mediaPlayerLayout.setVisibility(GONE);
    }

    @Override
    public IAnswerData getAnswer() {
        if (binaryName != null) {
            return new StringData(binaryName);
        } else {
            return null;
        }
    }

    @Override
    public void setBinaryData(Object binaryuri) {

        // get the file path and create a copy in the instance folder
        String binaryPath = MediaUtils.getPathFromUri(this.getContext(), (Uri) binaryuri,
                Audio.Media.DATA);
        String extension = binaryPath.substring(binaryPath.lastIndexOf("."));
        String destAudioPath = instanceFolder + File.separator
                + System.currentTimeMillis() + extension;

        File source = new File(binaryPath);
        File newAudio = new File(destAudioPath);
        FileUtils.copyFile(source, newAudio);

        if (newAudio.exists()) {
            // Add the copy to the content provider
            ContentValues values = new ContentValues(6);
            values.put(Audio.Media.TITLE, newAudio.getName());
            values.put(Audio.Media.DISPLAY_NAME, newAudio.getName());
            values.put(Audio.Media.DATE_ADDED, System.currentTimeMillis());
            values.put(Audio.Media.DATA, newAudio.getAbsolutePath());

            Uri audioURI = getContext().getContentResolver().insert(
                    Audio.Media.EXTERNAL_CONTENT_URI, values);
            Timber.i("Inserting AUDIO returned uri = %s", audioURI.toString());
            // when replacing an answer. remove the current media.
            if (binaryName != null && !binaryName.equals(newAudio.getName())) {
                deleteMedia();
            }
            binaryName = newAudio.getName();
            addMediaToPlayer();
            Timber.i("Setting current answer to %s", newAudio.getName());
        } else {
            Timber.e("Inserting Audio file FAILED");
        }

        Collect.getInstance().getFormController().setIndexWaitingForData(null);
    }

    @Override
    public void setFocus(Context context) {
        // Hide the soft keyboard if it's showing.
        InputMethodManager inputManager = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
    }

    @Override
    public boolean isWaitingForBinaryData() {
        return formEntryPrompt.getIndex().equals(
                Collect.getInstance().getFormController()
                        .getIndexWaitingForData());
    }

    @Override
    public void cancelWaitingForBinaryData() {
        Collect.getInstance().getFormController().setIndexWaitingForData(null);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        captureButton.setOnLongClickListener(l);
        chooseButton.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        captureButton.cancelLongPress();
        chooseButton.cancelLongPress();
    }

    private void addMediaToPlayer() {
        File f = new File(instanceFolder + File.separator + binaryName);
        mediaController.setMedia(f);
    }
}
