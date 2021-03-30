/*
 * Copyright (C) 2018 Shobhit Agarwal
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

package org.odk.collect.android.utilities;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import org.odk.collect.android.application.Collect;

public final class ContentUriHelper {

    private ContentUriHelper() {

    }

    public static Long getIdFromUri(Uri contentUri) {
        int lastSegmentIndex = contentUri.getPathSegments().size() - 1;
        String idSegment = contentUri.getPathSegments().get(lastSegmentIndex);
        return Long.parseLong(idSegment);
    }

    public static String getFileExtensionFromUri(Uri fileUri) {
        String mimeType = Collect.getInstance().getContentResolver().getType(fileUri);

        String extension = fileUri.getScheme() != null && fileUri.getScheme().equals(ContentResolver.SCHEME_CONTENT)
                ? MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
                : MimeTypeMap.getFileExtensionFromUrl(fileUri.toString());

        if (extension == null || extension.isEmpty()) {
            try (Cursor cursor = Collect.getInstance().getContentResolver().query(fileUri, null, null, null, null)) {
                String name = null;
                if (cursor != null && cursor.moveToFirst()) {
                    name = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME));
                }
                extension = name != null ? name.substring(name.lastIndexOf('.') + 1) : "";
            }
        }

        if (extension.isEmpty() && mimeType != null && mimeType.contains("/")) {
            extension = mimeType.substring(mimeType.lastIndexOf('/') + 1);
        }

        return extension;
    }

}