package org.odk.collect.android.widgets;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.widget.Button;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import org.junit.Before;
import org.mockito.Mock;
import org.odk.collect.android.R;
import org.odk.collect.android.utilities.FileUtil;
import org.odk.collect.android.utilities.MediaUtil;
import org.odk.collect.android.widgets.base.FileWidgetTest;

import java.io.File;

import static org.mockito.Mockito.when;

/**
 * @author James Knight
 */
public class VideoWidgetTest extends FileWidgetTest<VideoWidget> {

    @Mock
    Uri uri;

    @Mock
    MediaUtil mediaUtil;

    @Mock
    FileUtil fileUtil;

    @Mock
    File file;

    private String destinationName;

    @NonNull
    @Override
    public VideoWidget createWidget() {
        return new VideoWidget(activity, formEntryPrompt, fileUtil, mediaUtil);
    }

    @NonNull
    @Override
    public StringData getNextAnswer() {
        return new StringData(destinationName);
    }

    @Override
    public Object createBinaryData(StringData answerData) {
        return uri;
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        destinationName = RandomString.make();
    }

    @Override
    public void settingANewAnswerShouldCallDeleteMediaToRemoveTheOldFile() {
        prepareForSetAnswer();
        super.settingANewAnswerShouldCallDeleteMediaToRemoveTheOldFile();
    }

    @Override
    public void getAnswerShouldReturnCorrectAnswerAfterBeingSet() {
        prepareForSetAnswer();
        super.getAnswerShouldReturnCorrectAnswerAfterBeingSet();
    }

    @Override
    public void settingANewAnswerShouldRemoveTheOldAnswer() {
        prepareForSetAnswer();
        super.settingANewAnswerShouldRemoveTheOldAnswer();
    }

    public void prepareForSetAnswer() {
        when(formEntryPrompt.isReadOnly()).thenReturn(false);

        when(mediaUtil.getPathFromUri(
                activity,
                uri,
                MediaStore.Video.Media.DATA)

        ).thenReturn(String.format("%s.mp4", RandomString.make()));

        when(fileUtil.getRandomFilename()).thenReturn(destinationName);
        when(fileUtil.getFileAtPath(String.format("/%s.mp4", destinationName)))
                .thenReturn(file);

        when(file.getName()).thenReturn(destinationName);
    }

    @Override
    protected Intent getExpectedIntent(Button clickedButton, boolean permissionGranted) {
        Intent intent = null;
        switch (clickedButton.getId()) {
            case R.id.capture_video:
                if (permissionGranted) {
                    intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, MediaStore.Video.Media.EXTERNAL_CONTENT_URI.toString());
                    intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                }
                break;
            case R.id.choose_video:
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("video/*");
                break;
            case R.id.play_video:
                intent = new Intent("android.intent.action.VIEW");
                intent.setDataAndType(null, "video/*");
                break;
        }
        return intent;
    }
}