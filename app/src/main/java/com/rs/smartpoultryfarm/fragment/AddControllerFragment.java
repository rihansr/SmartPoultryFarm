package com.rs.smartpoultryfarm.fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.DialogFragment;
import com.android.volley.Request;
import com.google.android.material.textfield.TextInputEditText;
import com.rs.smartpoultryfarm.R;
import com.rs.smartpoultryfarm.api.ApiHandler;
import com.rs.smartpoultryfarm.controller.AppController;
import com.rs.smartpoultryfarm.model.Channel;
import com.rs.smartpoultryfarm.model.Feed;
import com.rs.smartpoultryfarm.model.PoultryData;
import com.rs.smartpoultryfarm.util.AppExtensions;
import com.rs.smartpoultryfarm.util.Constants;
import com.rs.smartpoultryfarm.util.CustomSnackBar;
import com.rs.smartpoultryfarm.util.SharedPreference;
import java.util.Objects;

@SuppressLint("ClickableViewAccessibility")
public class AddControllerFragment extends DialogFragment {

    private static final String TAG = AddControllerFragment.class.getSimpleName();
    private Context             context;
    private AppCompatImageView  backButton;
    private TextInputEditText   idInput;
    private TextInputEditText   readKeyInput;
    private TextInputEditText   writeKeyContact;
    private AppCompatButton     actionButton;
    private OnAddListener       mOnAddListener;
    private ProgressDialog      progressDialog;
    private SharedPreference    sp;

    public static AddControllerFragment show(){
        AddControllerFragment fragment = new AddControllerFragment();
        fragment.show(((AppCompatActivity) AppController.getActivity()).getSupportFragmentManager(), TAG);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        AppExtensions.halfScreenDialog(getDialog());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_layout_add_controller, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        idSetup(view);

        init();
    }

    private void idSetup(View view) {
        backButton = view.findViewById(R.id.back_Btn);
        idInput = view.findViewById(R.id.id_Input);
        readKeyInput = view.findViewById(R.id.readKey_Input);
        writeKeyContact = view.findViewById(R.id.writeKey_Input);
        actionButton = view.findViewById(R.id.action_Btn);
        progressDialog = new ProgressDialog(context, R.style.ProgressDialog);
        sp = new SharedPreference();
    }

    private void init(){
        Channel channel = sp.channelData(SharedPreference.CONTROLLER_CHANNEL_SP_KEY + "_" + sp.channelData(SharedPreference.POULTRY_CHANNEL_SP_KEY).getChannelId());
        idInput.setText(channel.getChannelId());
        readKeyInput.setText(channel.getReadKey());
        writeKeyContact.setText(channel.getWriteKey());

        actionButton.setText(AppExtensions.string(channel.getChannelId() == null ? R.string.add : R.string.update));

        backButton.setOnClickListener(view -> dismiss());

        actionButton.setOnClickListener(v -> {
            if (!Constants.IS_NETWORK_CONNECTED) {
                new CustomSnackBar(AppExtensions.rootView(getDialog()), AppExtensions.string(R.string.network_Error), false, CustomSnackBar.Duration.LONG).show();
                return;
            }

            addControllerChannel();
        });
    }

    private void addControllerChannel() {
        final String id = Objects.requireNonNull(idInput.getText()).toString().trim();
        final String readKey = Objects.requireNonNull(readKeyInput.getText()).toString().trim();
        final String writeKey = Objects.requireNonNull(writeKeyContact.getText()).toString().trim();

        /**
         *  Validation
         **/
        if (TextUtils.isEmpty(Objects.requireNonNull(id))) {
            idInput.setError(getResources().getString(R.string.channelId_Error));
            AppExtensions.requestFocus(idInput);
            return;
        } else if (id.length() < 7) {
            idInput.setError(getResources().getString(R.string.validChannelId_Error));
            AppExtensions.requestFocus(idInput);
            return;
        } else if (TextUtils.isEmpty(Objects.requireNonNull(readKey))) {
            readKeyInput.setError(getResources().getString(R.string.key_Error));
            AppExtensions.requestFocus(readKeyInput);
            return;
        } else if (readKey.length() < 16) {
            readKeyInput.setError(getResources().getString(R.string.validKeyError));
            AppExtensions.requestFocus(readKeyInput);
            return;
        } else if (TextUtils.isEmpty(Objects.requireNonNull(writeKey))) {
            writeKeyContact.setError(getResources().getString(R.string.key_Error));
            AppExtensions.requestFocus(writeKeyContact);
            return;
        } else if (writeKey.length() < 16) {
            writeKeyContact.setError(getResources().getString(R.string.validKeyError));
            AppExtensions.requestFocus(writeKeyContact);
            return;
        }

        progressDialog.setMessage(getResources().getString(R.string.processing));
        progressDialog.setCancelable(false);
        progressDialog.show();

        /**
         * Get data from https://thingspeak.com/
         **/
        ApiHandler.invoke(context, PoultryData.class, Request.Method.GET,
                ApiHandler.feedsUrl(id, readKey, 1),
                new ApiHandler.OnDataListener<PoultryData>() {
                    @Override
                    public void onData(PoultryData data) {
                        progressDialog.dismiss();
                        if (data == null || data.getCode() == 404) {
                            new CustomSnackBar(AppExtensions.rootView(getDialog()), R.string.dataNotExist, R.string.retry, CustomSnackBar.Duration.SHORT).show();
                            return;
                        }
                        sp.channelData(SharedPreference.CONTROLLER_CHANNEL_SP_KEY + "_" + sp.channelData(SharedPreference.POULTRY_CHANNEL_SP_KEY).getChannelId(),
                                new Channel(id, readKey, writeKey)
                        );
                        Feed feed = AppExtensions.isNullOrEmpty(data.getFeeds()) ? null : data.getFeeds().get(0);
                        if(mOnAddListener != null) mOnAddListener.onAdd(feed);
                        dismiss();
                    }
                    @Override
                    public void onError() {
                        progressDialog.dismiss();
                        new CustomSnackBar(AppExtensions.rootView(getDialog()), R.string.dataNotExist, R.string.retry, CustomSnackBar.Duration.SHORT).show();
                    }
                });
    }

    public void setOnAddListener(OnAddListener mOnAddListener) {
        this.mOnAddListener = mOnAddListener;
    }

    public interface OnAddListener {
        void onAdd(Feed controller);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
