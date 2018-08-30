/*
Copyright 2018 Ona

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License
*/

package org.odk.collect.android.services;

import android.app.IntentService;
import android.content.Intent;

import org.odk.collect.android.tasks.FormDownloadWorker;
import org.odk.collect.android.utilities.ApplicationConstants;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import timber.log.Timber;

/**
 * This service is the entry point for form download requests mainly from external apps. It then passes
 * over the task to {@link FormDownloadWorker}
 * <p>
 * How to call this service from an external app:
 * <p>
 * <code>
 * Intent intent = new Intent();
 * intent.putExtra("FORM_ID", formID);
 * intent.setClassName("org.odk.collect.android", "org.odk.collect.android.services.FormDownloadService");
 * context.startService(intent);
 * </code>
 *
 * @author Ephraim Kigamba (nek.eam@gmail.com)
 */
public class FormDownloadService extends IntentService {
    public FormDownloadService() {
        super("FormDownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Timber.i("RECEIVED FORM DOWNLOAD REQUEST IN SERVICE");

        String formId = intent.getStringExtra(ApplicationConstants.BundleKeys.FORM_ID);

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest formDownloadWork =
                new OneTimeWorkRequest.Builder(FormDownloadWorker.class)
                        .setConstraints(constraints)
                        .addTag(FormDownloadWorker.TAG)
                        .setInputData(new Data.Builder()
                                .putString(ApplicationConstants.BundleKeys.FORM_ID, formId)
                                .build())
                        .build();

        WorkManager.getInstance().enqueue(formDownloadWork);
    }
}
