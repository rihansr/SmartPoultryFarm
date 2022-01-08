package com.rs.smartpoultryfarm.util;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.rs.smartpoultryfarm.R;
import com.rs.smartpoultryfarm.controller.AppController;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;

public class AppExtensions {

    public static void buildCustomActionBar(ActionBar actionBar){
        if(actionBar == null) return;
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setLogo(R.drawable.app_logo_mini);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setElevation(0);
    }

    public static String formatVal(String val, String orElse) {
        if (val == null) return orElse;
        val = val.replaceAll("[^\\d.]", "");
        if (val.trim().isEmpty()) return orElse;

        return customDecimalFormat(Double.parseDouble(val));
    }

    public static String customDecimalFormat(double number) {
        DecimalFormat df = new DecimalFormat("#.#");
        return (number % 1 == 0) ? String.valueOf((int) number) : df.format(number);
    }

    public static boolean isNullOrEmpty(final List<?> l) {
        return l == null || l.isEmpty();
    }

    /**
     * Dialog & Activity Styles
     **/
    public static void halfScreenDialog(Dialog dialog) {
        if (dialog == null) return;

        Window window = dialog.getWindow();
        if (window == null) return;

        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.windowAnimations = R.style.DialogDefaultAnimation;
        window.setGravity(Gravity.BOTTOM);
        window.setAttributes(params);
    }

    public static void hideKeyboardInDialog() {
        InputMethodManager imm = (InputMethodManager) AppController.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null)  imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    public static void requestFocus(View view) {
        if (view.requestFocus()) {
            Objects.requireNonNull(AppController.getActivity().getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    /**
     * Resources
     **/
    public static String string(int id) {
        return AppController.getContext().getResources().getString(id);
    }

    public static Drawable drawable(int id){
        return ContextCompat.getDrawable(AppController.getActivity(), id);
    }

    public static View rootView(Dialog dialog){
        return Objects.requireNonNull(Objects.requireNonNull(dialog).getWindow()).getDecorView().getRootView();
    }

    public static void shareApk() {
        try {
            File initialApkFile = new File(AppController.getActivity().getPackageManager()
                    .getApplicationInfo(AppController.getActivity().getPackageName(), 0)
                    .sourceDir
            );

            File tempFile = new File(AppController.getActivity().getExternalCacheDir() + "/ExtractedApk");

            if (!tempFile.isDirectory())
                if (!tempFile.mkdirs())
                    return;

            tempFile = new File(tempFile.getPath() + "/" + string(R.string.app_name) + ".apk");

            if (!tempFile.exists()) {
                if (!tempFile.createNewFile()) {
                    return;
                }
            }

            InputStream in = new FileInputStream(initialApkFile);
            OutputStream out = new FileOutputStream(tempFile);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();

            shareFile(tempFile);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void shareFile(File sharePath) {
        Uri uri;
        if (Build.VERSION.SDK_INT < 24) {
            uri = Uri.parse("file://" + sharePath);
        }
        else {
            uri = FileProvider.getUriForFile(AppController.getContext(), AppController.getActivity().getPackageName() + ".fileprovider", new File(String.valueOf(sharePath)));
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, string(R.string.app_name));
        shareIntent.setType("*/*");
        AppController.getActivity().startActivity(Intent.createChooser(shareIntent, string(R.string.shareAPkVia)));
    }
}
