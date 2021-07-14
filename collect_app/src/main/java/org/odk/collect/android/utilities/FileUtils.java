/*
 * Copyright (C) 2017 University of Washington
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

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import androidx.exifinterface.media.ExifInterface;

import org.apache.commons.io.IOUtils;
import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.actions.setgeopoint.SetGeopointActionHandler;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.xform.util.XFormUtils;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;

import timber.log.Timber;

import static java.util.Arrays.asList;

/**
 * Static methods used for common file operations.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class FileUtils {

    // Used to validate and display valid form names.
    public static final String VALID_FILENAME = "[ _\\-A-Za-z0-9]*.x[ht]*ml";
    public static final String FORMID = "formid";
    public static final String VERSION = "version"; // arbitrary string in OpenRosa 1.0
    public static final String TITLE = "title";
    public static final String SUBMISSIONURI = "submission";
    public static final String BASE64_RSA_PUBLIC_KEY = "base64RsaPublicKey";
    public static final String AUTO_DELETE = "autoDelete";
    public static final String AUTO_SEND = "autoSend";
    public static final String GEOMETRY_XPATH = "geometryXpath";

    /** Suffix for the form media directory. */
    public static final String MEDIA_SUFFIX = "-media";

    /** Filename of the last-saved instance data. */
    public static final String LAST_SAVED_FILENAME = "last-saved.xml";

    /** Valid XML stub that can be parsed without error. */
    public static final String STUB_XML = "<?xml version='1.0' ?><stub />";

    private static final Integer[] EXIF_ORIENTATION_ROTATIONS =
            {ExifInterface.ORIENTATION_ROTATE_90, ExifInterface.ORIENTATION_ROTATE_180, ExifInterface.ORIENTATION_ROTATE_270};

    static int bufSize = 16 * 1024; // May be set by unit test

    private FileUtils() {
    }

    public static void saveAnswerFileFromUri(Uri uri, File destFile, Context context) {
        try (InputStream fileInputStream = context.getContentResolver().openInputStream(uri);
             OutputStream fileOutputStream = new FileOutputStream(destFile)) {
            IOUtils.copy(fileInputStream, fileOutputStream);
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    public static File createDestinationMediaFile(String fileLocation, String fileExtension) {
        return new File(fileLocation
                + File.separator
                + System.currentTimeMillis()
                + "."
                + fileExtension);
    }

    public static boolean createFolder(String path) {
        File dir = new File(path);
        return dir.exists() || dir.mkdirs();
    }

    public static Bitmap getBitmapScaledToDisplay(File file, int screenHeight, int screenWidth) {
        return getBitmapScaledToDisplay(file, screenHeight, screenWidth, false);
    }

    /**
     * Scales image according to the given display
     *
     * @param file           containing the image
     * @param screenHeight   height of the display
     * @param screenWidth    width of the display
     * @param upscaleEnabled determines whether the image should be up-scaled or not
     *                       if the window size is greater than the image size
     * @return scaled bitmap
     */
    public static Bitmap getBitmapScaledToDisplay(File file, int screenHeight, int screenWidth, boolean upscaleEnabled) {
        // Determine image size of file
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        getBitmap(file.getAbsolutePath(), options);

        Bitmap bitmap;
        double scale;
        if (upscaleEnabled) {
            // Load full size bitmap image
            options = new BitmapFactory.Options();
            options.inInputShareable = true;
            options.inPurgeable = true;
            bitmap = getBitmap(file.getAbsolutePath(), options);

            double heightScale = ((double) (options.outHeight)) / screenHeight;
            double widthScale = ((double) options.outWidth) / screenWidth;
            scale = Math.max(widthScale, heightScale);

            double newHeight = Math.ceil(options.outHeight / scale);
            double newWidth = Math.ceil(options.outWidth / scale);

            bitmap = Bitmap.createScaledBitmap(bitmap, (int) newWidth, (int) newHeight, false);
        } else {
            int heightScale = options.outHeight / screenHeight;
            int widthScale = options.outWidth / screenWidth;

            // Powers of 2 work faster, sometimes, according to the doc.
            // We're just doing closest size that still fills the screen.
            scale = Math.max(widthScale, heightScale);

            // get bitmap with scale ( < 1 is the same as 1)
            options = new BitmapFactory.Options();
            options.inInputShareable = true;
            options.inPurgeable = true;
            options.inSampleSize = (int) scale;
            bitmap = getBitmap(file.getAbsolutePath(), options);
        }

        if (bitmap != null) {
            Timber.i("Screen is %dx%d.  Image has been scaled down by %f to %dx%d",
                    screenHeight, screenWidth, scale, bitmap.getHeight(), bitmap.getWidth());
        }
        return bitmap;
    }


    /**
     * While copying the file, apply the exif rotation of sourceFile to destinationFile
     * so that sourceFile with EXIF has same orientation as destinationFile without EXIF
     */
    public static void copyImageAndApplyExifRotation(File sourceFile, File destFile) {
        ExifInterface sourceFileExif = null;
        try {
            sourceFileExif = new ExifInterface(sourceFile);
        } catch (IOException e) {
            Timber.w(e);
        }
        if (sourceFileExif == null ||
                !Arrays.asList(EXIF_ORIENTATION_ROTATIONS)
                        .contains(sourceFileExif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED))) {
            // Source Image doesn't have any EXIF Rotations, so a normal file copy will suffice
             copyFile(sourceFile, destFile);
        } else {
            Bitmap sourceImage = getBitmap(sourceFile.getAbsolutePath(), new BitmapFactory.Options());
            int orientation = sourceFileExif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotateBitmapAndSaveToFile(sourceImage, 90, destFile.getAbsolutePath());
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotateBitmapAndSaveToFile(sourceImage, 180, destFile.getAbsolutePath());
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotateBitmapAndSaveToFile(sourceImage, 270, destFile.getAbsolutePath());
                    break;
            }
        }
    }

    private static void rotateBitmapAndSaveToFile(Bitmap image, int degrees, String filePath) {
        try {
            Matrix matrix = new Matrix();
            matrix.postRotate(degrees);
            image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
            Timber.w(e);
        }
        saveBitmapToFile(image, filePath);
    }

    public static String copyFile(File sourceFile, File destFile) {
        if (sourceFile.exists()) {
            String errorMessage = actualCopy(sourceFile, destFile);
            if (errorMessage != null) {
                try {
                    Thread.sleep(500);
                    Timber.e("Retrying to copy the file after 500ms: %s",
                            sourceFile.getAbsolutePath());
                    errorMessage = actualCopy(sourceFile, destFile);
                } catch (InterruptedException e) {
                    Timber.i(e);
                }
            }
            return errorMessage;
        } else {
            String msg = "Source file does not exist: " + sourceFile.getAbsolutePath();
            Timber.e(msg);
            return msg;
        }
    }

    private static String actualCopy(File sourceFile, File destFile) {
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        FileChannel src = null;
        FileChannel dst = null;
        try {
            fileInputStream = new FileInputStream(sourceFile);
            src = fileInputStream.getChannel();
            fileOutputStream = new FileOutputStream(destFile);
            dst = fileOutputStream.getChannel();
            dst.transferFrom(src, 0, src.size());
            dst.force(true);
            return null;
        } catch (Exception e) {
            if (e instanceof FileNotFoundException) {
                Timber.e(e, "FileNotFoundException while copying file");
            } else if (e instanceof IOException) {
                Timber.e(e, "IOException while copying file");
            } else {
                Timber.e(e, "Exception while copying file");
            }
            return e.getMessage();
        } finally {
            IOUtils.closeQuietly(fileInputStream);
            IOUtils.closeQuietly(fileOutputStream);
            IOUtils.closeQuietly(src);
            IOUtils.closeQuietly(dst);
        }
    }

    /**
     * Given a form definition file, return a map containing form metadata. The form ID is required
     * by the specification and will always be included. Title and version are optionally included.
     * If the form definition contains a submission block, any or all of submission URI, base 64 RSA
     * public key, auto-delete and auto-send may be included.
     */
    public static HashMap<String, String> getMetadataFromFormDefinition(File formDefinitionXml) {
        FormDef formDef = XFormUtils.getFormFromFormXml(formDefinitionXml.getAbsolutePath(), "jr://file/" + LAST_SAVED_FILENAME);

        final HashMap<String, String> fields = new HashMap<>();

        fields.put(TITLE, formDef.getTitle());
        fields.put(FORMID, formDef.getMainInstance().getRoot().getAttributeValue(null, "id"));
        String version = formDef.getMainInstance().getRoot().getAttributeValue(null, "version");
        if (version != null && version.trim().isEmpty()) {
            version = null;
        }
        fields.put(VERSION, version);

        if (formDef.getSubmissionProfile() != null) {
            fields.put(SUBMISSIONURI, formDef.getSubmissionProfile().getAction());

            final String key = formDef.getSubmissionProfile().getAttribute("base64RsaPublicKey");
            if (key != null && key.trim().length() > 0) {
                fields.put(BASE64_RSA_PUBLIC_KEY, key.trim());
            }

            fields.put(AUTO_DELETE, formDef.getSubmissionProfile().getAttribute("auto-delete"));
            fields.put(AUTO_SEND, formDef.getSubmissionProfile().getAttribute("auto-send"));
        }

        fields.put(GEOMETRY_XPATH, getOverallFirstGeoPoint(formDef));
        return fields;
    }

    /**
     * Returns an XPath path representing the first geopoint of this form definition or null if the
     * definition does not contain any field of type geopoint.
     *
     * The first geopoint is either of:
     *      (1) the first geopoint in the body that is not in a repeat
     *      (2) if the form has a setgeopoint action, the first geopoint in the instance that occurs
     *          before (1) or (1) if there is no geopoint defined before it in the instance.
     */
    private static String getOverallFirstGeoPoint(FormDef formDef) {
        TreeReference firstTopLevelBodyGeoPoint = getFirstToplevelBodyGeoPoint(formDef);

        if (!formDef.hasAction(SetGeopointActionHandler.ELEMENT_NAME)) {
            return firstTopLevelBodyGeoPoint == null ? null : firstTopLevelBodyGeoPoint.toString(false);
        } else {
            return getInstanceGeoPointBefore(firstTopLevelBodyGeoPoint, formDef.getMainInstance().getRoot());
        }
    }

    /**
     * Returns the reference of the first geopoint in the body that is not in a repeat.
     */
    private static TreeReference getFirstToplevelBodyGeoPoint(FormDef formDef) {
        if (formDef.getChildren().size() == 0) {
            return null;
        } else {
            return getFirstTopLevelBodyGeoPoint(formDef, formDef.getMainInstance());
        }
    }

    /**
     * Returns the reference of the first child of the given element that is of type geopoint and
     * is not contained in a repeat.
     */
    private static TreeReference getFirstTopLevelBodyGeoPoint(IFormElement element, FormInstance primaryInstance) {
        if (element instanceof QuestionDef) {
            QuestionDef question = (QuestionDef) element;
            int dataType = primaryInstance.resolveReference((TreeReference) element.getBind().getReference()).getDataType();

            if (dataType == Constants.DATATYPE_GEOPOINT) {
                return (TreeReference) question.getBind().getReference();
            }
        } else if (element instanceof FormDef || element instanceof GroupDef) {
            if (element instanceof GroupDef && ((GroupDef) element).getRepeat()) {
                return null;
            } else {
                for (IFormElement child : element.getChildren()) {
                    // perform recursive depth-first search
                    TreeReference geoRef = getFirstTopLevelBodyGeoPoint(child, primaryInstance);
                    if (geoRef != null) {
                        return geoRef;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Returns the XPath path for the first geopoint in the primary instance that is before the given
     * reference and not in a repeat.
     */
    private static String getInstanceGeoPointBefore(TreeReference firstBodyGeoPoint, TreeElement element) {
        if (element.getRef().equals(firstBodyGeoPoint)) {
            return null;
        } else if (element.getDataType() == Constants.DATATYPE_GEOPOINT) {
            return element.getRef().toString(false);
        } else if (element.hasChildren()) {
            Set<TreeElement> childrenToAvoid = new HashSet<>();

            for (int i = 0; i < element.getNumChildren(); i++) {
                if (element.getChildAt(i).getMultiplicity() == TreeReference.INDEX_TEMPLATE) {
                    childrenToAvoid.addAll(element.getChildrenWithName(element.getChildAt(i).getName()));
                } else if (!childrenToAvoid.contains(element.getChildAt(i))) {
                    String geoPath = getInstanceGeoPointBefore(firstBodyGeoPoint, element.getChildAt(i));
                    if (geoPath != null) {
                        return geoPath;
                    }
                }
            }
        }

        return null;
    }

    public static void deleteAndReport(File file) {
        if (file != null && file.exists()) {
            // remove garbage
            if (!file.delete()) {
                Timber.d("%s will be deleted upon exit.", file.getAbsolutePath());
                file.deleteOnExit();
            } else {
                Timber.d("%s has been deleted.", file.getAbsolutePath());
            }
        }
    }

    public static String getFormBasename(File formXml) {
        return getFormBasename(formXml.getName());
    }

    public static String getFormBasename(String formFilePath) {
        return formFilePath.substring(0, formFilePath.lastIndexOf('.'));
    }

    public static String constructMediaPath(String formFilePath) {
        return getFormBasename(formFilePath) + MEDIA_SUFFIX;
    }

    public static File getFormMediaDir(File formXml) {
        final String formFileName = getFormBasename(formXml);
        return new File(formXml.getParent(), formFileName + MEDIA_SUFFIX);
    }

    public static String getFormBasenameFromMediaFolder(File mediaFolder) {
        /*
         * TODO (from commit 37e3467): Apparently the form name is neither
         * in the formController nor the formDef. In fact, it doesn't seem to
         * be saved into any object in JavaRosa. However, the mediaFolder
         * has the substring of the file name in it, so we extract the file name
         * from here. Awkward...
         */
        return mediaFolder.getName().split(MEDIA_SUFFIX)[0];
    }

    public static File getLastSavedFile(File formXml) {
        return new File(getFormMediaDir(formXml), LAST_SAVED_FILENAME);
    }

    public static String getLastSavedPath(File mediaFolder) {
        return mediaFolder.getAbsolutePath() + File.separator + LAST_SAVED_FILENAME;
    }

    /**
     * Returns the path to the last-saved file for this form,
     * creating a valid stub if it doesn't yet exist.
     */
    public static String getOrCreateLastSavedSrc(File formXml) {
        File lastSavedFile = getLastSavedFile(formXml);

        if (!lastSavedFile.exists()) {
            write(lastSavedFile, STUB_XML.getBytes(Charset.forName("UTF-8")));
        }

        return "jr://file/" + LAST_SAVED_FILENAME;
    }

    /**
     * @param mediaDir the media folder
     */
    public static void checkMediaPath(File mediaDir) {
        if (mediaDir.exists() && mediaDir.isFile()) {
            Timber.e("The media folder is already there and it is a FILE!! We will need to delete it and create a folder instead");
            boolean deleted = mediaDir.delete();
            if (!deleted) {
                throw new RuntimeException(
                        TranslationHandler.getString(Collect.getInstance(), R.string.fs_delete_media_path_if_file_error,
                                mediaDir.getAbsolutePath()));
            }
        }

        // the directory case
        boolean createdOrExisted = createFolder(mediaDir.getAbsolutePath());
        if (!createdOrExisted) {
            throw new RuntimeException(
                    TranslationHandler.getString(Collect.getInstance(), R.string.fs_create_media_folder_error,
                            mediaDir.getAbsolutePath()));
        }
    }

    public static void purgeMediaPath(String mediaPath) {
        File tempMediaFolder = new File(mediaPath);
        File[] tempMediaFiles = tempMediaFolder.listFiles();

        if (tempMediaFiles != null) {
            for (File tempMediaFile : tempMediaFiles) {
                deleteAndReport(tempMediaFile);
            }
        }

        deleteAndReport(tempMediaFolder);
    }

    public static void saveBitmapToFile(Bitmap bitmap, String path) {
        final Bitmap.CompressFormat compressFormat = path.toLowerCase(Locale.getDefault()).endsWith(".png") ?
                Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG;

        try (FileOutputStream out = new FileOutputStream(path)) {
            bitmap.compress(compressFormat, 100, out);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    /*
    This method is used to avoid OutOfMemoryError exception during loading an image.
    If the exception occurs we catch it and try to load a smaller image.
     */
    public static Bitmap getBitmap(String path, BitmapFactory.Options originalOptions) {
        BitmapFactory.Options newOptions = new BitmapFactory.Options();
        newOptions.inSampleSize = originalOptions.inSampleSize;
        if (newOptions.inSampleSize <= 0) {
            newOptions.inSampleSize = 1;
        }

        Bitmap bitmap;
        try {
            bitmap = BitmapFactory.decodeFile(path, originalOptions);
        } catch (OutOfMemoryError e) {
            Timber.i(e);
            newOptions.inSampleSize++;
            return getBitmap(path, newOptions);
        }

        return bitmap;
    }

    public static byte[] read(File file) {
        byte[] bytes = new byte[(int) file.length()];
        try (InputStream is = new FileInputStream(file)) {
            is.read(bytes);
        } catch (IOException e) {
            Timber.e(e);
        }
        return bytes;
    }

    public static void write(File file, byte[] data) {
        // Make sure the directory path to this file exists.
        file.getParentFile().mkdirs();

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    /** Sorts file paths as if sorting the path components and extensions lexicographically. */
    public static int comparePaths(String a, String b) {
        // Regular string compareTo() is incorrect, because it will sort "/" and "."
        // after other punctuation (e.g. "foo/bar" will sort AFTER "foo-2/bar" and
        // "pic.jpg" will sort AFTER "pic-2.jpg").  Replacing these delimiters with
        // '\u0000' and '\u0001' causes paths to sort correctly (assuming the paths
        // don't already contain '\u0000' or '\u0001').  This is a bit of a hack,
        // but it's a lot simpler and faster than comparing components one by one.
        String sortKeyA = a.replace('/', '\u0000').replace('.', '\u0001');
        String sortKeyB = b.replace('/', '\u0000').replace('.', '\u0001');
        return sortKeyA.compareTo(sortKeyB);
    }

    public static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            return "";
        }
        return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    public static void grantFilePermissions(Intent intent, Uri uri, Context context) {
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    }

    /**
     * Grants read permissions to a content URI added to the specified Intent.
     *
     * See {@link #grantFileReadPermissions(Intent, Uri, Context)} for details.
     */
    public static void grantFileReadPermissions(Intent intent, Uri uri, Context context) {
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    }

    @SuppressWarnings("PMD.DoNotHardCodeSDCard")
    public static String simplifyScopedStoragePath(String path) {
        if (path != null && path.startsWith("/storage/emulated/0/")) {
            return "/sdcard/" + path.substring("/storage/emulated/0/".length());
        }

        return path;
    }

    /** Iterates over all directories and files under a root path. */
    public static Iterable<File> walk(File root) {
        return () -> new Walker(root, true);
    }

    public static String getMimeType(File file) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(file.getAbsolutePath());
        String mimeType = extension != null ? MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) : null;

        if (mimeType == null || mimeType.isEmpty()) {
            FileNameMap fileNameMap = URLConnection.getFileNameMap();
            mimeType = fileNameMap.getContentTypeFor(file.getAbsolutePath());
        }

        if (mimeType == null || mimeType.isEmpty()) {
            mimeType = URLConnection.guessContentTypeFromName(file.getName());
        }

        return mimeType;
    }

    public static void createDir(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                String message = String.format("Cannot create directory: %s", dirPath);
                Timber.w(message);
                throw new RuntimeException(message);
            }
        } else {
            if (!dir.isDirectory()) {
                String message = String.format("%s exists, but is not a directory", dirPath);
                Timber.w(message);
                throw new RuntimeException(message);
            }
        }
    }

    public static List<File> listFiles(File file) {
        if (file != null && file.exists()) {
            return asList(file.listFiles());
        } else {
            return new ArrayList<>();
        }
    }

    /** An iterator that walks over all the directories and files under a given path. */
    private static class Walker implements Iterator<File> {
        private final List<File> queue = new ArrayList<>();
        private final boolean depthFirst;

        Walker(File root, boolean depthFirst) {
            queue.add(root);
            this.depthFirst = depthFirst;
        }

        @Override public boolean hasNext() {
            return !queue.isEmpty();
        }

        @Override public File next() {
            if (queue.isEmpty()) {
                throw new NoSuchElementException();
            }
            File next = queue.remove(0);
            if (next.isDirectory()) {
                queue.addAll(depthFirst ? 0 : queue.size(), Arrays.asList(next.listFiles()));
            }
            return next;
        }
    }
}
