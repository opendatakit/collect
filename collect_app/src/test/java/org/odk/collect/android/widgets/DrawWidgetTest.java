package org.odk.collect.android.widgets;

import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.DrawActivity;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.utilities.QuestionMediaManager;
import org.odk.collect.android.widgets.base.FileWidgetTest;
import org.odk.collect.android.widgets.support.FakeQuestionMediaManager;
import org.odk.collect.android.widgets.support.FakeWaitingForDataRegistry;
import org.odk.collect.shared.TempFiles;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

/**
 * @author James Knight
 */
@RunWith(AndroidJUnit4.class)
public class DrawWidgetTest extends FileWidgetTest<DrawWidget> {

    //Package visibility for sharing with related tests
    static final String DEFAULT_IMAGE_ANSWER = "jr://images/referenceURI";
    static final String USER_SPECIFIED_IMAGE_ANSWER = "current.bmp";

    private File currentFile;

    @NonNull
    @Override
    public DrawWidget createWidget() {
        QuestionMediaManager fakeQuestionMediaManager = new FakeQuestionMediaManager() {
            @Override
            public File getAnswerFile(String fileName) {
                File result;
                if (currentFile == null) {
                    result = super.getAnswerFile(fileName);
                } else {
                    result = fileName.equals(USER_SPECIFIED_IMAGE_ANSWER) ? currentFile : null;
                }
                return result;
            }
        };
        return new DrawWidget(activity,
                new QuestionDetails(formEntryPrompt, "formAnalyticsID", readOnlyOverride),
                fakeQuestionMediaManager, new FakeWaitingForDataRegistry(), TempFiles.getPathInTempDir());
    }

    @NonNull
    @Override
    public StringData getNextAnswer() {
        return new StringData(RandomString.make());
    }

    @Test
    public void buttonsShouldLaunchCorrectIntents() {
        stubAllRuntimePermissionsGranted(true);

        Intent intent = getIntentLaunchedByClick(R.id.simple_button);
        assertComponentEquals(activity, DrawActivity.class, intent);
        assertExtraEquals(DrawActivity.OPTION, DrawActivity.OPTION_DRAW, intent);
    }

    @Test
    public void usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        when(formEntryPrompt.isReadOnly()).thenReturn(true);

        assertThat(getSpyWidget().drawButton.getVisibility(), is(View.GONE));
    }

    @Test
    public void whenReadOnlyOverrideOptionIsUsed_shouldAllClickableElementsBeDisabled() {
        readOnlyOverride = true;
        when(formEntryPrompt.isReadOnly()).thenReturn(false);

        assertThat(getSpyWidget().drawButton.getVisibility(), is(View.GONE));
    }

}
