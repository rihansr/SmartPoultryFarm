package com.rs.smartpoultryfarm.util;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.rs.smartpoultryfarm.R;
import com.rs.smartpoultryfarm.controller.AppController;

import java.text.DecimalFormat;
import java.util.Objects;

public class AppExtensions {

    public static String formatValue(String value, String defaultText) {
        if (value == null || value.trim().isEmpty()) return defaultText;
        if (value.trim().matches("[a-zA-Z]+")) return defaultText;

        return customDecimalFormat(Double.parseDouble(value));
    }

    public static String customDecimalFormat(double number) {
        DecimalFormat df = new DecimalFormat("#.#");
        return (number % 1 == 0) ? String.valueOf((int) number) : df.format(number);
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
    public static String getString(int id) {
        return AppController.getContext().getResources().getString(id);
    }

}
