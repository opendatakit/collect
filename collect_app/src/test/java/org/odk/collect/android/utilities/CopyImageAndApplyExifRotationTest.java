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

package org.odk.collect.android.utilities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class CopyImageAndApplyExifRotationTest {

    @Test
    public void copyAndRotateImageNinety() throws IOException {
        File sourceFile = createTempImageFile("source");
        File destinationFile = createTempImageFile("destination");
        Map<String, String> attributes = new HashMap<>();
        attributes.put(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_90));
        saveTestBitmapToFile(sourceFile.getAbsolutePath(), 3000, 4000, attributes);
        FileUtils.copyImageAndApplyExifRotation(sourceFile, destinationFile);
        Bitmap image = FileUtils.getBitmap(destinationFile.getAbsolutePath(), new BitmapFactory.Options());
        assertEquals(4000, image.getWidth());
        assertEquals(3000, image.getHeight());
        verifyNoExifOrientationInDestinationFile(destinationFile.getAbsolutePath());
    }


    @Test
    public void copyAndRotateImageTwoSeventy() throws IOException {
        File sourceFile = createTempImageFile("source");
        File destinationFile = createTempImageFile("destination");
        Map<String, String> attributes = new HashMap<>();
        attributes.put(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_270));
        saveTestBitmapToFile(sourceFile.getAbsolutePath(), 3000, 4000, attributes);
        FileUtils.copyImageAndApplyExifRotation(sourceFile, destinationFile);
        Bitmap image = FileUtils.getBitmap(destinationFile.getAbsolutePath(), new BitmapFactory.Options());
        assertEquals(4000, image.getWidth());
        assertEquals(3000, image.getHeight());
        verifyNoExifOrientationInDestinationFile(destinationFile.getAbsolutePath());
    }


    @Test
    public void copyAndRotateImageOneEighty() throws IOException {
        File sourceFile = createTempImageFile("source");
        File destinationFile = createTempImageFile("destination");
        Map<String, String> attributes = new HashMap<>();
        attributes.put(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_180));
        saveTestBitmapToFile(sourceFile.getAbsolutePath(), 3000, 4000, attributes);
        FileUtils.copyImageAndApplyExifRotation(sourceFile, destinationFile);
        Bitmap image = FileUtils.getBitmap(destinationFile.getAbsolutePath(), new BitmapFactory.Options());
        assertEquals(3000, image.getWidth());
        assertEquals(4000, image.getHeight());
        verifyNoExifOrientationInDestinationFile(destinationFile.getAbsolutePath());
    }


    @Test
    public void copyAndRotateImageUndefined() throws IOException {
        File sourceFile = createTempImageFile("source");
        File destinationFile = createTempImageFile("destination");
        Map<String, String> attributes = new HashMap<>();
        attributes.put(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_UNDEFINED));
        saveTestBitmapToFile(sourceFile.getAbsolutePath(), 3000, 4000, attributes);
        FileUtils.copyImageAndApplyExifRotation(sourceFile, destinationFile);
        Bitmap image = FileUtils.getBitmap(destinationFile.getAbsolutePath(), new BitmapFactory.Options());
        assertEquals(3000, image.getWidth());
        assertEquals(4000, image.getHeight());
        verifyNoExifOrientationInDestinationFile(destinationFile.getAbsolutePath());
    }

    @Test
    public void copyAndRotateImageNoExif() throws IOException {
        File sourceFile = createTempImageFile("source");
        File destinationFile = createTempImageFile("destination");
        saveTestBitmapToFile(sourceFile.getAbsolutePath(), 3000, 4000, null);
        FileUtils.copyImageAndApplyExifRotation(sourceFile, destinationFile);
        Bitmap image = FileUtils.getBitmap(destinationFile.getAbsolutePath(), new BitmapFactory.Options());
        assertEquals(3000, image.getWidth());
        assertEquals(4000, image.getHeight());
        verifyNoExifOrientationInDestinationFile(destinationFile.getAbsolutePath());
    }


    private void verifyNoExifOrientationInDestinationFile(String destinationFilePath) {
        ExifInterface exifData = getTestImageExif(destinationFilePath);
        if (exifData != null) {
            assertEquals(ExifInterface.ORIENTATION_UNDEFINED, exifData.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED));
        }

    }


    private void saveTestBitmapToFile(String filePath, int width, int height, Map<String, String> attributes) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        FileUtils.saveBitmapToFile(bitmap, filePath);
        if (attributes != null) {
            try {
                ExifInterface exifInterface = new ExifInterface(filePath);
                for (String attributeName : attributes.keySet()) {
                    exifInterface.setAttribute(attributeName, attributes.get(attributeName));
                }
                exifInterface.saveAttributes();
            } catch (IOException e) {
                Timber.w(e);
            }
        }
    }

    private ExifInterface getTestImageExif(String imagePath) {
        try {
            return new ExifInterface(imagePath);
        } catch (Exception e) {
            Timber.w(e);
        }

        return null;
    }

    private File createTempImageFile(String imageName) throws IOException {
        File temp = File.createTempFile(imageName, ".jpg");
        temp.deleteOnExit();
        return temp;
    }

}
