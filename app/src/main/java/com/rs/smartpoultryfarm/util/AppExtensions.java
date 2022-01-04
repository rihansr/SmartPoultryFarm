package com.rs.smartpoultryfarm.util;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.core.content.ContextCompat;

import com.rs.smartpoultryfarm.R;
import com.rs.smartpoultryfarm.controller.AppController;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;

public class AppExtensions {

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
}
