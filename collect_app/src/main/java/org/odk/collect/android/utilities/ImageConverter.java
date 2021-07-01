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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.exifinterface.media.ExifInterface;

import org.javarosa.core.model.instance.TreeElement;
import org.odk.collect.android.R;
import org.odk.collect.android.widgets.QuestionWidget;

import java.io.IOException;

import timber.log.Timber;

import static org.odk.collect.android.utilities.ApplicationConstants.Namespaces.XML_OPENROSA_NAMESPACE;

public class ImageConverter {

    private ImageConverter() {
    }

    /**
     * Before proceed with scaling or rotating, make sure existing exif information is stored/restored.
     * @author Khuong Ninh (khuong.ninh@it-development.com)
     */
    public static void execute(String imagePath, QuestionWidget questionWidget, Context context, String imageSizeMode) {
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imagePath);
        } catch (IOException e) {
            Timber.w(e);
        }

        scaleDownImageIfNeeded(imagePath, questionWidget, context, imageSizeMode);

        if (exif != null) {
            try {
                exif.saveAttributes();
            } catch (IOException e) {
                Timber.w(e);
            }
        }
    }

    private static void scaleDownImageIfNeeded(String imagePath, QuestionWidget questionWidget, Context context, String imageSizeMode) {
        Integer maxPixels;

        if (questionWidget != null) {
            maxPixels = getMaxPixelsFromFormIfDefined(questionWidget);

            if (maxPixels == null) {
                maxPixels = getMaxPixelsFromSettings(context, imageSizeMode);
            }

            if (maxPixels != null && maxPixels > 0) {
                scaleDownImage(imagePath, maxPixels);
            }
        }
    }

    private static Integer getMaxPixelsFromFormIfDefined(QuestionWidget questionWidget) {
        Integer maxPixels = null;
        for (TreeElement attrs : questionWidget.getFormEntryPrompt().getBindAttributes()) {
            if ("max-pixels".equals(attrs.getName()) && XML_OPENROSA_NAMESPACE.equals(attrs.getNamespace())) {
                try {
                    maxPixels = Integer.parseInt(attrs.getAttributeValue());
                } catch (NumberFormatException e) {
                    Timber.i(e);
                }
            }
        }
        return maxPixels;
    }

    private static Integer getMaxPixelsFromSettings(Context context, String imageSizeMode) {
        Integer maxPixels = null;
        String[] imageEntryValues = context.getResources().getStringArray(R.array.image_size_entry_values);
        if (!imageSizeMode.equals(imageEntryValues[0])) {
            if (imageSizeMode.equals(imageEntryValues[1])) {
                maxPixels = 640;
            } else if (imageSizeMode.equals(imageEntryValues[2])) {
                maxPixels = 1024;
            } else if (imageSizeMode.equals(imageEntryValues[3])) {
                maxPixels = 2048;
            } else if (imageSizeMode.equals(imageEntryValues[4])) {
                maxPixels = 3072;
            }
        }
        return maxPixels;
    }

    /**
     * This method is used to reduce an original picture size.
     * maxPixels refers to the max pixels of the long edge, the short edge is scaled proportionately.
     */
    private static void scaleDownImage(String imagePath, int maxPixels) {
        Bitmap image = FileUtils.getBitmap(imagePath, new BitmapFactory.Options());

        if (image != null) {
            double originalWidth = image.getWidth();
            double originalHeight = image.getHeight();

            if (originalWidth > originalHeight && originalWidth > maxPixels) {
                int newHeight = (int) (originalHeight / (originalWidth / maxPixels));

                image = Bitmap.createScaledBitmap(image, maxPixels, newHeight, false);
                FileUtils.saveBitmapToFile(image, imagePath);
            } else if (originalHeight > maxPixels) {
                int newWidth = (int) (originalWidth / (originalHeight / maxPixels));

                image = Bitmap.createScaledBitmap(image, newWidth, maxPixels, false);
                FileUtils.saveBitmapToFile(image, imagePath);
            }
        }
    }

    public static Bitmap scaleImageToNewWidth(Bitmap bitmap, int newWidth) {
        int newHeight = (int) (((double) newWidth / (double) bitmap.getWidth()) * bitmap.getHeight());
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }
}
