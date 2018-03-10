package org.odk.collect.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import org.odk.collect.android.R;
import org.odk.collect.android.utilities.CameraUtils;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.views.CameraPreview;
import org.odk.collect.android.widgets.VideoWidget;

import java.io.File;
import java.io.IOException;

import timber.log.Timber;

/*https://github.com/googlesamples/android-MediaRecorder/blob/master/Application/src/main/
    java/com/example/android/mediarecorder/MainActivity.java*/

public class CaptureSelfieVideoActivity extends Activity {
    private Camera camera;
    private CameraPreview camPreview;
    private int cameraId;
    private boolean recording = false;
    private MediaRecorder mediaRecorder;
    String outputFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager
                .LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_capture_selfie);
        FrameLayout preview = findViewById(R.id.camera_preview);

        try {
            cameraId = CameraUtils.getFrontCameraId();
            camera = CameraUtils.getCameraInstance(this, cameraId);
        } catch (Exception e) {
            Timber.e(e);
        }

        this.camPreview = new CameraPreview(this, camera);
        preview.addView(this.camPreview);

        mediaRecorder = new MediaRecorder();

        this.camPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!recording) {
                    // initialize video camera
                    if (prepareVideoRecorder()) {
                        // Camera is available and unlocked, MediaRecorder is prepared,
                        // now you can start recording
                        mediaRecorder.start();

                        // inform the user that recording has started
                        ToastUtils.showLongToast(getString(R.string.stop_video_capture_instruction));
                        recording = true;
                    } else {
                        // prepare didn't work, release the camera
                        releaseMediaRecorder();
                    }
                } else {
                    // stop recording and release camera
                    mediaRecorder.stop();  // stop the recording
                    releaseMediaRecorder(); // release the MediaRecorder object
                    camera.lock();         // take camera access back from MediaRecorder

                    recording = false;

                    Intent i = new Intent();
                    i.setData(Uri.fromFile(new File(outputFile)));
                    setResult(RESULT_OK, i);
                    finish();
                }
            }
        });

        ToastUtils.showLongToast(getString(R.string.start_video_capture_instruction));
    }

    private boolean prepareVideoRecorder() {

        mediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        camera.unlock();
        mediaRecorder.setCamera(camera);

        // Step 2: Set sources
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        // Step 4: Set output file
        outputFile = VideoWidget.getOutputMediaFile(VideoWidget.MEDIA_TYPE_VIDEO).toString();
        mediaRecorder.setOutputFile(outputFile);

        // Step 5: Set the preview output
        mediaRecorder.setPreviewDisplay(camPreview.getHolder().getSurface());

        // Step 6: Prepare configured MediaRecorder
        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Timber.e(e);
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Timber.e(e);
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();   // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
            camera.lock();           // lock camera for later use
        }
    }

    @Override
    protected void onPause() {
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        camera = null;
        super.onPause();
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (camera == null) {
            setContentView(R.layout.activity_capture_selfie);
            FrameLayout preview = findViewById(R.id.camera_preview);

            try {
                cameraId = CameraUtils.getFrontCameraId();
                camera = CameraUtils.getCameraInstance(this, cameraId);
            } catch (Exception e) {
                Timber.e(e);
            }

            this.camPreview = new CameraPreview(this, camera);
            preview.addView(this.camPreview);
        }
    }
}
