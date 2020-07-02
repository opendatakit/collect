package org.odk.collect.android.openrosa.api;

import java.util.List;

public interface FormAPI {

    List<FormListItem> fetchFormList() throws FormAPIError;

    ManifestFile fetchManifest(String manifestURL) throws FormAPIError;
}