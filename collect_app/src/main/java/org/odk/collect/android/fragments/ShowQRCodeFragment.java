package org.odk.collect.android.fragments;

import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import org.json.JSONException;
import org.json.JSONObject;
import org.odk.collect.android.R;
import org.odk.collect.android.preferences.AdminPreferencesActivity;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.utilities.ToastUtils;

import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_CANCELED;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_FORMLIST_URL;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_GOOGLE_SHEETS_URL;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_PROTOCOL;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_SERVER_URL;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_SUBMISSION_URL;


/**
 * Created by shobhit on 6/4/17.
 */

public class ShowQRCodeFragment extends Fragment implements View.OnClickListener {

    private static final int QRCODE_CAPTURE = 1;
    private SharedPreferences adminSettings;
    private SharedPreferences settings;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.show_qrcode_fragment, container, false);
        ImageView qrImageView = (ImageView) view.findViewById(R.id.qr_iv);
        Button scan = (Button) view.findViewById(R.id.btnScan);

        scan.setOnClickListener(this);

        adminSettings = getActivity().getSharedPreferences(AdminPreferencesActivity.ADMIN_PREFERENCES, 0);
        settings = PreferenceManager.getDefaultSharedPreferences(getActivity());

        Bitmap qrCode = generateQRBitMap();
        if (qrCode != null) {
            qrImageView.setImageBitmap(qrCode);
        }

        return view;
    }


    private Bitmap generateQRBitMap() {

        final String content;
        try {
            content = getUserPreferences();
            Map<EncodeHintType, ErrorCorrectionLevel> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 512, 512, hints);

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();

            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bmp;
        } catch (WriterException | JSONException e) {
        }
        return null;
    }

    private String getUserPreferences() throws JSONException {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put(KEY_PROTOCOL, settings.getString(KEY_PROTOCOL, null));
        jsonObject.put(KEY_SERVER_URL, settings.getString(KEY_SERVER_URL,
                getActivity().getString(R.string.default_server_url)));
        jsonObject.put(KEY_GOOGLE_SHEETS_URL, settings.getString(KEY_GOOGLE_SHEETS_URL,
                getActivity().getString(R.string.default_google_sheets_url)));
        jsonObject.put(KEY_FORMLIST_URL, settings.getString(PreferenceKeys.KEY_FORMLIST_URL,
                getActivity().getString(R.string.default_odk_formlist)));
        jsonObject.put(KEY_SUBMISSION_URL, settings.getString(PreferenceKeys.KEY_SUBMISSION_URL,
                getActivity().getString(R.string.default_odk_submission)));

        return jsonObject.toString();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_CANCELED) {
            // request was canceled...
            ToastUtils.showShortToast("Scanning Cancelled");
            return;
        }

        switch (requestCode) {
            case QRCODE_CAPTURE:
                String dataObject = data.getStringExtra("SCAN_RESULT");
                try {
                    JSONObject jsonObject = new JSONObject(dataObject);
                    applySettings(jsonObject);
                    ToastUtils.showLongToast(getString(R.string.successfully_imported_settings));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private void applySettings(JSONObject jsonObject) throws JSONException {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(KEY_SERVER_URL, jsonObject.getString(KEY_SERVER_URL));
        editor.putString(KEY_GOOGLE_SHEETS_URL, jsonObject.getString(KEY_GOOGLE_SHEETS_URL));
        editor.putString(KEY_FORMLIST_URL, jsonObject.getString(KEY_FORMLIST_URL));
        editor.putString(KEY_SUBMISSION_URL, jsonObject.getString(KEY_SUBMISSION_URL));
        editor.apply();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnScan:
                Intent i = new Intent("com.google.zxing.client.android.SCAN");
                try {
                    startActivityForResult(i, QRCODE_CAPTURE);
                } catch (ActivityNotFoundException e) {
                    ToastUtils.showShortToast(R.string.barcode_scanner_error);
                }
                break;
        }
    }
}
