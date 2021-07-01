/*
 * Copyright 2017 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.instrumented.utilities;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.TestSettingsProvider;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.injection.config.AppDependencyComponent;
import org.odk.collect.android.support.RunnableRule;
import org.odk.collect.projects.Project;
import org.odk.collect.shared.Settings;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.ImageConverter;
import org.odk.collect.android.widgets.ImageWidget;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.utilities.ApplicationConstants.Namespaces.XML_OPENROSA_NAMESPACE;

@RunWith(AndroidJUnit4.class)
public class ImageConverterTest {

    private static final String IMAGE_SIZE_ORIGINAL = "original_image_size";
    private static final String IMAGE_SIZE_LARGE = "large";
    private static final String IMAGE_SIZE_MEDIUM = "medium";
    private static final String IMAGE_SIZE_SMALL = "small";
    private static final String IMAGE_SIZE_VERY_SMALL = "very_small";

    private String testImagePath;
    private final Settings generalSettings = TestSettingsProvider.getGeneralSettings();

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(new ResetStateRule())
            .around(new RunnableRule(() -> {
                // Set up demo project
                AppDependencyComponent component = DaggerUtils.getComponent(ApplicationProvider.<Application>getApplicationContext());
                component.projectImporter().importNewProject(Project.Companion.getDEMO_PROJECT());
                component.currentProjectProvider().setCurrentProject(Project.DEMO_PROJECT_ID);
            }));

    @Before
    public void setUp() {
        testImagePath = new StoragePathProvider().getOdkDirPath(StorageSubdirectory.INSTANCES) + File.separator + "testForm_2017-10-12_19-36-15" + File.separator + "testImage.jpg";
        new File(testImagePath).getParentFile().mkdirs();
    }

    @Test
    public void executeConversionWithoutAnySettings() {
        generalSettings.save("image_size", "original_image_size");
        saveTestBitmap(3000, 3000);
        ImageConverter.execute(testImagePath, getTestImageWidget(), Collect.getInstance(), IMAGE_SIZE_ORIGINAL);

        Bitmap image = FileUtils.getBitmap(testImagePath, new BitmapFactory.Options());
        assertEquals(3000, image.getWidth());
        assertEquals(3000, image.getHeight());
    }

    @Test
    public void scaleImageDownFormLevelOnly1() {
        generalSettings.save("image_size", "original_image_size");
        saveTestBitmap(4000, 3000);
        ImageConverter.execute(testImagePath, getTestImageWidget(XML_OPENROSA_NAMESPACE, "max-pixels", "2000"), Collect.getInstance(), IMAGE_SIZE_ORIGINAL);

        Bitmap image = FileUtils.getBitmap(testImagePath, new BitmapFactory.Options());
        assertEquals(2000, image.getWidth());
        assertEquals(1500, image.getHeight());
    }

    @Test
    public void scaleImageDownFormLevelOnly2() {
        generalSettings.save("image_size", "original_image_size");
        saveTestBitmap(3000, 4000);
        ImageConverter.execute(testImagePath, getTestImageWidget(XML_OPENROSA_NAMESPACE, "max-pixels", "2000"), Collect.getInstance(), IMAGE_SIZE_ORIGINAL);

        Bitmap image = FileUtils.getBitmap(testImagePath, new BitmapFactory.Options());
        assertEquals(1500, image.getWidth());
        assertEquals(2000, image.getHeight());
    }

    @Test
    public void scaleImageDownFormLevelOnly3() {
        generalSettings.save("image_size", "original_image_size");
        saveTestBitmap(3000, 3000);
        ImageConverter.execute(testImagePath, getTestImageWidget(XML_OPENROSA_NAMESPACE, "max-pixels", "2000"), Collect.getInstance(), IMAGE_SIZE_ORIGINAL);

        Bitmap image = FileUtils.getBitmap(testImagePath, new BitmapFactory.Options());
        assertEquals(2000, image.getWidth());
        assertEquals(2000, image.getHeight());
    }

    @Test
    public void scaleImageDownFormLevelOnly4() {
        generalSettings.save("image_size", "original_image_size");
        saveTestBitmap(3000, 3000);
        ImageConverter.execute(testImagePath, getTestImageWidget(XML_OPENROSA_NAMESPACE, "max-pixels", "3000"), Collect.getInstance(), IMAGE_SIZE_ORIGINAL);

        Bitmap image = FileUtils.getBitmap(testImagePath, new BitmapFactory.Options());
        assertEquals(3000, image.getWidth());
        assertEquals(3000, image.getHeight());
    }

    @Test
    public void scaleImageDownFormLevelOnly5() {
        generalSettings.save("image_size", "original_image_size");
        saveTestBitmap(3000, 3000);
        ImageConverter.execute(testImagePath, getTestImageWidget(XML_OPENROSA_NAMESPACE, "max-pixels", "4000"), Collect.getInstance(), IMAGE_SIZE_ORIGINAL);

        Bitmap image = FileUtils.getBitmap(testImagePath, new BitmapFactory.Options());
        assertEquals(3000, image.getWidth());
        assertEquals(3000, image.getHeight());
    }

    @Test
    public void scaleImageDownFormLevelOnly6() {
        generalSettings.save("image_size", "original_image_size");
        saveTestBitmap(3000, 3000);
        ImageConverter.execute(testImagePath, getTestImageWidget(XML_OPENROSA_NAMESPACE, "max-pixels", "2998"), Collect.getInstance(), IMAGE_SIZE_ORIGINAL);

        Bitmap image = FileUtils.getBitmap(testImagePath, new BitmapFactory.Options());
        assertEquals(2998, image.getWidth());
        assertEquals(2998, image.getHeight());
    }

    @Test
    public void scaleImageDownFormLevelOnly7() {
        generalSettings.save("image_size", "original_image_size");
        saveTestBitmap(3000, 3000);
        ImageConverter.execute(testImagePath, getTestImageWidget(XML_OPENROSA_NAMESPACE, "max-pixels", ""), Collect.getInstance(), IMAGE_SIZE_ORIGINAL);

        Bitmap image = FileUtils.getBitmap(testImagePath, new BitmapFactory.Options());
        assertEquals(3000, image.getWidth());
        assertEquals(3000, image.getHeight());
    }

    @Test
    public void scaleImageDownFormLevelOnly8() {
        generalSettings.save("image_size", "original_image_size");
        saveTestBitmap(3000, 3000);
        ImageConverter.execute(testImagePath, getTestImageWidget("", "max-pixels", "2000"), Collect.getInstance(), IMAGE_SIZE_ORIGINAL);

        Bitmap image = FileUtils.getBitmap(testImagePath, new BitmapFactory.Options());
        assertEquals(3000, image.getWidth());
        assertEquals(3000, image.getHeight());
    }

    @Test
    public void scaleImageDownFormLevelOnly9() {
        generalSettings.save("image_size", "original_image_size");
        saveTestBitmap(3000, 3000);
        ImageConverter.execute(testImagePath, getTestImageWidget(XML_OPENROSA_NAMESPACE, "max-pixel", "2000"), Collect.getInstance(), IMAGE_SIZE_ORIGINAL);

        Bitmap image = FileUtils.getBitmap(testImagePath, new BitmapFactory.Options());
        assertEquals(3000, image.getWidth());
        assertEquals(3000, image.getHeight());
    }

    @Test
    public void scaleImageDownFormLevelOnly10() {
        generalSettings.save("image_size", "original_image_size");
        saveTestBitmap(3000, 3000);
        ImageConverter.execute(testImagePath, getTestImageWidget(XML_OPENROSA_NAMESPACE, "max-pixels", "2000.5"), Collect.getInstance(), IMAGE_SIZE_ORIGINAL);

        Bitmap image = FileUtils.getBitmap(testImagePath, new BitmapFactory.Options());
        assertEquals(3000, image.getWidth());
        assertEquals(3000, image.getHeight());
    }

    @Test
    public void scaleImageDownFormLevelOnly11() {
        generalSettings.save("image_size", "original_image_size");
        saveTestBitmap(3000, 3000);
        ImageConverter.execute(testImagePath, getTestImageWidget(XML_OPENROSA_NAMESPACE, "max-pixels", "0"), Collect.getInstance(), IMAGE_SIZE_ORIGINAL);

        Bitmap image = FileUtils.getBitmap(testImagePath, new BitmapFactory.Options());
        assertEquals(3000, image.getWidth());
        assertEquals(3000, image.getHeight());
    }

    @Test
    public void scaleImageDownFormLevelOnly12() {
        generalSettings.save("image_size", "original_image_size");
        saveTestBitmap(3000, 3000);
        ImageConverter.execute(testImagePath, getTestImageWidget(XML_OPENROSA_NAMESPACE, "max-pixels", "-2000"), Collect.getInstance(), IMAGE_SIZE_ORIGINAL);

        Bitmap image = FileUtils.getBitmap(testImagePath, new BitmapFactory.Options());
        assertEquals(3000, image.getWidth());
        assertEquals(3000, image.getHeight());
    }

    @Test
    public void scaleImageDownSettingsLevelOnly1() {
        generalSettings.save("image_size", IMAGE_SIZE_VERY_SMALL);
        saveTestBitmap(3000, 3000);
        ImageConverter.execute(testImagePath, getTestImageWidget(), Collect.getInstance(), IMAGE_SIZE_VERY_SMALL);

        Bitmap image = FileUtils.getBitmap(testImagePath, new BitmapFactory.Options());
        assertEquals(640, image.getWidth());
        assertEquals(640, image.getHeight());
    }

    @Test
    public void scaleImageDownSettingsLevelOnly2() {
        generalSettings.save("image_size", IMAGE_SIZE_SMALL);
        saveTestBitmap(3000, 3000);
        ImageConverter.execute(testImagePath, getTestImageWidget(), Collect.getInstance(), IMAGE_SIZE_SMALL);

        Bitmap image = FileUtils.getBitmap(testImagePath, new BitmapFactory.Options());
        assertEquals(1024, image.getWidth());
        assertEquals(1024, image.getHeight());
    }

    @Test
    public void scaleImageDownSettingsLevelOnly3() {
        generalSettings.save("image_size", IMAGE_SIZE_MEDIUM);
        saveTestBitmap(3000, 3000);
        ImageConverter.execute(testImagePath, getTestImageWidget(), Collect.getInstance(), IMAGE_SIZE_MEDIUM);

        Bitmap image = FileUtils.getBitmap(testImagePath, new BitmapFactory.Options());
        assertEquals(2048, image.getWidth());
        assertEquals(2048, image.getHeight());
    }

    @Test
    public void scaleImageDownSettingsLevelOnly4() {
        generalSettings.save("image_size", IMAGE_SIZE_LARGE);
        saveTestBitmap(3000, 3000);
        ImageConverter.execute(testImagePath, getTestImageWidget(), Collect.getInstance(), IMAGE_SIZE_LARGE);

        Bitmap image = FileUtils.getBitmap(testImagePath, new BitmapFactory.Options());
        assertEquals(3000, image.getWidth());
        assertEquals(3000, image.getHeight());
    }

    @Test
    public void scaleImageDownSettingsLevelOnly5() {
        generalSettings.save("image_size", IMAGE_SIZE_LARGE);
        saveTestBitmap(4000, 4000);
        ImageConverter.execute(testImagePath, getTestImageWidget(), Collect.getInstance(), IMAGE_SIZE_LARGE);

        Bitmap image = FileUtils.getBitmap(testImagePath, new BitmapFactory.Options());
        assertEquals(3072, image.getWidth());
        assertEquals(3072, image.getHeight());
    }

    @Test
    public void scaleImageDownFormAndSettingsLevel1() {
        generalSettings.save("image_size", IMAGE_SIZE_SMALL);
        saveTestBitmap(4000, 4000);
        ImageConverter.execute(testImagePath, getTestImageWidget(XML_OPENROSA_NAMESPACE, "max-pixels", "2000"), Collect.getInstance(), IMAGE_SIZE_SMALL);

        Bitmap image = FileUtils.getBitmap(testImagePath, new BitmapFactory.Options());
        assertEquals(2000, image.getWidth());
        assertEquals(2000, image.getHeight());
    }

    @Test
    public void scaleImageDownFormAndSettingsLevel2() {
        generalSettings.save("image_size", "small");
        saveTestBitmap(4000, 4000);
        ImageConverter.execute(testImagePath, getTestImageWidget(XML_OPENROSA_NAMESPACE, "max-pixels", "650"), Collect.getInstance(), IMAGE_SIZE_SMALL);

        Bitmap image = FileUtils.getBitmap(testImagePath, new BitmapFactory.Options());
        assertEquals(650, image.getWidth());
        assertEquals(650, image.getHeight());
    }

    @Test
    public void scaleImageToNewWidthTest() {
        saveTestBitmap(2000, 1000);
        Bitmap image = FileUtils.getBitmap(testImagePath, new BitmapFactory.Options());
        image = ImageConverter.scaleImageToNewWidth(image, 500);
        assertEquals(500, image.getWidth());
        assertEquals(250, image.getHeight());

        saveTestBitmap(1000, 2000);
        image = FileUtils.getBitmap(testImagePath, new BitmapFactory.Options());
        image = ImageConverter.scaleImageToNewWidth(image, 500);
        assertEquals(500, image.getWidth());
        assertEquals(1000, image.getHeight());

        saveTestBitmap(500, 400);
        image = FileUtils.getBitmap(testImagePath, new BitmapFactory.Options());
        image = ImageConverter.scaleImageToNewWidth(image, 1000);
        assertEquals(1000, image.getWidth());
        assertEquals(800, image.getHeight());

        saveTestBitmap(400, 500);
        image = FileUtils.getBitmap(testImagePath, new BitmapFactory.Options());
        image = ImageConverter.scaleImageToNewWidth(image, 1000);
        assertEquals(1000, image.getWidth());
        assertEquals(1250, image.getHeight());
    }

    @Test
    public void keepExifTest1AfterScaling() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put(ExifInterface.TAG_ARTIST, ExifInterface.TAG_ARTIST);
        attributes.put(ExifInterface.TAG_DATETIME, ExifInterface.TAG_DATETIME);
        attributes.put(ExifInterface.TAG_DATETIME_ORIGINAL, ExifInterface.TAG_DATETIME_ORIGINAL);
        attributes.put(ExifInterface.TAG_DATETIME_DIGITIZED, ExifInterface.TAG_DATETIME_DIGITIZED);
        attributes.put(ExifInterface.TAG_GPS_ALTITUDE, dec2DMS(-17));
        attributes.put(ExifInterface.TAG_GPS_ALTITUDE_REF, ExifInterface.TAG_GPS_ALTITUDE_REF);
        attributes.put(ExifInterface.TAG_GPS_DATESTAMP, ExifInterface.TAG_GPS_DATESTAMP);
        attributes.put(ExifInterface.TAG_GPS_LATITUDE, dec2DMS(25.165173));
        attributes.put(ExifInterface.TAG_GPS_LATITUDE_REF, ExifInterface.TAG_GPS_LATITUDE_REF);
        attributes.put(ExifInterface.TAG_GPS_LONGITUDE, dec2DMS(23.988174));
        attributes.put(ExifInterface.TAG_GPS_LONGITUDE_REF, ExifInterface.TAG_GPS_LONGITUDE_REF);
        attributes.put(ExifInterface.TAG_GPS_PROCESSING_METHOD, ExifInterface.TAG_GPS_PROCESSING_METHOD);
        attributes.put(ExifInterface.TAG_MAKE, ExifInterface.TAG_MAKE);
        attributes.put(ExifInterface.TAG_MODEL, ExifInterface.TAG_MODEL);
        attributes.put(ExifInterface.TAG_SUBSEC_TIME, ExifInterface.TAG_SUBSEC_TIME);

        saveTestBitmap(3000, 4000, attributes);

        ImageConverter.execute(testImagePath, getTestImageWidget(), Collect.getInstance(), IMAGE_SIZE_ORIGINAL);
        ExifInterface exifData = getTestImageExif();
        assertNotNull(exifData);

        for (String attributeName : attributes.keySet()) {
            switch (attributeName) {
                case ExifInterface.TAG_GPS_LATITUDE:
                    assertThat(exifData.getAttribute(attributeName), is("25/1,9/1,54622/1000"));
                    break;
                case ExifInterface.TAG_GPS_LONGITUDE:
                    assertThat(exifData.getAttribute(attributeName), is("23/1,59/1,17426/1000"));
                    break;
                case ExifInterface.TAG_GPS_ALTITUDE:
                    assertThat(exifData.getAttribute(attributeName), is("17/1,0/1,0/1000"));
                    break;
                default:
                    assertThat(exifData.getAttribute(attributeName), is(attributeName));
                    break;
            }
        }
    }

    @Test
    public void verifyNoRotationAppliedForExifRotation() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_90));
        saveTestBitmap(3000, 4000, attributes);
        Bitmap image = FileUtils.getBitmap(testImagePath, new BitmapFactory.Options());
        assertEquals(3000, image.getWidth());
        assertEquals(4000, image.getHeight());
    }

    // https://stackoverflow.com/a/55252228/5479029
    private String dec2DMS(double coord) {
        coord = coord > 0 ? coord : -coord;
        String out = (int) coord + "/1,";
        coord = (coord % 1) * 60;
        out = out + (int) coord + "/1,";
        coord = (coord % 1) * 60000;
        out = out + (int) coord + "/1000";
        return out;
    }

    private void saveTestBitmap(int width, int height) {
        saveTestBitmap(width, height, new HashMap<>());
    }

    private void saveTestBitmap(int width, int height, Map<String, String> attributes) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        FileUtils.saveBitmapToFile(bitmap, testImagePath);

        try {
            ExifInterface exifInterface = new ExifInterface(testImagePath);
            for (String attributeName : attributes.keySet()) {
                exifInterface.setAttribute(attributeName, attributes.get(attributeName));
            }
            exifInterface.saveAttributes();
        } catch (IOException e) {
            Timber.w(e);
        }
    }

    private ExifInterface getTestImageExif() {
        try {
            return new ExifInterface(testImagePath);
        } catch (Exception e) {
            Timber.w(e);
        }

        return null;
    }

    private ImageWidget getTestImageWidget() {
        return getTestImageWidget(new ArrayList<>());
    }

    private ImageWidget getTestImageWidget(String namespace, String name, String value) {
        List<TreeElement> bindAttributes = new ArrayList<>();
        bindAttributes.add(TreeElement.constructAttributeElement(namespace, name, value));

        return getTestImageWidget(bindAttributes);
    }

    private ImageWidget getTestImageWidget(List<TreeElement> bindAttributes) {
        FormEntryPrompt formEntryPrompt = mock(FormEntryPrompt.class);

        when(formEntryPrompt.getBindAttributes()).thenReturn(bindAttributes);

        ImageWidget imageWidget = mock(ImageWidget.class);
        when(imageWidget.getFormEntryPrompt()).thenReturn(formEntryPrompt);

        return imageWidget;
    }
}
